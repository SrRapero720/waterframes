package me.srrapero720.waterframes.common.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.type.Color;

import java.util.function.Consumer;

public class WidgetCheckButton extends GuiButton {
    public boolean value;
    protected Icon icon = null;
    protected Color color;
    protected Color shadow;
    protected BooleanConsumer changed;
    protected Pair<Icon, Icon> stateIcons;
    protected Pair<Component, Component> stateComponents;

    public WidgetCheckButton(String name, boolean state) {
        this(name, state, null);
    }

    public WidgetCheckButton(String name, boolean state, Consumer<Integer> pressed) {
        super(name, pressed);
        this.color = Color.WHITE;
        this.shadow = Color.BLACK;
        this.value = state;
    }

    public WidgetCheckButton setColor(Color color) {
        this.color = color;
        return this;
    }

    public WidgetCheckButton setShadow(Color shadow) {
        this.shadow = shadow;
        return this;
    }

    public WidgetCheckButton setIconsState(Pair<Icon, Icon> stateIcons) {
        this.stateIcons = stateIcons;
        this.icon = this.value ? this.stateIcons.first() : this.stateIcons.second();
        return this;
    }

    public WidgetCheckButton setComponentsState(Pair<Component, Component> stateComponents) {
        this.stateComponents = stateComponents;
        this.setTitle(value ? this.stateComponents.first() : this.stateComponents.second());
        return this;
    }

    public WidgetCheckButton consumeChanged(BooleanConsumer changed) {
        this.changed = changed;
        return this;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        if (this.value != value) {
            this.value = value;
            this.raiseEvent(new GuiControlChangedEvent(this));
            if (this.changed != null) {
                this.changed.accept(value);
            }

            if (this.stateComponents != null) {
                this.setTitle(value ? this.stateComponents.first() : this.stateComponents.second());
            } else if (stateIcons != null) {
                this.icon = value ? this.stateIcons.first() : this.stateIcons.second();
            }
        }
    }

    @Override
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        this.set(!this.value);
        return super.mouseClicked(rect, x, y, button);
    }

    @Override
    protected int preferredWidth(int availableWidth) {
        return stateComponents != null ? super.preferredWidth(availableWidth) : 12;
    }

    @Override
    protected int preferredHeight(int width, int availableHeight) {
        return stateComponents != null ? super.preferredHeight(width, availableHeight) : 12;
    }

    @Override
    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        if ((stateComponents == null && stateIcons == null) || (stateComponents != null && stateIcons != null)) {
            throw new IllegalStateException("Both state values can't be defined or undefined, only one");
        } else if (stateComponents != null) {
            super.renderContent(pose, control, rect, mouseX, mouseY);
        } else {
            pose.pushPose();
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, icon.location());
            this.shadow.glColor();
            GuiRenderHelper.textureRect(pose, 1, 1, control.getContentWidth(), control.getContentHeight(), (float)this.icon.minX(), (float)this.icon.minY(), (float)(this.icon.minX() + this.icon.width()), (float)(this.icon.minY() + this.icon.height()));
            this.color.glColor();
            GuiRenderHelper.textureRect(pose, 0, 0, (int)rect.getWidth(), (int)rect.getHeight(), (float)this.icon.minX(), (float)this.icon.minY(), (float)(this.icon.minX() + this.icon.width()), (float)(this.icon.minY() + this.icon.height()));
            RenderSystem.disableBlend();
            pose.popPose();
        }
    }

    public ControlFormatting getControlFormatting() {
        return ControlFormatting.CLICKABLE;
    }
}
