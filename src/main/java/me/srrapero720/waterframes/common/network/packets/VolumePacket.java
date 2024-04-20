package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class VolumePacket extends DisplayControlPacket {
    public int volume;

    public VolumePacket() { super(); }
    public VolumePacket(BlockPos pos, int volume, boolean bounce) {
        super(pos, bounce);
        this.volume = volume;

    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.volume = WFConfig.maxVol(this.volume);
    }

}
