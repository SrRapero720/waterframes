package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WaterFrames.ID)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final ResourceLocation LOADING_ANIMATION = WaterFrames.genId("loading_animation");
    public static final long SYNC_TIME = 1000L;
    private static long ticks = 0;

    // BOOTSTRAP
    public WaterFrames() {
        WFConfig.init();
        WFRegistry.init(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static ResourceLocation genId(String id) {
        return new ResourceLocation(ID, id);
    }

    public static ResourceLocation genId(int texture) {
        return new ResourceLocation(ID, "texture_" + texture);
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

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() {
        return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getFrameTime();
    }

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        if (++ticks == Long.MAX_VALUE) ticks = 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static long getTicks() {
        return ticks;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) tick();
    }
}