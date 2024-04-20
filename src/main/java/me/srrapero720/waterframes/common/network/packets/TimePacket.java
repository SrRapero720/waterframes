package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TimePacket extends DisplayControlPacket {
    public long tick;
    public long tickMax;

    public TimePacket(){}
    public TimePacket(BlockPos pos, long tick, long tickMax, boolean bounce) {
        super(pos, bounce);
        this.tick = tick;
        this.tickMax = tickMax;
    }


    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        if (tile.data.url.isEmpty()) {
            tile.data.tickMax = -1;
            tile.data.tick = 0;
        } else {
            tile.data.tick = this.tick;
            final boolean maxNegative = tile.data.tickMax == -1;
            if (maxNegative) {
                tile.data.tick = 0;
            }

            if (tile.data.tickMax < this.tickMax) {
                tile.data.tickMax = this.tickMax;
                if (!maxNegative) LOGGER.warn("Received maxTick value major than current one, media differs?.");
            }
        }
    }

}
