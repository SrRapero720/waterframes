package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Face;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Text field wrapping vanilla's {@link EditBox}. Reusing it keeps IME, clipboard, selection
 * and cursor navigation working; only the frame is ours.
 */
public class InputText extends Element {
    protected final EditBox box;
    private Consumer<String> onSubmit;
    private Consumer<String> onCommit;
    // TEXT SNAPSHOT TAKEN WHEN FOCUS WAS GAINED, SO A COMMIT FIRES ONLY ON A REAL EDIT
    private String focusText;

    public InputText() {
        this("input");
    }

    public InputText(String name) {
        this.face = Face.NESTED;
        this.box = new EditBox(font(), 0, 0, 100, font().lineHeight, Component.literal(name));
        this.box.setBordered(false);
        this.box.setMaxLength(2048);
    }

    /**
     * Drawn by us at the very same spot as the real text and skipped as soon as there is any.
     * Vanilla's own suggestion trails the caret and starts one pixel to the left, which clipped
     * its first glyph against the field edge.
     */
    public InputText suggestion(String suggestion) {
        this.hint = suggestion;
        return this;
    }

    private String hint;

    private int textY() {
        return contentY() + centeredTextY(contentHeight());
    }

    public InputText maxLength(int length) {
        this.box.setMaxLength(length);
        return this;
    }

    public InputText onChange(Consumer<String> responder) {
        this.box.setResponder(responder);
        return this;
    }

    /** Alias for the format layer's {@code onTextChange} event name. */
    public InputText onTextChange(Consumer<String> onTextChange) {
        return this.onChange(onTextChange);
    }

    /** Fires the current text when Enter is pressed while focused, consuming the key. */
    public InputText onSubmit(Consumer<String> onSubmit) {
        this.onSubmit = onSubmit;
        return this;
    }

    /** Fires on blur or Enter, but only when the text changed since focus was gained. */
    public InputText onCommit(Consumer<String> onCommit) {
        this.onCommit = onCommit;
        return this;
    }

    // COMMITS THE CURRENT TEXT WHEN IT DIFFERS FROM THE FOCUS SNAPSHOT, THEN RE-BASELINES IT
    private void commitIfChanged() {
        if (onCommit != null && !box.getValue().equals(focusText)) {
            this.focusText = box.getValue();
            onCommit.accept(focusText);
        }
    }

    public String text() {
        return box.getValue();
    }

    public InputText text(String value) {
        this.box.setValue(value);
        // setValue LEAVES THE CURSOR AT THE END, WHICH SCROLLS THE VIEW AND HIDES THE START
        this.box.moveCursorToStart(false);
        return this;
    }

    /** Sets the initial text; alias of {@link #text(String)} for the format layer. */
    public InputText value(String value) {
        return this.text(value);
    }

    @Override
    protected int prefContentWidth(int available) {
        return 40;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return 10;
    }

    // THE TEXT SCROLLS INSIDE THE BOX, SO THE FIELD CAN BE SQUEEZED WELL BELOW ITS PREFERENCE
    @Override
    protected int minContentWidth(int available) {
        return 20;
    }

    @Override
    protected void arrange() {
        this.box.setX(contentX());
        this.box.setY(this.textY());
        this.box.setSize(contentWidth(), font().lineHeight);
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        // CLIPPED BECAUSE THE TEXT DOES NOT STOP AT THE FIELD EDGE ON ITS OWN
        graphics.enableScissor(contentX(), bounds.y, contentX() + contentWidth(), bounds.bottom());
        if (hint != null && box.getValue().isEmpty()) {
            graphics.drawString(font(), hint, contentX(), textY(), theme().textDisabled(), false);
        }
        this.box.render(graphics, mouseX, mouseY, partial);
        graphics.disableScissor();
    }

    @Override
    public boolean focusable() {
        return true;
    }

    @Override
    public void focusChanged(boolean focused) {
        this.box.setFocused(focused);
        if (focused) this.focusText = box.getValue();
        else this.commitIfChanged();
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3): CONSUMING A BUTTON THIS FIELD IGNORES WOULD OWN THE POINTER AND LOCK LEFT OUT
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        return this.box.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyDown(int key, int scan, int modifiers) {
        // ENTER SUBMITS AND COMMITS THE FIELD IN PLACE; CONSUMED SO THE SCREEN'S OWN ENTER STAYS OUT
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && (onSubmit != null || onCommit != null)) {
            if (onSubmit != null) onSubmit.accept(box.getValue());
            this.commitIfChanged();
            return true;
        }
        return box.keyPressed(key, scan, modifiers);
    }

    @Override
    public boolean charTyped(char character, int modifiers) {
        return box.charTyped(character, modifiers);
    }
}
