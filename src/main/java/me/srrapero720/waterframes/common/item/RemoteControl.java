package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.RemoteControlScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.gui.creator.ItemGuiCreator;

public class RemoteControl extends Item implements ItemGuiCreator {
    public RemoteControl(Properties pProperties) {
        super(pProperties.stacksTo(1).setNoRepair().fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pUsedHand) {
        if (pUsedHand == InteractionHand.OFF_HAND)
            return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(pUsedHand));

        if (!level.isClientSide && DisplayConfig.canInteract(player, level)) {
            var tag = player.getItemInHand(pUsedHand).getOrCreateTag();
            if (tag.isEmpty()) {
                player.displayClientMessage(new TextComponent("No display Binded"), true);
                return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(pUsedHand));
            } else {
                long[] pos = tag.getLongArray("pos");
                var blockPos = new BlockPos(pos[0], pos[1], pos[2]);
                var dimension = new ResourceLocation(tag.getString("dimension"));


                if (!(level.getBlockEntity(blockPos) instanceof DisplayTile)) {
                    player.getItemInHand(pUsedHand).setTag(new CompoundTag());
                    player.displayClientMessage(new TextComponent("Display is removed"), false);
                    return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(pUsedHand));
                } else {
                    if (!level.dimension().location().equals(dimension) || !Vec3.atCenterOf(blockPos).closerThan(player.position(), 32)) {
                        player.displayClientMessage(new TextComponent("You're out of distance"), false);
                        return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(pUsedHand));
                    } else {
                        GuiCreator.ITEM_OPENER.open(player.getItemInHand(pUsedHand).getOrCreateTag(), player, pUsedHand);
                        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(pUsedHand));
                    }
                }

            }
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(pUsedHand));
    }

    @Override
    public GuiLayer create(CompoundTag tag, Player player) {
        long[] pos = tag.getLongArray("pos");
        var blockPos = new BlockPos(pos[0], pos[1], pos[2]);
        return new RemoteControlScreen(player, (DisplayTile) player.level.getBlockEntity(blockPos), tag, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();

        if (context.getHand() == InteractionHand.OFF_HAND || !context.getItemInHand().getOrCreateTag().isEmpty() || context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }

        if (level.getBlockEntity(pos) instanceof DisplayTile) {
            var item = context.getItemInHand();
            var tag = item.getOrCreateTag();

            tag.putLongArray("pos", new long[] { pos.getX(), pos.getY(), pos.getZ() });
            tag.putString("dimension", level.dimension().location().toString());

            item.save(tag);
            player.displayClientMessage(new TextComponent("Binded"), false);
            return InteractionResult.SUCCESS;
        } else {
            player.displayClientMessage(new TextComponent("Invalid display tile"), false);
            return InteractionResult.FAIL;
        }

    }

    @Override
    public boolean canAttackBlock(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return pStack.hasTag();
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {

    }
}