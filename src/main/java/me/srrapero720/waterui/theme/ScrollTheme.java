package me.srrapero720.waterui.theme;

/**
 * Look of the scrollbar riding the right edge of a viewport.
 *
 * @param width pixel width of the bar
 * @param track surface of the full run
 * @param thumb grabbable part marking the visible slice
 */
public record ScrollTheme(int width, Drawable track, Drawable thumb) {}
