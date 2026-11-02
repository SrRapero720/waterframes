package me.srrapero720.waterui.core;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Transient layer an element opens over every panel but under the dragged copy, like a dropdown list.
 * The screen holds one at a time, paints it in screen space after the dialogs and routes it the
 * pointer first: a press it does not claim closes it, and escape closes it before the screen.
 */
public interface Popup {
    /** Paints the layer in screen space; the cursor is the real one, the layer owns the hover. */
    void paintPopup(GuiGraphics graphics, int mouseX, int mouseY, float partial);

    /** True when the press landed on the layer and was handled; false closes it (the press is still consumed). */
    boolean pressPopup(double mouseX, double mouseY, int button);

    /** Wheel travel while the layer is open; true consumes it. */
    boolean scrollPopup(double mouseX, double mouseY, double amount);

    /** Called by the screen when the layer leaves it, whatever the reason; must not reopen it. */
    void closedPopup();
}
