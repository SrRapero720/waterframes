package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiIcon;

import java.util.ArrayList;
import java.util.List;

public class WidgetClickableArea extends GuiIcon {
    private PositionHorizontal x;
    private PositionVertical y;

    public WidgetClickableArea(IGuiParent parent, String name, PositionHorizontal x, PositionVertical y) {
        super(parent, name, IconStyles.POS_BASE);
        this.x = x;
        this.y = y;
        this.setTooltip(getPositionTooltip());
    }

    public void setPosition(PositionHorizontal x, PositionVertical y) {
        this.x = x;
        this.y = y;
        this.setTooltip(getPositionTooltip());
    }

    public List<Component> getPositionTooltip() {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(GuiControl.translatable("waterframes.gui.position.desc"));
        tooltips.add(GuiControl.translatable("waterframes.gui.position.vertical", ChatFormatting.AQUA + GuiControl.translate("waterframes.gui.position." + y.name().toLowerCase())));
        tooltips.add(GuiControl.translatable("waterframes.gui.position.horizontal", ChatFormatting.AQUA + GuiControl.translate("waterframes.gui.position." + x.name().toLowerCase())));
        return tooltips;
    }

    public PositionHorizontal getX() {
        return x;
    }

    public PositionVertical getY() {
        return y;
    }
}
