package me.srrapero720.waterframes.custom.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class SyncTickPacket extends CreativePacket {

    public SyncTickPacket() {

    }

    public SyncTickPacket(BlockPos pos) {

    }

    @Override
    public void executeClient(Player player) {

    }

    @Override
    public void executeServer(ServerPlayer serverPlayer) {

    }
}