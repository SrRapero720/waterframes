package me.srrapero720.waterframes.custom.cc_gui;

import me.srrapero720.waterframes.FramesConfig;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.text.TextBuilder;

import java.util.Arrays;
import java.util.List;

public class GuiTextField extends GuiTextfield {
    
    private GuiButton saveButton;
    
    public GuiTextField(GuiButton saveButton, String name, String text) {
        super(name, text);
        this.saveButton = saveButton;
    }
    
    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        if (!canUse(true))
            return FramesConfig.ENABLE_WHITELIST.get() ? GuiWarningStyles.DISABLED_BORDER : GuiWarningStyles.WARNING_BORDER;
        return super.getBorder(style, display);
    }
    
    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        if (!canUse(true))
            return FramesConfig.ENABLE_WHITELIST.get() ? GuiWarningStyles.DISABLED_BACKGROUND : GuiWarningStyles.WARNING_BACKGROUND;
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
        if (!canUse(false))
            return new TextBuilder().text(ChatFormatting.RED + "" + ChatFormatting.BOLD).translate("label.waterframes.not_whitelisted.name").build();
        else if (!canUse(true))
            return new TextBuilder().text(ChatFormatting.GOLD + "").translate("label.waterframes.whitelist_warning.name").build();
        return null;
    }
    
    protected boolean canUse(boolean ignoreToggle) {
        return FramesConfig.canUse(getPlayer(), getText(), ignoreToggle);
    }
}
