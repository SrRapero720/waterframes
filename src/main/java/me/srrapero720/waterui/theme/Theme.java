package me.srrapero720.waterui.theme;

import me.srrapero720.waterui.core.Spacing;

/**
 * Look of a widget tree: the global colours, one {@link ElementTheme} per visual role and one
 * dedicated theme per control that paints parts a role cannot describe. Every colour the
 * screens paint comes from here, so publishing another theme on a screen reskins panels,
 * chrome bands, fields, selections, warnings and every control alike. An element that sets
 * its own border, padding, margin, shadow or surfaces overrides what its role declares here.
 *
 * @param text            colour of every enabled label
 * @param textDisabled    colour of a label on a disabled control
 * @param accent          face of an engaged control, e.g. the track of a switch turned on
 * @param accentBorder    frame marking the active input: url fields and the selected entry
 * @param disabledOverlay veil painted over a disabled control
 * @param field           sunken surface of chrome bands, entry fields and list rows
 * @param fieldHover      {@link #field} under the cursor
 * @param selection       face of the row whose media is on air
 * @param danger          face of a control with destructive weight, e.g. the power button
 * @param dangerBorder    frame around a {@link #danger} face
 * @param panel           role of the outer panel of a screen layer
 * @param clickable       role of anything that reacts to the cursor
 * @param nested          role of sunken surfaces: fields, sliders and lists
 * @param bar             role of progress tracks
 * @param toggle          theme of the switch control
 * @param slider          theme of the slider control
 * @param progress        theme of the progress and seek bars
 * @param scroll          theme of the viewport scrollbar
 */
public record Theme(int text, int textDisabled,
                    Drawable accent, Drawable accentBorder, Drawable disabledOverlay,
                    Drawable field, Drawable fieldHover, Drawable selection,
                    Drawable danger, Drawable dangerBorder,
                    ElementTheme panel, ElementTheme clickable, ElementTheme nested, ElementTheme bar,
                    SwitchTheme toggle, SliderTheme slider, ProgressTheme progress, ScrollTheme scroll) {

    public static final int SPACE_XS = 2, SPACE_SM = 4, SPACE_MD = 6;

    // LAST-RESORT LOOK OF A MISSING OR BROKEN THEME: LOUD, UGLY, NEVER CRASHES
    public static final Theme ERROR = new Theme(
            0xFFFFFFFF, 0xFF888888,
            new Color(0xFFFF00FF), new Color(0xFFFF00FF), new Color(0xFFFF00FF),
            new Color(0xFFFF00FF), new Color(0xFFFF00FF), new Color(0xFFFF00FF),
            new Color(0xFFFF00FF), new Color(0xFFFF00FF),
            new ElementTheme(1, Spacing.all(2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFFFF00FF), null),
            new ElementTheme(1, Spacing.all(2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFFFF00FF), null),
            new ElementTheme(1, Spacing.all(2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFFFF00FF), null),
            new ElementTheme(1, Spacing.all(2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFFFF00FF), null),
            new SwitchTheme(14, 8, 1, 3, 0, 6, 6, Spacing.all(1),
                    new Color(0xFF000000), new Color(0xFFFF00FF), new Color(0xFFFF00FF),
                    new Color(0xFFFF00FF), new Color(0xFFFFFFFF)),
            new SliderTheme(10, 4, null, new Color(0xFFFF00FF), null),
            new ProgressTheme(14, new Color(0xFFFF00FF)),
            new ScrollTheme(3, new Color(0xFFFF00FF), new Color(0xFFFF00FF)));

    // THE TOOLKIT DEFAULT (§2): NEUTRAL CONTROL COLOURS PLUS THE MOD'S AQUATIC ACCENTS.
    // MIRRORS assets/waterframes/ui/themes/waterui.ui.json, THE SILENT FALLBACK WHEN NO THEME IS DECLARED OR PAIRED
    public static final Theme WATERUI = new Theme(
            0xFFFFFFFF, 0xFF646464,
            new Color(0xFF2F8989), new Color(0xFF48DCDB),
            new Color(0x66000000),
            new Color(0xFF1B2434), new Color(0xFF2E3B50), new Color(0xFF364868),
            new Color(0xFF8C3834), new Color(0xFFFF3C3C),
            new ElementTheme(6, Spacing.all(5), Spacing.ZERO, true,
                    new Color(0xFF171E2A), new Color(0xFF253248), null),
            new ElementTheme(1, Spacing.hv(6, 2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFF666666), new Color(0xFF808080)),
            new ElementTheme(1, Spacing.all(2), Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFF808080), null),
            new ElementTheme(1, Spacing.ZERO, Spacing.ZERO, true,
                    new Color(0xFF000000), new Color(0xFF1A1A1A), null),
            new SwitchTheme(14, 8, 1, 3, 100, 6, 6, Spacing.all(1),
                    new Color(0xFF000000), new Color(0xFF666666), new Color(0xFF2F8989),
                    new Color(0xFF808080), new Color(0xFFFFFFFF)),
            new SliderTheme(10, 4, null, new Color(0xFF666666), null),
            new ProgressTheme(14, new Color(0xFF666666)),
            new ScrollTheme(3, new Color(0xFF1A1A1A), new Color(0xFF808080)));

    /** Alias of {@link #WATERUI}, the fallback of a detached element tree. */
    public static final Theme DEFAULT = WATERUI;

    /** Field surface for the given cursor state. */
    public Drawable field(boolean hovered) {
        return hovered ? fieldHover : field;
    }

    /** Defaults for a role; {@link Face#NONE} elements paint everything themselves. */
    public ElementTheme style(Face face) {
        return switch (face) {
            case PANEL -> panel;
            case CLICKABLE -> clickable;
            case NESTED -> nested;
            case BAR -> bar;
            case NONE -> ElementTheme.NONE;
        };
    }
}
