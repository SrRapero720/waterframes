package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class DataListSyncPacket extends DisplayDataPacket {
    public CompoundTag nbt;

    public DataListSyncPacket() {}
    public DataListSyncPacket(BlockPos pos, CompoundTag nbt) {
        super(pos);
        this.nbt = nbt;
    }

    @Override
    public void execServer(DisplayTile tile, ServerPlayer player) {
        DisplayData.syncList(tile, player, nbt);
    }

    @Override
    public void execClient(DisplayTile tile, Player player) {

    }

    @Override
    public void exec(DisplayTile tile, Player player) {

    }
}
