package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.RemoteControlScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

        var tag = stack.getOrCreateTag();
        if (tag.isEmpty()) {
            this.sendFailed(player, Component.translatable("waterframes.remote.bound.failed"));
            return InteractionResultHolder.pass(stack);
        }

        if (player.isCrouching() && !tag.isEmpty()) {
            stack.setTag(null);
            this.sendSuccess(player, Component.translatable("waterframes.remote.unbound.success"));
            return InteractionResultHolder.success(stack);
        }

        int[] pos = this.getPosition(tag);
        String dim = this.getDimension(tag);
        if (pos.length < 3 || dim.isEmpty()) {
            this.sendFailed(player, Component.translatable("waterframes.remote.code.failed"));
            LOGGER.error(IT, "NBT data is invalid, ensure your set pos as a long-int and the dimension as a resource location");
            return InteractionResultHolder.fail(stack);
        }

        var blockPos = new BlockPos(pos[0], pos[1], pos[2]);
        var dimension = new ResourceLocation(dim);

        if (level.getBlockEntity(blockPos) instanceof DisplayTile tile) {
            double distance = WaterFrames.getDistance(tile, player.position());
            if (level.dimension().location().equals(dimension) && distance < WFConfig.maxRcDis()) {
                GuiCreator.ITEM_OPENER.open(player.getItemInHand(hand).getOrCreateTag(), player, hand);
                return InteractionResultHolder.success(stack);
            }

            this.sendFailed(player, Component.translatable("waterframes.remote.distance.failed"));
            return InteractionResultHolder.fail(stack);
        }

        // FALLBACK UNBIND
        player.getItemInHand(hand).setTag(null);
        this.sendFailed(player, Component.translatable("waterframes.remote.display.failed"));
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();

        if (player == null || context.getHand() == InteractionHand.OFF_HAND || !context.getItemInHand().getOrCreateTag().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!WFConfig.canInteractItem(player)) {
            this.sendFatal(player, Component.translatable("waterframes.common.access.denied"));
            return InteractionResult.FAIL;
        }

        if (level.getBlockEntity(pos) instanceof DisplayTile) {
            var item = context.getItemInHand();
            var tag = item.getOrCreateTag();

            this.setPosition(tag, pos);
            this.setDimension(tag, level);

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

    public int[] getPosition(CompoundTag tag) {
        return tag.getIntArray(POSITION);
    }

    public String getDimension(CompoundTag tag) {
        return tag.getString(DIMENSION);
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
        var blockPos = new BlockPos((int) pos[0], (int) pos[1], (int) pos[2]);
        return new RemoteControlScreen(player, (DisplayTile) player.level.getBlockEntity(blockPos), tag, this);
    }

//    @Override
    // TODO: please send help
    public Component getHighlightTip(ItemStack item, Component displayName) {
        return Component.literal(displayName.getString()).withStyle(ChatFormatting.AQUA);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag isAdvanced) {
        Options opts = Minecraft.getInstance().options;
        pTooltipComponents.add(Component.translatable("waterframes.remote.description.1", opts.keyShift.key.getDisplayName(), opts.keyUse.key.getDisplayName()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        var tag = pStack.getTag();
        return tag != null && !tag.isEmpty() && (tag.contains("position") || tag.contains("pos")) && tag.contains("dimension");
    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return false;
    }
}