package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class TickPacket extends CreativePacket {

    public BlockPos pos;
    public int tickMax;
    public TickPacket() {}
    public TickPacket(BlockPos pos, int tickMax) {
        this.pos = pos;
        this.tickMax = tickMax;
    }

    @Override
    public void executeClient(Player player) {}

    @Override
    public void executeServer(ServerPlayer serverPlayer) {
        LOGGER.debug("Received maxTick packet for {} with value {}", pos, tickMax);
        BlockEntity be = serverPlayer.getLevel().getBlockEntity(pos);
        if (be instanceof DisplayTile<?> block) {
            boolean tickMaxNegative = block.data.tickMax == -1;
            if (tickMaxNegative) {
                block.data.tick = 0;
            }
            if (block.data.tickMax < tickMax) {
                block.data.tickMax = tickMax;
                if (!tickMaxNegative) LOGGER.warn("Received maxTick value major than current one, media differs?.");
            }
        }
    }
}