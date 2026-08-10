package me.srrapero720.waterframes.client.ui.screen.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterframes.client.ui.Icons;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** 3x3 grid picking where the picture anchors inside the display. */
public class AnchorPicker extends Element {
    private PositionHorizontal x;
    private PositionVertical y;
    private Runnable onChange;
    private Runnable onCommit;
    private boolean dragging;

    // CELL THE PRESS STARTED ON, SO THE RELEASE KNOWS WHETHER THE DRAG ACTUALLY MOVED IT
    private PositionHorizontal pressX;
    private PositionVertical pressY;

    public AnchorPicker() {
        this(PositionHorizontal.CENTER, PositionVertical.CENTER);
    }

    public AnchorPicker(PositionHorizontal x, PositionVertical y) {
        this.x = x;
        this.y = y;
    }

    public AnchorPicker positions(PositionHorizontal x, PositionVertical y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /** Initial horizontal cell; the {@code x=} property bakes the tile's current anchor into it. */
    public AnchorPicker x(PositionHorizontal x) {
        this.x = x;
        return this;
    }

    /** Initial vertical cell; the {@code y=} property bakes the tile's current anchor into it. */
    public AnchorPicker y(PositionVertical y) {
        this.y = y;
        return this;
    }

    /** Called every time a click or a drag lands on a different cell. */
    public AnchorPicker onChange(Runnable onChange) {
        this.onChange = onChange;
        return this;
    }

    /** Called once on release, only when the press settled on a different cell than it started. */
    public AnchorPicker onCommit(Runnable onCommit) {
        this.onCommit = onCommit;
        return this;
    }

    public PositionHorizontal x() {
        return x;
    }

    public PositionVertical y() {
        return y;
    }

    // MIRRORS THE ROW HEIGHT ONTO THE WIDTH SO THE GRID STAYS SQUARE AND AS BIG AS THE SLIDERS ALLOW
    @Override
    protected int prefContentWidth(int available) {
        return crossHint > 0 ? crossHint - insetX() : Icons.POS_BASE.width();
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return Icons.POS_BASE.height();
    }

    // THE BASE ICON IS ONLY A STARTING SIZE, THE GRID TAKES WHATEVER THE COLUMN LEAVES
    @Override
    protected int minContentHeight(int width, int available) {
        return 0;
    }

    // CELLS ARE DRAWN ON FLOATS, SO THE SIDE NEED NOT DIVIDE EVENLY BY THREE
    private float side() {
        return Math.min(contentWidth(), contentHeight());
    }

    private float originX() {
        return contentX() + (contentWidth() - side()) / 2f;
    }

    private float originY() {
        return contentY() + (contentHeight() - side()) / 2f;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        float side = side();
        Icons.POS_BASE.drawExact(graphics, originX(), originY(), side, side);

        // THE ENUMS ARE DECLARED LEFT/RIGHT/CENTER, SO THE GRID CELL IS NOT THE ORDINAL
        float cell = side / 3f;
        int column = switch (x) {
            case LEFT -> 0;
            case CENTER -> 1;
            case RIGHT -> 2;
        };
        int row = switch (y) {
            case TOP -> 0;
            case CENTER -> 1;
            case BOTTOM -> 2;
        };
        Icons.POS_ICON.drawExact(graphics, originX() + cell * column, originY() + cell * row, cell, cell);
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3): ANOTHER BUTTON WOULD PICK A CELL AND OWN THE POINTER
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        playSound(SoundEvents.UI_BUTTON_CLICK.value());
        this.dragging = true;
        this.pressX = x;
        this.pressY = y;
        this.pickCell(mouseX, mouseY);
        return true;
    }

    @Override
    public void mouseMove(double mouseX, double mouseY) {
        if (dragging) this.pickCell(mouseX, mouseY);
    }

    @Override
    public void mouseUp(double mouseX, double mouseY, int button) {
        if (!dragging) return;
        this.dragging = false;
        if (onCommit != null && (x != pressX || y != pressY)) onCommit.run();
    }

    private void pickCell(double mouseX, double mouseY) {
        float side = Math.max(1f, side());
        int column = Math.clamp((int) ((mouseX - originX()) * 3 / side), 0, 2);
        int row = Math.clamp((int) ((mouseY - originY()) * 3 / side), 0, 2);
        PositionHorizontal pickedX = switch (column) {
            case 0 -> PositionHorizontal.LEFT;
            case 1 -> PositionHorizontal.CENTER;
            default -> PositionHorizontal.RIGHT;
        };
        PositionVertical pickedY = switch (row) {
            case 0 -> PositionVertical.TOP;
            case 1 -> PositionVertical.CENTER;
            default -> PositionVertical.BOTTOM;
        };
        if (pickedX == x && pickedY == y) return;
        this.x = pickedX;
        this.y = pickedY;
        if (onChange != null) onChange.run();
    }

    @Override
    public List<Component> tooltip() {
        return List.of(
                translatable("waterframes.gui.position.desc"),
                translatable("waterframes.gui.position.vertical",
                        ChatFormatting.AQUA + translate("waterframes.gui.position." + y.name().toLowerCase())),
                translatable("waterframes.gui.position.horizontal",
                        ChatFormatting.AQUA + translate("waterframes.gui.position." + x.name().toLowerCase())));
    }
}
