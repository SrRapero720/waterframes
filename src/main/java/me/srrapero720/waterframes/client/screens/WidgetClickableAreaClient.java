package me.srrapero720.waterframes.client.screens;

import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.screens.widgets.WidgetClickableArea;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.gui.control.simple.GuiClientIcon;

public class WidgetClickableAreaClient extends GuiClientIcon<WidgetClickableArea> {

    public WidgetClickableAreaClient(WidgetClickableArea control) {
        super(control);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // mouseX/mouseY are already relative to this control (parent subtracts rect position)
        double w = rect.getWidth();
        double h = rect.getHeight();

        PositionHorizontal newX;
        if (mouseX < w / 3) newX = PositionHorizontal.LEFT;
        else if (mouseX < 2 * w / 3) newX = PositionHorizontal.CENTER;
        else newX = PositionHorizontal.RIGHT;

        PositionVertical newY;
        if (mouseY < h / 3) newY = PositionVertical.TOP;
        else if (mouseY < 2 * h / 3) newY = PositionVertical.CENTER;
        else newY = PositionVertical.BOTTOM;

        control.setPosition(newX, newY);
        playSound(SoundEvents.UI_BUTTON_CLICK);
        return true;
    }
}
