package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.DisplaysRegistry;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.item.data.RemoteData;
import me.srrapero720.waterframes.common.screens.RemoteControlScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;

import java.util.List;
import java.util.function.Consumer;

public class RemoteControl extends Item implements ItemGuiCreator {
    private static final String POSITION = "position";
    private static final String DIMENSION = "dimension";
    private static final Marker IT = MarkerManager.getMarker(RemoteControl.class.getSimpleName());
    public RemoteControl(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.FAIL;
        }

        if (!DisplaysConfig.canInteractRemote(player)) {
            this.sendFatal(player, Component.translatable("waterframes.common.access.denied"));
            return InteractionResult.FAIL;
        }

        var data = stack.get(DisplaysRegistry.REMOTE_DATA);
        if (data == null) {
            this.sendFailed(player, Component.translatable("waterframes.remote.bound.failed"));
            return InteractionResult.PASS;
        }

        if (player.isCrouching()) {
            stack.set(DisplaysRegistry.REMOTE_DATA, null);
            this.sendSuccess(player, Component.translatable("waterframes.remote.unbound.success"));
            return InteractionResult.SUCCESS;
        }

        var blockPos = new BlockPos(data.x(), data.y(), data.z());
        var dimension = ResourceLocation.parse(data.dimension());

        if (level.getBlockEntity(blockPos) instanceof DisplayTile tile) {
            double distance = WaterFrames.getDistance(tile, player.position());
            if (level.dimension().location().equals(dimension) && distance < DisplaysConfig.maxRcDis()) {
                var tag = new CompoundTag();
                tag.putString("dimension", data.dimension());
                tag.putIntArray("position", data.getPos());

                GuiCreator.ITEM_OPENER.open(tag, player, hand);
                return InteractionResult.SUCCESS;
            }

            this.sendFailed(player, Component.translatable("waterframes.remote.distance.failed"));
            return InteractionResult.FAIL;
        }

        // FALLBACK UNBIND
        player.getItemInHand(hand).set(DisplaysRegistry.REMOTE_DATA, null);
        this.sendFailed(player, Component.translatable("waterframes.remote.display.failed"));
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();
        var data = context.getItemInHand().get(DisplaysRegistry.REMOTE_DATA);

        if (player == null || context.getHand() == InteractionHand.OFF_HAND || data != null || !player.isCrouching()) {
            return InteractionResult.PASS;
        }

        if (!DisplaysConfig.canBindRemote(player)) {
            this.sendFatal(player, Component.translatable("waterframes.common.access.denied"));
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof DisplayTile) {
            var item = context.getItemInHand();

            item.set(DisplaysRegistry.REMOTE_DATA, new RemoteData(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ()));

            this.sendSuccess(player, Component.translatable("waterframes.remote.bound.success"));
            return InteractionResult.SUCCESS;
        }

        this.sendFailed(player, Component.translatable("waterframes.remote.display.invalid"));
        return InteractionResult.FAIL;
    }

    private void sendSuccess(Player player, MutableComponent component) {
        if (player.level.isClientSide) {
            player.displayClientMessage(component.withStyle(ChatFormatting.AQUA), true);
            player.playSound(NoteBlockInstrument.BELL.getSoundEvent().value(), 1.0f, 1.25f);
        }
    }

    private void sendFailed(Player player, MutableComponent component) {
        if (player.level.isClientSide) {
            player.displayClientMessage(component.withStyle(ChatFormatting.RED), true);
            player.playSound(NoteBlockInstrument.HARP.getSoundEvent().value(), 1.0f, 0.75f);
        }
    }

    private void sendFatal(Player player, MutableComponent component) {
        if (player.level.isClientSide) {
            player.displayClientMessage(component.withStyle(ChatFormatting.DARK_RED), true);
            player.playSound(NoteBlockInstrument.HARP.getSoundEvent().value(), 1.0f, 0.5f);
        }
    }

    public boolean hasPosition(CompoundTag tag) {
        return tag.contains(POSITION);
    }

    public boolean hasDimension(CompoundTag tag) {
        return tag.contains(DIMENSION);
    }

    public int[] getPosition(CompoundTag data) {
        return data.getIntArray(POSITION).orElse(null);
    }

    public int[] getPosition(RemoteData data) {
        return data.getPos();
    }

    public String getDimension(RemoteData data) {
        return data.dimension();
    }

    public void setPosition(CompoundTag tag, BlockPos pos) {
        tag.putIntArray(POSITION, new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    public void setDimension(CompoundTag tag, Level level) {
        tag.putString(DIMENSION, level.dimension().location().toString());
    }

    @Override
    public GuiLayer create(CompoundTag tag, Player player) {
        int[] pos = this.getPosition(tag);
        var blockPos = new BlockPos(pos[0], pos[1], pos[2]);
        return new RemoteControlScreen(player, (DisplayTile) player.level.getBlockEntity(blockPos), tag, this);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, TooltipDisplay tooltipDisplay, Consumer<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, tooltipDisplay, pTooltipComponents, pTooltipFlag);
        Options opts = Minecraft.getInstance().options;
        pTooltipComponents.accept(Component.translatable("waterframes.remote.description.1", opts.keyShift.key.getDisplayName(), opts.keyUse.key.getDisplayName()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.get(DisplaysRegistry.REMOTE_DATA) != null;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, LivingEntity pPlayer) {
        return false;
    }
}