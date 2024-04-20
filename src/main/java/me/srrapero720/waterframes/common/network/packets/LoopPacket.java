package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class LoopPacket extends DisplayControlPacket {
    public boolean loop;

    public LoopPacket(){}
    public LoopPacket(BlockPos pos, boolean loop, boolean bounce) {
        super(pos, bounce);
        this.loop = loop;
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.loop = this.loop;
    }

}
