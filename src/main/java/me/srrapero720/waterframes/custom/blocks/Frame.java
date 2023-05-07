package me.srrapero720.waterframes.custom.blocks;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.custom.blocks.property.VisibleProperty;
import me.srrapero720.waterframes.custom.screen.FrameScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import java.util.Random;

public class Frame extends BaseEntityBlock implements BlockGuiCreator {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final VisibleProperty VISIBLE = VisibleProperty.create();
    public static final float frameThickness = 0.0625F / 2F;

    public static @NotNull AlignedBox box(Direction direction) {
        var facing = Facing.get(direction);
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, frameThickness);
        else box.setMin(facing.axis, 1 - frameThickness);
        return box;
    }

    public Frame() {
        super(Properties.of(Material.WOOD).strength(0.25f, 2.5f).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    public BlockState rotate(@NotNull BlockState state, LevelAccessor world, BlockPos pos, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, VISIBLE); }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(VISIBLE, true);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    public VoxelShape getShape(@NotNull BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(state.getValue(FACING)).voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter level, BlockPos pos) {
        return box(state.getValue(FACING)).voxelShape();
    }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && WFConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        boolean hasSignal = false;
        var frame = (TileFrame) level.getBlockEntity(pos);
        for (var direction : Direction.values()) {
            var neighborBlockPos = pos.relative(direction);
            var neighborBlockState = level.getBlockState(neighborBlockPos);
            if (neighborBlockState.isSignalSource() && neighborBlockState.getSignal(level, neighborBlockPos, direction) != 0) {
                hasSignal = true;
                break;
            }
        }

        if (hasSignal) {
            if (frame.playing) frame.pause();
        } else {
            if (!frame.playing) frame.play();
        }

        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        var be = pLevel.getBlockEntity(pCurrentPos);
        var state = pState;
        if (be instanceof TileFrame wf) state = pState.setValue(VISIBLE, wf.visibleFrame);
        return super.updateShape(state, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }



    /* ---------------------------
     *             TICKS
     * --------------------------- */

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return TileFrame::tick;
    }

    /* ---------------------------
     *          GUI BASICS
     * --------------------------- */

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TileFrame(pos, state); }
    
    @Override
    public GuiLayer create(CompoundTag nbt, Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileFrame frame) return new FrameScreen(frame);
        return null;
    }


}
