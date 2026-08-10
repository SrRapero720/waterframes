package me.srrapero720.waterui.core;

/**
 * Drag gesture controller an element wears via {@link Element#thumb(DragThumb)}: whatever carries
 * it — an icon, a button, a picture — becomes the grab surface of its owner. The cursor becomes
 * an open hand over it, a closed one while held, and a small slide turns the hold into a drag.
 * It only speaks the gesture — grab, slide, drop or tap — the owner decides what moving means.
 */
public final class DragThumb {
    // SLIDE DISTANCE THAT TURNS A HOLD INTO A DRAG; ANYTHING SHORTER RELEASES AS A TAP
    private static final int THRESHOLD = 4;

    private PointerEvents.Drag onGrab;
    private PointerEvents.Drag onSlide;
    private Runnable onDrop;
    private Runnable onTap;

    private boolean holding;
    private boolean dragging;
    // TRACKS WHETHER THE LEFT BUTTON IS PHYSICALLY DOWN, SO A STALE HOLD FROM A REBUILD
    // DROPS ITSELF ON THE NEXT MOVE INSTEAD OF STARTING A PHANTOM DRAG
    private boolean buttonDown;
    private double pressX;
    private double pressY;

    /** Fires once, with the press point, when the slide crosses the threshold and the drag begins. */
    public DragThumb onGrab(PointerEvents.Drag onGrab) {
        this.onGrab = onGrab;
        return this;
    }

    /** Fires with the cursor position on every move while dragging. */
    public DragThumb onSlide(PointerEvents.Drag onSlide) {
        this.onSlide = onSlide;
        return this;
    }

    /** Fires when a drag releases. */
    public DragThumb onDrop(Runnable onDrop) {
        this.onDrop = onDrop;
        return this;
    }

    /** Fires when the hold releases without ever crossing the drag threshold. */
    public DragThumb onTap(Runnable onTap) {
        this.onTap = onTap;
        return this;
    }

    /** Abandons a session whose release got lost, e.g. the tree rebuilt mid-drag. */
    public void reset() {
        this.holding = false;
        this.dragging = false;
        this.buttonDown = false;
    }

    // GESTURE HOOKS, DRIVEN BY THE CARRIER ELEMENT'S ROUTED INPUT AND ITS PAINT PASS

    boolean press(double x, double y, int button) {
        if (button != 0) return false;
        this.holding = true;
        this.buttonDown = true;
        this.dragging = false;
        this.pressX = x;
        this.pressY = y;
        Cursors.claim(Cursors.Shape.GRABBING);
        return true;
    }

    void move(double x, double y) {
        if (!holding) return;
        // A HOLD THAT OUTLIVED ITS RELEASE (HOST-CACHED ROW REUSED ACROSS A REBUILD) SILENTLY
        // DROPS ITSELF WHEN THE BUTTON IS NO LONGER DOWN, INSTEAD OF STARTING A PHANTOM DRAG
        if (!buttonDown) {
            this.holding = false;
            this.dragging = false;
            return;
        }
        if (!dragging && (Math.abs(x - pressX) >= THRESHOLD || Math.abs(y - pressY) >= THRESHOLD)) {
            this.dragging = true;
            if (onGrab != null) onGrab.at(pressX, pressY);
        }
        if (dragging && onSlide != null) onSlide.at(x, y);
    }

    void release(int button) {
        if (button != 0 || !holding) return;
        this.holding = false;
        this.buttonDown = false;
        if (dragging) {
            this.dragging = false;
            if (onDrop != null) onDrop.run();
        } else if (onTap != null) {
            onTap.run();
        }
    }

    // CURSOR IS THE AFFORDANCE, RE-CLAIMED EVERY FRAME SO A CARRIER THAT STOPS PAINTING
    // HANDS THE ARROW BACK ON ITS OWN
    void frame(boolean hovering) {
        if (holding) Cursors.claim(Cursors.Shape.GRABBING);
        else if (hovering) Cursors.claim(Cursors.Shape.GRAB);
    }
}
