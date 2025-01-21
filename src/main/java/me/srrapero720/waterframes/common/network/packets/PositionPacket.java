package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;

public class PositionPacket extends DisplayControlPacket {

    public int positionHorizontal;
    public int positionVertical;

    public PositionPacket() {}
    public PositionPacket(BlockPos pos, PositionHorizontal positionHorizontal, PositionVertical positionVertical, boolean bounce) {
        super(pos, bounce);
        this.positionHorizontal = positionHorizontal == null ? -1 : positionHorizontal.ordinal();
        this.positionVertical = positionVertical == null ? -1 : positionVertical.ordinal();
    }

    @Override
    public void execServer(DisplayTile tile) {

    }

    @Override
    public void execClient(DisplayTile tile) {

    }

    @Override
    public void exec(DisplayTile tile) {
        if (positionHorizontal > -1 && PositionHorizontal.VALUES.length > positionHorizontal) {
            tile.data.setWidth(PositionHorizontal.VALUES[positionHorizontal], tile.data.getWidth());
        }
        if (positionVertical > -1 && PositionVertical.VALUES.length > positionVertical) {
            tile.data.setHeight(PositionVertical.VALUES[positionVertical], tile.data.getHeight());
        }
    }
}
