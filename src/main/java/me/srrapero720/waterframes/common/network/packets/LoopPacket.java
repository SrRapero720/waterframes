package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LoopPacket(BlockPos pos, boolean loop) implements ControlPacket {
    public static final Type<LoopPacket> TYPE = DisplayNetwork.type("loop");
    public static final StreamCodec<ByteBuf, LoopPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, LoopPacket::pos,
            ByteBufCodecs.BOOL, LoopPacket::loop,
            LoopPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.loop = loop;
        // REPEAT IS SESSION STATE: THE CLOCK WRAPS ON IT AND EVERY VIEWER MIRRORS IT FROM THERE
        if (tile.clock() != null) tile.clock().repeat(loop);
    }
}
