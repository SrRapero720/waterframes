
package me.srrapero720.waterframes.vlc;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

@Deprecated(forRemoval = false)
// I remove this because FancyVideo-API unwrap their vlc on game dir, and my cpde try to use it (and have broken things)
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
        if (RuntimeUtil.isWindows()) vlc = vlc.resolve("windows_x64");
        else if (RuntimeUtil.isNix()) vlc = vlc.resolve("linux");
        else vlc.resolve("mac");
        return new String[] { vlc.toString() };
    }
}
