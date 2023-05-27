package me.srrapero720.waterframes.mixin;

import com.sun.jna.NativeLibrary;
import me.srrapero720.waterframes.watercore_supplier.ReflectTools;
import org.spongepowered.asm.mixin.*;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static me.srrapero720.waterframes.WaterDisplays.LOGGER;

// When a class isn't obfuscated requires turn off remap
@Mixin(value = NativeDiscovery.class, remap = false)
public abstract class MixinNativeDiscovery {

    @Shadow private static boolean alreadyFound;
    @Final @Shadow private Iterable<NativeDiscoveryStrategy> discoveryStrategies;
    @Shadow private NativeDiscoveryStrategy successfulStrategy;
    @Shadow private String discoveredPath;

    @Unique
    private static Field searchPaths;
    @Unique private static Field libraries;

    @Shadow protected abstract void onFound(String path, NativeDiscoveryStrategy strategy);
    @Shadow protected abstract void onFailed(String path, NativeDiscoveryStrategy strategy);
    @Shadow protected abstract void onNotFound();
    @Shadow protected abstract void tryPluginPath(String path, NativeDiscoveryStrategy discoveryStrategy);
    @Shadow protected abstract boolean tryLoadingLibrary();

    /**
     * @author SrRapero720, Goedix
     * @reason You shouldn't do mixins on our libraries, btw, we just "try to fix" discovery to continue discovering.
     */
    @Overwrite
    public final boolean discover() {
        if (alreadyFound) {
            return true;
        } else {
            for (NativeDiscoveryStrategy discoveryStrategy : discoveryStrategies) {
                if (discoveryStrategy.supported()) {
                    String path = discoveryStrategy.discover();
                    if (path != null) {
                        if (discoveryStrategy.onFound(path)) {
                            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), path);
                        }
                        tryPluginPath(path, discoveryStrategy);
                        if (tryLoadingLibrary()) {
                            successfulStrategy = discoveryStrategy;
                            discoveredPath = path;
                            onFound(path, discoveryStrategy);
                            alreadyFound = true;
                            return true;
                        } else {
                            // Ignore this warning
                            // Mixin a class makes it a instance of the mixed class
                            // Extending the mixed class keeps extending by this class
                            if (attemptFix(path, discoveryStrategy)) continue;
                            // We have to stop here, because we already added a search path for the native library and
                            // any further search paths we add will be tried AFTER the one that already failed - the
                            // subsequent directories we may like to try will never actually be tried
                            onFailed(path, discoveryStrategy);
                            return false;
                        }
                    }
                }
            }
            onNotFound();
            return false;
        }
    }

    public boolean attemptFix(String path, NativeDiscoveryStrategy discoveryStrategy) {
        if (searchPaths == null) {
            searchPaths = ReflectTools.field(NativeLibrary.class, "searchPaths");
            libraries = ReflectTools.field(NativeLibrary.class, "libraries");
        }
        try {
            Map<String, Reference<NativeLibrary>> libs = (Map<String, Reference<NativeLibrary>>) libraries.get(null);
            Map<String, List<String>> paths = (Map<String, List<String>>) searchPaths.get(null);
            libs.remove(RuntimeUtil.getLibVlcCoreLibraryName());
            paths.remove(RuntimeUtil.getLibVlcCoreLibraryName());
            libs.remove(RuntimeUtil.getLibVlcLibraryName());
            paths.remove(RuntimeUtil.getLibVlcLibraryName());
            LOGGER.info("Failed to load VLC in '{}'", path);
            return true;
        } catch (IllegalArgumentException | IllegalAccessException ignored) {}
        return false;
    }
}