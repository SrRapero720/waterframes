package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.WFNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

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
        WFNetwork.sendVolumeClient(this, player.level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {}

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.volume = WFConfig.maxVol(this.volume);
        tile.data.maxVolumeDistance = WFConfig.maxVolDis(this.max);
        tile.data.minVolumeDistance = Math.min(this.max, this.min);
    }
}
