package me.srrapero720.waterframes.common.screens.styles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

@OnlyIn(Dist.CLIENT)
public class ScreenStyles {
    public static final StyleDisplay NO_BACKGROUND = new DisplayColor(0,0,0,0);
    public static final StyleDisplay WIDGET_BORDER = new DisplayColor(color(72), color(220), color(219), 1.0F);
    public static final StyleDisplay WIDGET_BACKGROUND = new DisplayColor(color(27), color(36), color(52), 1);
    public static final StyleDisplay WIDGET_BACKGROUND_DISABLED = new DisplayColor(color(27), color(36), color(52), 0.5f);
    public static final StyleDisplay WIDGET_HIGHLIGH = new DisplayColor(color(27 * 2), color(36 * 2), color(52 * 2), 1);


    public static final StyleDisplay RED_BORDER = new DisplayColor(color(255), color(60), color(60), 1.0F);
    public static final StyleDisplay RED_BACKGROUND = new DisplayColor(color(140), color(56), color(52), 1);

    public static final StyleDisplay SCREEN_BACKGROUND = new DisplayColor(color(37), color(50), color(72), 1.0F);
    public static final StyleDisplay SCREEN_BORDER = new DisplayColor(color(23), color(30), color(42), 1.0F);

    public static float color(int value) {
        return (1f / 255f) * value;
    }

    public static final GuiStyle DEFAULT_STYLE = new GuiStyle();

    static {
        DEFAULT_STYLE.disabled = WIDGET_BACKGROUND_DISABLED;
        DEFAULT_STYLE.clickable = WIDGET_BACKGROUND;
        DEFAULT_STYLE.background = WIDGET_BACKGROUND;
        DEFAULT_STYLE.border = WIDGET_BORDER;

        DEFAULT_STYLE.clickableHighlight = WIDGET_HIGHLIGH;
    }
}