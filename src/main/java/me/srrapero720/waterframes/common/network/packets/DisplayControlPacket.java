package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public abstract class DisplayControlPacket extends CreativePacket {

    public BlockPos pos; // display pos
    public boolean bounce; // should bounce back
    public DisplayControlPacket() {}
    public DisplayControlPacket(BlockPos pos, boolean bounce) {
        this.pos = pos;
        this.bounce = bounce;
    }

    public abstract void execServer(DisplayTile tile);
    public abstract void execClient(DisplayTile tile);
    public abstract void exec(DisplayTile tile);

    public void execute(DisplayTile tile, boolean isClientSide) {
        this.exec(tile);
        if (isClientSide) {
            this.execClient(tile);
        } else {
            this.execServer(tile);
            if (this.bounce) {
                this.bounce = false;
                DisplayNetwork.sendClient(this, tile);
            }
        }
    }

    @Override
    public void execute(Player player) {
        if (player.level.getBlockEntity(pos) instanceof DisplayTile tile) {
            this.execute(tile, player.level.isClientSide);
        } else {
            LOGGER.error("Received packet pointing to the invalid DisplayTile position {}", pos);
        }
    }

    @Override
    @Deprecated
    public void executeClient(Player player) {
        this.execute(player);
    }

    @Override
    @Deprecated
    public void executeServer(ServerPlayer player) {
        this.execute(player);
    }
}
