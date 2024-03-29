package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.network.CreativePacket;

public abstract class DisplayPacket extends CreativePacket {
    public BlockPos pos;

    public DisplayPacket() {}
    public DisplayPacket(BlockPos pos) {
        this.pos = pos;
    }

    public abstract void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level);
    public abstract void executeClient(DisplayTile tile, Player player, Level level);
    public abstract void execute(DisplayTile tile, Player player, Level level);

    @Override
    public void execute(Player player) {
        if (player.getLevel().getBlockEntity(pos) instanceof DisplayTile tile) {
            this.execute(tile, player, player.level);
            if (player.level.isClientSide) {
                this.executeClient(tile, player, player.level);
            } else {
                this.executeServer(tile, (ServerPlayer) player, (ServerLevel) player.level);
            }
        }
    }

    @Override public void executeClient(Player player) {
        throw new UnsupportedOperationException("NO-OP");
    }
    @Override public void executeServer(ServerPlayer serverPlayer) {
        throw new UnsupportedOperationException("NO-OP");
    }
}
