package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.theme.Face;
import me.srrapero720.waterui.theme.SliderTheme;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.function.DoubleConsumer;

/**
 * Horizontal value picker drawn as a bar with a grabbable knob, dressed by the
 * {@link SliderTheme} of whatever theme is live. A {@code step} of 0 keeps it continuous,
 * anything else snaps.
 */
public class Slider extends Element {
    private static final int ICON = 12;
    private static final int GAP = 2;

    public double min;
    public double max;
    public double step;

    private double value;
    private ValueFormat format;
    private DoubleConsumer onChange;
    private DoubleConsumer onCommit;
    private boolean dragging;
    // VALUE WHEN THE PRESS LANDED, SO A RELEASE CAN TELL A REAL DRAG FROM A NO-OP CLICK
    private double pressValue;

    // GLYPH SCALE FOR THE VALUE READOUT
    private float textScale = 1f;

    // OPTIONAL ICON PINNED TO THE LEFT, SO A LABELLED SLIDER NEEDS NO WRAPPER ROW
    private Icon icon;

    public Slider() {
        this(0, 0, 100, null);
    }

    public Slider(double value, double min, double max, ValueFormat format) {
        this.face = Face.NESTED;
        this.padding(0);
        this.min = min;
        this.max = max;
        this.value = Math.clamp(value, min, max);
        this.format = format;
    }

    public Slider(double value, double min, double max, ValueFormat format, DoubleConsumer onChange) {
        this(value, min, max, format);
        this.onChange = onChange;
    }

    public static Slider stepped(int value, int min, int max, ValueFormat format) {
        Slider slider = new Slider(value, min, max, format);
        slider.step = 1;
        return slider;
    }

    public static Slider stepped(int value, int min, int max, ValueFormat format, DoubleConsumer onChange) {
        Slider slider = stepped(value, min, max, format);
        slider.onChange = onChange;
        return slider;
    }

    /**
     * Pins an icon to the left of the bar. It lives in the margin, outside the frame, so the
     * slider shrinks to make room for it instead of painting it over its own track.
     */
    public Slider icon(Icon icon) {
        this.icon = icon;
        this.margin(new Spacing(0, 0, 0, ICON + GAP));
        return this;
    }

    public Slider onChange(DoubleConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    /** Fires once the interaction ends: the release of a drag that moved the value, or a typed-editor commit. */
    public Slider onCommit(DoubleConsumer onCommit) {
        this.onCommit = onCommit;
        return this;
    }

    /** Moves the bounds and pulls the current value back inside them. */
    public Slider range(double min, double max) {
        this.min = min;
        this.max = max;
        this.value = this.clamp(this.value);
        return this;
    }

    public Slider min(double min) {
        this.min = min;
        this.value = this.clamp(this.value);
        return this;
    }

    public Slider max(double max) {
        this.max = max;
        this.value = this.clamp(this.value);
        return this;
    }

    public Slider format(ValueFormat format) {
        this.format = format;
        return this;
    }

    /** Whole numbers only when set; a step of 0 leaves the bar continuous. */
    public Slider stepped(boolean stepped) {
        this.step = stepped ? 1 : 0;
        return this;
    }

    public Slider textScale(float textScale) {
        this.textScale = textScale;
        return this;
    }

    public float textScale() {
        return textScale;
    }

    public double value() {
        return value;
    }

    public int intValue() {
        return (int) Math.round(value);
    }

    public Slider value(double value) {
        this.value = this.clamp(value);
        return this;
    }

    private double clamp(double raw) {
        double clamped = Math.clamp(raw, min, max);
        // SNAPPING CAN OVERSHOOT WHEN THE RANGE EDGE IS NOT ON THE STEP GRID, SO CLAMP AGAIN
        return step > 0 ? Math.clamp(Math.round(clamped / step) * step, min, max) : clamped;
    }

    private double percent() {
        double span = max - min;
        return span <= 0 ? 0 : Math.clamp((value - min) / span, 0d, 1d);
    }

    @Override
    protected int prefContentWidth(int available) {
        return 40;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return theme().slider().height();
    }

    // THE TRACK OUTRANKS THE ROLE SURFACE, SO A THEME CAN DRESS THE BAR WITHOUT TOUCHING FIELDS
    @Override
    protected Drawable faceDisplay() {
        Drawable track = theme().slider().track();
        return track != null ? track : super.faceDisplay();
    }

    // RIGHT CLICK SWAPS THE BAR FOR A FIELD; DRAGGING WORKS IN WHOLE PIXELS
    private InputNumber editor;

    private void edit() {
        this.editor = new InputNumber("slider", value, typed -> {
            this.value(typed);
            if (onChange != null) onChange.accept(this.value);
            if (onCommit != null) onCommit.accept(this.value);
            this.editor = null;
        });
        this.editor.theme(this.theme());
        this.editor.layout(bounds.x, bounds.y, bounds.width, bounds.height);
        this.editor.focusChanged(true);
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (editor != null) {
            editor.render(graphics, mouseX, mouseY, partial);
            return;
        }
        // DRAWN IN THE MARGIN THE ICON RESERVED, WHICH SITS TO THE LEFT OF THIS VIEW'S BOX
        if (icon != null) {
            icon.drawSquared(graphics, bounds.x - ICON - GAP, bounds.y, ICON, bounds.height, shadow());
        }

        SliderTheme style = theme().slider();
        int knobX = contentX() + (int) Math.round((contentWidth() - style.knobWidth()) * this.percent());
        style.knob(hovering && enabled).draw(graphics, knobX, contentY(), style.knobWidth(), contentHeight());
        this.drawCenteredScaled(graphics, format != null ? format.format(value, max) : String.valueOf(intValue()), textColor(), textScale);
    }

    @Override
    protected void arrange() {
        if (editor != null) editor.layout(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public boolean focusable() {
        return true;
    }

    @Override
    public void focusChanged(boolean focused) {
        if (!focused && editor != null) {
            editor.commit();
            this.editor = null;
        }
    }

    @Override
    public boolean keyDown(int key, int scan, int modifiers) {
        if (editor == null) return false;
        // ESCAPE DROPS THE EDITOR WITHOUT COMMITTING, SO A MISTYPED VALUE CAN BE ABANDONED
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.editor = null;
            return true;
        }
        return editor.keyDown(key, scan, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return editor != null && editor.charTyped(character, modifiers);
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        if (!enabled) return false;
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (editor == null) this.edit();
            return true;
        }
        // LEFT DRAGS (§12.3): ANY OTHER BUTTON WOULD MOVE THE VALUE AND OWN THE POINTER
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (editor != null) {
            editor.mouseDown(mouseX, mouseY, button);
            return true;
        }
        this.dragging = true;
        this.pressValue = this.value;
        this.seek(mouseX);
        return true;
    }

    @Override
    public void mouseMove(double mouseX, double mouseY) {
        if (dragging) this.seek(mouseX);
    }

    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        // COMMIT ONLY WHEN THE DRAG ACTUALLY MOVED THE VALUE, SO A BARE CLICK ON THE KNOB SAVES NOTHING
        if (dragging && value != pressValue && onCommit != null) onCommit.accept(value);
        this.dragging = false;
    }

    private void seek(double mouseX) {
        int knob = theme().slider().knobWidth();
        int span = contentWidth() - knob;
        if (span <= 0) return;
        double ratio = Math.clamp((mouseX - contentX() - knob / 2d) / span, 0d, 1d);
        double next = this.clamp(min + ratio * (max - min));
        if (next == value) return;
        this.value = next;
        if (onChange != null) onChange.accept(next);
    }
}
