package me.srrapero720.waterframes.common.item;

import me.srrapero720.waterframes.common.screen.RemoteControlScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.ItemGuiCreator;

public class RemoteControl extends Item implements ItemGuiCreator {
    public RemoteControl(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public GuiLayer create(CompoundTag compoundTag, Player player) {
        return new RemoteControlScreen(player, compoundTag, this);
    }
}