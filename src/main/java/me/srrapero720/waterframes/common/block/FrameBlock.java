package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.FrameTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FrameBlock extends DisplayBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final float THICKNESS = 0.0625F / 2F;

    public FrameBlock() {
        this.registerDefaultState(defaultBlockState().setValue(VISIBLE, false));
    }

    @Override
    public DirectionProperty getFacing() {
        return FACING;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var facing = Facing.get(state.getValue(getFacing()));
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, THICKNESS);
        else box.setMin(facing.axis, 1 - THICKNESS);
        return box.voxelShape();
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter level, BlockPos pos) {
        return this.getShape(state, level, pos, null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE));
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(getFacing(), context.getClickedFace())
                .setValue(VISIBLE, true);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FrameTile(pos, state);
    }

    @Override
    public GuiLayer create(CompoundTag nbt, Level level, BlockPos pos, BlockState state, Player player) {
        super.create(nbt, level, pos, state, player);
        return (level.getBlockEntity(pos) instanceof FrameTile frame) ? new DisplayScreen(frame) : null;
    }
}