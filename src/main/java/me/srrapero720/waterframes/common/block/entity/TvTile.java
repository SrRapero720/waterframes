package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.data.TvData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TvTile extends DisplayTile<TvData> {
    public TvTile(BlockPos pPos, BlockState pBlockState) {
//        super(new TvData(), FrameRegistry.TILE_TV.get(), pPos, pBlockState);
        super(new TvData(), null, pPos, pBlockState);
    }
}