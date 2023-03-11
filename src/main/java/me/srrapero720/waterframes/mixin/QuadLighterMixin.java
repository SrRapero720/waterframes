package me.srrapero720.waterframes.mixin;

import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VertexLighterFlat.class, remap = false)
public abstract class QuadLighterMixin extends QuadGatheringTransformer {
    @Unique public int customTint = -1;
    @Shadow private int tint;

    @Inject(method = "updateColor([F[FFFFFI)V", at = @At(value = "HEAD"), remap = false, cancellable = true)
    public void mixColor(float[] normal, float[] color, float x, float y, float z, float tint, int multiplier, CallbackInfo info) {
        if (customTint != -1) {
            this.tint = (int) tint;
            color[0] = ((customTint >> 16) & 0xFF) / 255F;
            color[1] = ((customTint >> 8) & 0xFF) / 255F;
            color[2] = (customTint & 0xFF) / 255F;
            info.cancel();
        }
    }
}
