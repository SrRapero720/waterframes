package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SyncBlockPacket extends DisplayPacket {
    public CompoundTag nbt;

    public SyncBlockPacket() { super(); }
    public SyncBlockPacket(BlockPos pos, CompoundTag nbt) {
        super(pos);
        this.nbt = nbt;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        DisplayData.sync(tile, player, nbt);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {}

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {}
}