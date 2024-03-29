package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.WFNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ActivePacket extends DisplayPacket {

    public boolean active;
    public ActivePacket() { super(); }
    public ActivePacket(BlockPos pos, boolean active) {
        super(pos);
        this.active = active;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        WFNetwork.sendActiveClient(this, level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {

    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.active = active;
    }
}
