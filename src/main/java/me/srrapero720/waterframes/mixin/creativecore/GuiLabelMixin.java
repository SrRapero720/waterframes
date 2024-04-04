package me.srrapero720.waterframes.mixin.creativecore;

import me.srrapero720.waterframes.common.helpers.ScalableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import team.creative.creativecore.client.render.text.CompiledText;
import team.creative.creativecore.common.gui.GuiControl;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;

@Mixin(GuiLabel.class)
public abstract class GuiLabelMixin extends GuiControl implements ScalableText {

    @Shadow(remap = false) protected CompiledText text;

    public GuiLabelMixin(String name) { super(name); }

    @Override
    public ScalableText wf$setScale(float scale) {
        if (text instanceof ScalableText scalableText) {
            scalableText.wf$setScale(scale);
        }
        return this;
    }

    @Override
    public float wf$getScale() {
        if (text instanceof ScalableText scalableText) {
            return scalableText.wf$getScale();
        }
        return 0;
    }
}
