package me.srrapero720.waterframes.common.media;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.MediaSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.nio.ByteBuffer;
import org.watermedia.api.media.players.sync.Bridge;

/**
 * Byte carrier of one display session. A display is its position, which is all the routing this
 * needs: downstream to everyone tracking the chunk, upstream to the server.
 */
public record DisplayBridge(Level level, BlockPos pos) implements Bridge {

    /** Name of the session a display owns, derived from where the block stands. */
    public static ResourceLocation session(BlockPos pos) {
        return WaterFrames.asResource("display_at_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ());
    }

    public ResourceLocation session() {
        return session(this.pos);
    }

    @Override
    public void send(ByteBuffer payload) {
        byte[] bytes = new byte[payload.remaining()];
        payload.get(bytes);
        MediaSyncPacket packet = new MediaSyncPacket(this.pos, bytes);

        // WATERMEDIA SENDS FROM ITS OWN SYNC THREAD AND THE CHUNK VIEWER LIST IS SERVER THREAD ONLY;
        // THE CLIENT CONNECTION TAKES PACKETS FROM ANYWHERE, SO ONLY THE AUTHORITY NEEDS A HANDOVER
        if (this.level instanceof ServerLevel server) {
            // A STOPPED SERVER RUNS WHAT IS HANDED TO IT ON THE CALLING THREAD, WHICH IS NOT THIS ONE
            MinecraftServer host = server.getServer();
            if (!host.isStopped()) host.execute(() -> DisplayNetwork.sendChunk(server, this.pos, packet));
        } else {
            PacketDistributor.sendToServer(packet);
        }
    }
}
