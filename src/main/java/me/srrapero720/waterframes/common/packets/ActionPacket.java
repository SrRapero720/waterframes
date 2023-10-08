package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

public class ActionPacket extends CreativePacket {
    
    public BlockPos pos;
    public boolean playing;
    public int tick;
    
    public ActionPacket() {}
    
    public ActionPacket(BlockPos pos, boolean playing, int tick) {
        this.pos = pos;
        this.playing = playing;
        this.tick = tick;
    }
    
    @Override
    public void executeClient(Player player) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof DisplayTile<?> tile) {
            tile.data.playing = playing;
            tile.data.tick = tick;
            if (tile.display != null) {
                if (playing)
                    tile.display.resume();
                else
                    tile.display.pause();
            }
        }
    }
    
    @Override
    public void executeServer(ServerPlayer player) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof DisplayTile<?> tile) {
            tile.data.tick = tick;
            tile.data.playing = playing;
        }
    }

    @Override
    public void execute(Player player) { super.execute(player); }
}