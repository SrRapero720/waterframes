package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.theme.Icon;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/**
 * Two state icon button mirroring live state: the supplier is read on every draw, so the
 * icon is always the real value and never waits a tick to catch up. The click handler flips
 * the state at its source.
 */
public class Toggle extends Button {
    private Icon on;
    private Icon off;
    private BooleanSupplier value;

    public Toggle() {}

    public Toggle(Icon on, Icon off, BooleanSupplier value, IntConsumer onClick) {
        super(onClick);
        this.on = on;
        this.off = off;
        this.value = value;
    }

    public Toggle icons(Icon on, Icon off) {
        this.on = on;
        this.off = off;
        return this;
    }

    public Toggle iconOn(Icon on) {
        this.on = on;
        return this;
    }

    public Toggle iconOff(Icon off) {
        this.off = off;
        return this;
    }

    public Toggle value(BooleanSupplier value) {
        this.value = value;
        return this;
    }

    @Override
    public Toggle onClick(IntConsumer onClick) {
        super.onClick(onClick);
        return this;
    }

    public boolean value() {
        return value != null && value.getAsBoolean();
    }

    @Override
    public Icon icon() {
        return this.value() ? on : off;
    }
}
