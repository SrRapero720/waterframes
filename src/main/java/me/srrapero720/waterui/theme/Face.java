package me.srrapero720.waterui.theme;

/**
 * Visual role of an element. A widget declares its own and the theme decides what that role is
 * worth: frame thickness, inner spacing and the drawables painted behind the content.
 */
public enum Face {
    /** No frame and no fill; the element paints everything itself. */
    NONE,
    /** Progress track, framed but flush with its content. */
    BAR,
    /** Sunken surface of fields, sliders and lists. */
    NESTED,
    /** Raised surface of anything that reacts to the cursor. */
    CLICKABLE,
    /** Outer panel of a screen layer. */
    PANEL
}
