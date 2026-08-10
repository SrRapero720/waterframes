package me.srrapero720.waterui.theme;

import me.srrapero720.waterui.core.Spacing;

/**
 * Look of the switch control: the track geometry, the thumb riding it and its slide. Margins
 * are measured from the outer track box, so a zero margin lets the thumb cover the frame.
 *
 * @param trackWidth  outer width of the track
 * @param trackHeight outer height of the track
 * @param border      inset of the on/off surface, leaving the outline visible around it
 * @param gap         room between the label and the track
 * @param slideMs     thumb travel time between sides; zero snaps it
 * @param thumbWidth  width of the thumb
 * @param thumbHeight height of the thumb
 * @param thumbMargin how far the thumb rests from the track edges
 * @param outline     frame of the track, drawn over the whole track box
 * @param off         surface while off; null leaves the outline showing through
 * @param on          surface while on
 * @param thumb       the thumb itself
 * @param thumbHover  thumb under the cursor; null keeps the resting one
 */
public record SwitchTheme(int trackWidth, int trackHeight, int border, int gap, int slideMs,
                          int thumbWidth, int thumbHeight, Spacing thumbMargin,
                          Drawable outline, Drawable off, Drawable on,
                          Drawable thumb, Drawable thumbHover) {

    /** Track surface for the given state. */
    public Drawable face(boolean on) {
        return on ? this.on : off;
    }

    /** Thumb for the given cursor state. */
    public Drawable thumb(boolean hovered) {
        return hovered && thumbHover != null ? thumbHover : thumb;
    }
}
