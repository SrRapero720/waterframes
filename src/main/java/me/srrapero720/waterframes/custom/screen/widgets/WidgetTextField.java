package me.srrapero720.waterframes.custom.screen.widgets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.custom.screen.widgets.constants.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.List;

public class WidgetTextField extends GuiTextfield {
    private final GuiButton saveButton;

    public WidgetTextField(GuiButton saveButton, String name, String text) {
        super(name, text);
        this.saveButton = saveButton;
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        if (!canUse(true)) return Constants.WARN_DISABLED_BORDER;
        return super.getBorder(style, display);
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        if (!canUse(true))
            return WFConfig.ENABLE_WHITELIST.get() ? Constants.WARN_DISABLED_BACKGROUND : Constants.WARN_WARNING_BACKGROUND;
        return super.getBackground(style, display);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        saveButton.setEnabled(canUse(false));
        return pressed;
    }

    @Override
    public List<Component> getTooltip() {
        if (!canUse(false)) return List.of(new TextComponent(ChatFormatting.RED.toString()).append(new TranslatableComponent("label.waterframes.not_whitelisted")));
        else if (!canUse(true)) return List.of(new TextComponent(ChatFormatting.GOLD.toString()).append(new TranslatableComponent("label.waterframes.invalid_url")));
        return null;
    }

    protected boolean canUse(boolean ignoreToggle) {
        return WFConfig.canUse(getPlayer(), getText(), ignoreToggle);
    }
}
