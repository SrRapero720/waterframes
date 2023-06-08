package me.srrapero720.waterframes.mixin;

import me.srrapero720.waterframes.rendering.VLCRendering;
import me.srrapero720.waterframes.display.texture.TextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public ClientLevel level;

    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
    public void injectTick(CallbackInfo ci) {
        VLCRendering.tick();
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "runTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"))
    public void injectRunTick(boolean pRenderLevel, CallbackInfo ci) {
        TextureData.tick();
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/ClientPackSource;clearServerPack()V"))
    public void injectClearServerPack(Screen screen, CallbackInfo ci) {
        TextureData.unload();
        VLCRendering.unload();
    }

    @Inject(method = "setLevel", at = @At(value = "HEAD"))
    public void injectSetLevel(ClientLevel clientLevel, CallbackInfo ci) {
        TextureData.unload();
        VLCRendering.unload();
    }
}
