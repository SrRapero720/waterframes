package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class PreviousPacket extends DisplayControlPacket {
    public PreviousPacket() {}
    public PreviousPacket(BlockPos pos, boolean bounce) {
        super(pos, bounce);
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.prevUri();
    }
}
