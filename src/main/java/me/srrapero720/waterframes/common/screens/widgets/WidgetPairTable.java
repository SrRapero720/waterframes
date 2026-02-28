package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.parent.GuiColumn;
import team.creative.creativecore.common.gui.control.parent.GuiRow;
import team.creative.creativecore.common.gui.control.parent.GuiTable;
import team.creative.creativecore.common.gui.flow.GuiFlow;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WidgetPairTable extends GuiTable {
    protected GuiColumn left;
    protected GuiColumn right;

    protected boolean spaceBetween;

    private final GuiFlow defaultFlow;
    public WidgetPairTable(IGuiParent parent, GuiFlow columGuiFlow) {
        this(parent, columGuiFlow, 0);
    }

    public WidgetPairTable(IGuiParent parent, GuiFlow columGuiFlow, int spacing) {
        this(parent, columGuiFlow, Align.LEFT, spacing);
    }

    public WidgetPairTable(IGuiParent parent, GuiFlow defaultFlow, Align align, int spacing) {
        super(parent);
        this.defaultFlow = defaultFlow;
        super.setSpacing(spacing);
        super.setAlign(align);
        this.createRow();
    }

    public WidgetPairTable applySelf(Consumer<WidgetPairTable> consumer) {
        consumer.accept(this);
        return this;
    }

    public WidgetPairTable applyOnLeft(Consumer<GuiColumn> consumer) {
        consumer.accept(this.left);
        return this;
    }

    public WidgetPairTable applyOnRight(Consumer<GuiColumn> consumer) {
        consumer.accept(this.right);
        return this;
    }

    public WidgetPairTable createRow() {
        return createRow(defaultFlow);
    }

    public WidgetPairTable spaceBetween() {
        this.spaceBetween = true;
        this.left.setAlign(Align.LEFT);
        this.right.setAlign(Align.RIGHT);
        return this;
    }

    public WidgetPairTable createRow(GuiFlow flow) {
        this.addRow(row());
        if (flow != null) {
            left.setFlow(flow);
            right.setFlow(flow);
        }
        if (this.spaceBetween) {
            this.left.setAlign(Align.LEFT);
            this.right.setAlign(Align.RIGHT);
        }
        return this;
    }

    protected GuiRow row() {
        left = new GuiColumn(this);
        right = new GuiColumn(this);
        return new GuiRow(this, left, right);
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
        this.left.setFlow(flow);
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
        this.right.setFlow(flow);
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

    public WidgetPairTable setVAlignAll(VAlign align) {
        this.left.setVAlign(align);
        this.right.setVAlign(align);
        return this;
    }

    public WidgetPairTable setFlow(GuiFlow flow) {
        super.setFlow(flow);
        return this;
    }

    public WidgetPairTable setSpacing(int spacing) {
        super.setSpacing(spacing);
        return this;
    }

    public WidgetPairTable setAlign(Align aligh) {
        super.setAlign(aligh);
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