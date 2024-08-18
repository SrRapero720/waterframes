package me.srrapero720.waterframes.mixin.impl.creativecore;

import me.srrapero720.waterframes.common.compat.creativecore.IScalableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;

@Mixin(GuiLabel.class)
public abstract class GuiLabelMixin extends GuiControl implements IScalableText {

    @Shadow(remap = false) protected CompiledText text;

    public GuiLabelMixin(String name) { super(name); }

    @Override
    public void wf$setScale(float scale) {
        if (text instanceof IScalableText IScalableText) {
            IScalableText.wf$setScale(scale);
        }
    }

    @Override
    public float wf$getScale() {
        if (text instanceof IScalableText IScalableText) {
            return IScalableText.wf$getScale();
        }
        return 0;
    }
}
