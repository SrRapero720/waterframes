package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.packets.C2SSyncDataPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativeNetwork;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class DisplaysNet {
    private static final CreativeNetwork NET_DISPLAYS = new CreativeNetwork(1, LOGGER, new ResourceLocation(WaterFrames.ID, "displays"));
    private static final CreativeNetwork NET_DATA = new CreativeNetwork(1, LOGGER, new ResourceLocation(WaterFrames.ID, "data"));

    public static void updateData(BlockPos pos, Level level, CompoundTag tag) {
        NET_DATA.sendToServer(new C2SSyncDataPacket());
    }

    public static void sendPlayBackState(DisplayTile playstate) {
        NET_DISPLAYS.sendToServer(null);
    }

    public static void register() {
        NET_DATA.registerType(C2SSyncDataPacket.class, C2SSyncDataPacket::new);
    }
}
