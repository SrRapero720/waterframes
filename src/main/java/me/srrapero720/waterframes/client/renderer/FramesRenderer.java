package me.srrapero720.waterframes.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.common.tiles.BEFrame;
import me.srrapero720.waterframes.common.blocks.Frame;
import me.srrapero720.waterframes.api.displays.Display;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
public class FramesRenderer implements BlockEntityRenderer<BEFrame> {
    @Override
    public boolean shouldRenderOffScreen(BEFrame frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }
    
    @Override
    public boolean shouldRender(BEFrame frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.renderDistance);
    }
    
    @Override
    public void render(BEFrame frame, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (frame.isURLEmpty() || frame.alpha == 0) {
            if (frame.display != null) frame.display.release();
            return;
        }
        
        Display display = frame.requestDisplay();
        if (display == null) return;
        
        display.prepare(frame.getURL(), frame.volume * Minecraft.getInstance().options
                .getSoundSourceVolume(SoundSource.MASTER), frame.minDistance, frame.maxDistance, frame.playing, frame.loop, frame.tick);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(frame.brightness, frame.brightness, frame.brightness, frame.alpha);
        int texture = display.getTexID();
        
        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        Facing facing = Facing.get(frame.getBlockState().getValue(Frame.FACING));
        AlignedBox box = frame.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);
        
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-frame.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        // MY CODE
//        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL); (I USE THIS)
//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR); (RIGHT TEXT COLOR)

        // OLD LF
//        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        // NEW LF
//        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
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
}
