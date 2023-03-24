
package me.srrapero720.waterframes.vlc;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

@Deprecated(forRemoval = false)
// I remove this because FancyVideo-API unwrapp their vlc on game dir, and my cpde try to use it (and have broken things)
public class LocalFileDiscoveryDirectoryProvider implements DiscoveryDirectoryProvider {
    
    @Override
    public int priority() {
        return 2;
    }
    
    @Override
    public boolean supported() {
        return true;
    }
    
    @Override
    public String[] directories() {
        Path vlc = FMLPaths.GAMEDIR.get().resolve("vlc");
        if (RuntimeUtil.isNix())
            vlc = vlc.resolve("linux");
        else if (RuntimeUtil.isMac())
            vlc = vlc.resolve("mac");
        else {
            boolean is64 = System.getProperty("sun.arch.data.model").equals("64");
            vlc = vlc.resolve("windows_" + (is64 ? "x64" : "x86"));
        }
        return new String[] { vlc.toString() };
    }
}
