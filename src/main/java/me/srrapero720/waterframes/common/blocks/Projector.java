package me.srrapero720.waterframes.common.blocks;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.client.screen.ProjectorScreen;
import me.srrapero720.waterframes.common.tiles.BEProjector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.BlockGuiCreator;
import team.creative.creativecore.common.gui.handler.GuiCreator;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;

public class Projector extends BaseEntityBlock implements BlockGuiCreator {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static @NotNull AlignedBox box(Direction direction, int distance) {
        var facing = Facing.get(direction);
        var box = new AlignedBox();

        if (facing.positive) box.setMax(facing.axis, distance);
        else box.setMin(facing.axis, 1 - distance);
        return box;
    }

    public Projector() {
        super(Properties.of(Material.METAL).strength(2.5f, 10.0f).sound(SoundType.METAL).noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BEProjector(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState state, @NotNull Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && WFConfig.canInteract(player, level)) GuiCreator.BLOCK_OPENER.open(player, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Level level, BlockPos blockPos, BlockState blockState, Player player) {
        return (level.getBlockEntity(blockPos) instanceof BEProjector frame) ? new ProjectorScreen(frame) : null;
    }
}
