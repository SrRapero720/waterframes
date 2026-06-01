package me.srrapero720.waterframes.mixin.impl.watermedia;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.srrapero720.waterframes.WaterFrames;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.watermedia.api.render.RenderAPI;

@Mixin(RenderAPI.class)
public class WaterMediaMixin {
    @Redirect(method = {"uploadBuffer", "createTexture", "downloadBuffer", "bindTexture()V", "bindTexture(I)V"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
    private static void redirect$glBindTexture(int target, int texture) {
        if (target != GL11.GL_TEXTURE_2D) WaterFrames.LOGGER.warn("Received a non supported target for binding");
        RenderSystem.bindTexture(texture);
    }

    @Redirect(method = { "deleteTexture(I)V"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDeleteTextures(I)V"))
    private static void redirect$glDeleteTexture(int texture) {
        RenderSystem.deleteTexture(texture);
    }

    @Redirect(method = { "deleteTexture([I)V"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glDeleteTextures([I)V"))
    private static void redirect$glDeleteTexture(int[] textures) {
        GlStateManager._deleteTextures(textures);
    }

    @Redirect(method = {"uploadBuffer"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glPixelStorei(II)V"))
    private static void redirect$pixelStore(int name, int param) {
        RenderSystem.pixelStore(name, param);
    }

    @Redirect(method = {"createTexture",}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTexParameteri(III)V"))
    private static void redirect$texParameter(int target, int name, int param) {
        RenderSystem.texParameter(target, name, param);
    }
}
