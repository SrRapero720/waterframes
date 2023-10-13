package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.TvTile;
import me.srrapero720.waterframes.common.screen.TvScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class TvBlock extends DisplayBlock {
    public static final DirectionProperty ATTACHED_FACE = DirectionProperty.create("attached_face", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public TvBlock() {
        super(Properties.of(Material.METAL).strength(2.5f, 10.0f).sound(SoundType.METAL).noOcclusion());
    }

    public static @NotNull AlignedBox box(Direction direction, Direction attachedBlockFace, float scale) {
        var facing = Facing.get(direction);
        var box = new AlignedBox();

        if (facing.positive) {
            box.setMax(facing.axis, (4.0f / 16.0f));
            box.setMin(facing.axis, (2.0f / 16.0f));
        } else {
            box.setMax(facing.axis, 1f - (2.0f / 16.0f));
            box.setMin(facing.axis, 1f - (4.0f / 16.0f));
        }

        if (attachedBlockFace == Direction.DOWN) {
            box.setMax(Facing.UP.axis, (10f / 16.0f));
            box.setMin(Facing.UP.axis, -(10f / 16.0f));
        } else {
            box.setMax(Facing.UP.axis, (23.0f / 16.0f));
            box.setMin(Facing.UP.axis, (3.0f / 16.0f));
        }

        var wide = Facing.get(direction.getClockWise());
        if (attachedBlockFace == Direction.DOWN || attachedBlockFace == Direction.UP) {
            box.setMax(wide.axis, (24f / 16f));
            box.setMin(wide.axis, 1 - (24f / 16f));
        } else {
            box.setMax(wide.axis, 2);
            box.setMin(wide.axis, 0);
        }

        box.scale(scale);
        return box;
    }

    public static @NotNull AlignedBox renderBox(Direction direction, Direction attachedBlockFace) {
        var facing = Facing.get(direction);
        var box = new AlignedBox();

        if (facing.positive) {
            box.setMax(facing.axis, (4.0f / 16.0f));
            box.setMin(facing.axis, (2.0f / 16.0f));
        } else {
            box.setMax(facing.axis, 1f - (2.0f / 16.0f));
            box.setMin(facing.axis, 1f - (4.0f / 16.0f));
        }

        if (attachedBlockFace == Direction.DOWN) {
            box.setMax(Facing.UP.axis, (11f / 16.0f));
            box.setMin(Facing.UP.axis, -(11f / 16.0f));
        } else {
            box.setMax(Facing.UP.axis, (22.0f / 16.0f));
            box.setMin(Facing.UP.axis, (4.0f / 16.0f));
        }

        var wide = Facing.get(direction.getClockWise());
        if (attachedBlockFace == Direction.DOWN || attachedBlockFace == Direction.UP) {
            box.setMax(wide.axis, (23f / 16f));
            box.setMin(wide.axis, 1 - (23f / 16f));
        } else {
            box.setMax(wide.axis, (31f / 16f));
            box.setMin(wide.axis, (1f / 16f));
        }
        return box;
    }

    @Override
    public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current)
                .setValue(ATTACHED_FACE, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(ATTACHED_FACE);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), 1.01f).voxelShape();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), 1.01f).voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter level, BlockPos pos) {
        return TvBlock.box(state.getValue(getFacing()), state.getValue(ATTACHED_FACE), 1.01f).voxelShape();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TvTile(pPos, pState);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        return (level.getBlockEntity(blockPos) instanceof TvTile projector) ? new TvScreen(projector) : null;
    }

    @Override
    public DirectionProperty getFacing() {
        return FACING;
    }
}