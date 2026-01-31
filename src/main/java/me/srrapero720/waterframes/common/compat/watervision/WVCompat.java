package me.srrapero720.waterframes.common.compat.watervision;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.watervision.WaterVisionClient;

import java.net.URI;

public class WVCompat {
    public static final boolean INSTALLED = WaterFrames.isInstalled("watervision");

    public static boolean installed() {
        return INSTALLED;
    }

    public static void openScreen(String url, int volume) {
        if (!INSTALLED || url == null || url.isEmpty()) return;

        try {
            URI uri = new URI(url);
            WaterVisionClient.openScreen(uri, volume, 1.0f, false, 20.0F, 20.0F, true, true);
        } catch (Exception e) {
            WaterFrames.LOGGER.error("Failed to open WaterVision screen for URL: {}", url, e);
        }
    }
}
