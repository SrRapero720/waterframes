package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings({"deprecation", "null"})
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProjectorBlock extends DisplayBlock {
    @Override
    public DirectionProperty getFacing() {
        return BlockStateProperties.HORIZONTAL_FACING;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(getFacing());
        Facing facing = Facing.get(direction);
        var box = new AlignedBox();

        // fit projector model height
        box.maxY = 8f / 16f;

        // fit projector thickness
        float blockThickness = 4f / 16f;
        box.setMin(facing.axis, blockThickness);
        box.setMax(facing.axis, 1 - blockThickness);

        // fit anchor of it
        Facing clockWise = Facing.get(direction.getClockWise());
        box.setMin(clockWise.axis, 1f / 16f);
        box.setMax(clockWise.axis, 15f / 16f);

        return box.voxelShape();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, null);
    }

    @Override
    protected void registerDefaultState(BlockState state) {
        super.registerDefaultState(state.setValue(VISIBLE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(VISIBLE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return super.getStateForPlacement(context)
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ProjectorTile(pPos, pState);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        super.create(compoundTag, level, blockPos, blockState, player);
        return (level.getBlockEntity(blockPos) instanceof ProjectorTile projector) ? new DisplayScreen(projector) : null;
    }
}