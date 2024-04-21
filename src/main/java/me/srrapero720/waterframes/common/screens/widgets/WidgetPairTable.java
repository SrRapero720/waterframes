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
    private GuiColumn left;
    private GuiColumn right;

    private final GuiFlow columFlow;
    public WidgetPairTable(GuiFlow columGuiFlow) {
        this(columGuiFlow, 0);
    }

    public WidgetPairTable(GuiFlow columGuiFlow, int spacing) {
        this(columGuiFlow, Align.LEFT, spacing);
    }

    public WidgetPairTable(GuiFlow columGuiFlow, Align align, int spacing) {
        this.columFlow = columGuiFlow;
        this.spacing = spacing;
        this.align = align;
        this.createRow();
    }

    public WidgetPairTable createRow() {
        return createRow(columFlow);
    }

    public WidgetPairTable createRow(GuiFlow flow) {
        this.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        if (flow != null) {
            left.flow = flow;
            right.flow = flow;
        }
        return this;
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

    public WidgetPairTable expandX() {
        this.setExpandableX();
        return this;
    }

    public WidgetPairTable expandY() {
        this.setExpandableY();
        return this;
    }

    public GuiColumn left() {
        return left;
    }

    public GuiColumn right() {
        return right;
    }

    public WidgetPairTable left(GuiColumn guiChildControls) {
        left = guiChildControls;
        return this;
    }

    public WidgetPairTable right(GuiColumn guiChildControls) {
        right = guiChildControls;
        return this;
    }
}