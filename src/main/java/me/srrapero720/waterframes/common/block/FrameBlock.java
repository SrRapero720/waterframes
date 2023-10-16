package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.FrameTile;
import me.srrapero720.waterframes.common.block.properties.VisibleProperty;
import me.srrapero720.waterframes.common.screen.FrameScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.GuiLayer;

@SuppressWarnings("deprecation")
public class FrameBlock extends DisplayBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final VisibleProperty VISIBLE = VisibleProperty.VISIBLE_PROPERTY;
    public static final float THICKNESS = 0.0625F / 2F;

    public FrameBlock() {
        super(Properties.of(Material.WOOD).strength(0.25f, 2.5f).sound(SoundType.WOOD).noOcclusion());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(VISIBLE, WATERLOGGED);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    @NotNull
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(getFacing(), context.getClickedFace()).setValue(VISIBLE, true).setValue(WATERLOGGED, false);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE; }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getBlockBox(state.getValue(getFacing()), THICKNESS).voxelShape();
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter level, BlockPos pos) {
        return getBlockBox(state.getValue(getFacing()), THICKNESS).voxelShape();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FrameTile(pos, state); }

    @Override
    public GuiLayer create(CompoundTag nbt, Level level, BlockPos pos, BlockState state, Player player) {
        return (level.getBlockEntity(pos) instanceof FrameTile frame) ? new FrameScreen(frame) : null;
    }

    @Override
    public DirectionProperty getFacing() {
        return FrameTile.getDefaultDirectionalProperty();
    }
}