package me.srrapero720.waterframes.client.rendering;

import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.Direction;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class TvRenderer extends DisplayRenderer {
    @Override
    public Facing facing(DisplayTile tile, Direction direction) {
        return Facing.get(direction.getOpposite());
    }

    @Override
    public AlignedBox box(DisplayTile tile, Direction direction, Facing facing) {
        return TvBlock.box(direction, tile.getBlockState().getValue(TvBlock.ATTACHED_FACE), true);
    }

    @Override
    public float grwSize() {
        return 0.001F;
    }
}
