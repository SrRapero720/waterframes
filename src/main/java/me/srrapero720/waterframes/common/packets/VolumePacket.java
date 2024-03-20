package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

public class VolumePacket extends CreativePacket {

    public BlockPos pos;
    public short min;
    public short max;
    public short volume;

    public VolumePacket() {}

    public VolumePacket(BlockPos pos, short min, short max, short volume) {
        this.pos = pos;
        this.min = min;
        this.max = max;
        this.volume = volume;
    }
    
    @Override
    public void executeClient(Player player) {}
    
    @Override
    public void executeServer(ServerPlayer player) {}

    @Override
    public void execute(Player player) {
        BlockEntity be = player.level.getBlockEntity(pos);
        if (be instanceof DisplayTile tile) {
            tile.data.volume = DisplayConfig.maxVolume(this.volume);
            tile.data.maxVolumeDistance = DisplayConfig.maxVolumeDistance(this.max);
            tile.data.minVolumeDistance = Math.min(this.max, this.min);
        }

        super.execute(player);
    }
}