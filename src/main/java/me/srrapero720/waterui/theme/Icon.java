package me.srrapero720.waterui.theme;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/**
 * A sub-region of a texture atlas. Carries its source size so elements can use it as their
 * intrinsic size when they are set to wrap their content.
 */
public record Icon(ResourceLocation atlas, int u, int v, int width, int height, int atlasSize) implements Drawable {

    public Icon(ResourceLocation atlas, int u, int v, int width, int height) {
        this(atlas, u, v, width, height, 256);
    }

    @Override
    public void draw(GuiGraphics graphics, int x, int y, int drawWidth, int drawHeight) {
        graphics.blit(atlas, x, y, drawWidth, drawHeight, u, v, width, height, atlasSize, atlasSize);
    }

    /**
     * Draws the icon on a raw quad so position and size stay in float. Needed wherever a box
     * is split into a fraction that does not divide evenly, because rounding each cell to an
     * int makes the pieces drift away from the grid they belong to.
     */
    public void drawExact(GuiGraphics graphics, float x, float y, float width, float height) {
        float u1 = u / (float) atlasSize;
        float v1 = v / (float) atlasSize;
        float u2 = (u + this.width) / (float) atlasSize;
        float v2 = (v + this.height) / (float) atlasSize;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, atlas);

        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(matrix, x, y + height, 0).setUv(u1, v2);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(u2, v2);
        buffer.addVertex(matrix, x + width, y, 0).setUv(u2, v1);
        buffer.addVertex(matrix, x, y, 0).setUv(u1, v1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.disableBlend();
    }

    /**
     * Same as {@link #drawSquared} but repeating the icon one pixel down and right in black
     * first, producing the drop shadow controls paint under their icons.
     */
    public void drawSquared(GuiGraphics graphics, int x, int y, int boxWidth, int boxHeight, boolean shadow) {
        if (shadow) {
            RenderSystem.setShaderColor(0f, 0f, 0f, 0.25f);
            this.drawSquared(graphics, x + 1, y + 1, boxWidth, boxHeight);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        this.drawSquared(graphics, x, y, boxWidth, boxHeight);
    }

    /**
     * Draws the icon centred inside the box without deforming it: the largest whole size that
     * keeps the source aspect ratio and still fits.
     */
    public void drawSquared(GuiGraphics graphics, int x, int y, int boxWidth, int boxHeight) {
        if (width <= 0 || height <= 0 || boxWidth <= 0 || boxHeight <= 0) return;
        float scale = Math.min(boxWidth / (float) width, boxHeight / (float) height);
        int drawWidth = Math.max(1, Math.round(width * scale));
        int drawHeight = Math.max(1, Math.round(height * scale));
        this.draw(graphics, x + (boxWidth - drawWidth) / 2, y + (boxHeight - drawHeight) / 2, drawWidth, drawHeight);
    }
}
