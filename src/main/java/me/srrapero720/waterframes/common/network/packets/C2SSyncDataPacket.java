package me.srrapero720.waterframes.common.network.packets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;

public class C2SSyncDataPacket extends CreativePacket {

    private CompoundTag tag;
    private BlockPos pos;

    public C2SSyncDataPacket() {}
    public C2SSyncDataPacket(BlockPos pos, CompoundTag tag) {
        this.pos = pos;
        this.tag = tag;
    }

    @Override
    public void executeClient(Player player) {

    }

    @Override
    public void executeServer(ServerPlayer serverPlayer) {
        if (serverPlayer.level.getBlockEntity(pos) instanceof DisplayTile display) {
            display.sync(serverPlayer, tag);
        }
    }

    @Override
    public void execute(Player player) {
        super.execute(player);
    }
}
