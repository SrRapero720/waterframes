package me.srrapero720.waterui.widget;

import org.lwjgl.glfw.GLFW;

import java.util.function.DoubleConsumer;

/**
 * Text field that only accepts a number. Typing a value is the way to hit a figure the pixel
 * resolution of a slider cannot reach.
 */
public class InputNumber extends InputText {
    private final DoubleConsumer onCommit;

    public InputNumber(String name, double value, DoubleConsumer onCommit) {
        super(name);
        // FLUSH WITH ITS FRAME: AS A SLIDER EDITOR IT HAS TO LAND EXACTLY ON THE SLIDER BOX
        this.padding(0);
        this.onCommit = onCommit;
        this.maxLength(16);
        this.box.setFilter(InputNumber::acceptable);
        this.value(value);
    }

    // PLAIN DECIMAL SHAPES ONLY: parseDouble WOULD ALSO ACCEPT "1e5", "NaN" OR HEX FLOATS
    private static boolean acceptable(String text) {
        boolean dot = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '-' && i == 0) continue;
            if (c == '.' && !dot) { dot = true; continue; }
            if (c < '0' || c > '9') return false;
        }
        return true;
    }

    public void value(double value) {
        this.text(ValueFormat.decimal(value));
    }

    public void commit() {
        try {
            onCommit.accept(Double.parseDouble(this.text()));
        } catch (NumberFormatException ignored) {
            // HALF TYPED VALUE, THE OWNER KEEPS WHATEVER IT HAD
        }
    }

    @Override
    public boolean keyDown(int key, int scan, int modifiers) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            this.commit();
            return true;
        }
        return super.keyDown(key, scan, modifiers);
    }

    @Override
    public void focusChanged(boolean focused) {
        super.focusChanged(focused);
        if (!focused) this.commit();
    }
}
