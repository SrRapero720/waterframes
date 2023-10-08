package me.srrapero720.waterframes.common.block.entity;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.data.FrameData;
import me.srrapero720.waterframes.common.block.FrameBlock;
import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class FrameTile extends DisplayTile<FrameData> {
    public FrameTile(BlockPos pos, BlockState state) {
        super(new FrameData(), FrameRegistry.TILE_FRAME.get(), pos, state);
    }

    public static DirectionProperty getDefaultDirectionalProperty() {
        return BlockStateProperties.FACING;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() { return getBox().getBB(getBlockPos()); }

    @Override
    public AlignedBox getBox() {
        return getBox(FrameBlock.FACING, FrameBlock.THICKNESS, false);
    }

    @Override
    public AlignedBox getGifBox() {
        Direction direction = getBlockState().getValue(FrameBlock.FACING);
        Facing facing = Facing.get(direction);
        AlignedBox box = DisplayBlock.box(direction, FrameBlock.THICKNESS);

        Axis one = facing.one();
        Axis two = facing.two();

        box.setMin(one, 0.1f);
        box.setMax(one, 0.9f);

        box.setMin(two, 0.1f);
        box.setMax(two, 0.9f);

        return box;
    }
}