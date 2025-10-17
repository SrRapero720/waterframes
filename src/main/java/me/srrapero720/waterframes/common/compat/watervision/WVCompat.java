package me.srrapero720.waterframes.common.compat.watervision;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.client.Minecraft;

import java.net.URI;

public class WVCompat {
    public static final boolean INSTALLED = WaterFrames.isInstalled("watervision");

    public static boolean installed() {
        return INSTALLED;
    }

    public static void openScreen(URI uri, int volume) {
        if (INSTALLED) return;

        Minecraft.getInstance().setScreen(WVExpansion.getWaterVisionExpansion(uri, volume));
    }
}
