package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.entity.TvTile;
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

@SuppressWarnings({"deprecated", "null", "unused"})
@MethodsReturnNonnullByDefault
public class TvBlock extends DisplayBlock {
    public static final MapCodec<TvBlock> CODEC = simpleCodec(TvBlock::new);

    public TvBlock() {}
    public TvBlock(BlockBehaviour.Properties p) {}

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

    public static AlignedBox box(Direction direction, Direction attachedBlockFace, boolean renderMode) {
        Facing facing = Facing.of(direction.getOpposite());
        Facing wide = Facing.of(attachedBlockFace);
        AlignedBox box = new AlignedBox();

        // SETUP PROFUNDITY
        float renderMargin = renderMode ? 1f : 0;
        if (attachedBlockFace == direction) {
            if (facing.positive) {
                box.max(facing.axis, 1f - (4f / 16.0f));
                box.min(facing.axis, 1f - (6f / 16.0f));
            } else {
                box.max(facing.axis, (6f / 16.0f));
                box.min(facing.axis, (4f / 16.0f));
            }
        } else if (attachedBlockFace.getOpposite() == direction) {
            if (facing.positive) {
                box.max(facing.axis, 1f - (1f / 16.0f));
                box.min(facing.axis, 1f - (3f / 16.0f));
            } else {
                box.max(facing.axis, (3f / 16.0f));
                box.min(facing.axis, (1f / 16.0f));
            }
        } else if (attachedBlockFace == Direction.UP) {
            if (facing.positive) {
                box.max(facing.axis, (6f / 16.0f));
                box.min(facing.axis, (4f / 16.0f));
            } else {
                box.max(facing.axis, 1f - (4.0f / 16.0f));
                box.min(facing.axis, 1f - (6.0f / 16.0f));
            }
        } else {
            if (facing.positive) {
                box.max(facing.axis, (-1.0f / 16.0f));
                box.min(facing.axis, (-3.0f / 16.0f));
            } else {
                box.max(facing.axis, 1f - (-3.0f / 16.0f));
                box.min(facing.axis, 1f - (-1.0f / 16.0f));
            }
        }

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        // SETUP HEIGHT
        if (attachedBlockFace == Direction.DOWN) {
            box.max(two, ((12f - renderMargin) / 16f)); // render: 9.5
            box.min(two, -((9f - renderMargin) / 16f)); // render: 9.5
        } else if (attachedBlockFace == Direction.UP) {
            box.max(two, ((24f - renderMargin) / 16f));
            box.min(two, (3f + renderMargin) / 16f);
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.max(two, ((19f - renderMargin) / 16f));
            box.min(two, (-2f + renderMargin) / 16f);
        } else {
            box.max(two, ((19.0f - renderMargin) / 16.0f)); // render: 22.5
            box.min(two, ((-2.0f + renderMargin) / 16.0f)); // render: 3.5
        }

        // SETUP WIDE
        if (attachedBlockFace == Direction.DOWN || attachedBlockFace == Direction.UP) {
            box.max(one, ((25f - renderMargin) / 16f)); // render: 23
            box.min(one, 1 - ((25f - renderMargin) / 16f)); // render: 23
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.max(one, ((25f - renderMargin) / 16f));
            box.min(one, 1 - ((25f - renderMargin) / 16f));
        } else {
            if (wide.positive) {
                box.max(one, (32f - renderMargin) / 16f); // render: 31f / 16f
                box.min(one, (-2f + renderMargin) / 16f); // render: 1f / 16f
            } else {
                box.max(one, (18f - renderMargin) / 16f); // render: 15f / 16f
                box.min(one, -((16f - renderMargin) / 16f)); // render: 15f / 16f
            }
        }

        if (!renderMode) box.scale(1.01f);
        return box;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).shape();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState st = super.getStateForPlacement(context);
        return st.setValue(this.getFacing(), st.getValue(this.getFacing()).getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TvTile(pos, state);
    }
}