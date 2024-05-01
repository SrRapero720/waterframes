package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.packets.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.network.CreativePacket;

import java.util.List;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class DisplayNetwork {
    public static final CreativeNetwork NET = new CreativeNetwork(2, LOGGER, new ResourceLocation(WaterFrames.ID, "network"));

    public static void sendClient(CreativePacket packet, Level level, BlockPos pos) {
        for (ServerPlayer player: (List<ServerPlayer>) level.players()) {
            NET.sendToClient(packet, player);
        }
//        DATA.sendToClient(packet, level, pos);
    }

    public static void sendClient(CreativePacket packet, ServerPlayer player) {
        NET.sendToClient(packet, player);
    }

    public static void sendServer(CreativePacket packet) {
        NET.sendToServer(packet);
    }

    public static void sendClient(DisplayDataPacket packet, DisplayTile tile) {
        for (ServerPlayer player: (List<ServerPlayer>) tile.getLevel().players()) {
            NET.sendToClient(packet, player);
        }
    }

    public static void sendServer(DisplayDataPacket packet) {
        NET.sendToServer(packet);
    }

    public static void sendClient(DisplayControlPacket packet, DisplayTile tile) {
        if (packet.bounce) {
            packet.bounce = false;
            packet.execute(tile, false);
        }
        for (ServerPlayer player: (List<ServerPlayer>) tile.getLevel().players()) {
            NET.sendToClient(packet, player);
        }
//        NET.sendToClient(packet, tile.getLevel().getChunkAt(packet.pos));
    }

    public static void sendServer(DisplayControlPacket packet) {
        NET.sendToServer(packet);
    }
}
