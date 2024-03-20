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
    private final GuiButton saveButton;

    @Deprecated
    public WidgetURLTextField(Supplier<GuiButton> saveButton, String text) {
        super(DisplayData.URL);
        this.setMaxStringLength(2048);
        this.setSuggest("https://i.imgur.com/1yCDs5C.mp4");
        this.setText(text);
        this.saveButton = saveButton.get();
    }

    public WidgetURLTextField(DisplayTile tile, GuiButton saveButton) {
        super(DisplayData.URL);
        this.setMaxStringLength(2048);
        this.setSuggest("https://i.imgur.com/1yCDs5C.mp4");
        this.setText(tile.getUrl());
        this.saveButton = saveButton;
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.WIDGET_BORDER;
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return ScreenStyles.WIDGET_BACKGROUND;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        saveButton.setEnabled(DisplayConfig.canSave(getPlayer(), getText()));
        return pressed;
    }

    public WidgetURLTextField setSuggest(String suggest) {
        setSuggestion(suggest);
        return this;
    }

    public WidgetURLTextField expandX() {
        this.setExpandableX();
        return this;
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