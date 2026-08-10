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
import org.watermedia.api.media.players.ServerMediaPlayer;

/** Playback state of a display. A rewind is the stop button: it holds and goes back to zero. */
public record PausePacket(BlockPos pos, boolean paused, boolean rewind) implements ControlPacket {
    public static final Type<PausePacket> TYPE = DisplayNetwork.type("pause");
    public static final StreamCodec<ByteBuf, PausePacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PausePacket::pos,
            ByteBufCodecs.BOOL, PausePacket::paused,
            ByteBufCodecs.BOOL, PausePacket::rewind,
            PausePacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void apply(DisplayTile tile) {
        tile.data.paused = (DisplaysConfig.useMasterModeRedstone() && tile.isPowered()) || paused;

        // ORDINARY PAUSING IS RECONCILED BY THE TILE EVERY TICK; ONLY THE TWO ENDS NEED SAYING,
        // BECAUSE A STOPPED OR FINISHED CLOCK REFUSES TO BE RESUMED AND HAS TO BE STARTED OVER
        ServerMediaPlayer clock = tile.clock();
        if (clock == null) return;
        if (rewind) clock.stop();
        else if (!tile.data.paused && (clock.stopped() || clock.ended())) clock.start();
    }
}
