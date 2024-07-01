package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;nextTickTime:J", opcode = Opcodes.PUTFIELD, ordinal = 2), locals = LocalCapture.CAPTURE_FAILHARD)
    public void inject$runServer(CallbackInfo ci, long time) {
        if (time > 1000L) {
            DisplayTile.setLagTickTime(time);
        }
    }
}
