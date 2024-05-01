package me.srrapero720.waterframes.common.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.util.ArrayList;
import java.util.List;

public class WidgetClickableArea extends GuiIcon {
    private PositionHorizontal x;
    private PositionVertical y;
    private boolean selected = false;
    public WidgetClickableArea(String name, PositionHorizontal x, PositionVertical y) {
        super(name, IconStyles.POS_BASE);
        this.x = x;
        this.y = y;
    }

    @Override
    protected void renderContent(GuiGraphics graphics, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        super.renderContent(graphics, control, rect, mouseX, mouseY);
        this.renderSelector(graphics, control, rect, mouseX, mouseY);
    }

    protected void renderSelector(GuiGraphics graphics, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        PoseStack pose = graphics.pose();
        var icon = IconStyles.POS_ICON;
        int width = Math.round(control.getContentWidth() / 3f);
        int height = Math.round(control.getContentHeight() / 3f);

        int offsetX = switch (x) {
            case LEFT -> 0;
            case CENTER -> width;
            case RIGHT -> Math.round(width * 2f) - 1;
        };

        int offsetY = switch (y) {
            case TOP -> 0;
            case CENTER -> height;
            case BOTTOM -> Math.round(height * 2f) - 1;
        };

        pose.pushPose();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon.location());

        this.color.glColor();
        GuiRenderHelper.textureRect(graphics, offsetX, offsetY, width, height, (float) icon.minX(), (float) icon.minY(), (float)(icon.minX() + icon.width()), (float)(icon.minY() + icon.height()));
        RenderSystem.disableBlend();
        pose.popPose();
    }

    @Override
    public boolean mouseClicked(Rect rect, double mouseX, double mouseY, int button) {
        playSound(SoundEvents.UI_BUTTON_CLICK);
        this.selected = true;
        this.mouseMoved(rect, mouseX, mouseY);
        return true;
    }

    @Override
    public void mouseMoved(Rect rect, double mouseX, double mouseY) {
        super.mouseMoved(rect, mouseX, mouseY);
        if (selected) {
            int areaX = (int) ((mouseX / rect.getWidth()) * 3);
            int areaY = (int) ((mouseY / rect.getHeight()) * 3);

            this.x = switch (areaX) {
                case 0 -> PositionHorizontal.LEFT;
                case 1 -> PositionHorizontal.CENTER;
                case 2, 3 -> PositionHorizontal.RIGHT;
                default -> areaX > 3 ? PositionHorizontal.RIGHT : PositionHorizontal.LEFT;
            };

            this.y = switch (areaY) {
                case 0 -> PositionVertical.TOP;
                case 1 -> PositionVertical.CENTER;
                case 2, 3 -> PositionVertical.BOTTOM;
                default -> areaY > 3 ? PositionVertical.BOTTOM : PositionVertical.TOP;
            };

        }
    }

    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        this.selected = false;
        super.mouseReleased(rect, x, y, button);
    }

    @Override
    public List<Component> getTooltip() {
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(translatable("waterframes.gui.position.desc"));
        tooltips.add(translatable("waterframes.gui.position.vertical", ChatFormatting.AQUA + translate("waterframes.gui.position." + y.name().toLowerCase())));
        tooltips.add(translatable("waterframes.gui.position.horizontal", ChatFormatting.AQUA + translate("waterframes.gui.position." + x.name().toLowerCase())));
        return tooltips;
    }

    public PositionHorizontal getX() {
        return x;
    }

    public PositionVertical getY() {
        return y;
    }
}
