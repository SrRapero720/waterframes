package me.srrapero720.waterframes.mixin.impl.creativecore;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.compat.creativecore.IScalableText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.controls.simple.GuiSlider;

@Mixin(GuiSlider.class)
public class GuiSliderMixin implements IScalableText {
    @Unique
    float wf$scale = 1.0f;

    @Redirect(method = "renderContent", at = @At(value = "INVOKE", target = "Lteam/creative/creativecore/client/render/GuiRenderHelper;drawStringCentered(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/lang/String;FFIZ)V"), remap = false)
    @Environment(EnvType.CLIENT)
    public void redirect$render(PoseStack pose, String text, float width, float height, int color, boolean shadow) {
        pose.pushPose();
        pose.scale(wf$scale, wf$scale, wf$scale);
        // this only works on negative scaling
        GuiRenderHelper.drawStringCentered(pose, text, width, wf$scale < 1.0f ? height + (wf$scale * 2) : height, color, shadow);
        pose.popPose();
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
