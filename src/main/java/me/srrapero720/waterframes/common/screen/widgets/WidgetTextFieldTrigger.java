package me.srrapero720.waterframes.common.screen.widgets;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WidgetTextFieldTrigger extends GuiTextfield {
    private final Supplier<GuiButton> saveButton;
    private String suggestion_c = "";

    public WidgetTextFieldTrigger(Supplier<GuiButton> saveButton, String name, String text) {
        super(name);
        this.setMaxStringLength(2048);
        this.setSuggest("https://i.imgur.com/1yCDs5C.mp4");
        this.setText(text);
        this.saveButton = saveButton;
    }

    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        return WidgetStyles.NORMAL_BORDER;
    }

    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return WidgetStyles.NORMAL_BACKGROUND;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        saveButton.get().setEnabled(DisplayConfig.canSave(getPlayer(), getText()));
        return pressed;
    }

    public WidgetTextFieldTrigger setSuggest(String suggest) {
        setSuggestion(suggest);
        return this;
    }

    public WidgetTextFieldTrigger expandX() {
        this.setExpandableX();
        return this;
    }

    public WidgetTextFieldTrigger expandY() {
        this.setExpandableY();
        return this;
    }

    @Override
    public void setSuggestion(@Nullable String p_195612_1_) {
        if (getText().isEmpty()) super.setSuggestion(p_195612_1_);
        suggestion_c = p_195612_1_;
    }

    @Override
    public void focus() {
        super.setSuggestion("");
        super.focus();
    }

    @Override
    public void looseFocus() {
        if (getText().isEmpty()) super.setSuggestion(suggestion_c);
        super.looseFocus();
    }

    public WidgetTextFieldTrigger setWidth(int width) {
        this.preferredWidth = width;
        this.hasPreferredDimensions = true;
        return this;
    }

    public WidgetTextFieldTrigger setHeight(int height) {
        this.preferredHeight = height;
        this.hasPreferredDimensions = true;
        return this;
    }

    @Override
    public List<Component> getTooltip() {
        var tooltips = new ArrayList<Component>();

        if (!DisplayConfig.isWhiteListed(getText()))
            tooltips.add(new TranslatableComponent("label.waterframes.not_whitelisted").withStyle(ChatFormatting.RED));
        if (!FrameTools.isUrlValid(getText()))
            tooltips.add(new TranslatableComponent("label.waterframes.invalid_url").withStyle(ChatFormatting.RED));

        return tooltips.isEmpty() ? null : tooltips;
    }
}