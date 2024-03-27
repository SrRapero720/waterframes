package me.srrapero720.waterframes;

import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WaterFrames.ID)
public class WaterFrames {
    // TOOLS
    public static final String ID = "waterframes";
    public static final String COMM_PREFIX = "§6§l[§r§bWATERF§3r§bAMES§6§l]: §r";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    // BOOTSTRAP
    public WaterFrames() {
        DisplayConfig.init();
        FrameRegistry.init(FMLJavaModLoadingContext.get().getModEventBus());
    }
}