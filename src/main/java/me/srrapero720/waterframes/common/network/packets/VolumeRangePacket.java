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

public record VolumeRangePacket(BlockPos pos, int min, int max) implements ControlPacket {
    public static final Type<VolumeRangePacket> TYPE = DisplayNetwork.type("volume_range");
    public static final StreamCodec<ByteBuf, VolumeRangePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, VolumeRangePacket::pos,
            ByteBufCodecs.VAR_INT, VolumeRangePacket::min,
            ByteBufCodecs.VAR_INT, VolumeRangePacket::max,
            VolumeRangePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.maxVolumeDistance = DisplaysConfig.maxVolDis(max);
        tile.data.minVolumeDistance = Math.min(tile.data.maxVolumeDistance, min);
    }
}
