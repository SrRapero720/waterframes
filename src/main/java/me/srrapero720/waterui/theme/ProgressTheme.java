package me.srrapero720.waterui.theme;

/**
 * Look of the progress and seek bars: the track height and the surface of the elapsed part.
 *
 * @param height content height of the bar
 * @param fill   surface of the elapsed part, painted over the bar role face
 */
public record ProgressTheme(int height, Drawable fill) {}
