package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.flow.GuiFlow;

import java.util.function.Supplier;

public class WidgetTripleTable extends WidgetPairTable {
    private GuiColumn center;

    public WidgetTripleTable(GuiFlow columGuiFlow) {
        super(columGuiFlow);
    }

    @Override
    public WidgetTripleTable spaceBetween() {
        this.center.align = Align.CENTER;
        super.spaceBetween();
        return this;
    }

    @Override
    protected GuiRow row() {
        return new GuiRow(left = new GuiColumn(), center = new GuiColumn(), right = new GuiColumn());
    }

    @Override
    public WidgetTripleTable createRow() {
        super.createRow();
        return this;
    }

    @Override
    public WidgetTripleTable createRow(GuiFlow flow) {
        super.createRow(flow);
        if (flow != null) {
            center.flow = flow;
        }
        if (spaceBetween) {
            this.center.align = Align.CENTER;
        }
        return this;
    }

    public WidgetTripleTable addCenter(GuiControl... guiControls) {
        for (GuiControl c: guiControls) {
            this.center.add(c);
        }
        return this;
    }

    @Override
    public WidgetTripleTable addLeft(GuiControl... guiControls) {
        super.addLeft(guiControls);
        return this;
    }

    @Override
    public WidgetTripleTable addRight(GuiControl... guiControls) {
        super.addRight(guiControls);
        return this;
    }

    public WidgetTripleTable addCenter(boolean condition, Supplier<GuiControl> guiControls) {
        if (condition) return this.addCenter(guiControls.get());
        return this;
    }

    public WidgetTripleTable setFlowCenter(GuiFlow flow) {
        this.center.flow = flow;
        return this;
    }

    public WidgetTripleTable setAlignCenter(Align align) {
        this.center.setAlign(align);
        return this;
    }

    public WidgetTripleTable setVAlignCenter(VAlign align) {
        this.center.setVAlign(align);
        return this;
    }

    public WidgetTripleTable setCenterExpandableX() {
        this.center.setExpandableX();
        return this;
    }

    public WidgetTripleTable setCenterExpandableY() {
        this.center.setExpandableY();
        return this;
    }

    @Override
    public WidgetTripleTable setAllExpandableX() {
        this.setCenterExpandableX();
        super.setAllExpandableX();
        return this;
    }

    @Override
    public WidgetTripleTable setAllExpandableY() {
        this.setCenterExpandableY();
        super.setAllExpandableY();
        return this;
    }

    public GuiColumn center() {
        return center;
    }
}
