package me.srrapero720.waterframes.mixin;

import me.srrapero720.waterframes.display.texture.TextureCache;
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

    @Inject(method = "tick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V"))
    public void injectTick(CallbackInfo ci) {
        TextureCache.tick();
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "runTick", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V"))
    public void injectRunTick(boolean pRenderLevel, CallbackInfo ci) {
        TextureCache.renderInternal();
    }
}
