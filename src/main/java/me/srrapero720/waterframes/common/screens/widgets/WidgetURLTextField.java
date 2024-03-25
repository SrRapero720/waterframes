package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.cossporting.Crossponent;
import me.srrapero720.waterframes.util.FrameTools;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WidgetURLTextField extends GuiTextfield {

    public WidgetURLTextField(DisplayTile tile) {
        super(DisplayData.URL);
        this.setMaxStringLength(2048);
        this.setSuggestion("https://i.imgur.com/1yCDs5C.mp4");
        this.setText(tile.data.url);
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.BLUE_BORDER;
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.DARK_BLUE_BACKGROUND;
    }

    @Override
    public List<Component> getTooltip() {
        var tooltips = new ArrayList<Component>();

        if (getText().isEmpty()) {
            tooltips.add(Crossponent.translatable("waterframes.gui.url.tooltip.empty").withStyle(ChatFormatting.BLUE));
        } else if (!DisplayConfig.isWhiteListed(getText()))
            tooltips.add(Crossponent.translatable("waterframes.gui.url.tooltip.not_whitelisted").withStyle(ChatFormatting.RED));
        if (!FrameTools.isUrlValid(getText()))
            tooltips.add(Crossponent.translatable("waterframes.gui.url.tooltip.invalid_url").withStyle(ChatFormatting.RED));

        return tooltips.isEmpty() ? null : tooltips;
    }
}