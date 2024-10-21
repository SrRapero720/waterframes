package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class PausePacket extends DisplayControlPacket {
    public boolean paused;
    public int tick;

    public PausePacket(){}
    public PausePacket(BlockPos pos, boolean paused, int tick, boolean bounce) {
        super(pos, bounce);
        this.paused = paused;
        this.tick = tick;
    }

    @Override
    public void execServer(DisplayTile tile) {
    }

    @Override
    public void execClient(DisplayTile tile) {
        if (tile.display != null) { // TODO: this is redundant, but i have no time to debug this
            tile.display.setPauseMode(this.paused);
        }
    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.paused = (WFConfig.useMasterModeRedstone() && tile.isPowered()) || this.paused;
        if (this.tick != -1) tile.data.tick = this.tick;
    }

}
