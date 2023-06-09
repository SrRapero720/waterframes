package me.srrapero720.waterframes.client.screen.widgets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.client.screen.widgets.constants.Constants;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.ArrayList;
import java.util.List;

public class WidgetTextField extends GuiTextfield {
    private final GuiButton saveButton;

    public WidgetTextField(GuiButton saveButton, String name, String text) {
        super(name, text);
        this.saveButton = saveButton;
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return Constants.NORMAL_BORDER;
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return Constants.NORMAL_BACKGROUND;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        saveButton.setEnabled(WFConfig.canUse(getPlayer(), getText()));
        return pressed;
    }

    @Override
    public List<Component> getTooltip() {
        var tooltips = new ArrayList<Component>();

        if (!WFConfig.canUse(getPlayer(), getText()))
            tooltips.add(new TranslatableComponent("label.waterframes.not_whitelisted").withStyle(ChatFormatting.RED));
        if (!WaterMediaAPI.isURLValid(getText()))
            tooltips.add(new TranslatableComponent("label.waterframes.invalid_url").withStyle(ChatFormatting.RED));

        return tooltips.isEmpty() ? null : tooltips;
    }
}
