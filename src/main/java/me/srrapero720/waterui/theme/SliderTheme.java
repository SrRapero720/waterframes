package me.srrapero720.waterui.theme;

/**
 * Look of the slider control: the bar height, the knob and the track behind it.
 *
 * @param height    content height of the bar
 * @param knobWidth width of the knob; it always spans the full bar height
 * @param track     surface behind the knob; null keeps the one the nested role paints
 * @param knob      the knob itself
 * @param knobHover knob under the cursor; null keeps the resting one
 */
public record SliderTheme(int height, int knobWidth, Drawable track, Drawable knob, Drawable knobHover) {

    /** Knob for the given cursor state. */
    public Drawable knob(boolean hovered) {
        return hovered && knobHover != null ? knobHover : knob;
    }
}
