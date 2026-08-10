package me.srrapero720.waterui.theme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * A sprite of the GUI atlas. Vanilla reads the scaling of each sprite from its own metadata,
 * so nine-slice frames like the button keep their corners at any size.
 */
public record Sprite(ResourceLocation sprite) implements Drawable {

    /** Sprite shipped by the game itself, e.g. {@code widget/button}. */
    public static Sprite vanilla(String path) {
        return new Sprite(ResourceLocation.withDefaultNamespace(path));
    }

    /** Sprite from any namespace, e.g. {@code mymod:widget/button} or a bare path defaulting to minecraft. */
    public static Sprite of(String path) {
        return new Sprite(path.contains(":") ? ResourceLocation.parse(path) : ResourceLocation.withDefaultNamespace(path));
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.blitSprite(sprite, x, y, width, height);
    }
}
