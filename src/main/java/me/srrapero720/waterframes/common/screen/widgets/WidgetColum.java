package me.srrapero720.waterframes.common.screen.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class WidgetColum extends GuiColumn {
    public WidgetColum() {
        super();
    }

    public WidgetColum(GuiFlow flow) {
        super(flow);
    }

    public WidgetColum(int width) {
        super(width);
    }

    public WidgetColum(int width, GuiFlow flow, Align align) {
        super(width, flow);
        this.align = align;
    }

    public WidgetColum(int width, Align align) {
        super(width);
        this.align = align;
    }

    public WidgetColum(GuiFlow flow, Align align) {
        super(flow);
        this.align = align;
    }

    public WidgetColum setFlow(GuiFlow flow) {
        this.flow = flow;
        return this;
    }

    public WidgetColum setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public WidgetColum setAlign(Align aligh) {
        this.align = aligh;
        return this;
    }

    public WidgetColum setWidth(int width) {
        this.preferredWidth = width;
        return this;
    }
}