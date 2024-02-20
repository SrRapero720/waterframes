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
public class FrameBlock extends DisplayBlock {
    public static final VisibleProperty VISIBLE = VisibleProperty.VISIBLE_PROPERTY;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final float THICKNESS = 0.0625F / 2F;

    public FrameBlock() {
        super(Properties.of(Material.METAL).strength(1f).sound(SoundType.METAL).noOcclusion());
        registerDefaultState(defaultBlockState().setValue(VISIBLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(getFacing(), context.getClickedFace()).setValue(VISIBLE, true);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

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
        return BlockStateProperties.FACING;
    }
}