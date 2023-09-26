package me.srrapero720.waterframes.custom.packets;

import me.srrapero720.waterframes.api.block.entity.BasicBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import team.creative.creativecore.common.network.CreativePacket;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class SyncTickPacket extends CreativePacket {

    public BlockPos pos;
    public int tickMax;
    public SyncTickPacket() {}
    public SyncTickPacket(BlockPos pos, int tickMax) {
        this.pos = pos;
        this.tickMax = tickMax;
    }

    @Override
    public void executeClient(Player player) {}

    @Override
    public void executeServer(ServerPlayer serverPlayer) {
        LOGGER.debug("Received maxTick packet for {} with value {}", pos, tickMax);
        BlockEntity be = serverPlayer.getLevel().getBlockEntity(pos);
        if (be instanceof BasicBlockEntity<?> block) {
            if (block.data.tickMax < tickMax) {
                block.data.tickMax = tickMax;
                LOGGER.warn("Received a new maxTick value with different duration.");
            }
        }
    }
}