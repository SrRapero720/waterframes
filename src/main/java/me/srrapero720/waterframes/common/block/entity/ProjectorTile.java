package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ProjectorTile extends DisplayTile {
    public ProjectorTile(BlockPos pos, BlockState state) {
        super(new DisplayData(), DisplayCaps.PROJECTOR, WFRegistry.TILE_PROJECTOR, pos, state);
    }
}