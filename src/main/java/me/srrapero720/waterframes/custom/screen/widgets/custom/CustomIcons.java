package me.srrapero720.waterframes.custom.screen.widgets.custom;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.resources.ResourceLocation;
import team.creative.creativecore.common.gui.style.GuiIcon;

public class CustomIcons {
    public static final ResourceLocation location = new ResourceLocation(WaterFrames.ID, "textures/screen_atlas.png");

    // VOLUME COLUM (chunk 0)
    public static final GuiIcon VOLUME = create(0, 0);
    public static final GuiIcon VOLUME_RANGE_MIN = create(0, 10);
    public static final GuiIcon VOLUME_RANGE_MAX = create(0, 11);

    // ICON COLUM (chunk 1)
    public static final GuiIcon ROTATION = create(1, 2);
    public static final GuiIcon TRANSPARENCY = create(1, 3);
    public static final GuiIcon BRIGHTNESS = create(1, 4);
    public static final GuiIcon DISTANCE = create(1, 10);
    public static final GuiIcon PROJECTION_DISTANCE = create(1, 11);

    // STATUS ICONS (chunk 2)
    public static final GuiIcon STATUS_OK = create(2, 0);
    public static final GuiIcon STATUS_ALERT = create(2, 1);
    public static final GuiIcon STATUS_CHILL = create(2, 2);
    public static final GuiIcon STATUS_ERROR = create(2, 3);
    public static final GuiIcon STATUS_HACKED = create(2, 4);
    public static final GuiIcon STATUS_PEM = create(2, 5);
    public static final GuiIcon STATUS_AFFECTED = create(2, 6);
    public static final GuiIcon STATUS_CASSETE_MODE = create(2, 7);

    // POSITION ICONS (chunk 3)
    public static final GuiIcon POS_1 = create(3, 0);
    public static final GuiIcon POS_2 = create(3, 1);
    public static final GuiIcon POS_3 = create(3, 2);
    public static final GuiIcon POS_4 = create(3, 3);
    public static final GuiIcon POS_5 = create(3, 4);
    public static final GuiIcon POS_6 = create(3, 5);
    public static final GuiIcon POS_7 = create(3, 6);
    public static final GuiIcon POS_8 = create(3, 7);
    public static final GuiIcon POS_9 = create(3, 8);
    public static final GuiIcon[][] POS_CORD = new GuiIcon[][] {
            new GuiIcon[] { POS_1, POS_4, POS_7 },
            new GuiIcon[] { POS_2, POS_5, POS_8 },
            new GuiIcon[] { POS_3, POS_6, POS_9 }
    };

    // ACTION COLUM (chunk 15)
    public static final GuiIcon PLAY = create(15, 0);
    public static final GuiIcon PAUSE = create(15, 1);
    public static final GuiIcon STOP = create(15, 2);

    private static GuiIcon create(int chunkX, int chunkY) {
        return new GuiIcon(location, 16 * chunkX, 16 * chunkY, 16, 16);
    }

}