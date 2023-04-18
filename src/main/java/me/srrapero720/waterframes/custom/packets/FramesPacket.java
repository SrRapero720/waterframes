package me.srrapero720.waterframes.custom.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;
import me.srrapero720.waterframes.custom.blocks.TileFrame;

public class FramesPacket extends CreativePacket {
    
    public BlockPos pos;
    public boolean playing;
    public int tick;
    
    public FramesPacket() {}
    
    public FramesPacket(BlockPos pos, boolean playing, int tick) {
        this.pos = pos;
        this.playing = playing;
        this.tick = tick;
    }
    
    @Override
    public void executeClient(Player player) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof TileFrame frame) {
            frame.playing = playing;
            frame.tick = tick;
            if (frame.display != null) {
                if (playing)
                    frame.display.resume(frame.getURL(), frame.volume, frame.minDistance, frame.maxDistance, frame.playing, frame.loop, frame.tick);
                else
                    frame.display.pause(frame.getURL(), frame.volume, frame.minDistance, frame.maxDistance, frame.playing, frame.loop, frame.tick);
            }
        }
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
