package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.packets.*;
import me.srrapero720.waterframes.common.screens.DisplayScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativeNetwork;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class WFNetwork {
    public static final CreativeNetwork NET_DISPLAYS = new CreativeNetwork(1, LOGGER, new ResourceLocation(WaterFrames.ID, "displays"));
    public static final CreativeNetwork NET_DATA = new CreativeNetwork(1, LOGGER, new ResourceLocation(WaterFrames.ID, "data"));

    public static void updateDataServer(DisplayTile tile, DisplayScreen screen) {
        NET_DATA.sendToServer(new SyncBlockPacket(tile.getBlockPos(), DisplayData.build(screen, tile)));
    }
    public static void updateDataClient(SyncBlockPacket packet, Level level) {
        NET_DATA.sendToClient(packet, level, packet.pos);
    }

    public static void sendLoopServer(DisplayTile tile, boolean looping) {
        NET_DISPLAYS.sendToServer(new LoopPacket(tile.getBlockPos(), looping));
    }
    public static void sendLoopClient(LoopPacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void sendMutedServer(DisplayTile tile, boolean muted) {
        NET_DISPLAYS.sendToServer(new MutedPacket(tile.getBlockPos(), muted));
    }
    public static void sendMutedClient(MutedPacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void sendActiveServer(DisplayTile tile, boolean active) {
        NET_DISPLAYS.sendToServer(new ActivePacket(tile.getBlockPos(), active));
    }
    public static void sendActiveClient(ActivePacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void sendPlaybackServer(DisplayTile tile, boolean paused, int tickTime) {
        NET_DISPLAYS.sendToServer(new PauseModePacket(tile.getBlockPos(), paused, tickTime));
    }
    public static void sendPlaybackClient(PauseModePacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void sendPlaytimeServer(DisplayTile tile, int tick, int tickMax) {
        NET_DISPLAYS.sendToServer(new PlaytimePacket(tile.getBlockPos(), tick, tickMax));
    }
    public static void sendPlaytimeClient(PlaytimePacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void sendVolumeServer(DisplayTile tile, int volumeMin, int volumeMax, int volume) {
        NET_DISPLAYS.sendToServer(new VolumePacket(tile.getBlockPos(), volumeMin, volumeMax, volume));
    }
    public static void sendVolumeClient(VolumePacket packet, Level level) {
        NET_DISPLAYS.sendToClient(packet, level, packet.pos);
    }

    public static void register() {
        // DATA CHANNEL
        NET_DATA.registerType(SyncBlockPacket.class, SyncBlockPacket::new);
        NET_DATA.registerType(PermissionLevelPacket.class, PermissionLevelPacket::new);

        // ACTIONS CHANNELS
        NET_DISPLAYS.registerType(ActivePacket.class, ActivePacket::new);
        NET_DISPLAYS.registerType(LoopPacket.class, LoopPacket::new);
        NET_DISPLAYS.registerType(MutedPacket.class, MutedPacket::new);
        NET_DISPLAYS.registerType(PauseModePacket.class, PauseModePacket::new);
        NET_DISPLAYS.registerType(PlaytimePacket.class, PlaytimePacket::new);
        NET_DISPLAYS.registerType(VolumePacket.class, VolumePacket::new);
    }
}
