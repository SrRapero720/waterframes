package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

public class Icon extends Element {
    // INTRINSIC SIZE OF A PLAIN ICON IN GUI PIXELS
    private static final int DEFAULT_SIZE = 12;

    protected me.srrapero720.waterui.theme.Icon icon;
    // LIVE SOURCE READ ON EVERY DRAW; NULL FALLS BACK TO THE FIXED icon
    private Supplier<me.srrapero720.waterui.theme.Icon> source;

    public Icon() {
        this(null);
    }

    public Icon(me.srrapero720.waterui.theme.Icon icon) {
        this.icon = icon;
    }

    public Icon icon(me.srrapero720.waterui.theme.Icon icon) {
        this.icon = icon;
        return this;
    }

    /** Mirrors a live icon read on every draw instead of the fixed one. */
    public Icon source(Supplier<me.srrapero720.waterui.theme.Icon> source) {
        this.source = source;
        return this;
    }

    public me.srrapero720.waterui.theme.Icon icon() {
        return source != null ? source.get() : icon;
    }

    @Override
    protected int prefContentWidth(int available) {
        return DEFAULT_SIZE;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return DEFAULT_SIZE;
    }

    // ICONS SCALE DOWN CLEANLY, SO THE INTRINSIC SIZE IS A PREFERENCE AND NOT A FLOOR
    @Override
    protected int minContentWidth(int available) {
        return 4;
    }

    @Override
    protected int minContentHeight(int width, int available) {
        return 4;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        me.srrapero720.waterui.theme.Icon current = this.icon();
        if (current != null) current.drawSquared(graphics, contentX(), contentY(), contentWidth(), contentHeight(), shadow());
    }
}
