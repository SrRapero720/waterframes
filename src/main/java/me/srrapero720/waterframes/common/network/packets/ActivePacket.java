package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.gui.packet.LayerPacket;
import team.creative.creativecore.common.network.CreativePacket;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class ActivePacket extends DisplayPacket {

    public boolean active;
    public ActivePacket() { super(); }
    public ActivePacket(BlockPos pos, boolean active) {
        super(pos);
        this.active = active;
    }

    @Override
    public void executeServer(DisplayTile tile, ServerPlayer player, ServerLevel level) {
        DisplaysNet.sendActiveClient(this, level);
    }

    @Override
    public void executeClient(DisplayTile tile, Player player, Level level) {

    }

    @Override
    public void execute(DisplayTile tile, Player player, Level level) {
        tile.data.active = active;
    }
}
