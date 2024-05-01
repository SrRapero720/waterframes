package me.srrapero720.waterframes.mixin.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.srrapero720.waterframes.common.block.DisplayBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedStoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", at = @At("RETURN"), cancellable = true)
    private static void inject$canConnect(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;

        if (state.getBlock() instanceof DisplayBlock display) {
            cir.setReturnValue(display.canConnectRedstone(state, null, null, direction));
        }
    }
}
