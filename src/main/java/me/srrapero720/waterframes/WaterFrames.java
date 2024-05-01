package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WaterFrames.ID)
public class WaterFrames {
    // TOOLS
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final String PREFIX = "§6§l[§r§bWATERF§3r§bAMES§6§l]: §r";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static IEventBus bus;
    public static ModContainer modContainer;
    public static final long SYNC_TIME = 1000L;
    private static int SERVER_OP_LEVEL = -1;

    // BOOTSTRAP
    public WaterFrames(IEventBus bus, ModContainer container) {
        WFConfig.init(bus, container);
        WFRegistry.init(bus, container);
    }

    public static boolean isInstalled(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    public static boolean isInstalled(String... mods) {
        for (String id: mods) {
            if (FMLLoader.getLoadingModList().getModFileById(id) == null) {
                return false;
            }
        }
        return true;
    }

    public static int getServerOpPermissionLevel(Level level) {
        if (level != null && !level.isClientSide) {
            SERVER_OP_LEVEL = level.getServer().getOperatorUserPermissionLevel();
        }
        return SERVER_OP_LEVEL;
    }

    public static void setOpPermissionLevel(int level) {
        SERVER_OP_LEVEL = level;
    }

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false); }
}