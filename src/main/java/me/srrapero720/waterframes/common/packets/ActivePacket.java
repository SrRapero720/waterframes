package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

public class ActivePacket extends CreativePacket {

    public BlockPos pos;
    public boolean active;

    public ActivePacket() {}

    public ActivePacket(BlockPos pos, boolean active) {
        this.pos = pos;
        this.active = active;
    }
    
    @Override
    public void executeClient(Player player) {
        
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}

    @Override
    public void execute(Player player) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof DisplayTile<?> tile) {
            tile.data.active = active;
        }
        super.execute(player);
    }
}