package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BigTvTile extends DisplayTile {
    public BigTvTile(BlockPos pos, BlockState state) {
        super(new DisplayData().setProjectionDistance(0), DisplayCaps.BIG_TV, DisplaysRegistry.TILE_BIG_TV.get(), pos, state);
    }
}
