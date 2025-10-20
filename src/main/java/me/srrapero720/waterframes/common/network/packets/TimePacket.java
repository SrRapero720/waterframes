package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TimePacket extends DisplayControlPacket {
    public int tick;
    public int tickMax;

    public TimePacket(){}
    public TimePacket(BlockPos pos, int tick, int tickMax, boolean bounce) {
        super(pos, bounce);
        this.tick = tick;
        this.tickMax = tickMax;
    }


    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {
        if (tile.display != null) {
            tile.display.forceSeek();
        }
    }

    @Override
    public void exec(DisplayTile tile) {
        if (!tile.data.hasUri()) {
            tile.data.tickMax = -1;
            tile.data.tick = 0;
        } else {
            tile.data.tick = this.tick;
            if (tile.data.tickMax == -1) {
                tile.data.tick = 0;
            } else if (tile.data.tickMax > 0 && tile.data.tickMax != this.tickMax) { // SEND A MISMATCH WHEN TIME IS NOT ZERO
                LOGGER.warn("Mismatch! Max time in tile ({}) doesn't equals to received max time ({})", tile.data.tickMax, this.tickMax);
            }

            if (this.tickMax > tile.data.tickMax) {
                tile.data.tickMax = this.tickMax;
            }
        }
    }
}
