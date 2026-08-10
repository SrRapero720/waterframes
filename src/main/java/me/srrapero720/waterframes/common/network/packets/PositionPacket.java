package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Ordinals travel on the wire; -1 means "leave this axis untouched". */
public record PositionPacket(BlockPos pos, int horizontal, int vertical) implements ControlPacket {
    public static final Type<PositionPacket> TYPE = DisplayNetwork.type("position");
    public static final StreamCodec<ByteBuf, PositionPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PositionPacket::pos,
            ByteBufCodecs.VAR_INT, PositionPacket::horizontal,
            ByteBufCodecs.VAR_INT, PositionPacket::vertical,
            PositionPacket::new);

    public PositionPacket(BlockPos pos, PositionHorizontal horizontal, PositionVertical vertical) {
        this(pos, horizontal == null ? -1 : horizontal.ordinal(), vertical == null ? -1 : vertical.ordinal());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        if (horizontal > -1 && horizontal < PositionHorizontal.VALUES.length) {
            tile.data.setWidth(PositionHorizontal.VALUES[horizontal], tile.data.getWidth());
        }
        if (vertical > -1 && vertical < PositionVertical.VALUES.length) {
            tile.data.setHeight(PositionVertical.VALUES[vertical], tile.data.getHeight());
        }
    }
}
