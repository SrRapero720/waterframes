package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.flow.GuiFlow;

public class WidgetDoubleTable extends GuiTable {
    private GuiColumn tableLeft;
    private GuiColumn tableRight;

    private final GuiFlow columFlow;
    public WidgetDoubleTable(GuiFlow columGuiFlow) {
        this(columGuiFlow, 0);
    }

    public WidgetDoubleTable(GuiFlow columGuiFlow, int spacing) {
        this(columGuiFlow, Align.LEFT, spacing);
    }

    public WidgetDoubleTable(GuiFlow columGuiFlow, Align align, int spacing) {
        this.columFlow = columGuiFlow;
        this.spacing = spacing;
        this.align = align;
        this.createRow();
    }

    public WidgetDoubleTable createRow() {
        return createRow(columFlow);
    }

    public WidgetDoubleTable createRow(GuiFlow flow) {
        this.addRow(new GuiRow(tableLeft = new GuiColumn(), tableRight = new GuiColumn()));
        if (flow != null) {
            tableLeft.flow = flow;
            tableRight.flow = flow;
        }
        return this;
    }

    public WidgetDoubleTable addLeft(GuiControl... guiControls) {
        for (GuiControl c: guiControls)
            this.tableLeft.add(c);
        return this;
    }

    public WidgetDoubleTable addRight(GuiControl... guiControls) {
        for (GuiControl c: guiControls) {
            this.tableRight.add(c);
        }
        return this;
    }

    public WidgetDoubleTable addLeftIf(boolean condition, GuiControl... guiControls) {
        if (condition) this.addLeft(guiControls);
        return this;
    }

    public WidgetDoubleTable addRightIf(boolean conditional, GuiControl... guiControls) {
        if (conditional) this.addRight(guiControls);
        return this;
    }

    public WidgetDoubleTable setFlowLeft(GuiFlow flow) {
        this.tableLeft.flow = flow;
        return this;
    }

    public WidgetDoubleTable setAlignLeft(Align align) {
        this.tableLeft.setAlign(align);
        return this;
    }

    public WidgetDoubleTable setVAlignLeft(VAlign align) {
        this.tableLeft.setVAlign(align);
        return this;
    }

    public WidgetDoubleTable setFlowRight(GuiFlow flow) {
        this.tableRight.flow = flow;
        return this;
    }

    public WidgetDoubleTable setAlignRight(Align align) {
        this.tableRight.setAlign(align);
        return this;
    }

    public WidgetDoubleTable setVAlignRight(VAlign align) {
        this.tableRight.setVAlign(align);
        return this;
    }

    public WidgetDoubleTable setFlow(GuiFlow flow) {
        this.flow = flow;
        return this;
    }

    public WidgetDoubleTable setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public WidgetDoubleTable setAlign(Align aligh) {
        this.align = aligh;
        return this;
    }

    public WidgetDoubleTable setLeftExpandableX() {
        this.tableLeft.setExpandableX();
        return this;
    }

    public WidgetDoubleTable setLeftExpandableY() {
        this.tableLeft.setExpandableY();
        return this;
    }

    public WidgetDoubleTable setRightExpandableX() {
        this.tableRight.setExpandableX();
        return this;
    }

    public WidgetDoubleTable setRightExpandableY() {
        this.tableRight.setExpandableY();
        return this;
    }

    public WidgetDoubleTable expandX() {
        this.setExpandableX();
        return this;
    }

    public WidgetDoubleTable expandY() {
        this.setExpandableY();
        return this;
    }

    public GuiColumn left() {
        return tableLeft;
    }

    public GuiColumn right() {
        return tableRight;
    }

    public WidgetDoubleTable left(GuiColumn guiChildControls) {
        tableLeft = guiChildControls;
        return this;
    }

    public WidgetDoubleTable right(GuiColumn guiChildControls) {
        tableRight = guiChildControls;
        return this;
    }
}