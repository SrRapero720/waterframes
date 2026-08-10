package me.srrapero720.waterframes.client.ui;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterui.theme.Icon;
import net.minecraft.resources.ResourceLocation;

/**
 * The screen atlas icons Java picks at runtime, addressed by their 16px chunk coordinates.
 * Everything a document can name declaratively lives in {@code ui/constants/icons.ui} instead.
 */
public class Icons {
    public static final ResourceLocation ATLAS = WaterFrames.asResource("textures/screen_atlas.png");

    // VOLUME COLUM (chunk 0): PICKED BY LEVEL IN volume(int, boolean)
    public static final Icon VOLUME = icon(0, 0);
    public static final Icon VOLUME_0 = icon(0, 1);
    public static final Icon VOLUME_1 = icon(0, 2);
    public static final Icon VOLUME_2 = icon(0, 3);
    public static final Icon VOLUME_3 = icon(0, 4);
    public static final Icon VOLUME_OVERFLOW = icon(0, 5);

    // ANCHOR GRID (chunk 0): THE AnchorPicker PAINTS ITS OWN CELLS
    public static final Icon POS_ICON = icon(0, 12);
    public static final Icon POS_BASE = icon(0, 13, 3, 3);

    // SOURCE ACTIONS (chunk 6): THE SUBMIT BUTTON SWAPS BETWEEN THEM BY URL VALIDITY
    public static final Icon SEARCH = icon(6, 6);

    // STATUS ICONS (chunk 3-4): PICKED FROM THE MEDIA AND PLAYER STATE
    public static final Icon STATUS_OK = icon(3, 0);
    public static final Icon STATUS_ERROR = icon(4, 1);
    public static final Icon STATUS_IDLE = icon(3, 2);
    public static final Icon STATUS_LOADING = icon(4, 2);
    public static final Icon STATUS_OFF = icon(4, 3);
    public static final Icon STATUS_INTERNAL_ERROR = icon(3, 4);
    public static final Icon STATUS_BUFFERING = icon(4, 5);

    // SIGNAL BARS (chunk 5): PICKED BY THE REMOTE'S DISTANCE TO THE DISPLAY
    public static final Icon SIGNAL_4 = icon(5, 0);
    public static final Icon SIGNAL_3 = icon(5, 1);
    public static final Icon SIGNAL_2 = icon(5, 2);
    public static final Icon SIGNAL_1 = icon(5, 3);
    public static final Icon SIGNAL_0 = icon(5, 4);

    // ACTION COLUM 1 (chunk 15)
    public static final Icon ADD = icon(15, 7);

    private static Icon icon(int chunkX, int chunkY) {
        return icon(chunkX, chunkY, 1, 1);
    }

    private static Icon icon(int chunkX, int chunkY, int chunksWide, int chunksTall) {
        return new Icon(ATLAS, 16 * chunkX, 16 * chunkY, 16 * chunksWide, 16 * chunksTall);
    }

    public static Icon volume(int volume, boolean muted) {
        if (muted || volume < 1) return VOLUME_0;
        if (volume > 100) return VOLUME_OVERFLOW;
        if (volume >= 90) return VOLUME;
        if (volume >= 65) return VOLUME_3;
        if (volume >= 35) return VOLUME_2;
        return VOLUME_1;
    }
}
