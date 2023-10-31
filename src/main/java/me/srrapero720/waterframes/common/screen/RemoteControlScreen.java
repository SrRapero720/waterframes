package me.srrapero720.waterframes.common.screen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import team.creative.creativecore.common.gui.GuiLayer;

public class RemoteControlScreen extends GuiLayer {
    private final Player player;
    private final CompoundTag nbt;
    private final Item item;
    public RemoteControlScreen(Player player, CompoundTag nbt, Item item) {
        super("remote_screen", 80, 150);
        this.player = player;
        this.nbt = nbt;
        this.item = item;
    }

    @Override
    public void create() {

    }
}