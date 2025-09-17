package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.packets.*;
import team.creative.creativecore.common.network.CreativeNetwork;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class DisplayNetwork {
    public static final CreativeNetwork NET = new CreativeNetwork(2, LOGGER, WaterFrames.asResource("network"));

    public static void sendServer(DisplayDataPacket packet) {
        NET.sendToServer(packet);
    }

    public static void sendServer(DisplayControlPacket packet) {
        NET.sendToServer(packet);
    }

    public static void sendClient(DisplayControlPacket packet, DisplayTile tile) {
        if (packet.bounce) {
            packet.bounce = false;
            packet.execute(tile, false);
        }
        NET.sendToClient(packet, tile.getLevel().getChunkAt(packet.pos));
    }
}
