package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Unique private static long wf$lastMillisTime = 0;
    @Unique private static long wf$timeStack = 0;

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J", ordinal = 0))
    public long redirect$runServer$getMillis() {
        return wf$lastMillisTime = Util.getMillis();
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J", ordinal = 1))
    public long redirect$runServer$getMillisWhile() {
        long millis = Util.getMillis();
        long time = millis - wf$lastMillisTime;
        if (time > 50) // 50ms is 1 tick
            wf$timeStack += time;

        if (wf$timeStack > WaterFrames.SYNC_TIME) {
            DisplayTile.setLagTickTime(wf$timeStack);
            LOGGER.warn("Server seems overloading, jumping {}ms or {} ticks", wf$timeStack, wf$timeStack / 50L);
            wf$timeStack -= WaterFrames.SYNC_TIME;
        }

        return wf$lastMillisTime = Util.getMillis();
    }
}