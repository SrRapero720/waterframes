package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Face;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.LongSupplier;

/**
 * Read only progress track: same face, fill and centred label as the seek bar, but deaf to
 * the mouse. It mirrors whatever its suppliers say on every draw, so it never goes stale.
 */
public class ProgressBar extends Element {
    protected LongSupplier value;
    protected LongSupplier max;
    private ValueFormat format;
    private float textScale = 1f;

    public ProgressBar() {
        this(null, null, null);
    }

    public ProgressBar(LongSupplier value, LongSupplier max, ValueFormat format) {
        this.face = Face.BAR;
        this.value = value;
        this.max = max;
        this.format = format;
    }

    public ProgressBar value(LongSupplier value) {
        this.value = value;
        return this;
    }

    public ProgressBar max(LongSupplier max) {
        this.max = max;
        return this;
    }

    public ProgressBar format(ValueFormat format) {
        this.format = format;
        return this;
    }

    public ProgressBar textScale(float textScale) {
        this.textScale = textScale;
        return this;
    }

    public float textScale() {
        return textScale;
    }

    /** Value the bar paints; the seek bar swaps it for the drag position while grabbed. */
    protected long current() {
        return value != null ? value.getAsLong() : 0;
    }

    /** Span of the bar; an unwired one reads as an empty percentage instead of dividing by nothing. */
    protected long max() {
        return max != null ? max.getAsLong() : 100;
    }

    @Override
    protected int prefContentWidth(int available) {
        return 40;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return theme().progress().height();
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        long max = this.max();
        long value = this.current();
        int filled = max <= 0 ? 0 : (int) Math.round(contentWidth() * Math.clamp(value / (double) max, 0d, 1d));
        if (filled > 0) {
            theme().progress().fill().draw(graphics, contentX(), contentY(), filled, contentHeight());
        }
        this.drawCenteredScaled(graphics, format != null ? format.format(value, max) : String.valueOf(value), textColor(), textScale);
    }
}
