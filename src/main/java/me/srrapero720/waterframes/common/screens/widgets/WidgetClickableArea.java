package me.srrapero720.waterframes.common.screens.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.joml.Matrix4f;
import team.creative.creativecore.common.gui.control.simple.GuiIcon;
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
    protected void renderContent(GuiGraphics graphics, Rect control, Rect rect, double scale, int mouseX, int mouseY) {
        super.renderContent(graphics, control, rect, scale, mouseX, mouseY);
        this.renderSelector(graphics, control, rect, mouseX, mouseY);
    }

    protected void renderSelector(GuiGraphics graphics, Rect control, Rect rect, int mouseX, int mouseY) {
        PoseStack pose = graphics.pose();
        var icon = IconStyles.POS_ICON;
        float width = ((float) rect.getWidth()) / 3f;
        float height = ((float) rect.getWidth()) / 3f;

        float offsetX = switch (x) {
            case LEFT -> 0;
            case CENTER -> width;
            case RIGHT -> (float) (width * 2d);
        };

        float offsetY = switch (y) {
            case TOP -> 0;
            case CENTER -> height;
            case BOTTOM -> (float) (height * 2d);
        };

        pose.pushPose();
        this.color.glColor();

        float x, x2, y, y2;
        x = offsetX;
        x2= offsetX + width;
        y = offsetY;
        y2= offsetY + height;

        float u, v, u2, v2;
        u = icon.minX() / 256f;
        v = icon.minY() / 256f;
        u2 = (icon.minX() + icon.width()) / 256f;
        v2 = (icon.minY() + icon.height()) / 256f;

        graphics.drawSpecial(b -> {
            ResourceLocation rl = icon.location();
            final VertexConsumer vertexConsumer = b.getBuffer(RenderType.guiTextured(rl));
            float bo = 0;
            Matrix4f matrix = graphics.pose().last().pose();
            vertexConsumer.addVertex(matrix, x, y2, 0).setUv(u, v2).setColor(-1);
            vertexConsumer.addVertex(matrix, x2, y2, 0).setUv(u2, v2).setColor(-1);
            vertexConsumer.addVertex(matrix, x2, y, 0).setUv(u2, v).setColor(-1);
            vertexConsumer.addVertex(matrix, x, y, 0).setUv(u, v).setColor(-1);
        });
        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        playSound(SoundEvents.UI_BUTTON_CLICK);
        this.selected = true;
        this.mouseMoved(mouseX, mouseY);
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        if (selected) {
            int areaX = (int) (mouseX / rect.getWidth() * 3d);
            int areaY = (int) (mouseY / rect.getHeight() * 3d);

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
    public void mouseReleased(double x, double y, int button) {
        this.selected = false;
        super.mouseReleased(x, y, button);
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
