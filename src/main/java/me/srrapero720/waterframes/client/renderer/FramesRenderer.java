package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.common.blockentities.BEFrame;
import me.srrapero720.waterframes.common.blocks.FrameBlock;
import me.srrapero720.waterframes.client.displays.IDisplay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class FramesRenderer implements BlockEntityRenderer<BEFrame> {
    @Override
    public boolean shouldRenderOffScreen(BEFrame frame) { return frame.getSizeX() > 16 || frame.getSizeY() > 16; }
    @Override
    public boolean shouldRender(BEFrame frame, @NotNull Vec3 vec) { return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.data.renderDistance); }
    
    @Override
    public void render(BEFrame frame, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (frame.getUrl().isEmpty() || frame.data.alpha == 0) {
            if (frame.display != null) frame.display.release();
            return;
        }
        
        IDisplay display = frame.requestDisplay();
        if (display == null) return;
        
        display.prepare(frame.data);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(frame.data.brightness, frame.data.brightness, frame.data.brightness, frame.data.alpha);
        int texture = display.texture();
        
        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        Facing facing = Facing.get(frame.getBlockState().getValue(FrameBlock.FACING));
        AlignedBox box = frame.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);
        
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-frame.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;
        for (BoxCorner corner : face.corners)
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != frame.data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.data.flipY ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        tesselator.end();
        
        if (frame.data.bothSides) {
            builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
            
            for (int i = face.corners.length - 1; i >= 0; i--) {
                BoxCorner corner = face.corners[i];
                builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                        .uv(corner.isFacing(face.getTexU()) != frame.data.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != frame.data.flipY ? 1 : 0).color(-1)
                        .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
            tesselator.end();
        }
        
        pose.popPose();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}