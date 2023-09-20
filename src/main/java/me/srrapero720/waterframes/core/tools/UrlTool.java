package me.srrapero720.waterframes.core.tools;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class UrlTool {
    public static boolean url_isValid(String url) {
        try { new URL(url); return true; } catch (Exception ignored) {}
        return false;
    }

//    @Deprecated
    @OnlyIn(Dist.CLIENT)
    public static @NotNull String fixUrl(@NotNull String url) {
        return url.replace("minecraft://",("file:///" + FMLPaths.GAMEDIR.get().toAbsolutePath()).replace("\\", "/") + "/");
    }
}