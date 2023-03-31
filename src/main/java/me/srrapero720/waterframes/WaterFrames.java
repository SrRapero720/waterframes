package me.srrapero720.waterframes;

import me.srrapero720.waterframes.custom.packets.WaterFramePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.1", LOGGER, new ResourceLocation(ID, "main"));

    public static LittleFramesConfig CONFIG;

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() {
        MinecraftForge.EVENT_BUS.register(this);

        LittleFramesRegistry.register(bus());

        WaterFrames.bus().addListener(WaterFrames::init);
        if (FMLEnvironment.dist.isClient()) WaterFrames.bus().addListener(LittleFramesClient::setup);
    }


    public static void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(ID, CONFIG = new LittleFramesConfig());
        NETWORK.registerType(WaterFramePacket.class, WaterFramePacket::new);
    }
}
