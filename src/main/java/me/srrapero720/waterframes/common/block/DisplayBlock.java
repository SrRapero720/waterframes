package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.PermLevelPacket;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class DisplayBlock extends BaseEntityBlock implements BlockGuiCreator, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty VISIBLE = new BooleanProperty("frame"){};
    public static final DirectionProperty ATTACHED_FACE = DirectionProperty.create("attached_face", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);

    private static final Material MATERIAL = new Material.Builder(MaterialColor.NONE).noCollider().build();
    private static final Properties PROPERTIES = Properties.of(MATERIAL)
            .strength(1f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .isSuffocating(Blocks::never)
            .isViewBlocking(Blocks::never)
            .requiresCorrectToolForDrops();

    protected DisplayBlock() {
        super(PROPERTIES);
    }

    public abstract DirectionProperty getFacing();

    @Override
    public GuiLayer create(CompoundTag tag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide) {
            DisplayNetwork.sendClient(new PermLevelPacket(level.getServer()), level, blockPos);
        }
        return level.getBlockEntity(blockPos) instanceof DisplayTile tile ? new DisplayScreen(tile) : null;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && WFConfig.canInteractBlock(player)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(this.getFacing()) == direction;
    }

    @Override protected void registerDefaultState(BlockState state) {
        super.registerDefaultState(state
                .setValue(WATERLOGGED, false)
                .setValue(POWERED, false)
                .setValue(POWER, 0)
                .setValue(LIT, false)
        );
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder
                .add(getFacing())
                .add(ATTACHED_FACE)
                .add(POWERED)
                .add(POWER)
                .add(WATERLOGGED)
                .add(LIT)
        );
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        return this.defaultBlockState()
                .setValue(WATERLOGGED, false)
                .setValue(POWERED, false)
                .setValue(POWER, 0)
                .setValue(ATTACHED_FACE, context.getClickedFace())
                .setValue(getFacing(), context.getPlayer().isCrouching() ? current.getOpposite() : current)
                .setValue(LIT, false);
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return state.hasProperty(VISIBLE) ? (state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE) : RenderShape.MODEL;
    }

    @Override public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return this.getShape(state, level, pos, null);
    }

    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return (l, pos, state, be) -> {
            if (be instanceof DisplayTile tile) {
                tile.tick(pos, state);
            }
        };
    }

    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        if (!WFConfig.useRedstone() || !(level.getBlockEntity(pos) instanceof DisplayTile tile)) return;
        boolean signal = level.hasNeighborSignal(pos);

        if (state.getValue(POWERED) != signal) {
            level.setBlock(pos, state.setValue(POWERED, signal), 3);
            if (!level.isClientSide) tile.setPause(false, signal);
        }
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(POWER);
    }

    @Override public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(LIT)) {
            return level.getMaxLightLevel();
            // TODO: this needs be synchronized on data refresh
//            if (level.getBlockEntity(pos) instanceof DisplayTile tile) {
//                return (tile.data.brightness / 255) * level.getMaxLightLevel();
//            }
        } else {
            return 0;
        }
    }

    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0f;
    }

    @Override public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.DESTROY;
    }

    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(state);
    }

    @Override public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
        return this.rotate(state, rotation);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(this.getFacing(), rotation.rotate(state.getValue(this.getFacing())));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(this.getFacing(), mirror.mirror(state.getValue(this.getFacing())));
    }
}