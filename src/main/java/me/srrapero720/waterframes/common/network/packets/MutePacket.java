package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class MutePacket extends DisplayControlPacket {
    public boolean muted;

    public MutePacket() {}
    public MutePacket(BlockPos pos, boolean muted, boolean bounce) {
        super(pos, bounce);
        this.muted = muted;
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {
        if (tile.display != null) tile.display.setMuteMode(this.muted);
    }

    @Override
    public void exec(DisplayTile tile) {
        tile.data.muted = this.muted;
    }

}
