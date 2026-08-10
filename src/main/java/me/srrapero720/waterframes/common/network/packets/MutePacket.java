package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MutePacket(BlockPos pos, boolean muted) implements ControlPacket {
    public static final Type<MutePacket> TYPE = DisplayNetwork.type("mute");
    public static final StreamCodec<ByteBuf, MutePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MutePacket::pos,
            ByteBufCodecs.BOOL, MutePacket::muted,
            MutePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.muted = muted;
    }

    @Override
    public void applyClient(DisplayTile tile) {
        if (tile.display != null) tile.display.setMuteMode(muted);
    }
}
