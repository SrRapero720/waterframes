package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.WFRegistry;
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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.Nullable;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;

import java.util.List;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class RemoteControl extends Item implements ItemGuiCreator {
    private static final String POSITION = "position";
    private static final String DIMENSION = "dimension";
    private static final Marker IT = MarkerManager.getMarker(RemoteControl.class.getSimpleName());
    public RemoteControl(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResultHolder.fail(stack);
        }

        if (!WFConfig.canInteractItem(player)) {
            this.sendFatal(player, Component.translatable("waterframes.common.access.denied"));
            return InteractionResultHolder.fail(stack);
        }

        var data = stack.get(WFRegistry.REMOTE_DATA);
        if (data == null) {
            this.sendFailed(player, Component.translatable("waterframes.remote.bound.failed"));
            return InteractionResultHolder.pass(stack);
        }

        if (player.isCrouching()) {
            stack.set(WFRegistry.REMOTE_DATA, null);
            this.sendSuccess(player, Component.translatable("waterframes.remote.unbound.success"));
            return InteractionResultHolder.success(stack);
        }

        var blockPos = new BlockPos(data.x(), data.y(), data.z());
        var dimension = ResourceLocation.parse(data.dimension());

        if (level.getBlockEntity(blockPos) instanceof DisplayTile tile) {
            double distance = WaterFrames.getDistance(tile, player.position());
            if (level.dimension().location().equals(dimension) && distance < WFConfig.maxRcDis()) {
                var tag = new CompoundTag();
                tag.putString("dimension", data.dimension());
                tag.putIntArray("position", data.getPos());

                GuiCreator.ITEM_OPENER.open(tag, player, hand);
                return InteractionResultHolder.success(stack);
            }

            this.sendFailed(player, Component.translatable("waterframes.remote.distance.failed"));
            return InteractionResultHolder.fail(stack);
        }

        // FALLBACK UNBIND
        player.getItemInHand(hand).set(WFRegistry.REMOTE_DATA, null);
        this.sendFailed(player, Component.translatable("waterframes.remote.display.failed"));
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();
        var data = context.getItemInHand().get(WFRegistry.REMOTE_DATA);

        if (player == null || context.getHand() == InteractionHand.OFF_HAND || data != null) {
            return InteractionResult.PASS;
        }

        if (!WFConfig.canInteractItem(player)) {
            this.sendFatal(player, Component.translatable("waterframes.common.access.denied"));
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof DisplayTile) {
            var item = context.getItemInHand();

            item.set(WFRegistry.REMOTE_DATA, new RemoteData(level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ()));

            this.sendSuccess(player, Component.translatable("waterframes.remote.bound.success"));
            return InteractionResult.SUCCESS;
        }

        this.sendFailed(player, Component.translatable("waterframes.remote.display.invalid"));
        return InteractionResult.FAIL;
    }

    private void sendSuccess(Player player, MutableComponent component) {
        if (player.level.isClientSide) player.displayClientMessage(component.withStyle(ChatFormatting.AQUA), true);
    }

    private void sendFailed(Player player, MutableComponent component) {
        if (player.level.isClientSide) player.displayClientMessage(component.withStyle(ChatFormatting.RED), true);
    }

    private void sendFatal(Player player, MutableComponent component) {
        if (player.level.isClientSide) player.displayClientMessage(component.withStyle(ChatFormatting.DARK_RED), true);
    }

    public boolean hasPosition(CompoundTag tag) {
        return tag.contains(POSITION);
    }

    public boolean hasDimension(CompoundTag tag) {
        return tag.contains(DIMENSION);
    }

    public int[] getPosition(CompoundTag data) {
        return data.getIntArray(POSITION);
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
    public Component getHighlightTip(ItemStack item, Component displayName) {
        return Component.literal(displayName.getString()).withStyle(ChatFormatting.AQUA);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        Options opts = Minecraft.getInstance().options;
        pTooltipComponents.add(Component.translatable("waterframes.remote.description.1", opts.keyShift.getKey().getDisplayName(), opts.keyUse.getKey().getDisplayName()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.get(WFRegistry.REMOTE_DATA) != null;
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return false;
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }
}