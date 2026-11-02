package me.srrapero720.waterframes.common.block;

import com.mojang.serialization.MapCodec;
import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.common.block.data.DisplayCaps;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * The one block behind every display. What tells a frame from a projector or a TV is its
 * {@link DisplayCaps}: the block reads its facing property, hitbox, permission and placement
 * rule from there and keeps the interaction, redstone and fluid behaviour in one place.
 */
@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public final class DisplayBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<DisplayBlock> CODEC = DisplayCaps.CODEC.fieldOf("caps").xmap(DisplayBlock::of, block -> block.caps);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty VISIBLE = BooleanProperty.create("frame");
    public static final DirectionProperty ATTACHED_FACE = DirectionProperty.create("attached_face", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);

    // THE STATE DEFINITION IS BUILT INSIDE Block's CONSTRUCTOR, BEFORE ANY FIELD OF THIS CLASS EXISTS, SO
    // THE KIND UNDER CONSTRUCTION IS HANDED OVER THROUGH THIS SLOT; REGISTRATION RUNS ON ONE THREAD
    private static DisplayCaps building;

    public final DisplayCaps caps;

    private DisplayBlock(final DisplayCaps caps) {
        super(properties(caps));
        this.caps = caps;
    }

    /** Creates the block of a kind; the only way to build one. */
    public static DisplayBlock of(final DisplayCaps caps) {
        building = caps;
        try { return new DisplayBlock(caps); }
        finally { building = null; }
    }

    private static Properties properties(final DisplayCaps caps) {
        if (caps.solid()) return Properties.of().strength(1f).sound(SoundType.WOOD);
        return Properties.of()
                .strength(1f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .forceSolidOff()
                .isSuffocating(Blocks::never)
                .isViewBlocking(Blocks::never)
                .requiresCorrectToolForDrops();
    }

    // ==========================================================================
    // BLOCK STATE
    // ==========================================================================

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        final DisplayCaps caps = building;
        builder.add(caps.facing(), ATTACHED_FACE, POWERED, WATERLOGGED);
        if (caps.hideModel()) builder.add(VISIBLE);
    }

    @Override
    public void registerDefaultState(final BlockState state) {
        super.registerDefaultState(building.hideModel()
                ? state.setValue(WATERLOGGED, false).setValue(POWERED, false).setValue(VISIBLE, true)
                : state.setValue(WATERLOGGED, false).setValue(POWERED, false));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        final Player player = context.getPlayer();
        final Direction horizontal = context.getHorizontalDirection();
        // A FRAME LIES ON THE CLICKED FACE; THE REST STAND ON THE FLOOR LOOKING AWAY FROM THE PLAYER, OR AT
        // THEM WHILE SNEAKING, AND THE TV KINDS ARE MODELLED THE OTHER WAY ROUND
        final Direction base = this.caps.facing() == BlockStateProperties.FACING ? context.getClickedFace()
                : player != null && player.isCrouching() ? horizontal.getOpposite() : horizontal;
        final Direction facing = this.caps.flipPlacement() ? base.getOpposite() : base;
        return this.defaultBlockState()
                .setValue(ATTACHED_FACE, context.getClickedFace())
                .setValue(this.caps.facing(), facing);
    }

    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState neighborState, final LevelAccessor level, final BlockPos pos, final BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(this.caps.facing(), rotation.rotate(state.getValue(this.caps.facing())));
    }

    @Override
    public BlockState mirror(final BlockState state, final Mirror mirror) {
        return state.setValue(this.caps.facing(), mirror.mirror(state.getValue(this.caps.facing())));
    }

    @Override
    public FluidState getFluidState(final BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(state);
    }

    // ==========================================================================
    // SHAPE AND RENDERING
    // ==========================================================================

    @Override
    public RenderShape getRenderShape(final BlockState state) {
        return this.caps.hideModel() && !state.getValue(VISIBLE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return this.caps.shape(state);
    }

    @Override
    public VoxelShape getInteractionShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return this.caps.shape(state);
    }

    @Override
    public float getShadeBrightness(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return this.caps.solid() ? 0.2f : 0f;
    }

    @Override
    public boolean isCollisionShapeFullBlock(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return this.caps.solid();
    }

    @Override
    public PushReaction getPistonPushReaction(final BlockState state) {
        return this.caps.solid() ? PushReaction.NORMAL : PushReaction.DESTROY;
    }

    @Override
    public int getLightEmission(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof final DisplayTile tile ? tile.getLightLevel() : 0;
    }

    // ==========================================================================
    // INTERACTION
    // ==========================================================================

    @Override
    public ItemInteractionResult useItemOn(final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit) {
        // A REMOTE BOUND TO THIS VERY DISPLAY TOGGLES ITS PLAYBACK INSTEAD OF OPENING THE SCREEN
        if (stack.getItem() instanceof RemoteControl && RemoteControl.bound(stack, level, pos)) {
            if (level.getBlockEntity(pos) instanceof final DisplayTile tile) {
                if (player instanceof final ServerPlayer server) {
                    if (DisplaysConfig.canControl(server, tile)) tile.setPause(false, !tile.data.paused);
                } else {
                    // A PUFF OF AQUA DUST AROUND THE BLOCK CENTRE, THE REMOTE'S ACKNOWLEDGEMENT
                    final Vec3 center = Vec3.atCenterOf(pos);
                    final DustParticleOptions dust = new DustParticleOptions(new Vector3f(Vec3.fromRGB24(ChatFormatting.AQUA.getColor()).toVector3f()), 1.3f);
                    for (int i = 0; i < 4; i++) {
                        level.addParticle(dust, center.x + (Math.random() * 2 - 1) / 4, center.y, center.z + (Math.random() * 2 - 1) / 4,
                                Math.random() * 2 - 1, Math.random() * 3, Math.random() * 2 - 1);
                    }
                }
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.FAIL;
        }
        this.useWithoutItem(state, level, pos, player, hit);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hit) {
        if (player instanceof final ServerPlayer server && DisplaysConfig.canInteractBlock(player, this)) DisplayNetwork.openScreen(server, pos, false);
        return InteractionResult.SUCCESS;
    }

    // ==========================================================================
    // REDSTONE
    // ==========================================================================

    @Override
    public boolean canConnectRedstone(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction direction) {
        return state.getValue(this.caps.facing()) == direction;
    }

    @Override
    public void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block, final BlockPos neighborPos, final boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        if (!DisplaysConfig.useRedstone() || !(level.getBlockEntity(pos) instanceof final DisplayTile tile)) return;
        final boolean signal = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) == signal) return;

        level.setBlock(pos, state.setValue(POWERED, signal), UPDATE_ALL);
        if (level.isClientSide) return;
        // THE RISING EDGE PAUSES THE DISPLAY AND THE FALLING EDGE UNDOES THAT PAUSE ONLY; A PAUSE THE
        // PLAYER MADE BY HAND SURVIVES THE SIGNAL GOING AWAY, UNLESS REDSTONE RULES IN MASTER MODE
        if (signal) {
            if (tile.data.paused) return;
            tile.data.redstonePaused = true;
            tile.setPause(false, true);
        } else if (tile.data.redstonePaused || DisplaysConfig.useMasterModeRedstone()) {
            tile.data.redstonePaused = false;
            tile.setPause(false, false);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof final DisplayTile tile ? tile.getAnalogOutput() : 0;
    }

    // ==========================================================================
    // BLOCK ENTITY
    // ==========================================================================

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new DisplayTile(DisplaysRegistry.tileType(this.caps), pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> type) {
        return (l, pos, s, entity) -> { if (entity instanceof final DisplayTile tile) tile.tick(); };
    }
}
