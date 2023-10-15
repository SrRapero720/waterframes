package me.srrapero720.waterframes.common.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import team.creative.creativecore.common.network.CreativePacket;

import java.util.List;

public class SyncUrlListPacket extends CreativePacket {

    BlockPos pos;
    List<String> url_list;
    int index;

    public SyncUrlListPacket() {

    }

    public SyncUrlListPacket(BlockPos pos, List<String> list, int index) {
        this.pos = pos;
        this.url_list = list;
        this.index = index;
    }
    @Override
    public void executeClient(Player player) {

    }

    @Override
    public void executeServer(ServerPlayer serverPlayer) {
        BlockEntity be = serverPlayer.getLevel().getBlockEntity(pos);
    }
}