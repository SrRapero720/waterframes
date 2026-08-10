package me.srrapero720.waterui.core;

/**
 * Placement flags as a bitmask so both axes can be combined in a single int, e.g.
 * {@code Anchor.END | Anchor.CENTER_V}.
 */
public final class Anchor {
    public static final int START = 1;
    public static final int END = 1 << 1;
    public static final int CENTER_H = 1 << 2;
    public static final int STRETCH_H = 1 << 3;

    public static final int TOP = 1 << 4;
    public static final int BOTTOM = 1 << 5;
    public static final int CENTER_V = 1 << 6;
    public static final int STRETCH_V = 1 << 7;

    public static final int CENTER = CENTER_H | CENTER_V;
    public static final int STRETCH = STRETCH_H | STRETCH_V;

    private Anchor() {}

    /** Left edge of a child of {@code size} placed inside {@code [x, x + available)}. */
    public static int x(int anchor, int x, int available, int size) {
        if ((anchor & CENTER_H) != 0 && (anchor & STRETCH_H) == 0) return x + (available - size) / 2;
        if ((anchor & END) != 0) return x + available - size;
        return x;
    }

    /** Top edge of a child of {@code size} placed inside {@code [y, y + available)}. */
    public static int y(int anchor, int y, int available, int size) {
        if ((anchor & CENTER_V) != 0 && (anchor & STRETCH_V) == 0) return y + (available - size) / 2;
        if ((anchor & BOTTOM) != 0) return y + available - size;
        return y;
    }
}
