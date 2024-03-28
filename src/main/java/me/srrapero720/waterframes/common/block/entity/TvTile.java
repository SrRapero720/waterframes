package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TvTile extends DisplayTile {
    public TvTile(BlockPos pPos, BlockState pBlockState) {
        super(new DisplayData(), WFRegistry.TILE_TV.get(), pPos, pBlockState);
    }

    @Override
    public boolean canHideModel() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return false;
    }

    @Override
    public boolean canProject() {
        return false;
    }

    @Override
    public boolean canResize() {
        return false;
    }
}