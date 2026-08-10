package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ActivePacket(BlockPos pos, boolean active) implements ControlPacket {
    public static final Type<ActivePacket> TYPE = DisplayNetwork.type("active");
    public static final StreamCodec<ByteBuf, ActivePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ActivePacket::pos,
            ByteBufCodecs.BOOL, ActivePacket::active,
            ActivePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.active = active;
    }
}
