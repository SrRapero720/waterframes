package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.SwitchTheme;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Boolean toggle drawn as a sliding track beside its label, dressed by the
 * {@link SwitchTheme} of whatever theme is live. Holds its own value while editing, or
 * mirrors live state when built with a supplier, which is read on every draw so it never
 * waits a tick to catch up.
 */
public class Switch extends Element {
    private Component text;
    // LIVE STATE WHEN SET; THE LOCAL EDIT VALUE OTHERWISE
    private BooleanSupplier source;
    private boolean value;
    private Consumer<Boolean> onChange;

    // GLYPH SCALE FOR THE LABEL TEXT
    private float textScale = 1f;

    // SLIDE STATE: THE LAST PAINTED VALUE AND WHEN IT FLIPPED, DRIVING THE THUMB TRAVEL
    private boolean shown;
    private long slideStart;

    public Switch() {
        this(Component.empty(), false);
    }

    public Switch(Component text, boolean value) {
        this.text = text;
        this.value = value;
        this.shown = value;
        // FULL ROW BY DEFAULT: THE LABEL SITS LEFT AND THE TRACK PINS TO THE CONTAINER EDGE
        this.width(FILL);
    }

    public static Switch translated(String key, boolean value) {
        return new Switch(Component.translatable(key), value);
    }

    /** Mirrors state owned by someone else instead of holding its own. */
    public static Switch live(String key, BooleanSupplier source) {
        return new Switch(Component.translatable(key), source.getAsBoolean()).source(source);
    }

    public Switch text(Component text) {
        this.text = text;
        this.dirty();
        return this;
    }

    /** Alias for the format layer's {@code component} property name. */
    public Switch component(Component text) {
        return this.text(text);
    }

    /** Takes over the value: the supplier is read on every draw instead of the local one. */
    public Switch source(BooleanSupplier source) {
        this.source = source;
        this.value = source != null && source.getAsBoolean();
        this.shown = this.value;
        return this;
    }

    public Switch onChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
        return this;
    }

    /** Alias for the format layer's {@code onToggle} event name. */
    public Switch onToggle(Consumer<Boolean> onToggle) {
        return this.onChange(onToggle);
    }

    public Switch textScale(float textScale) {
        this.textScale = textScale;
        this.dirty();
        return this;
    }

    public float textScale() {
        return textScale;
    }

    public boolean value() {
        return source != null ? source.getAsBoolean() : value;
    }

    public Switch value(boolean value) {
        this.value = value;
        return this;
    }

    @Override
    protected int prefContentWidth(int available) {
        SwitchTheme style = theme().toggle();
        return style.trackWidth() + style.gap() + (text == null ? 0 : Math.round(font().width(text) * textScale));
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return Math.max(theme().toggle().trackHeight(), Math.round(font().lineHeight * textScale));
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        // LABEL FIRST, TRACK PINNED TO THE RIGHT EDGE OF THE BOX
        if (text != null) {
            int scaledGlyph = Math.round(GLYPH_HEIGHT * textScale);
            int y = contentY() + Math.max(0, (contentHeight() - scaledGlyph + 1) / 2);
            if (textScale == 1f) {
                graphics.drawString(font(), text, contentX(), y, textColor(), true);
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(contentX(), y, 0);
                graphics.pose().scale(textScale, textScale, 1f);
                graphics.drawString(font(), text, 0, 0, textColor(), true);
                graphics.pose().popPose();
            }
        }

        boolean value = this.value();
        SwitchTheme style = theme().toggle();
        int border = style.border();
        int x = contentX() + contentWidth() - style.trackWidth();
        int y = contentY() + Math.max(0, (contentHeight() - style.trackHeight()) / 2);

        if (style.outline() != null) style.outline().draw(graphics, x, y, style.trackWidth(), style.trackHeight());
        Drawable face = style.face(value);
        if (face != null) {
            face.draw(graphics, x + border, y + border, style.trackWidth() - border * 2, style.trackHeight() - border * 2);
        }

        // A FLIP REVERSES THE TRAVEL FROM WHERE THE THUMB STANDS: THE EASING IS SYMMETRIC, SO
        // KEEPING THE REMAINING TIME AS THE ELAPSED ONE LANDS ON THE SAME SPOT WITHOUT A JUMP
        if (value != shown) {
            this.shown = value;
            long now = Util.getMillis();
            long remaining = style.slideMs() - Math.min(style.slideMs(), now - slideStart);
            this.slideStart = now - remaining;
        }
        float pos = value ? 1f : 0f;
        if (style.slideMs() > 0) {
            float linear = Math.min(1f, (Util.getMillis() - slideStart) / (float) style.slideMs());
            float eased = linear * linear * (3f - 2f * linear);
            pos = value ? eased : 1f - eased;
        }

        Spacing margin = style.thumbMargin();
        int rest = x + margin.left();
        int engaged = x + style.trackWidth() - margin.right() - style.thumbWidth();
        style.thumb(hovering && enabled).draw(graphics, Math.round(rest + (engaged - rest) * pos),
                y + margin.top(), style.thumbWidth(), style.thumbHeight());

        if (!enabled) theme().disabledOverlay().draw(graphics, x, y, style.trackWidth(), style.trackHeight());
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3): A RIGHT PRESS MUST NOT FLIP THE SWITCH OR SHIP A COMMIT
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        playClick();
        boolean next = !this.value();
        this.value = next;
        if (onChange != null) onChange.accept(next);
        return true;
    }
}
