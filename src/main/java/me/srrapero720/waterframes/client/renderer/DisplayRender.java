package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.client.renderer.engine.RenderVertex;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

public class DisplayRender {
    static final ImageRenderer LOADING_TEX = ImageAPI.loadingGif(WaterFrames.ID);

    public static void render(PoseStack pose, DisplayTile<?> block, Facing facing, AlignedBox renderBox,
                              boolean renderFrontSide, boolean renderBackside, boolean forceXFlip, boolean forceYFlip) {

        TextureDisplay display = block.requestDisplay();
        if (display == null || display.isBroken()) return;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);

        BoxFace boxFace = BoxFace.get(facing);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder builder = t.getBuilder();

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);
        if (!display.isLoading() && display.canRender()) {
            int texture = display.texture();
            if (texture != -1) {
                RenderSystem.bindTexture(texture);
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                builder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR_NORMAL);
                if (renderFrontSide)
                    RenderVertex.front(renderBox, boxFace, pose, builder, forceXFlip != block.data.flipX, forceYFlip != block.data.flipY);

                if (renderBackside)
                    RenderVertex.back(renderBox, boxFace, pose, builder, forceXFlip != block.data.flipX, forceYFlip != block.data.flipY);
                t.end();
            }
            if (display.isBuffering()) {
                renderLoading(pose, block, facing, renderBox, boxFace, builder, renderFrontSide, renderBackside, forceXFlip, forceYFlip);
            }
        } else {
            renderLoading(pose, block, facing, renderBox, boxFace, builder, renderFrontSide, renderBackside, forceXFlip, forceYFlip);
        }
        pose.popPose();
    }

    private static void renderLoading(PoseStack pose, DisplayTile<?> block, Facing facing,
                                      AlignedBox renderBox, BoxFace boxFace, BufferBuilder builder,
                                      boolean renderFrontSide, boolean renderBackside, boolean forceXFlip, boolean forceYFlip) {

        int loadingTexture = LOADING_TEX.texture(DisplayControl.getTickTime(), MathAPI.tickToMs(FrameTools.deltaFrames()), true);
        RenderSystem.bindTexture(loadingTexture);
        RenderSystem.setShaderTexture(0, loadingTexture);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        AlignedBox expanded = new AlignedBox(renderBox);
        expanded.grow(facing.axis, facing.positive ? 0.01f : 1 - 0.01f);
        builder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR_NORMAL);
        AlignedBox squared = RenderBox.squaredOf(block, facing, expanded);
        if (renderFrontSide)
            RenderVertex.frontHalf(squared, boxFace, pose, builder, forceXFlip != block.data.flipX, forceYFlip != block.data.flipY);

        if (renderBackside)
            RenderVertex.backHalf(squared, boxFace, pose, builder, forceXFlip != block.data.flipX, forceYFlip != block.data.flipY);

        Tesselator.getInstance().end();
    }
}