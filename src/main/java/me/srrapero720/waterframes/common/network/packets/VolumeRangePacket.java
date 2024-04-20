package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class VolumeRangePacket extends DisplayControlPacket {
    public int min;
    public int max;

    public VolumeRangePacket() { super(); }
    public VolumeRangePacket(BlockPos pos, int min, int max, boolean bounce) {
        super(pos, bounce);
        this.min = min;
        this.max = max;
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.maxVolumeDistance = WFConfig.maxVolDis(this.max);
        tile.data.minVolumeDistance = Math.min(tile.data.maxVolumeDistance, this.min);
    }

}
