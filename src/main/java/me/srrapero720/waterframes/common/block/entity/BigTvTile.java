package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.WFRegistry;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BigTvTile extends DisplayTile {
    public BigTvTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), DisplayCaps.BIG_TV, WFRegistry.TILE_BIG_TV, pos, state);
    }
}
