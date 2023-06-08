package me.srrapero720.waterframes;

import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.rendering.VLCRendering;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.creative.creativecore.common.network.CreativeNetwork;

@Mod(WaterFrames.ID)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final Logger LOGGER = LoggerFactory.getLogger(ID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.2", LogManager.getLogger(ID), new ResourceLocation(ID, "main"));

    public static IEventBus bus() { return FMLJavaModLoadingContext.get().getModEventBus(); }
    public WaterFrames() { FramesRegistry.register(); }

    @Mod.EventBusSubscriber(modid = WaterFrames.ID)
    private static final class Events {

        @SubscribeEvent
        private static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) TextureData.tick();
        }

        @SubscribeEvent
        private static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) VLCRendering.tick();
        }

        @SubscribeEvent
        private static void onUnloadingLevel(WorldEvent.Unload unload) {
            if (unload.getWorld() != null && unload.getWorld().isClientSide()) {
                TextureData.unload();
                VLCRendering.unload();
            }
        }
    }
}