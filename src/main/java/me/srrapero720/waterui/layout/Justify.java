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
    BETWEEN;

    /** Flow-axis offset of the packed group given {@code leftover} free pixels; BETWEEN packs at the start. */
    public int offset(int leftover) {
        if (leftover <= 0) return 0;
        return switch (this) {
            case CENTER -> leftover / 2;
            case END -> leftover;
            default -> 0;
        };
    }

    /** Extra BETWEEN gap before step {@code step} of {@code gaps}, spreading {@code budget} without edge drift. */
    public int gap(int budget, int step, int gaps) {
        if (this != BETWEEN || budget <= 0 || gaps <= 0 || step <= 0 || step > gaps) return 0;
        return budget * step / gaps - budget * (step - 1) / gaps;
    }
}
