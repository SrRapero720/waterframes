package me.srrapero720.waterui.theme;

import net.minecraft.client.gui.GuiGraphics;

public record Color(int argb) implements Drawable {
    @Override
    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, argb);
    }
}
