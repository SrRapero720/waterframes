package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FrameTile extends DisplayTile {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), WFRegistry.TILE_FRAME.get(), pos, state);
        this.data.projectionDistance = FrameBlock.THICKNESS;
    }

    @Override
    public boolean canHideModel() {
        return true;
    }

    @Override
    public boolean canProject() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return true;
    }

    @Override
    public boolean canResize() {
        return true;
    }
}