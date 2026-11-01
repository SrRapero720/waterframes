package me.srrapero720.waterframes.common.network.packets;

import io.netty.buffer.ByteBuf;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.ControlPacket;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.watermedia.api.player.MediaPlayer;
import org.watermedia.api.player.ServerMediaPlayer;

/** Playback position of a display: an absolute time, or a jump relative to where the clock stands. */
public record SeekPacket(BlockPos pos, long time, boolean relative) implements ControlPacket {
    public static final Type<SeekPacket> TYPE = DisplayNetwork.type("seek");
    public static final StreamCodec<ByteBuf, SeekPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SeekPacket::pos,
            ByteBufCodecs.VAR_LONG, SeekPacket::time,
            ByteBufCodecs.BOOL, SeekPacket::relative,
            SeekPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // THE CLOCK MOVES AND ITS NEXT SNAPSHOT CARRIES THE NEW POSITION TO EVERY VIEWER
    @Override
    public void apply(DisplayTile tile) {
        ServerMediaPlayer clock = tile.clock();
        if (clock == null) return;
        if (relative) clock.skipTime(time);
        else clock.seek(time);
    }

    // THE FOLLOWER JUMPS AHEAD OF THE SNAPSHOT SO THE PICTURE ANSWERS THE CLICK AT ONCE
    @Override
    public void applyClient(DisplayTile tile) {
        MediaPlayer follower = tile.follower();
        if (follower == null) return;
        follower.seekQuick(Math.max(0, relative ? follower.time() + time : time));
    }
}
