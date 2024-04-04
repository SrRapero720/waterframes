package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.WFNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PauseModePacket extends DisplayPacket {
    public boolean paused;
    public int tick;

    public PauseModePacket(){ super(); }
    public PauseModePacket(BlockPos pos, boolean paused, int tickTime) {
        super(pos);
        this.paused = paused;
        this.tick = tickTime;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        WFNetwork.sendPlaybackClient(this, player.level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {
        if (tile.display != null) { // TODO: this is redundant, but i have no time to debug this
            tile.display.setPauseMode(this.paused);
        }
    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.paused = this.paused;
        if (this.tick != -1) tile.data.tick = this.tick;
    }
}
