package me.srrapero720.waterframes.common.data;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.block.entity.TvTile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.GuiLayer;

import java.util.ArrayList;
import java.util.List;

public class TvData extends DisplayData {
    public List<String> url_list = new ArrayList<>();
    public int url_index = 0;

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
    }

    public static CompoundTag build(GuiLayer gui) {
        CompoundTag tag = DisplayData.build(gui);
        tag.put("url_list", new ListTag());
        tag.putInt("url_index", 0);
        return tag;
    }

    public static void sync(TvTile block, Player player, CompoundTag nbt) {
        DisplayData.sync(block, player, nbt, data -> {
            block.data.url_index = nbt.getInt("url_index");
        });
    }
}