package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.WFRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class TvTile extends DisplayTile {
    public TvTile(BlockPos pPos, BlockState pBlockState) {
        super(new DisplayData(), WFRegistry.TILE_TV.get(), pPos, pBlockState);
    }

    @Override
    public AlignedBox getRenderBox() {
        return TvBlock.box(this.getDirection(), this.getBlockState().getValue(DisplayBlock.ATTACHED_FACE), true);
    }

    @Override
    public boolean flip3DFace() {
        return getBlockState().getValue(DisplayBlock.ATTACHED_FACE) != getDirection();
    }

    @Override
    public float growSize() {
        return 0.001F;
    }

    @Override
    public boolean canHideModel() {
        return false;
    }

    @Override
    public boolean canRenderBackside() {
        return false;
    }

    @Override
    public boolean canProject() {
        return false;
    }

    @Override
    public boolean canResize() {
        return false;
    }
}