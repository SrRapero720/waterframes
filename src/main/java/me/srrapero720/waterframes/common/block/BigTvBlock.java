package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.entity.BigTvTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import me.srrapero720.waterframes.common.util.geo.Axis;
import me.srrapero720.waterframes.common.util.geo.Facing;
import me.srrapero720.waterframes.common.util.geo.AlignedBox;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
public class BigTvBlock extends DisplayBlock {
    public static final MapCodec<BigTvBlock> CODEC = simpleCodec(BigTvBlock::new);

    public BigTvBlock() {}
    public BigTvBlock(BlockBehaviour.Properties p) {}

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    @Override
    public PermissionNode<Boolean> getPermissionNode() {
        return DisplaysRegistry.PERM_DISPLAYS_INTERACT_TV;
    }

    public static AlignedBox box(Direction direction, Direction attachedFace, boolean renderMode) {
        Facing facing = Facing.of(direction.getOpposite());
        var box = new AlignedBox();

        float renderMargin = renderMode ? 1f : 0;

        // fit
        if (facing.positive) {
            box.max(facing.axis, (4f / 16.0f));
            box.min(facing.axis, (2f / 16.0f));
        } else {
            box.max(facing.axis, 1f - (2f / 16.0f));
            box.min(facing.axis, 1f - (4f / 16.0f));
        }

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        // fit height
        if (direction == attachedFace) {
            box.min(two, (renderMargin) / 16f);
            box.max(two, 2 - (2f / 16f) - (renderMargin / 16f));
        } else {
            box.min(two, (2f + renderMargin) / 16f);
            box.max(two, 2 - (renderMargin / 16f));
        }

        // fit width
        box.min(one, (-14f + renderMargin) / 16f);
        box.max(one, (30f - renderMargin) / 16f);

        return box;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BigTvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).shape();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState st = super.getStateForPlacement(context);
        return st.setValue(this.getFacing(), st.getValue(this.getFacing()).getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigTvTile(pos, state);
    }
}
