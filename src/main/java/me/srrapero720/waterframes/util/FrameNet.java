package me.srrapero720.waterframes.util;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.network.packets.C2SSyncDataPacket;
import me.srrapero720.waterframes.common.packets.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativeNetwork;

import java.util.List;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class FrameNet {
    private static final CreativeNetwork NETWORK = new CreativeNetwork(2, LOGGER, new ResourceLocation(WaterFrames.ID, "network"));

    public static void syncDisplayToServer(BlockPos blockPos, Player player, Level level, CompoundTag tag) {
        NETWORK.sendToServer(new C2SSyncDataPacket());
    }

    public static void syncDisplayToClients(BlockPos blockPos, Player player, Level level, CompoundTag tag) {
        NETWORK.sendToClientAll(level.getServer(), new C2SSyncDataPacket());
    }

    public static void sendVolumeUpdate(BlockPos pos, Level level, int min, int max, int volume) {
        NETWORK.sendToClient(new VolumePacket(pos, (short) min, (short) max, (short) volume), level, pos);
    }

    public static void sendPlaybackState(BlockPos pos, Level level, boolean playing, int tick) {
        NETWORK.sendToClient(new ActionPacket(pos, playing, tick), level, pos);
    }

    public static void sendActiveToggle(BlockPos pos, Level level, boolean active) {
        NETWORK.sendToClient(new ActivePacket(pos, active), level, pos);
    }

    public static void syncPlaybackState(BlockPos pos, boolean playing, int tick) {
        NETWORK.sendToServer(new ActionPacket(pos, playing, tick));
    }

    public static void syncMaxTickTime(BlockPos pos, int maxTick) {
        NETWORK.sendToServer(new TickPacket(pos, maxTick));
    }

    public static void syncUrlList(BlockPos pos, List<String> urlList, int index) {
        NETWORK.sendToServer(new SourceCollectionPacket(pos, urlList, index));
    }

    static void register() {
        NETWORK.registerType(ActionPacket.class, ActionPacket::new);
        NETWORK.registerType(TickPacket.class, TickPacket::new);
        NETWORK.registerType(VolumePacket.class, VolumePacket::new);
        NETWORK.registerType(ActivePacket.class, ActivePacket::new);
    }
}