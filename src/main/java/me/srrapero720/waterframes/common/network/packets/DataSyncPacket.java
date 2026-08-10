package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DataPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record DataSyncPacket(BlockPos pos, CompoundTag nbt) implements DataPacket {
    public static final Type<DataSyncPacket> TYPE = DisplayNetwork.type("data_sync");
    public static final StreamCodec<ByteBuf, DataSyncPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DataSyncPacket::pos,
            ByteBufCodecs.COMPOUND_TAG, DataSyncPacket::nbt,
            DataSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void applyServer(DisplayTile tile, ServerPlayer player) {
        DisplayData.sync(tile, player, nbt);
    }
}
