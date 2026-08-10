package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.DisplaysConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VolumePacket(BlockPos pos, int volume) implements ControlPacket {
    public static final Type<VolumePacket> TYPE = DisplayNetwork.type("volume");
    public static final StreamCodec<ByteBuf, VolumePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, VolumePacket::pos,
            ByteBufCodecs.VAR_INT, VolumePacket::volume,
            VolumePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.volume = DisplaysConfig.maxVol(volume);
    }
}
