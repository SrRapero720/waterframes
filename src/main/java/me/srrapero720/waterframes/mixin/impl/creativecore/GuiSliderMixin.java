package me.srrapero720.waterframes.mixin.impl.creativecore;

import me.srrapero720.waterframes.common.compat.creativecore.IScalableText;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
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

    @Redirect(method = "renderContent", at = @At(value = "INVOKE", target = "Lteam/creative/creativecore/client/render/GuiRenderHelper;drawStringCentered(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/String;FFIZ)V"), remap = false)
    @Environment(EnvType.CLIENT)
    public void redirect$render(GuiGraphics graphics, String text, float width, float height, int color, boolean shadow) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.scale(wf$scale, wf$scale, wf$scale);
        // this only works on negative scaling
        GuiRenderHelper.drawStringCentered(graphics, text, width, wf$scale < 1.0f ? height + (wf$scale * 2) : height, color, shadow);
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
