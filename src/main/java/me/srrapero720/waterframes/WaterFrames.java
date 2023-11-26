package me.srrapero720.waterframes;

import me.srrapero720.waterframes.util.FrameRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Mod(WaterFrames.ID)
public class WaterFrames {
    // TOOLS
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    // BOOTSTRAP
    public WaterFrames() {
        DisplayConfig.init();
        FrameRegistry.init(bus());
    }

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
}