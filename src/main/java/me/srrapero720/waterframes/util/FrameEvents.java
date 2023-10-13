package me.srrapero720.waterframes.util;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.client.display.DisplayControl;
import me.srrapero720.waterframes.client.rendering.FrameRender;
import me.srrapero720.waterframes.client.rendering.ProjectorRender;
import me.srrapero720.waterframes.client.rendering.TvRender;
import me.srrapero720.waterframes.util.events.PauseUpdateEvent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class FrameEvents {
    public static void init(IEventBus bus) {
        bus.addListener(Client::init);
        bus.addListener(Common::init);
    }

    @Mod.EventBusSubscriber(value = { Dist.CLIENT, Dist.DEDICATED_SERVER }, modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    private static class Common {
        private static void init(FMLCommonSetupEvent event) { common(); }
        private static void common() { FrameNet.register(); }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    @OnlyIn(Dist.CLIENT)
    private static class Client {
        private static void init(FMLClientSetupEvent event) { client(); }

        @OnlyIn(Dist.CLIENT)
        private static void client() {
            BlockEntityRenderers.register(FrameRegistry.TILE_FRAME.get(), (x) -> new FrameRender());
            BlockEntityRenderers.register(FrameRegistry.TILE_PROJECTOR.get(), (x) -> new ProjectorRender());
            BlockEntityRenderers.register(FrameRegistry.TILE_TV.get(), (x) -> new TvRender());
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onUnloadingLevel(WorldEvent.Unload unload) {
            if (unload.getWorld() != null && unload.getWorld().isClientSide()) DisplayControl.release();
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) DisplayControl.tick();
        }

        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onPause(PauseUpdateEvent event) {
            if (event.isPaused()) DisplayControl.pause();
        }
    }
}