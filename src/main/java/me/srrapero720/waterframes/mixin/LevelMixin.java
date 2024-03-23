package me.srrapero720.waterframes.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public class LevelMixin {

    // TODO: remove before official release.
    @Redirect(method = "blockEntityChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;updateNeighbourForOutputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V"))
    public void redirect$noTwiceCall(Level instance, BlockPos blockpos, Block direction) {
        // NO-OP
    }
}
