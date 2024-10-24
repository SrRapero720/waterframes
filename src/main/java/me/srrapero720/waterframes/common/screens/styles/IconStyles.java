package me.srrapero720.waterframes.common.screens.styles;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.gui.style.Icon;

public class IconStyles {
    public static final ResourceLocation location = new ResourceLocation(WaterFrames.ID, "textures/screen_atlas.png");

    // VOLUME COLUM (chunk 0)
    public static final Icon VOLUME = create(0, 0);
    public static final Icon VOLUME_MUTED = create(0, 1);
    public static final Icon VOLUME_1 = create(0, 2);
    public static final Icon VOLUME_2 = create(0, 3);
    public static final Icon VOLUME_3 = create(0, 4);
    public static final Icon VOLUME_OVERFLOW = create(0, 5);
    public static final Icon VOLUME_RANGE_MIN = create(0, 6);
    public static final Icon VOLUME_RANGE_MAX = create(0, 7);
    public static final Icon VOLUME_MUTE = create(0, 8);
    public static final Icon AUDIO_POS_BLOCK = create(0, 13, 2);
    public static final Icon AUDIO_POS_CENTER = create(0, 14, 2);
    public static final Icon AUDIO_POS_PICTURE = create(0, 15, 2);


    // ICON COLUM (chunk 1)
    public static final Icon EXPAND_X = create(1, 0);
    public static final Icon EXPAND_Y = create(1, 1);
    public static final Icon ROTATION = create(1, 2);
    public static final Icon TRANSPARENCY = create(1, 3);
    public static final Icon BRIGHTNESS = create(1, 4);
    public static final Icon REPEAT_ON = create(1, 5);
    public static final Icon REPEAT_OFF = create(1, 6);
    public static final Icon DISTANCE = create(1, 7);
    public static final Icon PROJECTION_DISTANCE = create(1, 8);
    public static final Icon SAVE = create(1, 9);
    public static final Icon VIDEOPLAYER_PLAY = create(1, 12);

    // STATUS ICONS (chunk 2)
    public static final Icon STATUS_OK = create(2, 0);
    public static final Icon STATUS_WARN = create(2, 1);
    public static final Icon STATUS_IDLE = create(2, 2);
    public static final Icon STATUS_ERROR = create(2, 3);
    public static final Icon STATUS_HACKED = create(2, 4);
    public static final Icon STATUS_PEM = create(2, 5);
    public static final Icon STATUS_AFFECTED = create(2, 6);
    public static final Icon STATUS_CASSETE_MODE = create(2, 7);
    public static final Icon STATUS_OFF = create(2, 8);
    public static final Icon STATUS_MEDIA_ERROR = create(2, 9);
    public static final Icon STATUS_OK_CACHE = create(2, 10);
    public static final Icon MIRROR_ON = create(2, 14, 2);
    public static final Icon MIRROR_OFF = create(2, 15, 2);


    // POSITION AND SIGNAL ICONS (chunk 3)
    public static final Icon SIGNAL_4 = create(3, 0);
    public static final Icon SIGNAL_3 = create(3, 1);
    public static final Icon SIGNAL_2 = create(3, 2);
    public static final Icon SIGNAL_1 = create(3, 3);
    public static final Icon SIGNAL_0 = create(3, 4);
    public static final Icon POS_ICON = create(4, 12);
    public static final Icon POS_BASE = create(4, 13, 3, 3);

    // ACTION COLUM 2 (chunk 14)
    public static final Icon ARROW_UP = create(14, 0);
    public static final Icon ARROW_DOWN = create(14, 1);
    public static final Icon ARROW_LEFT = create(14, 2);
    public static final Icon ARROW_RIGHT = create(14, 3);
    public static final Icon ARROW_CENTER = create(14, 4);


    // ACTION COLUM 1 (chunk 15)
    public static final Icon PLAY = create(15, 0);
    public static final Icon PAUSE = create(15, 1);
    public static final Icon STOP = create(15, 2);
    public static final Icon FAST_FOWARD = create(15, 3);
    public static final Icon FAST_BACKWARD = create(15, 4);
    public static final Icon NEXT_MEDIA = create(15, 5);
    public static final Icon BACK_MEDIA = create(15, 6);
    public static final Icon ADD = create(15, 7);
    public static final Icon OFF_ON = create(15, 8);
    public static final Icon RELOAD = create(15, 9);
    public static final Icon VOLUME_DOWN = create(15, 10);
    public static final Icon VOLUME_UP = create(15, 11);
    public static final Icon CHANNEL_UP = create(15, 12);
    public static final Icon CHANNEL_DOWN = create(15, 13);

    private static Icon create(int chunkX, int chunkY) {
        return new Icon(location, 16 * chunkX, 16 * chunkY, 16, 16);
    }

    private static Icon create(int chunkX, int chunkY, int chunkXMultiplier) {
        return new Icon(location, 16 * chunkX, 16 * chunkY, 16 * chunkXMultiplier, 16);
    }

    private static Icon create(int chunkX, int chunkY, int chunkXMultiplier, int chunkYMultiplier) {
        return new Icon(location, 16 * chunkX, 16 * chunkY, 16 * chunkXMultiplier, 16 * chunkYMultiplier);
    }

    public static Icon getVolumeIcon(int volume, boolean muted) {
        if (muted) return VOLUME_MUTE;
        if (volume > 100) {
            return IconStyles.VOLUME_OVERFLOW;
        } else if (volume >= 90){
            return IconStyles.VOLUME;
        } else if (volume >= 65) {
            return IconStyles.VOLUME_3;
        } else if (volume >= 35) {
            return IconStyles.VOLUME_2;
        } else if (volume >= 1) {
            return IconStyles.VOLUME_1;
        } else {
            return IconStyles.VOLUME_MUTED;
        }
    }
}