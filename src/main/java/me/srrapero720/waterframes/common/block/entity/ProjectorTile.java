package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.client.rendering.core.RenderBox;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;

public class ProjectorTile extends DisplayTile {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), WFRegistry.TILE_PROJECTOR.get(), pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return RenderBox.simple(this, Facing.get(this.getBlockState().getValue(getDisplayBlock().getFacing())), 0).getBB(getBlockPos());
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