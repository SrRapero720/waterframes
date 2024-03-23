package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MutedPacket extends DisplayPacket {
    public boolean muted;

    public MutedPacket() {}
    public MutedPacket(BlockPos pos, boolean muted) {
        super(pos);
        this.muted = muted;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        DisplaysNet.sendMutedClient(this, level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {
        if (tile.display != null) tile.display.setMuteMode(this.muted);
    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.muted = this.muted;
    }
}
