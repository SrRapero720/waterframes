package me.srrapero720.waterframes.common.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.screens.ScalableCompiledText;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.util.List;

public class WidgetLabel extends GuiControl {
    protected ScalableCompiledText text = ScalableCompiledText.createAnySize();
    protected final float scale;

    public WidgetLabel(String name, float scale) {
        super(name);
        this.scale = scale;
    }

    public WidgetLabel(String name, int width, int height, float scale) {
        super(name);
        this.setDim((int) (width * scale), (int) (height * scale));
        this.scale = scale;
    }

    public WidgetLabel setDefaultColor(int color) {
        this.text.defaultColor = color;
        return this;
    }

    public WidgetLabel setAlign(Align align) {
        this.text.alignment = align;
        return this;
    }

    public WidgetLabel setTranslate(String translate) {
        return this.setTitle(translatable(translate));
    }

    public WidgetLabel setTitle(Component component) {
        this.text.setText(component);
        if (this.hasGui()) {
            this.reflow();
        }

        return this;
    }

    public WidgetLabel setTitle(List<Component> components) {
        this.text.setText(components);
        if (this.hasGui()) {
            this.reflow();
        }

        return this;
    }

    public void init() {
    }

    public void closed() {
    }

    public void tick() {
    }

    public ControlFormatting getControlFormatting() {
        return ControlFormatting.TRANSPARENT;
    }

    @Override
    public void render(PoseStack pose, GuiChildControl control, Rect controlRect, Rect realRect, double scale, int mouseX, int mouseY) {
        this.text.setScale(this.scale);
        this.text.render(pose);
    }

    @Override
    protected void renderContent(PoseStack poseStack, GuiChildControl guiChildControl, Rect rect, int i, int i1) {

    }

    public void flowX(int width, int preferred) {
        this.text.setDimension(width, Integer.MAX_VALUE);
    }

    @Override
    public void flowY(int height, int prefferred, int width) {
        this.text.setMaxHeight(height);
    }

    @Override
    protected int preferredWidth(int i) {
        return this.text.getTotalWidth();
    }

    @Override
    protected int preferredHeight(int i, int i1) {
        return this.text.getTotalHeight();
    }
}