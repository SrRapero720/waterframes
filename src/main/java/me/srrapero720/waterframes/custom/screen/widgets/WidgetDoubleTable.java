package me.srrapero720.waterframes.custom.screen.widgets;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.flow.GuiFlow;

import java.util.function.Supplier;

public class WidgetDoubleTable extends GuiTable {
    private static final Supplier<WidgetColum> DEFAULT_FACTORY = WidgetColum::new;

    private WidgetColum first;
    private WidgetColum second;
    private final Supplier<WidgetColum> factory;
    public WidgetDoubleTable() {
        this(DEFAULT_FACTORY);
    }

    public WidgetDoubleTable(Supplier<WidgetColum> columFactory) {
        super();
        this.factory = columFactory;
        this.createRow();
    }

    public WidgetDoubleTable createRow() {
        this.addRow(new GuiRow(first = factory.get(), second = factory.get()));
        return this;
    }

    public WidgetDoubleTable addOnFirst(GuiControl control) {
        this.first.add(control);
        return this;
    }

    public WidgetColum getFirstRow() {
        return first;
    }

    public WidgetDoubleTable addOnSecond(GuiControl control) {
        this.second.add(control);
        return this;
    }

    public WidgetColum getSecondRow() {
        return second;
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
}