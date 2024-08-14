package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.flow.GuiFlow;

import java.util.function.Supplier;

public class WidgetPairTable extends GuiTable {
    protected GuiColumn left;
    protected GuiColumn right;

    protected boolean spaceBetween;

    private final GuiFlow defaultFlow;
    public WidgetPairTable(GuiFlow columGuiFlow) {
        this(columGuiFlow, 0);
    }

    public WidgetPairTable(GuiFlow columGuiFlow, int spacing) {
        this(columGuiFlow, Align.LEFT, spacing);
    }

    public WidgetPairTable(GuiFlow defaultFlow, Align align, int spacing) {
        this.defaultFlow = defaultFlow;
        this.spacing = spacing;
        this.align = align;
        this.createRow();
    }

    public WidgetPairTable createRow() {
        return createRow(defaultFlow);
    }

    public WidgetPairTable spaceBetween() {
        this.spaceBetween = true;
        this.left.align = Align.LEFT;
        this.right.align = Align.RIGHT;
        return this;
    }

    public WidgetPairTable createRow(GuiFlow flow) {
        this.addRow(row());
        if (flow != null) {
            left.flow = flow;
            right.flow = flow;
        }
        if (this.spaceBetween) {
            this.left.align = Align.LEFT;
            this.right.align = Align.RIGHT;
        }
        return this;
    }

    protected GuiRow row() {
        return new GuiRow(left = new GuiColumn(), right = new GuiColumn());
    }

    public WidgetPairTable addLeft(GuiControl... guiControls) {
        for (GuiControl c: guiControls)
            this.left.add(c);
        return this;
    }

    public WidgetPairTable addRight(GuiControl... guiControls) {
        for (GuiControl c: guiControls) {
            this.right.add(c);
        }
        return this;
    }

    public WidgetPairTable addLeft(boolean condition, Supplier<GuiControl> guiControls) {
        if (condition) this.addLeft(guiControls.get());
        return this;
    }

    public WidgetPairTable addRight(boolean conditional, Supplier<GuiControl> guiControls) {
        if (conditional) this.addRight(guiControls.get());
        return this;
    }

    public WidgetPairTable setFlowLeft(GuiFlow flow) {
        this.left.flow = flow;
        return this;
    }

    public WidgetPairTable setAlignLeft(Align align) {
        this.left.setAlign(align);
        return this;
    }

    public WidgetPairTable setVAlignLeft(VAlign align) {
        this.left.setVAlign(align);
        return this;
    }

    public WidgetPairTable setFlowRight(GuiFlow flow) {
        this.right.flow = flow;
        return this;
    }

    public WidgetPairTable setAlignRight(Align align) {
        this.right.setAlign(align);
        return this;
    }

    public WidgetPairTable setVAlignRight(VAlign align) {
        this.right.setVAlign(align);
        return this;
    }

    public WidgetPairTable setFlow(GuiFlow flow) {
        this.flow = flow;
        return this;
    }

    public WidgetPairTable setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public WidgetPairTable setAlign(Align aligh) {
        this.align = aligh;
        return this;
    }

    public WidgetPairTable setLeftExpandableX() {
        this.left.setExpandableX();
        return this;
    }

    public WidgetPairTable setLeftExpandableY() {
        this.left.setExpandableY();
        return this;
    }

    public WidgetPairTable setRightExpandableX() {
        this.right.setExpandableX();
        return this;
    }

    public WidgetPairTable setRightExpandableY() {
        this.right.setExpandableY();
        return this;
    }

    public WidgetPairTable setAllExpandableX() {
        this.setLeftExpandableX();
        this.setRightExpandableX();
        return this;
    }

    public WidgetPairTable setAllExpandableY() {
        this.setLeftExpandableY();
        this.setRightExpandableY();
        return this;
    }

    public GuiColumn left() {
        return left;
    }

    public GuiColumn right() {
        return right;
    }
}