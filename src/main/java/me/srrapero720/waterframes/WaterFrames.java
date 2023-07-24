package me.srrapero720.waterframes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final String VERSION = ModList.get().getModFileById(ID).versionString();
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.2", LogManager.getLogger(ID), new ResourceLocation(ID, "main"));

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() { WFRegistry.register(); }
}