package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import me.srrapero720.waterframes.mixin.creativecore.IBoxFaceAccessor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL;

@OnlyIn(Dist.CLIENT)
public class ProjectorRender implements BlockEntityRenderer<ProjectorTile> {
    @Override
    public boolean shouldRenderOffScreen(ProjectorTile frame) {
        return frame.data.getSizeX() > 8 || frame.data.getSizeY() > 8;
    }
    
    @Override
    public boolean shouldRender(ProjectorTile block, @NotNull Vec3 playerPos) {
        Direction direction = block.getBlockState().getValue(ProjectorBlock.FACING);
        BlockPos blockPos = block.getBlockPos();
        BlockPos projectionPos = blockPos.relative(direction, block.data.projectionDistance);
        return Vec3.atCenterOf(projectionPos).closerThan(playerPos, block.data.renderDistance) && Vec3.atCenterOf(blockPos).closerThan(playerPos, block.data.projectionDistance + (double) block.data.renderDistance / 2);
    }

    @Override
    public void render(ProjectorTile block, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        Direction direction = block.getBlockState().getValue(ProjectorBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox alignedBox = RenderEngine.getBasicRenderBox(block, direction, block.data.projectionDistance);
        BoxFace boxFace = BoxFace.get(facing);
        alignedBox.grow(facing.axis, 0.99f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR_NORMAL);

        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);
        if (display.isLoading()) {
            RenderEngine.renderVertexGif(pose, builder, block, facing, (IBoxFaceAccessor) (Enum<BoxFace>) boxFace, true, 0.99f);
        } else {
            if (display.canRender()) {
                int texture = display.texture();
                RenderSystem.bindTexture(texture);
                RenderSystem.setShaderTexture(0, texture);
                if (texture != -1) {
                    RenderEngine.vertexBackSideXFlipped(alignedBox, boxFace, pose, builder, block.data);
                }

                if (display.isBuffering()) {
                    RenderEngine.renderVertexGif(pose, builder, block, facing, (IBoxFaceAccessor) (Enum<BoxFace>) boxFace, true, 0.99f);
                }
            }
        }
        pose.popPose();

        tesselator.end();
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}