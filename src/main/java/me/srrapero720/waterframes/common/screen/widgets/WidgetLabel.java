package me.srrapero720.waterframes.common.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.screen.text.ScalableCompiledText;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        super(name, (int) (width * scale), (int) (height * scale));
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
        return this.setTitle((Component)(new TranslatableComponent(translate)));
    }

    public WidgetLabel setTitle(Component component) {
        this.text.setText(component);
        if (this.hasGui()) {
            this.reflow();
        }

        return this;
    }

    public WidgetLabel expandX() {
        this.setExpandableX();
        return this;
    }

    public WidgetLabel expandY() {
        this.setExpandableY();
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

    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack matrix, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        this.text.setScale(scale);
        this.text.render(matrix);
    }

    public void flowX(int width, int preferred) {
        this.text.setDimension(width, Integer.MAX_VALUE);
    }

    public void flowY(int height, int preferred) {
        this.text.setMaxHeight(height);
    }

    public int getMinWidth() {
        return (int) (10 * scale);
    }

    public int preferredWidth() {
        return (int) (this.text.getTotalWidth() * scale) + 8;
    }

    public int getMinHeight() {
        return (int) (9 * scale);
    }

    public int preferredHeight() {
        return (int) (this.text.getTotalHeight() * scale) + 2;
    }
}