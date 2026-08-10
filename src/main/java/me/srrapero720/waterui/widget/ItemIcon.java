package me.srrapero720.waterui.widget;

import com.mojang.blaze3d.platform.Lighting;
import me.srrapero720.waterui.core.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * An item stack drawn as a plain icon: no frame, no fill, no stack decorations, centred and
 * scaled to whatever box it is given.
 */
public class ItemIcon extends Element {
    /** Side of a vanilla item render, the size every stack is authored at. */
    protected static final int ITEM_SIZE = 16;
    private static final int LIGHT_FULL = 15728880;

    private ItemStack stack;
    private boolean flipX;

    public ItemIcon() {
        this(ItemStack.EMPTY);
    }

    public ItemIcon(ItemStack stack) {
        this.stack = stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * Faces the model the other way. Block models take the mirrored GUI angles, the way the
     * left hand mirrors held items; flat sprites carry no angles and stay as they are.
     */
    public ItemIcon flipX() {
        this.flipX = true;
        return this;
    }

    public ItemIcon stack(ItemStack stack) {
        this.stack = stack != null ? stack : ItemStack.EMPTY;
        return this;
    }

    public ItemStack stack() {
        return stack;
    }

    @Override
    protected int prefContentWidth(int available) {
        return ITEM_SIZE;
    }

    @Override
    protected int prefContentHeight(int width, int available) {
        return ITEM_SIZE;
    }

    @Override
    protected void draw(GuiGraphics graphics, int mouseX, int mouseY, float partial) {
        if (stack.isEmpty()) return;

        // VANILLA RENDERS STACKS AT A FIXED 16px, SO FITTING THE BOX MEANS SCALING THE POSE
        float scale = Math.min(contentWidth(), contentHeight()) / (float) ITEM_SIZE;
        if (scale <= 0) return;
        float x = contentX() + (contentWidth() - ITEM_SIZE * scale) / 2f;
        float y = contentY() + (contentHeight() - ITEM_SIZE * scale) / 2f;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0f);
        graphics.pose().scale(scale, scale, 1f);
        if (flipX) this.renderMirrored(graphics);
        else graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    // VANILLA'S OWN GUI ITEM SETUP, ASKING FOR THE LEFT HAND: THE TRANSFORM ANGLES MIRROR
    // INSIDE MODEL SPACE, SO NO NEGATIVE SCALE EVER TOUCHES THE WINDING OR THE TEXTURES
    private void renderMirrored(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        var renderer = minecraft.getItemRenderer();
        var model = renderer.getModel(stack, null, null, 0);

        graphics.pose().pushPose();
        graphics.pose().translate(8f, 8f, 150f);
        graphics.pose().scale(16f, -16f, 16f);

        boolean flat = !model.usesBlockLight();
        if (flat) Lighting.setupForFlatItems();
        renderer.render(stack, ItemDisplayContext.GUI, true, graphics.pose(),
                minecraft.renderBuffers().bufferSource(), LIGHT_FULL, OverlayTexture.NO_OVERLAY, model);
        graphics.flush();
        if (flat) Lighting.setupFor3DItems();
        graphics.pose().popPose();
    }
}
