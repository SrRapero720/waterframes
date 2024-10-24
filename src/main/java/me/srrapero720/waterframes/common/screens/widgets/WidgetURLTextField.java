package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.text.TextBuilder;

import java.util.List;

public class WidgetURLTextField extends GuiTextfield {

    public WidgetURLTextField(DisplayTile tile) {
        super(DisplayData.URL);
        this.setMaxStringLength(2048);
        this.setSuggestion("https://i.imgur.com/1yCDs5C.mp4");
        this.setText(tile.data.uri != null ? tile.data.uri.toString() : "");
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return isUrlValid(this.getText()) ? ScreenStyles.BLUE_BORDER : ScreenStyles.RED_BORDER;
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.DARK_BLUE_BACKGROUND;
    }

    @Override
    public List<Component> getTooltip() {
        var builder = new TextBuilder();

        if (this.getText().isEmpty()) {
            builder.text(ChatFormatting.BLUE + GuiControl.translate("waterframes.gui.url.tooltip.empty"));
        } else if (!isUrlValid(this.getText())) {
            builder.text(ChatFormatting.RED + GuiControl.translate("waterframes.gui.url.tooltip.invalid_url"));
        } else if (!WFConfig.canSave(this.getPlayer(), this.getText())) {
            builder.text(ChatFormatting.RED + GuiControl.translate("waterframes.gui.url.tooltip.not_whitelisted"));
        }

        var result = builder.build();
        return result.isEmpty() ? null : result;
    }

    public static boolean isUrlValid(String url) {
        return WaterFrames.createURI(url) != null;
    }
}