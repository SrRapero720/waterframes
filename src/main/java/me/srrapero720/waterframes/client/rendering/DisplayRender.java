package me.srrapero720.waterframes.client.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.client.display.TextureDisplay;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import org.lwjgl.opengl.GL11;

public class DisplayRender {


    public static void render(PoseStack pose, DisplayTile<?> block, BufferBuilder builder, Direction direction, float growingSize, boolean renderBackside) {
        TextureDisplay display = block.requestDisplay();
        if (display == null) return;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(block.data.brightness, block.data.brightness, block.data.brightness, block.data.alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);


        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
    }
}