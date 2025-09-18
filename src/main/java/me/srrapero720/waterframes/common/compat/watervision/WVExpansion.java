package me.srrapero720.waterframes.common.compat.watervision;

import me.srrapero720.watervision.client.screens.VisionScreen;
import net.minecraft.client.gui.screens.Screen;

import java.net.URI;

public class WVExpansion {

    public static Screen getWaterVisionExpansion(URI uri, int volume) {
        return new VisionScreen(uri, volume, 1.0f, false, 20.0F, 20.0F, true, true);
    }
}
