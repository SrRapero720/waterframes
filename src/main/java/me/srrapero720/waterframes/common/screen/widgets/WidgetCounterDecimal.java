package me.srrapero720.waterframes.common.screen.widgets;

import net.minecraft.util.Mth;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonHoldSlim;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.event.GuiEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;

public class WidgetCounterDecimal extends GuiParent {
    static ControlFormatting CLICKABLE = new ControlFormatting(ControlFormatting.ControlStyleBorder.SMALL, 0, ControlFormatting.ControlStyleFace.CLICKABLE);


    final float scaleMultiplier;
    public float min;
    public float max;
    public GuiTextfield textfield;

    public WidgetCounterDecimal(String name, float value, float min, float max, float scaleMultiplier) {
        super(name);
        this.min = min;
        this.max = max;
        this.flow = GuiFlow.STACK_X;
        this.valign = VAlign.STRETCH;
        this.spacing = 0;
        this.textfield = (new GuiTextfield("value", "" + Mth.clamp(value, min, max), 20, 8)).setFloatOnly();
        this.add(this.textfield.setExpandableX());
        GuiParent buttons = new GuiParent(GuiFlow.STACK_Y);
        buttons.spacing = 0;
        this.add(buttons);
        buttons.add((new GuiButtonHoldSlim("+", (x) -> {
            this.textfield.setText("" + this.stepUp(this.textfield.parseFloat()));
            this.raiseEvent(new GuiControlChangedEvent(this));
        }) {
            @Override
            public ControlFormatting getControlFormatting() {
                return CLICKABLE;
            }
        }).setTranslate("gui.plus"));
        buttons.add((new GuiButtonHoldSlim("-", (x) -> {
            this.textfield.setText("" + this.stepDown(this.textfield.parseFloat()));
            this.raiseEvent(new GuiControlChangedEvent(this));
        })  {
            @Override
            public ControlFormatting getControlFormatting() {
                return CLICKABLE;
            }
        }).setTranslate("gui.minus"));
        this.scaleMultiplier = scaleMultiplier;
    }


    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }

    public void raiseEvent(GuiEvent event) {
        if (event instanceof GuiControlChangedEvent controlEvent) {
            if (controlEvent.control.is(new String[]{"value"})) {
                super.raiseEvent(new GuiControlChangedEvent(this));
                return;
            }
        }

        super.raiseEvent(event);
    }

    public boolean isExpandableX() {
        return this.expandableX;
    }

    public float getValue() {
        return Mth.clamp(this.textfield.parseFloat(), this.min, this.max);
    }

    public void setValue(float value) {
        GuiTextfield var10000 = this.textfield;
        float var10001 = Mth.clamp(value, this.min, this.max);
        var10000.setText("" + var10001);
    }

    public float stepUp(float value) {
        int scaled = (int) (value / scaleMultiplier);
        scaled++;
        return Math.min(max, scaled * scaleMultiplier);
    }

    public float stepDown(float value) {
        int scaled = (int) (value / scaleMultiplier);
        scaled--;
        return Math.max(min, scaled * scaleMultiplier);
    }

    public WidgetCounterDecimal setSpacing(int spacing) {
        this.spacing = spacing;
        return this;
    }

    public WidgetCounterDecimal setAlign(Align aligh) {
        this.align = aligh;
        return this;
    }

    public WidgetCounterDecimal expandX() {
        this.setExpandableX();
        return this;
    }

    public WidgetCounterDecimal expandY() {
        this.setExpandableY();
        return this;
    }

    public WidgetCounterDecimal add2(GuiControl guiControl) {
        super.add(guiControl);
        return this;
    }
}