package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.theme.Face;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Clickable control showing a label or an icon. Icon buttons keep the 20x20 intrinsic size
 * the previous GUI used, text buttons size themselves to their label.
 */
public class Button extends Element {
    private static final int ICON_SIZE = 20;
    // ICONS SCALE DOWN CLEANLY, SO AN ICON BUTTON CAN BE SQUEEZED WELL BELOW ITS NATURAL SIZE
    private static final int ICON_FLOOR = 4;

    // FUSED INTO A COMBOBUTTON: BORDER AND BASE FACE ARE SKIPPED, HOVER SHADE RETAINED
    boolean fused;

    protected Component text;
    protected Icon icon;
    protected IntConsumer onClick;
    // LIVE FACE: READ AT DRAW WHEN SET, SO THE BUTTON NEVER WAITS A TICK TO CATCH UP
    private Supplier<Icon> source;

    // HOLD-TO-REPEAT (§11): interval <= 0 DISABLES IT; WHILE HELD, RE-FIRES onClick EVERY interval TICKS AFTER delay
    private int repeatDelay;
    private int repeatInterval;
    private boolean held;
    private int heldTicks;

    public Button() {
        this(null);
    }

    public Button(IntConsumer onClick) {
        this.face = Face.CLICKABLE;
        this.onClick = onClick;
    }

    public Button onClick(IntConsumer onClick) {
        this.onClick = onClick;
        return this;
    }

    /** Hold-to-repeat: after {@code delay} ticks held, re-fires the click every {@code interval} ticks (§11). */
    public Button repeat(int delay, int interval) {
        this.repeatDelay = delay;
        this.repeatInterval = interval;
        return this;
    }

    public static Button of(Component text, IntConsumer onClick) {
        return new Button(onClick).text(text);
    }

    public static Button of(Icon icon, IntConsumer onClick) {
        return new Button(onClick).icon(icon);
    }

    public Button text(Component text) {
        this.text = text;
        return this;
    }

    public Button icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    /** Live icon source; while set it outranks the pinned {@link #icon(Icon)}. */
    public Button source(Supplier<Icon> source) {
        this.source = source;
        return this;
    }

    public boolean fused() {
        return fused;
    }

    public Icon icon() {
        return source != null ? source.get() : icon;
    }

    // ICON BUTTONS KEEP A SQUARE FRAME: THE WIDE TEXT PADDING WOULD BLOAT EVERY ICON GRID
    @Override
    public Spacing padding() {
        if (this.icon() != null && !paddingSet()) return Spacing.all(style().padding().top());
        return super.padding();
    }

    @Override
    protected int prefContentWidth(int available) {
        if (this.icon() != null) return ICON_SIZE;
        return text == null ? 0 : font().width(text);
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        if (this.icon() != null) return ICON_SIZE;
        return text == null ? 0 : font().lineHeight;
    }

    @Override
    protected int minContentWidth(int available) {
        return this.icon() != null ? ICON_FLOOR : prefContentWidth(available);
    }

    @Override
    protected int minContentHeight(int width, int available) {
        return this.icon() != null ? ICON_FLOOR : prefContentHeight(width, available);
    }

    @Override
    protected Drawable borderDisplay() {
        return fused ? null : super.borderDisplay();
    }

    @Override
    protected Drawable faceDisplay() {
        if (fused) return enabled && hovering ? style().face(true) : null;
        return super.faceDisplay();
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        Icon current = this.icon();
        if (current != null) {
            current.drawSquared(graphics, contentX(), contentY(), contentWidth(), contentHeight(), shadow());
            return;
        }
        if (text != null) this.drawCentered(graphics, text.getString(), textColor());
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3): ANOTHER BUTTON NEITHER CLICKS NOR ARMS, AND FALLS THROUGH TO ANCESTORS
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        playClick();
        this.click(button);
        // ARM HOLD-TO-REPEAT; THIS INITIAL FIRE STANDS, tick() ADDS THE RE-FIRES
        if (repeatInterval > 0) { this.held = true; this.heldTicks = 0; }
        return true;
    }

    // THE USUAL STOP, ROUTED BY POINTER CAPTURE EVEN OUTSIDE; tick()'S PHYSICAL CHECK COVERS A LOST RELEASE
    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        this.held = false;
        this.heldTicks = 0;
    }

    @Override
    public void tick() {
        if (repeatInterval <= 0 || !held) return;
        // A BUTTON DISABLED MID-HOLD STOPS AND STAYS STOPPED UNTIL THE NEXT PRESS
        if (!enabled) { this.held = false; this.heldTicks = 0; return; }
        // THE PHYSICAL BUTTON IS THE AUTHORITY (H1): A RELEASE THAT NEVER REACHED THIS BUTTON
        // (TOUCHSCREEN MODE, FOCUS LOST, ROW RE-ADOPTED) WOULD OTHERWISE LATCH THE HOLD FOREVER
        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
            this.held = false;
            this.heldTicks = 0;
            return;
        }
        this.heldTicks++;
        if (heldTicks >= repeatDelay && (heldTicks - repeatDelay) % repeatInterval == 0) this.click(0);
    }

    protected void click(int button) {
        if (onClick != null) onClick.accept(button);
    }
}
