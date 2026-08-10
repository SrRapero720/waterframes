package me.srrapero720.waterui.core;

/** Edge insets used for both padding and margin. Mirrors WaterMedia's own UI vocabulary. */
public record Spacing(int top, int right, int bottom, int left) {
    public static final Spacing ZERO = new Spacing(0, 0, 0, 0);

    public static Spacing all(int value) {
        return new Spacing(value, value, value, value);
    }

    public static Spacing hv(int horizontal, int vertical) {
        return new Spacing(vertical, horizontal, vertical, horizontal);
    }

    public int horizontal() {
        return left + right;
    }

    public int vertical() {
        return top + bottom;
    }
}
