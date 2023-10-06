package me.srrapero720.waterframes.custom.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.api.block.entity.BasicBlockEntity;
import me.srrapero720.waterframes.api.display.IDisplay;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.waterframes.custom.block.FrameBlock;
import me.srrapero720.waterframes.custom.block.entity.FrameTile;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

public abstract class WaterBERenderer<T extends BasicBlockEntity<?>> implements BlockEntityRenderer<T> {
    private static final ImageRenderer LOADING_GIF = ImageAPI.loadingGif("watermedia");

    @Override
    public int getViewDistance() {
        return BlockEntityRenderer.super.getViewDistance();
    }

    @Override
    public boolean shouldRenderOffScreen(T pBlockEntity) {
        return BlockEntityRenderer.super.shouldRenderOffScreen(pBlockEntity);
    }

    @Override
    public boolean shouldRender(T pBlockEntity, Vec3 pCameraPos) {
        return BlockEntityRenderer.super.shouldRender(pBlockEntity, pCameraPos);
    }

    @Override
    public void render(T block, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        IDisplay display = block.requestDisplay();
        if (display == null) return;

        display.preRender(); // TODO: deprecate in WATERMeDIA and WATERFrAMES

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);

        Facing facing = Facing.get(block.getBlockState().hasProperty(ProjectorTile.getDefaultDirectionalProperty())
                ? block.getBlockState().getValue(ProjectorTile.getDefaultDirectionalProperty())
                : block.getBlockState().getValue(FrameTile.getDefaultDirectionalProperty()));
        AlignedBox alignedBox = block.getBox();
        BoxFace boxFace = BoxFace.get(facing);

        if (display.isLoading()) {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderEngine.vextexFillFrontSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR, MathAPI.getColorARGB(255, 0, 0, 0));

            // GIF PROCESS
            int texture = LOADING_GIF.texture(block.data.tick, MathAPI.tickToMs(TimerTool.deltaFrames()), true);

            RenderSystem.bindTexture(texture);
            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            this.renderVertexGif(pose, alignedBox, boxFace);
        } else {
            if (display.canRender()) {
                int texture = display.texture();
                RenderSystem.bindTexture(texture);
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                if (texture != -1) this.waterRender(block, display, pose, facing, alignedBox, boxFace);
            }

            if (display.isBuffering()) {
                int texture = LOADING_GIF.texture(block.data.tick, MathAPI.tickToMs(TimerTool.deltaFrames()), true);

                RenderSystem.bindTexture(texture);
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.renderVertexGif(pose, alignedBox, boxFace);
            }
        }

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }

    private void renderVertexGif(PoseStack pose, AlignedBox alignedBox, BoxFace boxFace) {
        pose.pushPose();
        pose.scale(0.75f, 0.75f, 0.75f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);

        Vec3i normal = boxFace.facing.normal;
        int x = normal.getX();
        int y = normal.getY();
        if (x > y) x = y;
        if (y > x) y = x;


        for (BoxCorner corner : boxFace.corners) {
            builder.vertex(pose.last().pose(), alignedBox.get(corner.x), alignedBox.get(corner.y), alignedBox.get(corner.z))
                    .uv(0, 0).color(-1)
                    .normal(pose.last().normal(), x, y, normal.getZ()).endVertex();
        }
        tesselator.end();

        pose.popPose();
    }

    public abstract void waterRender(T block, IDisplay display, PoseStack pose, Facing facing, AlignedBox alignedBox, BoxFace boxFace);
}