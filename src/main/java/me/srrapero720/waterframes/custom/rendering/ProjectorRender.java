package me.srrapero720.waterframes.custom.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import me.srrapero720.waterframes.api.display.IDisplay;
import me.srrapero720.waterframes.custom.block.ProjectorBlock;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class ProjectorRender implements BlockEntityRenderer<ProjectorTile> {
    @Override
    public boolean shouldRenderOffScreen(ProjectorTile frame) {
        return frame.getSizeX() > 8 || frame.getSizeY() > 8;
    }
    
    @Override
    public boolean shouldRender(ProjectorTile block, Vec3 vec) {
//        Direction d = block.getBlockState().getValue(ProjectorBlock.FACING);
//        Vec3.atCenterOf(block.getBlockPos());
        return Vec3.atCenterOf(block.getBlockPos()).closerThan(vec, block.data.renderDistance + block.data.projectionDistance);
    }
    
    @Override
    public void render(ProjectorTile block, float partialTicks, PoseStack pose, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (block.getUrl().isEmpty() || block.data.alpha == 0) {
            if (block.display != null) block.display.release(false);
            return;
        }
        
        IDisplay display = block.requestDisplay();
        if (display == null) return;
        
        display.preRender();


        
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);
        int texture = display.texture();
        
        if (texture == -1) return;
        RenderSystem.bindTexture(texture);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        
        Facing facing = Facing.get(block.getBlockState().getValue(ProjectorBlock.FACING));
        AlignedBox box = block.getBox();
        box.grow(facing.axis, 0.01F);
        BoxFace face = BoxFace.get(facing);
        
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
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
                    .uv(corner.isFacing(face.getTexU()) != block.data.flipX ? 0 : 1, corner.isFacing(face.getTexV()) != block.data.flipY ? 1 : 0).color(-1)
                    .normal(mat3f, normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }
}