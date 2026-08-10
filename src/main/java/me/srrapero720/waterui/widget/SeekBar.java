package me.srrapero720.waterui.widget;

import org.lwjgl.glfw.GLFW;

import java.util.function.DoubleConsumer;
import java.util.function.LongSupplier;

/**
 * Playback scrubber over the progress track, in milliseconds. It mirrors the media while idle
 * and takes over only while dragging, so the session moving underneath does not fight the cursor.
 */
public class SeekBar extends ProgressBar {
    private DoubleConsumer onSeekStart;
    private DoubleConsumer onSeek;
    private DoubleConsumer onSeekEnd;
    private DoubleConsumer onScroll;

    private boolean dragging;
    private long dragTime;

    public SeekBar() {
        this(null, null);
    }

    public SeekBar(LongSupplier time, LongSupplier duration) {
        super(time, duration, ValueFormat.DURATION);
    }

    /** Decoupled time source; DURATION stays the default format unless overridden. */
    public SeekBar time(LongSupplier time) {
        this.value = time;
        return this;
    }

    public SeekBar duration(LongSupplier duration) {
        this.max = duration;
        return this;
    }

    @Override
    public SeekBar format(ValueFormat format) {
        super.format(format);
        return this;
    }

    @Override
    public SeekBar textScale(float textScale) {
        super.textScale(textScale);
        return this;
    }

    public SeekBar onSeekStart(DoubleConsumer onSeekStart) {
        this.onSeekStart = onSeekStart;
        return this;
    }

    public SeekBar onSeek(DoubleConsumer onSeek) {
        this.onSeek = onSeek;
        return this;
    }

    public SeekBar onSeekEnd(DoubleConsumer onSeekEnd) {
        this.onSeekEnd = onSeekEnd;
        return this;
    }

    public SeekBar onScroll(DoubleConsumer onScroll) {
        this.onScroll = onScroll;
        return this;
    }

    @Override
    protected long current() {
        return dragging ? dragTime : super.current();
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3): ANY OTHER BUTTON WOULD SCRUB AND OWN THE POINTER
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !enabled || max == null || max.getAsLong() <= 0) return false;
        this.dragging = true;
        this.dragTime = current();
        // ANNOUNCE THE SCRUB ORIGIN ONCE, BEFORE THE PRESS JUMPS THE HANDLE TO THE CURSOR
        if (onSeekStart != null) onSeekStart.accept(dragTime);
        this.seek(mouseX);
        return true;
    }

    @Override
    public void mouseMove(double mouseX, double mouseY) {
        if (dragging) this.seek(mouseX);
    }

    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        if (!dragging) return;
        this.dragging = false;
        if (onSeekEnd != null) onSeekEnd.accept(dragTime);
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        if (!enabled || onScroll == null) return false;
        onScroll.accept(amount);
        return true;
    }

    private void seek(double mouseX) {
        int width = contentWidth();
        long duration = this.max();
        if (width <= 0 || duration <= 0) return;
        double ratio = Math.clamp((mouseX - contentX()) / width, 0d, 1d);
        this.dragTime = Math.round(ratio * duration);
        if (onSeek != null) onSeek.accept(dragTime);
    }
}
