package me.srrapero720.waterframes.mixin.impl.videoplayer;

import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TVBlockEntity.class)
public abstract class VideoPlayerMixin {

    @Shadow(remap = false) public abstract String getUrl();

    @Inject(method = "requestDisplay", at = @At(value = "INVOKE", target = "Lcom/github/NGoedix/watchvideo/block/entity/custom/TVBlockEntity;getUrl()Ljava/lang/String;", shift = At.Shift.AFTER), cancellable = true, remap = false)
    private void request$inject(CallbackInfoReturnable<IDisplay> cir) {
        if (getUrl().isEmpty()) cir.setReturnValue(null);
    }
}
