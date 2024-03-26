package me.srrapero720.waterframes.common.screens.widgets;

import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.type.Color;

import java.util.function.Consumer;

public class WidgetCheckButtonIcon extends GuiButtonIcon {
    protected Icon on, off;
    public boolean value;

    public WidgetCheckButtonIcon(String name, Icon on, Icon off, boolean state) {
        this(name, on, off, state, null);
    }

    public WidgetCheckButtonIcon(String name, Icon on, Icon off, boolean state, Consumer<Integer> pressed) {
        super(name, state ? on : off, pressed);
        this.value = state;
        this.on = on;
        this.off = off;
    }

    public WidgetCheckButtonIcon setOnIcon(Icon icon) {
        this.on = icon;
        return this;
    }

    public WidgetCheckButtonIcon setOffIcon(Icon icon) {
        this.off = icon;
        return this;
    }

    @Override
    public WidgetCheckButtonIcon setColor(Color color) {
        this.color = color;
        return this;
    }

    @Override
    public WidgetCheckButtonIcon setShadow(Color shadow) {
        this.shadow = shadow;
        return this;
    }

    @Override
    public WidgetCheckButtonIcon setSquared(boolean squared) {
        this.squared = squared;
        return this;
    }

    public boolean getState() {
        return value;
    }

    public void setState(boolean value) {
        if (this.value != value) {
            this.value = value;
            this.icon = value ? on : off;
            this.raiseEvent(new GuiControlChangedEvent<>(this));
        }
    }

    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        this.setState(!this.value);
        return super.mouseClicked(rect, x, y, button);
    }
}