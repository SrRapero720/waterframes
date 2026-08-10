package me.srrapero720.waterframes.common.network;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Playback command travelling in both directions. The server applies it and echoes it to
 * every player tracking the chunk, so all viewers converge on the same state.
 */
public interface ControlPacket extends CustomPacketPayload {

    BlockPos pos();

    /** State change shared by both sides. */
    void apply(DisplayTile tile);

    /** Client-only follow up on the live player, runs right after {@link #apply}. */
    default void applyClient(DisplayTile tile) {}
}
