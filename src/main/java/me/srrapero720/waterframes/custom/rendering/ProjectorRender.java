package me.srrapero720.waterframes.custom.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.api.display.IDisplay;
import me.srrapero720.waterframes.custom.block.ProjectorBlock;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.math.box.BoxFace;

@OnlyIn(Dist.CLIENT)
public class ProjectorRender extends WaterBERenderer<ProjectorTile> {
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
    public void waterRender(ProjectorTile block, IDisplay display, PoseStack pose, Facing facing, AlignedBox alignedBox, BoxFace boxFace) {
        pose.pushPose();

        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(facing.rotation().rotation((float) Math.toRadians(-block.data.rotation)));
        pose.translate(-0.5, -0.5, -0.5);

        RenderEngine.vertexBackSide(alignedBox, boxFace, pose, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, block.data);

        pose.popPose();
    }
}