package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class PermissionLevelPacket extends CreativePacket {
    public int level;

    public PermissionLevelPacket() {}
    public PermissionLevelPacket(MinecraftServer server) {
        this.level = server.getOperatorUserPermissionLevel();
    }

    @Override
    public void executeClient(Player player) {
        WaterFrames.setOpPermissionLevel(this.level);
        WaterFrames.LOGGER.warn("PACKET HANDLED");
    }

    @Override
    public void executeServer(ServerPlayer serverPlayer) {

    }
}
