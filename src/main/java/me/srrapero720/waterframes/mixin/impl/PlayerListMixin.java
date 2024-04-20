package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.WFNetwork;
import me.srrapero720.waterframes.common.packets.PermissionLevelPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

    @Inject(method = "sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("RETURN"))
    private void wf$sendMaxServerPermissionLevel(ServerPlayer player, CallbackInfo ci) {
        WFNetwork.NET_DATA.sendToClient(new PermissionLevelPacket(this.server), player);
    }
}