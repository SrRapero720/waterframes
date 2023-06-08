package me.srrapero720.waterframes.mixin;

import me.srrapero720.waterframes.display.texture.TextureData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;close()V"))
    public void injectStopServer(ServerLevel instance) throws IOException {
        if (instance.isClientSide) TextureData.unload();
        instance.close();
    }
}