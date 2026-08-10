package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.theme.Face;
import net.minecraft.client.gui.GuiGraphics;

/** Empty cell marker: zero preferred size, empty draw. The {@code continue} of a container. */
public class Blank extends Element {

    public Blank() {
        this.face = Face.NONE;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {}
}
