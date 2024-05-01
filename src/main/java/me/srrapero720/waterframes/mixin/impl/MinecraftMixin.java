package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.client.display.DisplayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow private volatile boolean pause;

    @Shadow @Nullable public ClientLevel level;

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    public void injectRunTick(boolean pRenderLevel, CallbackInfo ci) {
        DisplayList.onClientPause(pause);
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;", shift = At.Shift.AFTER, opcode = Opcodes.GETFIELD))
    public void clearlevel(Screen screen, CallbackInfo ci) {
        if (level != null) {
            DisplayList.onUnloadingLevel(level);
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void setLevel(ClientLevel levelClient, CallbackInfo ci) {
        if (level != null) {
            DisplayList.onUnloadingLevel(level);
        }
    }
}