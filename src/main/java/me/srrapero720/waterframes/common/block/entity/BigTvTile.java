package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.WFRegistry;
import me.srrapero720.waterframes.common.block.BigTvBlock;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class BigTvTile extends DisplayTile {

    public BigTvTile(BlockPos pPos, BlockState pBlockState) {
        super(new DisplayData(), WFRegistry.TILE_BIG_TV.get(), pPos, pBlockState);
    }

    @Override
    public AlignedBox getRenderBox() {
        return BigTvBlock.box(this.getDirection(), true);
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
