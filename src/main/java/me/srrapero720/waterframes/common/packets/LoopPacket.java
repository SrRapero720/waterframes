package me.srrapero720.waterframes.common.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.WFNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LoopPacket extends DisplayPacket {

    public boolean loop;
    public LoopPacket(){ super(); }
    public LoopPacket(BlockPos pos, boolean loop) {
        super(pos);
        this.loop = loop;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        WFNetwork.sendLoopClient(this, level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {

    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.loop = this.loop;
    }
}
