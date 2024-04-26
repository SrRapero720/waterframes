package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.base.Facing;

public class TvRenderer extends DisplayRenderer {
    @Override
    public Facing facing(DisplayTile tile, Direction direction) {
        return Facing.get(direction);
    }

    @Override
    public float grwSize() {
        return 0.001F;
    }

    @Override
    public boolean invBoxFace(DisplayTile tile) {
        return tile.getBlockState().getValue(DisplayBlock.ATTACHED_FACE) != tile.getDirection();
    }
}
