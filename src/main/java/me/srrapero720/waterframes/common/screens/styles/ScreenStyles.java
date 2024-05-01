package me.srrapero720.waterframes.common.screens.styles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

@Environment(EnvType.CLIENT)
public class ScreenStyles {
    public static final GuiStyle REMOTE_CONTROL = new GuiStyle();
    public static final GuiStyle DISPLAYS = new GuiStyle();

    public static final StyleDisplay BLUE_BORDER = new DisplayColor(color(72), color(220), color(219), 1.0F);
    public static final StyleDisplay BLUE_BACKGROUND = new DisplayColor(color(47), color(137), color(137), 1.0F);
    public static final StyleDisplay DARK_BLUE_BACKGROUND = new DisplayColor(color(27), color(36), color(52), 1);

    public static final StyleDisplay DARK_BLUE_BACKGROUND_DISABLED = new DisplayColor(color(27), color(36), color(52), 0.5f);
    public static final StyleDisplay DARK_BLUE_HIGHLIGHT = new DisplayColor(color(27 * 2), color(36 * 2), color(52 * 2), 1);

    public static final StyleDisplay RED_BORDER = new DisplayColor(color(255), color(60), color(60), 1.0F);
    public static final StyleDisplay RED_BACKGROUND = new DisplayColor(color(140), color(56), color(52), 1);

    public static final StyleDisplay SCREEN_BACKGROUND = new DisplayColor(color(37), color(50), color(72), 1.0F);
    public static final StyleDisplay SCREEN_BORDER = new DisplayColor(color(23), color(30), color(42), 1.0F);

    static {
        REMOTE_CONTROL.borderThickWidth = 6;
        DISPLAYS.borderThickWidth = 6;
    }

    public static float color(int value) {
        return Math.min(((float) (1d / 255d) * value), 1.0f);
    }
}