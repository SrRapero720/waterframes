package me.srrapero720.waterframes.common.block;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.handler.BlockGuiCreator;
import team.creative.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

@SuppressWarnings("deprecation")
public abstract class DisplayBlock extends BaseEntityBlock implements BlockGuiCreator {

    protected DisplayBlock(Properties pProperties) { super(pProperties); }

    public static AlignedBox getBlockBox(Direction direction, float thickness) { return getBlockBox(Facing.get(direction), thickness); }
    public static AlignedBox getBlockBox(Facing facing, float thickness) {
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, thickness);
        else box.setMin(facing.axis, 1 - thickness);
        return box;
    }

    public abstract DirectionProperty getFacing();

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return DisplayTile::tick;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof DisplayTile<?> displayTile) {
            return !displayTile.data.url.isEmpty() ? 15 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof DisplayTile<?> displayTile) {
            return !displayTile.data.url.isEmpty() ? 15 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public @NotNull InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && DisplayConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    public BlockState rotate(@NotNull BlockState state, LevelAccessor world, BlockPos pos, @NotNull Rotation rotation) {
        return state.setValue(getFacing(), rotation.rotate(state.getValue(getFacing())));
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        return state.setValue(getFacing(), rotation.rotate(state.getValue(getFacing())));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        return state.setValue(getFacing(), mirror.mirror(state.getValue(getFacing())));
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) { return RenderShape.MODEL; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(getFacing());
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
        if (!DisplayConfig.useRedstone() && level.getBlockEntity(pos) instanceof ProjectorTile tile) {
            boolean signal = false;
            for (Direction direction: Direction.values()) {
                BlockPos neighborPose = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPose);
                if (neighborState.isSignalSource() && neighborState.getSignal(level, neighborPose, direction) != 0) {
                    signal = true;
                    break;
                }
            }

            if (signal && tile.data.playing) tile.pause();
            if (!signal && !tile.data.playing) tile.play();
        }
    }
}