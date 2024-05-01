package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public abstract class DisplayDataPacket extends CreativePacket {
    public BlockPos pos; // display pos
    public DisplayDataPacket() {}
    public DisplayDataPacket(BlockPos pos) {
        this.pos = pos;
    }

    public abstract void execServer(DisplayTile tile, ServerPlayer player);
    public abstract void execClient(DisplayTile tile, Player player);
    public abstract void exec(DisplayTile tile, Player player);

    @Override
    public void execute(Player player) {
        if (player.level.getBlockEntity(pos) instanceof DisplayTile tile) {
            this.exec(tile, player);
            if (player.level.isClientSide) {
                this.execClient(tile, player);
            } else {
                this.execServer(tile, (ServerPlayer) player);
            }
        }
    }

    @Override
    @Deprecated
    public void executeClient(Player player) {
        if (player.level.getBlockEntity(pos) instanceof DisplayTile tile) {
            this.execClient(tile, player);
        }
    }

    @Override
    @Deprecated
    public void executeServer(ServerPlayer player) {
        if (player.level.getBlockEntity(pos) instanceof DisplayTile tile) {
            this.execServer(tile, player);
        }
    }
}
