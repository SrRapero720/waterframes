package me.srrapero720.waterui.theme;

import me.srrapero720.waterui.core.Spacing;

/**
 * Defaults one kind of element starts from: frame thickness, spacing and surfaces. Elements
 * read these lazily through their inherited {@link Theme}, so overriding a single field on
 * the element itself always wins over what is declared here.
 */
public record ElementTheme(int border, Spacing padding, Spacing margin, boolean shadow,
                           Drawable outline, Drawable face, Drawable hover) {

    /** Frameless and unfilled; the element paints everything itself. */
    public static final ElementTheme NONE = new ElementTheme(0, Spacing.ZERO, Spacing.ZERO, true, null, null, null);

    /** Surface for the given cursor state; roles without a hover shade keep their face. */
    public Drawable face(boolean hovered) {
        return hovered && hover != null ? hover : face;
    }
}
