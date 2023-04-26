package me.srrapero720.waterframes.custom.screen.widgets.constants;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.style.display.DisplayColor;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

@OnlyIn(Dist.CLIENT)
public class Constants {
    public static final StyleDisplay WARN_DISABLED_BORDER = new DisplayColor(0.196F, 0, 0, 1);
    public static final StyleDisplay WARN_WARNING_BORDER = new DisplayColor(0.196F, 0, 0, 1);
    public static final StyleDisplay WARN_DISABLED_BACKGROUND = new DisplayColor(0.588F, 0.352F, 0.352F, 1);
    public static final StyleDisplay WARN_WARNING_BACKGROUND = new DisplayColor(0.588F, 0.588F, 0.352F, 1);
}
