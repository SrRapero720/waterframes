package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Icon;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Supplier;

/** Plain picture element behind the {@code Icon} tag, fixed or mirroring a live icon source. */
public class IconView extends Element {
    // INTRINSIC SIZE OF A PLAIN ICON IN GUI PIXELS
    private static final int DEFAULT_SIZE = 12;

    protected Icon icon;
    // LIVE SOURCE READ ON EVERY DRAW; NULL FALLS BACK TO THE FIXED icon
    private Supplier<Icon> source;

    public IconView() {
        this(null);
    }

    public IconView(Icon icon) {
        this.icon = icon;
    }

    public IconView icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    /** Mirrors a live icon read on every draw instead of the fixed one. */
    public IconView source(Supplier<Icon> source) {
        this.source = source;
        return this;
    }

    public Icon icon() {
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
        Icon current = this.icon();
        if (current != null) current.drawSquared(graphics, contentX(), contentY(), contentWidth(), contentHeight(), shadow());
    }
}
