package me.srrapero720.waterframes.common.compat.videoplayer;

//import com.github.NGoedix.watchvideo.client.ClientHandler;
import me.srrapero720.waterframes.WaterFrames;

public class VPCompat {
    public static final boolean VP_MODE = WaterFrames.isInstalled("videoplayer");

    public static boolean installed() {
        return VP_MODE;
    }

    public static void playVideo(String url, int volume, boolean blockControls, boolean canSkip) {
        if (!installed()) return;

//        ClientHandler.openVideo(url, volume, blockControls, canSkip);
    }
}