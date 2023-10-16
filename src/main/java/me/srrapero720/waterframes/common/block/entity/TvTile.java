package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.TvBlock;
import me.srrapero720.waterframes.common.data.TvData;
import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class TvTile extends DisplayTile<TvData> {
    public TvTile(BlockPos pPos, BlockState pBlockState) {
//        super(new TvData(), FrameRegistry.TILE_TV.get(), pPos, pBlockState);
        super(new TvData(), null, pPos, pBlockState);
    }

    @Override
    public AlignedBox getRenderBox() {
        return TvBlock.box(getBlockState().getValue(TvBlock.FACING), getBlockState().getValue(TvBlock.ATTACHED_FACE), true);
    }

    @Override
    public AlignedBox getRenderGifBox() {
        return null;
    }
}