package me.srrapero720.waterframes.mixin;

import me.srrapero720.watermedia.WaterMedia;
import me.srrapero720.watermedia.vlc.VLCManager;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(value = WaterMedia.class, remap = false)
public class WM_INDEV {

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lme/srrapero720/watermedia/vlc/VLCManager;init(Ljava/nio/file/Path;Z)Z"))
    private static boolean redirectLoad(Path gameDir, boolean devMode) {
        if (!FMLEnvironment.production) VLCManager.init(gameDir, false);
        return false;
    }
}
