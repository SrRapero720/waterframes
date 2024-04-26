package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;

public class TvRenderer extends DisplayRenderer {
    @Override
    public float grwSize() {
        return 0.001F;
    }

    @Override
    public boolean invBoxFace(DisplayTile tile) {
        return tile.getBlockState().getValue(DisplayBlock.ATTACHED_FACE) != tile.getDirection();
    }
}
