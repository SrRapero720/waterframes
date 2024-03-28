package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WaterFrames.ID)
public class WaterFrames {
    // TOOLS
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final String PREFIX = "§6§l[§r§bWATERF§3r§bAMES§6§l]: §r";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    private static int ticks = 0;

    // BOOTSTRAP
    public WaterFrames() {
        DisplayConfig.init();
        WFRegistry.init(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static boolean isInstalled(String modId) {
        ModList list;
        return (list = ModList.get()) != null ? list.isLoaded(modId) : FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static void tick() {
        if (++ticks == Integer.MAX_VALUE) ticks = 0;
    }

    public static int getTicks() { return ticks; }

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getFrameTime(); }
}