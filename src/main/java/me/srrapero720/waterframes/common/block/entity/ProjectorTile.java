package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.renderer.engine.RenderBox;
import me.srrapero720.waterframes.common.block.ProjectorBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;

public class ProjectorTile extends DisplayTile {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), FrameRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return RenderBox.getBasic(this, Facing.get(this.getBlockState().getValue(ProjectorBlock.FACING)), data.projectionDistance + 0.99f).getBB(getBlockPos());
    }

    @Override
    public boolean canHideModel() {
        return true;
    }

    @Override
    public boolean canRenderBackside() {
        return false;
    }

    @Override
    public boolean canProject() {
        return true;
    }

    @Override
    public boolean canResize() {
        return true;
    }
}