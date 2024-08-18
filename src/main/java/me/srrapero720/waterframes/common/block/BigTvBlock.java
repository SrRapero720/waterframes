package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.BigTvTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public class BigTvBlock extends DisplayBlock {
    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    public static AlignedBox box(Direction direction, boolean renderMode) {
        Facing facing = Facing.get(direction.getOpposite());
        var box = new AlignedBox();

        float renderMargin = renderMode ? 1f : 0;

        // fit
        if (facing.positive) {
            box.setMax(facing.axis, (4f / 16.0f));
            box.setMin(facing.axis, (2f / 16.0f));
        } else {
            box.setMax(facing.axis, 1f - (2f / 16.0f));
            box.setMin(facing.axis, 1f - (4f / 16.0f));
        }

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        // fit height
        box.setMin(two, (2f + renderMargin) / 16f);
        box.setMax(two, 2 - (renderMargin / 16f));

        // fit width
        box.setMin(one, (-14f + renderMargin) / 16f);
        box.setMax(one, (30f - renderMargin) / 16f);

        return box;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BigTvBlock.box(state.getValue(getFacing()), false).voxelShape();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        Player player = context.getPlayer();
        return super.getStateForPlacement(context)
                .setValue(this.getFacing(), player != null && player.isCrouching() ? current : current.getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigTvTile(pos, state);
    }
}
