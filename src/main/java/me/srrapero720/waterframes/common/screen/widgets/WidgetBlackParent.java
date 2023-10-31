package me.srrapero720.waterframes.common.screen.widgets;

import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetStyles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

public class WidgetBlackParent extends WidgetParent {
    public WidgetBlackParent(String name, GuiFlow flow) {
        super(name, flow);
    }

    public WidgetBlackParent(String name, GuiFlow flow, int width, int height) {
        super(name, flow, width, height);
    }

    public WidgetBlackParent(String name, GuiFlow flow, Align align, VAlign valign) {
        super(name, flow, align, valign);
    }

    public WidgetBlackParent(String name, GuiFlow flow, int width, int height, Align align, VAlign valign) {
        super(name, flow, width, height, align, valign);
    }

    public WidgetBlackParent(String name, GuiFlow flow, int width, int height, VAlign valign) {
        super(name, flow, width, height, valign);
    }

    public WidgetBlackParent(String name) {
        super(name);
    }

    public WidgetBlackParent() {
        super();
    }

    public WidgetBlackParent(GuiFlow flow) {
        super(flow);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return WidgetStyles.BACKGROUND_BORDER;
    }
}