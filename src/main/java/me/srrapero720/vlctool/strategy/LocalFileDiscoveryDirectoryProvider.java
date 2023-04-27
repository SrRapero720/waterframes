
package me.srrapero720.vlctool.strategy;

import java.nio.file.Path;

import net.minecraftforge.fml.loading.FMLPaths;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.provider.DiscoveryDirectoryProvider;

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
