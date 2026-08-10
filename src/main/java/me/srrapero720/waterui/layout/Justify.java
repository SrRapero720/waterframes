package me.srrapero720.waterui.layout;

/** Distribution of children along the flow axis when there is leftover space. */
public enum Justify {
    /** Children packed at the flow start (default). */
    START,
    /** Packed group centered. */
    CENTER,
    /** Packed at the flow end. */
    END,
    /** Even gaps between children, none at the edges. */
    BETWEEN
}
