package me.srrapero720.waterframes.common.compat.watervision;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.watervision.client.screens.VisionScreen;
import net.minecraft.client.Minecraft;

import java.net.URI;

public class WVCompat {
    public static final boolean INSTALLED = WaterFrames.isInstalled("watervision");

    public static boolean installed() {
        return INSTALLED;
    }

    public static void openScreen(URI uri, int volume) {
        Minecraft.getInstance().setScreen(new VisionScreen(uri, volume, 1.0f, false, 20.0F, 20.0F, true, true));
    }
}
