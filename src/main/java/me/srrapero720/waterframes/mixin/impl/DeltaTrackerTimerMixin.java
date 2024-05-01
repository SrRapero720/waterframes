package me.srrapero720.waterframes.mixin.impl;

import me.srrapero720.waterframes.client.display.DisplayList;
import net.minecraft.client.DeltaTracker;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerMixin {
    @Inject(method = "pause", at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;pausedDeltaTickResidual:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    void pause(CallbackInfo ci) {
        DisplayList.pause();
    }

    @Inject(method = "unPause", at = @At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;deltaTickResidual:F", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    void resume(CallbackInfo ci) {
        DisplayList.resume();
    }
}
