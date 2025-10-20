package me.srrapero720.waterframes.common.compat.watervision;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.watervision.WaterVisionClient;

import java.net.URI;

public class WVCompat {
    public static final boolean INSTALLED = WaterFrames.isInstalled("watervision");

    public static boolean installed() {
        return INSTALLED;
    }

    public static void openScreen(URI uri, int volume) {
        if (!INSTALLED) return;

        WaterVisionClient.openScreen(uri, volume, 1.0f, false, 20.0F, 20.0F, true, true);
    }
}
