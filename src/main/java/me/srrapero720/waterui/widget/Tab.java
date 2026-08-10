package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Icon;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * One slot of a tab strip. The selected tab takes the panel colour so it reads as part of
 * the body underneath, which is what separates it from the ones still to be picked.
 */
public class Tab extends Button {
    public boolean selected;

    public Tab() {
        // BARELY ANY PADDING: THE ICON IS THE WHOLE TAB, AND THE STRIP IS ONLY 18px TALL
        this.border(0).padding(Theme.SPACE_XS);
    }

    public Tab(Icon icon, Component title, IntConsumer onClick) {
        this();
        this.icon(icon).title(title).onClick(onClick);
    }

    @Override
    public Tab icon(Icon icon) {
        super.icon(icon);
        return this;
    }

    /** The strip shows icons only, so the title lives in the tooltip. */
    public Tab title(Component title) {
        this.tooltip(List.of(title));
        return this;
    }

    @Override
    public Tab onClick(IntConsumer onClick) {
        super.onClick(onClick);
        return this;
    }

    public Tab selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    // NO FRAME: THE STRIP IS A ROW OF FLAT SURFACES MEETING EACH OTHER AND THE PANEL
    @Override
    protected Drawable borderDisplay() {
        return null;
    }

    @Override
    protected Drawable faceDisplay() {
        if (selected) return theme().panel().face();
        return theme().field(hovering && enabled);
    }

    @Override
    public boolean mouseDown(double mouseX, double mouseY, int button) {
        // LEFT ONLY (§12.3); A SELECTED TAB STILL CONSUMES SO THE PRESS CANNOT FALL THROUGH THE STRIP
        if (!enabled || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
        if (selected) return true;
        return super.mouseDown(mouseX, mouseY, button);
    }
}
