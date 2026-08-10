package me.srrapero720.waterui.screen;

import me.srrapero720.waterui.layout.FrameLayout;
import me.srrapero720.waterui.theme.Theme;

/** Layer host of a {@link WaterScreen}: the root, outer elements and Dialog overlays share this rect. */
public class ScreenStack extends FrameLayout {
    final WaterScreen screen;

    ScreenStack(WaterScreen screen) {
        this.screen = screen;
        this.padding(0);
        this.border(0);
    }

    @Override
    protected Theme inheritedTheme() {
        return screen.theme;
    }

    @Override
    protected void queueReflow() {
        screen.reflowQueued();
    }
}
