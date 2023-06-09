package me.srrapero720.waterframes.custom.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.api.IDisplay;
import me.srrapero720.waterframes.custom.blocks.Projector;
import me.srrapero720.waterframes.custom.tiles.TileProjector;
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
public class ProjectorRenderer implements BlockEntityRenderer<TileProjector> {
    @Override
    public boolean shouldRenderOffScreen(TileProjector frame) {
        return frame.getSizeX() > 16 || frame.getSizeY() > 16;
    }
    
    @Override
    public boolean shouldRender(TileProjector frame, Vec3 vec) {
        return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.renderDistance);
    }
    
    @Override
    public void render(TileProjector projector, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (projector.isURLEmpty() || projector.alpha == 0) {
            if (projector.display != null) projector.display.release();
            return;
        }
        
        IDisplay display = projector.requestDisplay();
        if (display == null) return;
        
        display.prepare(projector.getURL(), projector.volume * Minecraft.getInstance().options
                .getSoundSourceVolume(SoundSource.MASTER), projector.minDistance, projector.maxDistance, projector.playing, projector.loop, projector.tick);
        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(projector.brightness, projector.brightness, projector.brightness, projector.alpha);
        int texture = display.getTexID();
        
        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        Facing facing = Facing.get(projector.getBlockState().getValue(Projector.FACING));
        AlignedBox box = projector.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);
        
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-projector.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        Matrix4f mat = pose.last().pose();
        Matrix3f mat3f = pose.last().normal();
        Vec3i normal = face.facing.normal;

        for (int i = face.corners.length - 1; i >= 0; i--) {
            BoxCorner corner = face.corners[i];
            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
                    .uv(corner.isFacing(face.getTexU()) != projector.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != projector.flipY ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();

//        builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
//
//        for (int i = face.corners.length - 1; i >= 0; i--) {
//            BoxCorner corner = face.corners[i];
//            builder.vertex(mat, box.get(corner.x), box.get(corner.y), box.get(corner.z))
//                    .uv(corner.isFacing(face.getTexU()) != projector.flipX ? 1 : 0, corner.isFacing(face.getTexV()) != projector.flipY ? 1 : 0).color(-1)
//                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
//        }
//
//        tesselator.end();

        pose.popPose();
    }
}
