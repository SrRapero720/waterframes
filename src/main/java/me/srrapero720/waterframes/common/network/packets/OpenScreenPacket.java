package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to client order to open a display GUI. The server checks permissions and distance
 * before sending it, so the client never opens a screen the player is not allowed to use.
 */
public record OpenScreenPacket(BlockPos pos, boolean remote) implements CustomPacketPayload {
    public static final Type<OpenScreenPacket> TYPE = DisplayNetwork.type("open_screen");
    public static final StreamCodec<ByteBuf, OpenScreenPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenScreenPacket::pos,
            ByteBufCodecs.BOOL, OpenScreenPacket::remote,
            OpenScreenPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
