package me.srrapero720.waterframes.mixin;

import me.srrapero720.waterframes.custom.displayers.texture.TextureCache;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {
    @OnlyIn(Dist.CLIENT)
    @Inject(method = "runTick", at = @At("HEAD"))
    public void injectRunTick(boolean pRenderLevel, CallbackInfo ci) {
        TextureCache.renderInternal();
        TextureCache.tick();
    }
}
