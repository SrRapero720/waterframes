package me.srrapero720.waterframes.custom.blocks;

import me.srrapero720.watercore.internal.WaterConsole;
import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.BlockGuiCreator;
import team.creative.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import me.srrapero720.waterframes.custom.cc_gui.GuiWaterFrame;

public class WaterPictureFrame extends BaseEntityBlock implements BlockGuiCreator {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final float frameThickness = 0.031F;
    
    public static @NotNull AlignedBox box(Direction direction) {
        Facing facing = Facing.get(direction);
        AlignedBox box = new AlignedBox();
        if (facing.positive) box.setMax(facing.axis, frameThickness);
        else box.setMin(facing.axis, 1 - frameThickness);
        return box;
    }
    
    public WaterPictureFrame() {

        super(BlockBehaviour.Properties.of(Material.WOOD).explosionResistance(2.5F).destroyTime(2.0F).noOcclusion());
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
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }
    
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
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
        if (!level.isClientSide && WaterFrames.CONFIG.canInteract(player, level))
            GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        boolean hasSignal = false;
        var frame = (BlockEntityWaterFrame) level.getBlockEntity(pos);
        for (var direction : Direction.values()) {
            var neighborBlockPos = pos.relative(direction);
            var neighborBlockState = level.getBlockState(neighborBlockPos);
            if (neighborBlockState.isSignalSource()) {
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BlockEntityWaterFrame::tick;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityWaterFrame(pos, state);
    }
    
    @Override
    public GuiLayer create(CompoundTag nbt, Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityWaterFrame frame)
            return new GuiWaterFrame(frame);
        return null;
    }
    
}
