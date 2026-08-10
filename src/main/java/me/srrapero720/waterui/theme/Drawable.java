package me.srrapero720.waterui.theme;

import net.minecraft.client.gui.GuiGraphics;

/** Anything that can paint itself into a rectangle: flat colors, borders or atlas icons. */
@FunctionalInterface
public interface Drawable {
    void draw(GuiGraphics graphics, int x, int y, int width, int height);
}
