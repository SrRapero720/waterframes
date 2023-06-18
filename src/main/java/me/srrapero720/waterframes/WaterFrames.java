package me.srrapero720.waterframes;

import me.srrapero720.waterframes.display.MediaDisplay;
import me.srrapero720.waterframes.display.texture.TextureCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.versions.forge.ForgeVersion;
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

    @Mod.EventBusSubscriber(modid = ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        @SubscribeEvent
        public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) TextureCache.tick();
        }

        @SubscribeEvent
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) MediaDisplay.tick();
        }

        @SubscribeEvent
        public static void onUnloadingLevel(WorldEvent.Unload unload) {
            if (unload.getWorld() != null && unload.getWorld().isClientSide()) {
                TextureCache.unload();
                MediaDisplay.unload();
            }
        }
    }
}