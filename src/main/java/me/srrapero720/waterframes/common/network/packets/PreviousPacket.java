package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PreviousPacket(BlockPos pos) implements ControlPacket {
    public static final Type<PreviousPacket> TYPE = DisplayNetwork.type("previous");
    public static final StreamCodec<ByteBuf, PreviousPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PreviousPacket::pos,
            PreviousPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        // A ONE ENTRY PLAYLIST LANDS BACK ON THE SAME URL, AND STILL HAS TO START OVER
        tile.data.prevUrl();
        tile.restartClock();
    }
}
