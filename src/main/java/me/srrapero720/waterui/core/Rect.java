package me.srrapero720.waterui.core;

/** Mutable rectangle in GUI pixels, reused across layout passes instead of reallocated. */
public final class Rect {
    public int x, y, width, height;

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public void set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    @Override
    public String toString() {
        return "Rect[" + x + ", " + y + ", " + width + "x" + height + "]";
    }
}
