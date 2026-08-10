package me.srrapero720.waterui.core;

import net.minecraft.Util;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/**
 * Listener bag and press bookkeeping behind {@link Element}'s generic pointer events.
 * Allocated on the first listener, so an element that never listens costs one null field.
 * Click means press (UI-SPEC.md §13); double and long presses ride the same left press.
 */
public final class PointerEvents {
    /** Position-carrying pointer listener, in the element's local space. */
    public interface Drag { void at(double x, double y); }

    // HOW LONG A LEFT PRESS MUST HOLD BEFORE IT COUNTS AS A LONG PRESS
    private static final long LONG_PRESS_MS = 500;
    // WINDOW BETWEEN TWO LEFT PRESSES THAT UPGRADES THE SECOND ONE TO A DOUBLE CLICK
    private static final long DOUBLE_CLICK_MS = 250;

    Runnable click;
    Runnable rightClick;
    Runnable doubleClick;
    Runnable longPress;
    Runnable pressStart;
    Runnable pressEnd;
    Drag drag;
    Consumer<Boolean> hover;
    Runnable enter;
    Runnable exit;
    DoubleConsumer scroll;
    Runnable focusIn;
    Runnable focusOut;
    Consumer<List<Path>> fileDrop;

    // LEFT-PRESS BOOKKEEPING DRIVING pressEnd/longPress/doubleClick/drag
    private boolean pressed;
    private boolean longFired;
    private long pressTime;
    private long lastPress;

    // CONSUME-BY-DEFAULT (§13): ANY LISTENER RELEVANT TO THIS BUTTON EATS THE PRESS, SO IT
    // NEVER BUBBLES PAST AN ELEMENT THAT REACTED TO IT. IRRELEVANT BUTTONS PASS THROUGH
    boolean press(int button) {
        if (button == 1) {
            if (rightClick == null) return false;
            rightClick.run();
            return true;
        }
        if (button != 0) return false;
        if (click == null && doubleClick == null && longPress == null
                && pressStart == null && pressEnd == null && drag == null) return false;

        long now = Util.getMillis();
        if (click != null) click.run();
        if (doubleClick != null && now - lastPress <= DOUBLE_CLICK_MS) doubleClick.run();
        this.lastPress = now;
        this.pressed = true;
        this.longFired = false;
        this.pressTime = now;
        if (pressStart != null) pressStart.run();
        return true;
    }

    void release(int button) {
        if (button != 0 || !pressed) return;
        this.pressed = false;
        if (pressEnd != null) pressEnd.run();
    }

    void move(double x, double y) {
        if (pressed && drag != null) drag.at(x, y);
    }

    // FRAME PULSE FROM THE PAINT PASS, WHERE A HELD PRESS CROSSES THE LONG-PRESS THRESHOLD
    void frame() {
        if (!pressed || longFired || longPress == null) return;
        if (Util.getMillis() - pressTime >= LONG_PRESS_MS) {
            this.longFired = true;
            longPress.run();
        }
    }

    void hover(boolean inside) {
        if (hover != null) hover.accept(inside);
        if (inside && enter != null) enter.run();
        if (!inside && exit != null) exit.run();
    }

    void focus(boolean focused) {
        if (focused && focusIn != null) focusIn.run();
        if (!focused && focusOut != null) focusOut.run();
    }

    boolean wheel(double amount) {
        if (scroll == null) return false;
        scroll.accept(amount);
        return true;
    }
}
