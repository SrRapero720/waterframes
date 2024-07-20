package me.srrapero720.waterframes.mixin.impl.self;

import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(TextureDisplay.class)
@Pseudo
public class VSCompat {
    @Shadow(remap = false) @Final private DisplayTile tile;

    @Redirect(method = "rangedVol", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;distToCenterSqr(Lnet/minecraft/core/Position;)D"))
    private double redirect$rangedVol(BlockPos instance, Position position) {
        return VSGameUtilsKt.squaredDistanceBetweenInclShips(tile.level, instance.getX(), instance.getY(), instance.getZ(), position.x(), position.y(), position.z());
    }
}
