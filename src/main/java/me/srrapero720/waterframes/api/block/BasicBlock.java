package me.srrapero720.waterframes.api.block;

import me.srrapero720.waterframes.api.block.entity.BasicBlockEntity;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import me.srrapero720.waterframes.core.WaterConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.common.gui.handler.BlockGuiCreator;
import team.creative.creativecore.common.gui.handler.GuiCreator;

public abstract class BasicBlock extends BaseEntityBlock implements BlockGuiCreator {
    protected BasicBlock(Properties pProperties) {
        super(pProperties);
    }

    public abstract DirectionProperty getFacing();

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BasicBlockEntity::tick;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) { return RenderShape.MODEL; }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && WaterConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
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

    // REDSTONE LOGIC
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Block block, BlockPos neighborPos, boolean isMoving) {
        if (!WaterConfig.isDisabledRedstone() && level.getBlockEntity(pos) instanceof ProjectorTile tile) {
            var signal = false;
            for (var direction: Direction.values()) {
                var neightborPos = pos.relative(direction);
                var neightborState = level.getBlockState(neightborPos);
                if (neightborState.isSignalSource() && neightborState.getSignal(level, neightborPos, direction) != 0) {
                    signal = true;
                    break;
                }
            }

            if (signal && tile.data.playing) tile.pause();
            if (!signal && !tile.data.playing) tile.play();
        }

        super.neighborChanged(state, level, pos, block, neighborPos, isMoving);
    }
}