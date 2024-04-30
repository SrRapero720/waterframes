package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.TvTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"deprecated", "null", "unused"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TvBlock extends DisplayBlock {
    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    public static AlignedBox box(Direction direction, Direction attachedBlockFace, boolean renderMode) {
        Facing facing = Facing.get(direction);
        Facing wide = Facing.get(attachedBlockFace);
        AlignedBox box = new AlignedBox();

        // SETUP PROFUNDITY
        float renderMargin = renderMode ? 1f : 0;
        if (attachedBlockFace.getOpposite() == direction) {
            if (facing.positive) {
                box.setMax(facing.axis, 1f - (4f / 16.0f));
                box.setMin(facing.axis, 1f - (6f / 16.0f));
            } else {
                box.setMax(facing.axis, (6f / 16.0f));
                box.setMin(facing.axis, (4f / 16.0f));
            }
        } else if (attachedBlockFace == direction) {
            if (facing.positive) {
                box.setMax(facing.axis, 1f - (2f / 16.0f));
                box.setMin(facing.axis, 1f - (4f / 16.0f));
            } else {
                box.setMax(facing.axis, (3f / 16.0f));
                box.setMin(facing.axis, (1f / 16.0f));
            }
        } else if (attachedBlockFace == Direction.UP) {
            if (facing.positive) {
                box.setMax(facing.axis, (6.0f / 16.0f));
                box.setMin(facing.axis, (4.0f / 16.0f));
            } else {
                box.setMax(facing.axis, 1f - (4.0f / 16.0f));
                box.setMin(facing.axis, 1f - (6.0f / 16.0f));
            }
        } else {
            if (facing.positive) {
                box.setMax(facing.axis, (-1.0f / 16.0f));
                box.setMin(facing.axis, (-3.0f / 16.0f));
            } else {
                box.setMax(facing.axis, 1f - (-3.0f / 16.0f));
                box.setMin(facing.axis, 1f - (-1.0f / 16.0f));
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
            box.setMax(two, ((12f - renderMargin) / 16f)); // render: 9.5
            box.setMin(two, -((9f - renderMargin) / 16f)); // render: 9.5
        } else if (attachedBlockFace == Direction.UP) {
            box.setMax(two, ((24f - renderMargin) / 16f));
            box.setMin(two, (3f + renderMargin) / 16f);
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.setMax(two, ((19f - renderMargin) / 16f));
            box.setMin(two, (-2f + renderMargin) / 16f);
        } else {
            box.setMax(two, ((19.0f - renderMargin) / 16.0f)); // render: 22.5
            box.setMin(two, ((-2.0f + renderMargin) / 16.0f)); // render: 3.5
        }

        // SETUP WIDE
        if (attachedBlockFace == Direction.DOWN || attachedBlockFace == Direction.UP) {
            box.setMax(one, ((25f - renderMargin) / 16f)); // render: 23
            box.setMin(one, 1 - ((25f - renderMargin) / 16f)); // render: 23
        } else if (attachedBlockFace.getOpposite() == direction || attachedBlockFace == direction) {
            box.setMax(one, ((25f - renderMargin) / 16f));
            box.setMin(one, 1 - ((25f - renderMargin) / 16f));
        } else {
            if (wide.positive) {
                box.setMax(one, (32f - renderMargin) / 16f); // render: 31f / 16f
                box.setMin(one, (-2f + renderMargin) / 16f); // render: 1f / 16f
            } else {
                box.setMax(one, (18f - renderMargin) / 16f); // render: 15f / 16f
                box.setMin(one, -((16f - renderMargin) / 16f)); // render: 15f / 16f
            }
        }

        if (!renderMode) box.scale(1.01f);
        return box;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return super.getStateForPlacement(context)
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), false).voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.getShape(state, level, pos, null);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TvTile(pPos, pState);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        super.create(compoundTag, level, blockPos, blockState, player);
        return (level.getBlockEntity(blockPos) instanceof TvTile tile) ? new DisplayScreen(tile) : null;
    }
}