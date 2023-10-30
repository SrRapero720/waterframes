package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.rendering.RenderEngine;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.data.ProjectorData;
import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ProjectorTile extends DisplayTile<ProjectorData> {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new ProjectorData(), FrameRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        Direction direction = this.getBlockState().getValue(ProjectorBlock.FACING);
        return RenderEngine.getBasicRenderBox(this, direction, data.projectionDistance + 0.99f).getBB(getBlockPos());
    }
}