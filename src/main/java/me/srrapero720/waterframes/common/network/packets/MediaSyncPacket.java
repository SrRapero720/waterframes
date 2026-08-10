package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Raw session traffic, straight from WaterMedia and never read by the mod. It travels both ways:
 * the server clock broadcasts where playback stands, every viewer reports and asks back.
 */
public record MediaSyncPacket(BlockPos pos, byte[] payload) implements CustomPacketPayload {
    /** The largest packet of the protocol is 29 bytes; the rest is room for a future revision. */
    private static final int MAX_PAYLOAD = 64;

    public static final Type<MediaSyncPacket> TYPE = DisplayNetwork.type("media_sync");
    public static final StreamCodec<ByteBuf, MediaSyncPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MediaSyncPacket::pos,
            ByteBufCodecs.byteArray(MAX_PAYLOAD), MediaSyncPacket::payload,
            MediaSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
