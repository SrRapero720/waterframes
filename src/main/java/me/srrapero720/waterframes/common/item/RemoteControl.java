package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.WaterFramesConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.item.data.RemoteData;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.List;

/**
 * Hand-held control of one display. Sneak-use on a display binds it, use opens the remote
 * screen while in range and in the same dimension, sneak-use in the air unbinds it.
 */
public class RemoteControl extends Item {
    public RemoteControl(final Properties properties) {
        super(properties);
    }

    /** Whether the stack is a remote bound to that block of that level. */
    public static boolean bound(final ItemStack stack, final Level level, final BlockPos pos) {
        final RemoteData data = stack.get(WaterFrames.REMOTE_DATA);
        return data != null && data.matches(level, pos);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) return InteractionResultHolder.fail(stack);

        if (!WaterFramesConfig.canInteractRemote(player)) {
            feedback(player, "waterframes.common.access.denied", ChatFormatting.DARK_RED, 0.5f);
            return InteractionResultHolder.fail(stack);
        }

        final RemoteData data = stack.get(WaterFrames.REMOTE_DATA);
        if (data == null) {
            feedback(player, "waterframes.remote.bound.failed", ChatFormatting.RED, 0.75f);
            return InteractionResultHolder.pass(stack);
        }

        if (player.isCrouching()) {
            stack.set(WaterFrames.REMOTE_DATA, null);
            feedback(player, "waterframes.remote.unbound.success", ChatFormatting.AQUA, 1.25f);
            return InteractionResultHolder.success(stack);
        }

        // RANGE FIRST: A FAR AWAY BINDING MUST NOT MAKE THE SERVER LOAD A CHUNK JUST TO SAY NO
        final BlockPos pos = data.pos();
        if (data.matches(level, pos) && WaterFrames.getDistance(level, pos, player.position()) < WaterFramesConfig.maxRcDis()) {
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof DisplayTile) {
                if (player instanceof final ServerPlayer server) DisplayNetwork.openScreen(server, pos, true);
                return InteractionResultHolder.success(stack);
            }
            feedback(player, "waterframes.remote.distance.failed", ChatFormatting.RED, 0.75f);
            return InteractionResultHolder.fail(stack);
        }

        // THE DISPLAY IS GONE OR LIVES IN ANOTHER DIMENSION: THE BINDING IS WORTHLESS, DROP IT
        stack.set(WaterFrames.REMOTE_DATA, null);
        feedback(player, "waterframes.remote.display.failed", ChatFormatting.RED, 0.75f);
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Player player = context.getPlayer();
        final ItemStack stack = context.getItemInHand();
        if (player == null || context.getHand() == InteractionHand.OFF_HAND || stack.has(WaterFrames.REMOTE_DATA) || !player.isCrouching())
            return InteractionResult.PASS;

        if (!WaterFramesConfig.canBindRemote(player)) {
            feedback(player, "waterframes.common.access.denied", ChatFormatting.DARK_RED, 0.5f);
            return InteractionResult.FAIL;
        }

        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();
        if (level.getBlockEntity(pos) instanceof DisplayTile) {
            stack.set(WaterFrames.REMOTE_DATA, new RemoteData(level, pos));
            feedback(player, "waterframes.remote.bound.success", ChatFormatting.AQUA, 1.25f);
            return InteractionResult.SUCCESS;
        }

        feedback(player, "waterframes.remote.display.invalid", ChatFormatting.RED, 0.75f);
        return InteractionResult.FAIL;
    }

    // ACTION BAR LINE PLUS A NOTE: A BELL ABOVE NORMAL PITCH FOR GOOD NEWS, A HARP BELOW IT FOR BAD ONES
    private static void feedback(final Player player, final String key, final ChatFormatting color, final float pitch) {
        if (!player.level().isClientSide) return;
        final MutableComponent message = Component.translatable(key).withStyle(color);
        player.displayClientMessage(message, true);
        final NoteBlockInstrument instrument = pitch > 1f ? NoteBlockInstrument.BELL : NoteBlockInstrument.HARP;
        player.playSound(instrument.getSoundEvent().value(), 1.0f, pitch);
    }

    @Override
    public Component getHighlightTip(final ItemStack item, final Component displayName) {
        return Component.literal(displayName.getString()).withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // KEYBIND COMPONENTS RESOLVE ON THE CLIENT AT RENDER TIME, SO TOOLTIPS STAY SERVER-SAFE
        tooltip.add(Component.translatable("waterframes.remote.description.1", Component.keybind("key.sneak"), Component.keybind("key.use")));
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return stack.has(WaterFrames.REMOTE_DATA);
    }

    @Override
    public boolean canAttackBlock(final BlockState state, final Level level, final BlockPos pos, final Player player) {
        return false;
    }

    @Override
    public boolean canDisableShield(final ItemStack stack, final ItemStack shield, final LivingEntity entity, final LivingEntity attacker) {
        return true;
    }
}
