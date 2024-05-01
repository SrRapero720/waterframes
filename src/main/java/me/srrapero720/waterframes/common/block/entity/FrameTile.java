package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FrameTile extends DisplayTile {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new DisplayData().setProjectionDistance(FrameBlock.THICKNESS), DisplayCaps.FRAME, WFRegistry.TILE_FRAME, pos, state);
    }
}