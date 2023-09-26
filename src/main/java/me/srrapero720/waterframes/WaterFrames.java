package me.srrapero720.waterframes;

import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.core.WaterEvents;
import me.srrapero720.waterframes.core.WaterRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    // TOOLS
    public static final String ID = "waterframes";
    public static final String VERSION = ModList.get().getModFileById(ID).versionString();
    public static final Logger LOGGER = LogManager.getLogger(ID);
    private static final Marker IT = MarkerFactory.getMarker("Bootstrap");

    // BOOTSTRAP
    public WaterFrames() {
        WaterEvents.init(bus());
        WaterConfig.init(bus());
        WaterRegistry.init(bus());
    }

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
}