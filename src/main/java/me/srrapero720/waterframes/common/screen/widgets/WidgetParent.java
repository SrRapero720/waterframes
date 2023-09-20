package me.srrapero720.waterframes.common.screen.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class WidgetParent extends GuiParent {

    public WidgetParent(String name, GuiFlow flow) {
        super(name, flow);
    }

    public WidgetParent(String name, GuiFlow flow, int width, int height) {
        super(name, flow, width, height);
    }

    public WidgetParent(String name, GuiFlow flow, Align align, VAlign valign) {
        super(name, flow, align, valign);
    }

    public WidgetParent(String name, GuiFlow flow, int width, int height, Align align, VAlign valign) {
        super(name, flow, width, height, align, valign);
    }

    public WidgetParent(String name, GuiFlow flow, int width, int height, VAlign valign) {
        super(name, flow, width, height, valign);
    }

    public WidgetParent(String name) {
        super(name);
    }

    public WidgetParent() {
        super();
    }

    public WidgetParent(GuiFlow flow) {
        super(flow);
    }

    public WidgetParent setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public WidgetParent setAlign(Align align) {
        this.align = align;
        return this;
    }

    public WidgetParent add2(GuiControl control) {
        this.add(control);
        return this;
    }
}