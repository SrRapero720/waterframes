package me.srrapero720.waterframes.mixin.creativecore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxFace;

@Mixin(value = BoxFace.class, remap = false)
public interface IBoxFaceAccessor {
    @Accessor
    Axis getOne();

    @Accessor
    Axis getTwo();

    @Accessor
    BoxCorner[] getCorners();

    @Accessor
    Facing getFacing();

    @Accessor(value = "texU")
    Facing texU();

    @Accessor(value = "texV")
    Facing texV();
}