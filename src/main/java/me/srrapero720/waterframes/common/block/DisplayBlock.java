package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.handler.BlockGuiCreator;
import team.creative.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@SuppressWarnings("deprecation")
public abstract class DisplayBlock extends BaseEntityBlock implements BlockGuiCreator, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected DisplayBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    // FRAMES AND TVs
    public static AlignedBox getBlockBox(Direction direction, float thickness) {
        return getBlockBox(Facing.get(direction), thickness);
    }

    // FRAMES AND TVs
    public static AlignedBox getBlockBox(Facing facing, float thickness) {
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, thickness);
        else box.setMin(facing.axis, 1 - thickness);
        return box;
    }

    // FOR PROJECTORS
    public static @NotNull AlignedBox getBlockBox(Direction direction) {
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

        return box;
    }

    public abstract DirectionProperty getFacing();

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getMaxLightLevel();
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 0f;
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && DisplayConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(getFacing()).add(POWERED).add(WATERLOGGED));
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);

        if (DisplayConfig.useRedstone() || !(level.getBlockEntity(pos) instanceof DisplayTile<?> tile)) return;

        state.setValue(POWERED, false);

        for (var direction: Direction.values()) {
            BlockPos neighPos = pos.relative(direction);
            BlockState neighState = level.getBlockState(neighPos);

            if (neighState.getSignal(level, neighPos, direction) != 0) {
                state.setValue(POWERED, true);
                break;
            }
        }

        if (state.getValue(POWERED) && tile.data.playing) tile.pause();
        if (!state.getValue(POWERED) && !tile.data.playing) tile.play();
    }

    @Override
    public @NotNull BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborpos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborpos);
    }

    @Override
    @NotNull
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(pState);
    }

    @Override
    protected void registerDefaultState(BlockState pState) {
        super.registerDefaultState(pState.setValue(WATERLOGGED, false).setValue(POWERED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(WATERLOGGED, false).setValue(POWERED, false);
    }

    @Override @NotNull public RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return this.rotate(state, rotation);
    }

    @Override @NotNull public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(getFacing(), rotation.rotate(state.getValue(getFacing())));
    }

    @Override @NotNull public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(getFacing(), mirror.mirror(state.getValue(getFacing())));
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return DisplayTile::tick;
    }
}