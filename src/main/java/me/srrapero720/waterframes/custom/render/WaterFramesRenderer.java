package me.srrapero720.waterframes.custom.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.custom.blocks.BlockEntityWaterFrame;
import me.srrapero720.waterframes.custom.blocks.WaterPictureFrame;
import me.srrapero720.waterframes.custom.displayers.DisplayerApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class WaterFramesRenderer implements BlockEntityRenderer<BlockEntityWaterFrame> {
    final BlockEntityRendererProvider.Context context;

    public WaterFramesRenderer(BlockEntityRendererProvider.Context c) {
        this.context = c;
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityWaterFrame frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }
    
    @Override
    public boolean shouldRender(BlockEntityWaterFrame frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.renderDistance);
    }
    
    @Override
    public void render(BlockEntityWaterFrame frame, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (frame.isURLEmpty() || frame.alpha == 0) {
            if (frame.display != null) frame.display.release();
            return;
        }
        
        DisplayerApi display = frame.requestDisplay();
        if (display == null) return;
        
        display.prepare(frame.getURL(), frame.volume * Minecraft.getInstance().options
                .getSoundSourceVolume(SoundSource.MASTER), frame.minDistance, frame.maxDistance, frame.playing, frame.loop, frame.tick);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
        int texture = display.texture();
        
        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        Facing facing = Facing.get(frame.getBlockState().getValue(WaterPictureFrame.FACING));
        AlignedBox box = frame.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);
        
        pose.pushPose();
        
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-frame.rotation)));
        pose.translate(-0.5, -0.5, -0.5);
        
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();
        
        if (frame.bothSides) {
            builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            
            for (int i = face.corners.length - 1; i >= 0; i--) {
                BoxCorner corner = face.corners[i];
                builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                        .uv(corner.isFacing(face.getTexU()) != frame.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.flipY ? 1 : 0).color(-1)
                        .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
            tesselator.end();
        }
        
        pose.popPose();
    }

    private void add(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v, int combinedLight) {
        renderer.vertex(stack.last().pose(), x, y, z)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(combinedLight)
                .normal(1.0F, 0.0F, 0.0F)
                .endVertex();
    }

//    class Dummy {
//        public void render(OPFEntity frame, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
//            frame.tickTexture();
//            Direction direction = (Direction)frame.getBlockState().getValue(OPFBlock.FACING);
//            matrixStack.pushPose();
//            matrixStack.translate(0.5, 0.0, 0.5);
//            applyDirection(matrixStack, direction);
//            if (frame.onFloor) {
//                matrixStack.translate(-0.5, -0.49, 0.5);
//                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
//            } else if ((Boolean)frame.getBlockState().getValue(OPFBlock.ATTACHED)) {
//                matrixStack.translate(-0.5, 0.0, -0.93);
//            } else {
//                matrixStack.translate(-0.5, 0.0, 0.01);
//            }
//
//            VertexConsumer builder = null;
//            if (frame.shouldLoadTexture()) {
//                frame.loadTexture();
//            }
//
//            builder = buffer.getBuffer(RenderType.entityTranslucent(frame.textureLocation));
//            this.add(builder, matrixStack, 0.0F, 0.0F, 0.5F, frame.flippedX ? 0.0F : 1.0F, !frame.flippedY ? 1.0F : 0.0F, combinedLightIn);
//            this.add(builder, matrixStack, (float)frame.sizeX, 0.0F, 0.5F, frame.flippedX ? 1.0F : 0.0F, !frame.flippedY ? 1.0F : 0.0F, combinedLightIn);
//            this.add(builder, matrixStack, (float)frame.sizeX, (float)frame.sizeY, 0.5F, frame.flippedX ? 1.0F : 0.0F, !frame.flippedY ? 0.0F : 1.0F, combinedLightIn);
//            this.add(builder, matrixStack, 0.0F, (float)frame.sizeY, 0.5F, frame.flippedX ? 0.0F : 1.0F, !frame.flippedY ? 0.0F : 1.0F, combinedLightIn);
//            if (!frame.textureWorker.loaded) {
//                Font fontrenderer = this.context.getFont();
//                matrixStack.translate(0.10000000149011612, (double)(0.5F * (float)(Math.max(frame.sizeY, 2) / 2)), 0.550000011920929);
//                matrixStack.scale(0.01F * (float)frame.sizeX, 0.01F * (float)frame.sizeY, 0.002F);
//                matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
//                if (!frame.textureWorker.error) {
//                    fontrenderer.drawInBatch("Still Loading...", 0.0F, 0.0F, DyeColor.LIME.getTextColor(), false, matrixStack.last().pose(), buffer, false, 0, combinedLightIn);
//                } else {
//                    fontrenderer.drawInBatch("Error", 0.0F, 0.0F, DyeColor.RED.getTextColor(), false, matrixStack.last().pose(), buffer, false, 0, combinedLightIn);
//                }
//            }
//
//            matrixStack.popPose();
//        }
//
//        private void add(VertexConsumer renderer, PoseStack stack, float x, float y, float z, float u, float v, int combinedLight) {
//            renderer.vertex(stack.last().pose(), x, y, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(combinedLight).normal(1.0F, 0.0F, 0.0F).endVertex();
//        }
//    }
}
