package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.entity.FrameTile;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class FrameRender implements BlockEntityRenderer<FrameTile> {
    @Override
    public boolean shouldRenderOffScreen(FrameTile frame) { return frame.getSizeX() > 12 || frame.getSizeY() > 12; }
    @Override
    public boolean shouldRender(FrameTile frame, @NotNull Vec3 vec) { return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.data.renderDistance); }

    @Override
    public void render(FrameTile block, float pPartialTick, PoseStack pose, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);

        Facing facing = Facing.get(block.getBlockState().getValue(FrameBlock.FACING));
        AlignedBox alignedBox = block.getBox();
        BoxFace boxFace = BoxFace.get(facing);
        alignedBox.grow(facing.axis, 0.01f);

        if (display.isLoading()) {
            RenderEngine.renderVertexGif(block, pose, boxFace,false);
        } else {
            if (display.canRender()) {
                int texture = display.texture();
                RenderSystem.bindTexture(texture);
                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                if (texture != -1) {
                    pose.pushPose();

                    pose.translate(0.5, 0.5, 0.5);
                    pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
                    pose.translate(-0.5, -0.5, -0.5);

                    RenderEngine.vertexFrontSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, block.data);
                    if (block.data.bothSides) RenderEngine.vertexBackSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, block.data);

                    pose.popPose();
                }
            }

            if (display.isBuffering()) {
                RenderEngine.renderVertexGif(block, pose, boxFace, false);
            }
        }

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}