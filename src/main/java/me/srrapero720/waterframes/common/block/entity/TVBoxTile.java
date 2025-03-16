package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TVBoxTile extends DisplayTile {
    public TVBoxTile(BlockPos pos, BlockState state) {
        super(new DisplayData().setProjectionDistance(0), DisplayCaps.TV_BOX, DisplaysRegistry.TILE_TV_BOX.get(), pos, state);
    }
}
