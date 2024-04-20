package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class ActivePacket extends DisplayControlPacket {
    public boolean active;
    public ActivePacket() {}
    public ActivePacket(BlockPos pos, boolean active, boolean bounce) {
        super(pos, bounce);
        this.active = active;
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.active = active;
    }

}
