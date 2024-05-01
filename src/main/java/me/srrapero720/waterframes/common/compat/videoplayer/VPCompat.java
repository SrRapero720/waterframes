package me.srrapero720.waterframes.common.compat.videoplayer;

//import com.github.NGoedix.videoplayer.client.ClientHandler;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.client.Minecraft;

public class VPCompat {
    public static final boolean VP_MODE = WaterFrames.isInstalled("videoplayer");

    public static boolean installed() {
        return VP_MODE;
    }

    public static void playVideo(String url, int volume, boolean blockControls, boolean canSkip) {
        if (!installed()) return;

//        ClientHandler.openVideo(Minecraft.getInstance(), url, volume, blockControls, canSkip);
    }
}