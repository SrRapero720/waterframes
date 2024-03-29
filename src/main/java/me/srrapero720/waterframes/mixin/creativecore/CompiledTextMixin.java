package me.srrapero720.waterframes.mixin.creativecore;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.helpers.ScalableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.client.render.text.CompiledText;

@Mixin(CompiledText.class)
public class CompiledTextMixin implements ScalableText {
    @Unique
    float wf$scale = 1.0f;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
    public void render(PoseStack stack, CallbackInfo ci) {
        stack.scale(wf$scale, wf$scale, wf$scale);
    }

    @Override
    public ScalableText wf$setScale(float scale) {
        wf$scale = scale;
        return this;
    }

    @Override
    public float wf$getScale() {
        return wf$scale;
    }
}
