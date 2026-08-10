package me.srrapero720.waterui.format;

import com.mojang.blaze3d.platform.NativeImage;
import me.srrapero720.waterui.WaterUI;
import me.srrapero720.waterui.theme.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves {@code loadAtlas(...)} handles (UI-SPEC.md §5.7). The reference carries no
 * {@code textures/} prefix and no extension; this locates the actual texture format-agnostically
 * through the resource manager and reads its side length with {@link NativeImage}, so a texture
 * pack shipping a different (e.g. webp) encoding just works as long as the runtime can decode it.
 */
public final class UIAtlases {
    private static final Marker IT = MarkerManager.getMarker(UIAtlases.class.getSimpleName());
    // TRIED IN ORDER; A webp-CAPABLE RUNTIME SUPPLIES ITS OWN DECODER BEHIND NativeImage.read
    private static final String[] EXTENSIONS = {".png", ".webp"};
    private static final Map<ResourceLocation, Handle> CACHE = new HashMap<>();

    private UIAtlases() {}

    /** An atlas handle: the located texture and its square side in pixels, addressed in 16-px chunks. */
    public record Handle(ResourceLocation texture, int size) {
        public Icon icon(int chunkX, int chunkY, int chunksWide, int chunksTall) {
            return new Icon(texture, chunkX * 16, chunkY * 16, chunksWide * 16, chunksTall * 16, size);
        }
    }

    /** Handle for {@code loadAtlas("<[ns:]path>")}, resolved once and cached until a resource reload. */
    public static Handle load(String ref, String defaultNs) {
        int colon = ref.indexOf(':');
        String ns = colon >= 0 ? ref.substring(0, colon) : defaultNs;
        String path = colon >= 0 ? ref.substring(colon + 1) : ref;
        ResourceLocation base;
        try {
            base = ResourceLocation.fromNamespaceAndPath(ns, "textures/" + path);
        } catch (RuntimeException e) {
            throw new UIFormatException(null, -1, "invalid atlas path '" + ref + "'");
        }
        Handle cached = CACHE.get(base);
        if (cached != null) return cached;

        Handle handle = locate(base);
        CACHE.put(base, handle);
        return handle;
    }

    // TRIES EACH KNOWN EXTENSION; THE FIRST RESOURCE THAT EXISTS IS DECODED FOR ITS SQUARE SIDE
    private static Handle locate(ResourceLocation base) {
        var manager = Minecraft.getInstance().getResourceManager();
        for (String ext: EXTENSIONS) {
            ResourceLocation loc = base.withPath(base.getPath() + ext);
            Optional<Resource> resource = manager.getResource(loc);
            if (resource.isEmpty()) continue;
            try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                if (width != height) {
                    WaterUI.LOGGER.warn(IT, "atlas {} is not square ({}x{}), defaulting size to 256", loc, width, height);
                    return new Handle(loc, 256);
                }
                if (width % 16 != 0) {
                    WaterUI.LOGGER.warn(IT, "atlas {} side {} is not divisible by 16, defaulting size to 256", loc, width);
                    return new Handle(loc, 256);
                }
                WaterUI.LOGGER.debug(IT, "atlas {} located ({}px)", loc, width);
                return new Handle(loc, width);
            } catch (Exception e) {
                WaterUI.LOGGER.warn(IT, "atlas {} could not be decoded, defaulting size to 256", loc, e);
                return new Handle(loc, 256);
            }
        }
        WaterUI.LOGGER.warn(IT, "atlas {} not found (tried {}), defaulting size to 256", base, String.join(", ", EXTENSIONS));
        return new Handle(base.withPath(base.getPath() + ".png"), 256);
    }

    /** Clears cached handles; called by the resource-reload listener. */
    public static void invalidate() {
        CACHE.clear();
    }
}
