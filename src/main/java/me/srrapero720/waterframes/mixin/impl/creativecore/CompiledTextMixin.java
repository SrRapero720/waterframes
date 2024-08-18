package me.srrapero720.waterframes.mixin.impl.creativecore;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.compat.creativecore.IScalableText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.creativecore.client.render.text.CompiledText;

@Mixin(CompiledText.class)
public class CompiledTextMixin implements IScalableText {
    @Unique
    float wf$scale = 1.0f;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 0, shift = At.Shift.AFTER))
    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, CallbackInfo ci) {
        pose.scale(wf$scale, wf$scale, wf$scale);
    }

    @Override
    public void wf$setScale(float scale) {
        wf$scale = scale;
    }

    @Override
    public float wf$getScale() {
        return wf$scale;
    }
}
