package me.srrapero720.waterframes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littleframes.LittleFramesConfig;
import me.srrapero720.waterframes.custom.packets.WaterFramePacket;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static LittleFramesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(ID, "main"));;
    public WaterFrames() {
        MinecraftForge.EVENT_BUS.register(this);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(WaterFrames::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleFramesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));

        LittleFramesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleFramesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }


    public static void init(final FMLCommonSetupEvent event) {
        CreativeConfigRegistry.ROOT.registerValue(ID, CONFIG = new LittleFramesConfig());

        NETWORK.registerType(WaterFramePacket.class, WaterFramePacket::new);
    }
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
    }
}
