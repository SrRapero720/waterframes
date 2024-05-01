package me.srrapero720.waterframes.mixin.impl.creativecore;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import team.creative.creativecore.client.render.GuiRenderHelper;

@Mixin(GuiRenderHelper.class)
public class GuiRenderHelperMixin {

    @Redirect(method = {"lambda$drawTextureRect$6", "lambda$drawTextureRect$7"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setUv(FF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), remap = false)
    private static VertexConsumer drawTextureRect(VertexConsumer instance, float v, float v2) {
        return instance.setUv(v, v2).setColor(-1);
    }
}
