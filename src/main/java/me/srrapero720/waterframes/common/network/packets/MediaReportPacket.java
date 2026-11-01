package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * What a viewer learned about the media it opened. The server clock decodes nothing itself, so
 * the length of the media and whether it is a live stream reach it from here, client to server.
 */
public record MediaReportPacket(BlockPos pos, long duration, boolean live) implements CustomPacketPayload {
    public static final Type<MediaReportPacket> TYPE = DisplayNetwork.type("media_report");
    public static final StreamCodec<ByteBuf, MediaReportPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MediaReportPacket::pos,
            ByteBufCodecs.VAR_LONG, MediaReportPacket::duration,
            ByteBufCodecs.BOOL, MediaReportPacket::live,
            MediaReportPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
