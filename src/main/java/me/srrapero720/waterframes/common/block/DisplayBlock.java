package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.PermLevelPacket;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.LogicalSide;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.BlockGuiCreator;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.util.math.base.Axis;
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

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if (!level.isClientSide) {
            DisplayNetwork.sendClient(new PermLevelPacket(level.getServer()), level, blockPos);
        }
        return null;
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

    // FOR PROJECTORS AND FRAMES
    public static AlignedBox getRenderBox(DisplayTile tile, Facing facing, float spacing, boolean squared) {
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, (tile.data.projectionDistance + spacing));
        else box.setMin(facing.axis, 1 - (tile.data.projectionDistance + spacing));

        Axis one = facing.one();
        Axis two = facing.two();

        if (facing.axis != Axis.Z) {
            one = facing.two();
            two = facing.one();
        }

        box.setMin(one, tile.data.min.x);
        box.setMax(one, tile.data.max.x);

        box.setMin(two, tile.data.min.y);
        box.setMax(two, tile.data.max.y);

        if (!squared) return box;

        float width = tile.data.getWidth();
        float height = tile.data.getHeight();

        // FIXME: corner pictures makes squared look closer to the block
        if (width > height) {
            getBox$square(box, one, tile.data.getPosX().ordinal(), height);
        } else {
            getBox$square(box, two, tile.data.getPosY().ordinal(), width);
        }

        return box;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && WFConfig.canInteractBlock(player)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        if (!WFConfig.useRedstone() || !(level.getBlockEntity(pos) instanceof DisplayTile tile)) return;
        boolean signal = level.hasNeighborSignal(pos);

        state.setValue(POWERED, signal);
        if (!level.isClientSide) {
            tile.setPause(false, signal);
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof DisplayTile tile) {
            if (tile.data.tickMax == -1) return 0;
            if (!tile.data.active) return 0;

            return 1 + (Math.round((float) tile.data.tick / tile.data.tickMax) * BlockStateProperties.MAX_LEVEL_15 - 1);
        }

        return 0;
    }

    private static void getBox$square(final AlignedBox box, Axis axis, int mode, float size) {
        switch (mode) {
            case 0 -> {
                box.setMin(axis, 0f);
                box.setMax(axis, size);
            }
            case 1 -> {
                box.setMin(axis, 1 - size);
                box.setMax(axis, 1);
            }
            default -> {
                float middle = size / 2;
                box.setMin(axis, 0.5f - middle);
                box.setMax(axis, 0.5f + middle);
            }
        }
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
    protected void registerDefaultState(BlockState state) {
        super.registerDefaultState(state.setValue(WATERLOGGED, false).setValue(POWERED, false));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(this.getFacing()) == direction;
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
        return state.setValue(this.getFacing(), rotation.rotate(state.getValue(this.getFacing())));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(this.getFacing(), mirror.mirror(state.getValue(this.getFacing())));
    }
}