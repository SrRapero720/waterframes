package me.srrapero720.waterframes.core.tools;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ModTool {
    // IS LOADED SOMETHING
    public static boolean isModFMLoading(String id) { return FMLLoader.getLoadingModList().getModFileById(id) != null; }

    public static boolean isModLoaded(String id) { return ModList.get().isLoaded(id); }

    public static boolean isPackageLoaded(String id) { return Thread.currentThread().getContextClassLoader().getDefinedPackage(id) != null; }
}