package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TvTile extends DisplayTile {
    public TvTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), DisplayCaps.TV, WFRegistry.TILE_TV, pos, state);
    }
}