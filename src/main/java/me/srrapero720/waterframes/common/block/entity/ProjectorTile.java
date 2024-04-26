package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProjectorTile extends DisplayTile {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), WFRegistry.TILE_PROJECTOR.get(), pos, state);
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