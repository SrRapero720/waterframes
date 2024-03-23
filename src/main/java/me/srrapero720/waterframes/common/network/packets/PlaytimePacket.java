package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativePacket;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class PlaytimePacket extends DisplayPacket {
    public int tick;
    public int tickMax;

    public PlaytimePacket(){ super(); }
    public PlaytimePacket(BlockPos pos, int tick, int tickMax) {
        super(pos);
        this.tick = tick;
        this.tickMax = tickMax;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        DisplaysNet.sendPlaytimeClient(this, player.level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {

    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        if (tile.data.url.isEmpty()) {
            tile.data.tickMax = -1;
            tile.data.tick = 0;
        } else {
            tile.data.tick = this.tick;
            final boolean maxNegative = tile.data.tickMax == -1;
            if (maxNegative) {
                tile.data.tick = 0;
            }

            if (tile.data.tickMax < this.tickMax) {
                tile.data.tickMax = this.tickMax;
                if (!maxNegative) LOGGER.warn("Received maxTick value major than current one, media differs?.");
            }
        }
    }
}
