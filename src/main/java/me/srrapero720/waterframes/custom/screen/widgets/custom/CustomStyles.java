package me.srrapero720.waterframes.custom.screen.widgets.custom;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

@OnlyIn(Dist.CLIENT)
public class CustomStyles {
    public static final StyleDisplay NO_BACKGROUND = new DisplayColor(0,0,0,0);
    public static final StyleDisplay NORMAL_BORDER = new DisplayColor(0.196F, 0, 0, 1);
    public static final StyleDisplay NORMAL_BACKGROUND = new DisplayColor(0.588F, 0.588F, 0.352F, 1);
    public static final StyleDisplay BACKGROUND_COLOR = new DisplayColor(0.25F, 0.25F, 0.25F, 1.0F);
    public static final StyleDisplay BACKGROUND_BORDER = new DisplayColor(0.1F, 0.1F, 0.1F, 0.90F);

}