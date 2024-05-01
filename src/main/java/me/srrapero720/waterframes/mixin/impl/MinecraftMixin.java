package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.client.display.DisplayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
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
    @Shadow @Nullable public ClientLevel level;

    @Inject(method = "clearClientLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;", shift = At.Shift.BEFORE, opcode = Opcodes.PUTFIELD))
    public void clearlevel(Screen screen, CallbackInfo ci) {
        if (level != null) {
            DisplayList.onUnloadingLevel(level);
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    public void setLevel(ClientLevel level, ReceivingLevelScreen.Reason reason, CallbackInfo ci) {
        if (level != null) {
            DisplayList.onUnloadingLevel(level);
        }
    }
}