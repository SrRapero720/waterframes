package me.srrapero720.waterframes.custom.cc_gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

@Environment(EnvType.CLIENT)
@OnlyIn(Dist.CLIENT)
public class GuiWarningStyles {
    
    public static final StyleDisplay DISABLED_BORDER = new DisplayColor(0.196F, 0, 0, 1);
    public static final StyleDisplay DISABLED_BACKGROUND = new DisplayColor(0.588F, 0.352F, 0.352F, 1);
    
    public static final StyleDisplay WARNING_BORDER = new DisplayColor(0.196F, 0, 0, 1);
    public static final StyleDisplay WARNING_BACKGROUND = new DisplayColor(0.588F, 0.588F, 0.352F, 1);
    
}
