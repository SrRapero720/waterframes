package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

public class VolumePacket extends DisplayPacket {
    public int min;
    public int max;
    public int volume;

    public VolumePacket() { super(); }
    public VolumePacket(BlockPos pos, int min, int max, int volume) {
        super(pos);
        this.min = min;
        this.max = max;
        this.volume = volume;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        DisplaysNet.sendVolumeClient(this, player.level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {}

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.volume = DisplayConfig.maxVolume(this.volume);
        tile.data.maxVolumeDistance = DisplayConfig.maxVolumeDistance(this.max);
        tile.data.minVolumeDistance = Math.min(this.max, this.min);
    }
}
