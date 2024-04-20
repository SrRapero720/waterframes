package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.WaterFrames;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class PermLevelPacket extends CreativePacket {
    public int level;

    public PermLevelPacket() {}
    public PermLevelPacket(MinecraftServer server) {
        this.level = server.getOperatorUserPermissionLevel();
    }

    @Override
    public void executeClient(Player player) {
        WaterFrames.setOpPermissionLevel(this.level);
    }

    @Override
    public void executeServer(ServerPlayer serverPlayer) {

    }
}
