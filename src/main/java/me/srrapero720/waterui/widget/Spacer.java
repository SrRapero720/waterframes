package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Element;
import net.minecraft.client.gui.GuiGraphics;

/** Empty element used to push siblings apart, either fixed or taking the leftover. */
public class Spacer extends Element {

    public Spacer() {}

    public static Spacer flexible() {
        Spacer spacer = new Spacer();
        spacer.size(FILL);
        return spacer;
    }

    public static Spacer of(int width, int height) {
        Spacer spacer = new Spacer();
        spacer.size(width, height);
        return spacer;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {}
}
