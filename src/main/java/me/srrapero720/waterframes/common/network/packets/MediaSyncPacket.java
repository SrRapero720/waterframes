package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.watermedia.api.player.MediaPlayer.PlayerState;

import java.nio.ByteBuffer;

/**
 * Snapshot of a display's session clock, straight from WaterMedia and never read by the mod. It
 * travels one way: the server broadcasts where playback stands and every viewer follows it.
 */
public record MediaSyncPacket(BlockPos pos, byte[] payload) implements CustomPacketPayload {
    public static final Type<MediaSyncPacket> TYPE = DisplayNetwork.type("media_sync");
    public static final StreamCodec<ByteBuf, MediaSyncPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MediaSyncPacket::pos,
            ByteBufCodecs.byteArray(PlayerState.BYTES), MediaSyncPacket::payload,
            MediaSyncPacket::new);

    public MediaSyncPacket(BlockPos pos, PlayerState state) {
        this(pos, bytes(state));
    }

    private static byte[] bytes(PlayerState state) {
        ByteBuffer buffer = state.toBytes();
        byte[] payload = new byte[buffer.remaining()];
        buffer.get(payload);
        return payload;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
