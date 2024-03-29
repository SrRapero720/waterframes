package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DisplayBlock extends BaseEntityBlock implements BlockGuiCreator, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty VISIBLE = new BooleanProperty("frame"){};

    protected DisplayBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    public abstract DirectionProperty getFacing();

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
    public static AlignedBox getProjectorBlockBox(Direction direction) {
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && DisplayConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        if (DisplayConfig.useRedstone() || !(level.getBlockEntity(pos) instanceof DisplayTile tile)) return;

        state.setValue(POWERED, false);

        for (var direction: Direction.values()) {
            BlockPos neighPos = pos.relative(direction);
            BlockState neighState = level.getBlockState(neighPos);

            if (neighState.getSignal(level, neighPos, direction) != 0) {
                state.setValue(POWERED, true);
                break;
            }
        }

//        if (state.getValue(POWERED) && tile.data.playing) tile.pause();
//        if (!state.getValue(POWERED) && !tile.data.playing) tile.play();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(getFacing()).add(POWERED).add(WATERLOGGED));
    }

    @Override
    protected void registerDefaultState(BlockState pState) {
        super.registerDefaultState(pState.setValue(WATERLOGGED, false).setValue(POWERED, false));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(getFacing()).getOpposite() == direction;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(WATERLOGGED, false).setValue(POWERED, false);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return DisplayTile::tick;
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return state.hasProperty(VISIBLE) ? (state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE) : RenderShape.MODEL;
    }

    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0f;
    }

    @Override public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getMaxLightLevel();
    }

    @Override public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getMaxLightLevel();
    }

    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(state);
    }

    @Override public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return this.rotate(state, rotation);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(getFacing(), rotation.rotate(state.getValue(getFacing())));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(getFacing(), mirror.mirror(state.getValue(getFacing())));
    }
}