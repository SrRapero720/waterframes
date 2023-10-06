package me.srrapero720.waterframes.custom.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.api.display.IDisplay;
import me.srrapero720.waterframes.custom.block.FrameBlock;
import me.srrapero720.waterframes.custom.block.entity.FrameTile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class FrameRender extends WaterBERenderer<FrameTile> {
    @Override
    public boolean shouldRenderOffScreen(FrameTile frame) { return frame.getSizeX() > 12 || frame.getSizeY() > 12; }
    @Override
    public boolean shouldRender(FrameTile frame, @NotNull Vec3 vec) { return Vec3.atCenterOf(frame.getBlockPos()).closerThan(vec, frame.data.renderDistance); }

    @Override
    public void waterRender(FrameTile block, IDisplay display, PoseStack pose, Facing facing, AlignedBox alignedBox, BoxFace boxFace) {
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderEngine.vertexFrontSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, block.data);
        if (block.data.bothSides) RenderEngine.vertexBackSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, block.data);

        pose.popPose();
    }
}