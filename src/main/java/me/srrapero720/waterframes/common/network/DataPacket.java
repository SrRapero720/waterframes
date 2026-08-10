package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

/**
 * Screen submission travelling client to server only. The sender is carried through so the
 * server can run the permission and whitelist checks before writing anything.
 */
public interface DataPacket extends CustomPacketPayload {

    BlockPos pos();

    void applyServer(DisplayTile tile, ServerPlayer player);
}
