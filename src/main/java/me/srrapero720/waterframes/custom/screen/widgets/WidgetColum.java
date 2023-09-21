package me.srrapero720.waterframes.custom.screen.widgets;

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

    public WidgetColum(int width, GuiFlow flow) {
        super(width, flow);
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
}