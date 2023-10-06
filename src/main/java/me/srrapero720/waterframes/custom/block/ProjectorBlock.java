package me.srrapero720.waterframes.custom.block;

import me.srrapero720.waterframes.api.block.BasicBlock;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import me.srrapero720.waterframes.custom.screen.ProjectorScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.GuiLayer;

public class ProjectorBlock extends BasicBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public ProjectorBlock() {
        super(Properties.of(Material.METAL).strength(2.5f, 10.0f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public VoxelShape getInteractionShape(@NotNull BlockState state, BlockGetter level, BlockPos pos) {
        return FrameBlock.box(state.getValue(FACING), 1.0f).voxelShape();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, context.getPlayer().isCrouching() ? current.getOpposite() : current);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) { return new ProjectorTile(pPos, pState); }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        return (level.getBlockEntity(blockPos) instanceof ProjectorTile projector) ? new ProjectorScreen(projector) : null;
    }

    @Override
    public DirectionProperty getFacing() {
        return ProjectorTile.getDefaultDirectionalProperty();
    }
}