package me.srrapero720.waterui.theme;

import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Function;

/**
 * A theme-role reference used as a face or outline override. Instead of pinning a fixed colour it
 * resolves to the live drawable of a named role each time it is painted, so it follows a theme swap.
 * It is never drawn directly: {@code Element.faceDisplay/borderDisplay} resolve it against the
 * element's live theme first.
 */
public final class ThemeRole implements Drawable {
    private final Function<Theme, Drawable> selector;

    public ThemeRole(Function<Theme, Drawable> selector) {
        this.selector = selector;
    }

    /** Live drawable of this role under {@code theme}. */
    public Drawable resolve(Theme theme) {
        return selector.apply(theme);
    }

    // RESOLVED BY THE ELEMENT BEFORE PAINT; A DIRECT DRAW LACKS THE ELEMENT'S THEME, SO IT IS A NO-OP
    @Override
    public void draw(GuiGraphics graphics, int x, int y, int width, int height) {}
}
