package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.item.RemoteControl;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;

import java.util.function.ToIntFunction;

@SuppressWarnings("deprecation")
@MethodsReturnNonnullByDefault
@FieldsAreNonnullByDefault
public abstract class DisplayBlock extends BaseEntityBlock implements BlockGuiCreator, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty VISIBLE = new BooleanProperty("frame"){};
    public static final IntegerProperty LIGHT_LEVEL = BlockStateProperties.LEVEL;
    public static final DirectionProperty ATTACHED_FACE = DirectionProperty.create("attached_face", Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    protected static final Properties PROPERTIES = FabricBlockSettings.create()
            .luminance(value -> value.getValue(LIGHT_LEVEL))
            .strength(1f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .forceSolidOff()
            .isSuffocating(Blocks::never)
            .isViewBlocking(Blocks::never)
            .pushReaction(PushReaction.DESTROY)
            .requiresCorrectToolForDrops();

    protected DisplayBlock() {
        super(PROPERTIES);
    }

    protected DisplayBlock(Properties properties) {
        super(properties);
    }

    public abstract DirectionProperty getFacing();

    @Override
    public GuiLayer create(CompoundTag tag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
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
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof RemoteControl control) {
            boolean matchDim = control.getDimension(stack.getOrCreateTag()).equals(level.dimension().location().toString());
            int[] position = control.getPosition(stack.getOrCreateTag());
            if (position.length == 0) return InteractionResult.FAIL;
            boolean matchPos = new BlockPos(position[0], position[1], position[2]).equals(pos);

            if (matchDim && matchPos && level.getBlockEntity(pos) instanceof DisplayTile tile) {
                if (level.isClientSide) {
                    tile.setPause(true, !tile.data.paused);
                }
                Vec3 vec = Vec3.atCenterOf(pos);
                var opts = new DustParticleOptions(new Vector3f(Vec3.fromRGB24(ChatFormatting.AQUA.getColor()).toVector3f()), 1.3f);

                int i = 0;
                do {
                    level.addParticle(opts, vec.x + randomNegative(Math.random()) / 4, vec.y, vec.z + randomNegative(Math.random()) / 4,
                            randomNegative(Math.random()), Math.random() * 3, randomNegative(Math.random()));
                    i++;
                } while (i < 4);
                return InteractionResult.SUCCESS;
            }
        }

        if (!level.isClientSide && WFConfig.canInteractBlock(player)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(this.getFacing()) == direction;
    }

    private double randomNegative(double v) {
        return Math.random() > 0.5d ? -v : v;
    }

    @Override protected void registerDefaultState(BlockState state) {
        super.registerDefaultState(state
                .setValue(WATERLOGGED, false)
                .setValue(POWERED, false)
                .setValue(LIGHT_LEVEL, 0)
        );
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder
                .add(this.getFacing())
                .add(ATTACHED_FACE)
                .add(POWERED)
                .add(WATERLOGGED)
                .add(LIGHT_LEVEL)
        );
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction current = context.getHorizontalDirection();
        Player player = context.getPlayer();
        return this.defaultBlockState()
                .setValue(ATTACHED_FACE, context.getClickedFace())
                .setValue(this.getFacing(), player != null && player.isCrouching() ? current.getOpposite() : current);
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
                tile.tick(state);
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
        return level.getBlockEntity(pos) instanceof DisplayTile tile ? tile.getAnalogOutput() : 0;
    }

    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0f;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return false;
    }

    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(true) : super.getFluidState(state);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(this.getFacing(), rotation.rotate(state.getValue(this.getFacing())));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(this.getFacing(), mirror.mirror(state.getValue(this.getFacing())));
    }
}