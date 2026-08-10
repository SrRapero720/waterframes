package me.srrapero720.waterui.theme;

/** Colours by role: neutral control tones and WATERFrAMES aquatic accents. */
public final class Palette {

    public static int argb(int r, int g, int b, float alpha) {
        return ((int) (alpha * 255f) & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    private static int gray(float value, float alpha) {
        int channel = Math.round(value * 255f);
        return argb(channel, channel, channel, alpha);
    }

    // NEUTRAL CONTROL COLOURS
    public static final int BORDER = argb(0, 0, 0, 1f);
    public static final int NESTED = gray(0.5f, 1f);
    public static final int BAR = gray(0.1f, 1f);
    public static final int CLICKABLE = gray(0.4f, 1f);
    public static final int CLICKABLE_HOVER = gray(0.5f, 1f);
    public static final int DISABLED_OVERLAY = argb(0, 0, 0, 0.4f);

    public static final int TEXT = argb(255, 255, 255, 1f);
    public static final int TEXT_DISABLED = argb(100, 100, 100, 1f);

    // AQUATIC OVERRIDES
    public static final int SCREEN_BACKGROUND = argb(37, 50, 72, 1f);
    public static final int SCREEN_BORDER = argb(23, 30, 42, 1f);
    public static final int DARK_BLUE_BACKGROUND = argb(27, 36, 52, 1f);
    public static final int DARK_BLUE_HIGHLIGHT = argb(54, 72, 104, 1f);
    public static final int SCREEN_HIGHLIGHT = argb(46, 59, 80, 1f);
    public static final int BLUE_BORDER = argb(72, 220, 219, 1f);
    public static final int BLUE_BACKGROUND = argb(47, 137, 137, 1f);
    public static final int RED_BORDER = argb(255, 60, 60, 1f);
    public static final int RED_BACKGROUND = argb(140, 56, 52, 1f);

    // VANILLA EMULATION: TONES THE GAME ITSELF PAINTS ITS WIDGETS AND LISTS WITH.
    // THE PANEL IS THE SHADE BLUR PLUS VEIL PRODUCE; THE menu_background TEXTURES ARE PURE
    // BLACK AT PARTIAL ALPHA, A VEIL FOR THE BLUR SHADER AND NEVER AN OPAQUE SURFACE
    public static final int MC_PANEL = argb(30, 30, 30, 1f);
    public static final int MC_TEXT_DISABLED = argb(160, 160, 160, 1f);
    public static final int MC_GREEN = argb(98, 175, 68, 1f);
    public static final int MC_RED = argb(150, 40, 40, 1f);
    public static final int MC_RED_BORDER = argb(255, 85, 85, 1f);
    public static final int MC_FIELD = argb(0, 0, 0, 0.45f);
    public static final int MC_FIELD_HOVER = argb(70, 70, 70, 0.6f);
    public static final int MC_SELECTION = argb(255, 255, 255, 0.25f);

    private Palette() {}
}
