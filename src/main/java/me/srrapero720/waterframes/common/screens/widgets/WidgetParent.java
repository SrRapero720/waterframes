package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class WidgetParent extends GuiParent {
    public WidgetParent(GuiFlow flow, GuiControl... childs) {
        this(flow, 2, childs);
    }

    public WidgetParent(GuiFlow flow, int spacing, GuiControl... childs) {
        this(flow, Align.LEFT, spacing, childs);
    }

    public WidgetParent(GuiFlow flow, Align align, GuiControl... childs) {
        this(flow, align, 2, childs);
    }

    public WidgetParent(GuiFlow flow, Align align, int spacing, GuiControl... childs) {
        super("", flow, align, VAlign.TOP);
        this.setSpacing(spacing);
        this.addWidget(childs);
    }

    public WidgetParent setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    @Override
    public WidgetParent setAlign(Align align) {
        this.align = align;
        return this;
    }

    public WidgetParent setFlow(GuiFlow flow) {
        this.flow = flow;
        return this;
    }

    public WidgetParent addWidget(GuiControl... controls) {
        for (GuiControl control: controls) {
            this.add(control);
        }
        return this;
    }

    public WidgetParent addWidgetIf(boolean conditional, GuiControl... control) {
        if (conditional) {
            this.addWidget(control);
        }
        return this;
    }
}