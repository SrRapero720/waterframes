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

public record DataListSyncPacket(BlockPos pos, CompoundTag nbt) implements DataPacket {
    public static final Type<DataListSyncPacket> TYPE = DisplayNetwork.type("data_list_sync");
    public static final StreamCodec<ByteBuf, DataListSyncPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DataListSyncPacket::pos,
            ByteBufCodecs.COMPOUND_TAG, DataListSyncPacket::nbt,
            DataListSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void applyServer(DisplayTile tile, ServerPlayer player) {
        DisplayData.syncList(tile, player, nbt);
    }
}
