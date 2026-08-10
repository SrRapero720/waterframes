package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NextPacket(BlockPos pos) implements ControlPacket {
    public static final Type<NextPacket> TYPE = DisplayNetwork.type("next");
    public static final StreamCodec<ByteBuf, NextPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, NextPacket::pos,
            NextPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        // A ONE ENTRY PLAYLIST LANDS BACK ON THE SAME URL, AND STILL HAS TO START OVER
        tile.data.nextUrl();
        tile.restartClock();
    }
}
