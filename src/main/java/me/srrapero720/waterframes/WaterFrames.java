package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.display.DisplayList;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.compat.valkyrienskies.VSCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WaterFrames.ID)
@EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID, bus = EventBusSubscriber.Bus.GAME)
public class WaterFrames {
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final ResourceLocation LOADING_ANIMATION = WaterFrames.asResource("loading_animation");
    public static IEventBus bus;
    public static ModContainer modContainer;
    public static final long SYNC_TIME = 1000L;
    private static long ticks = 0;

    // BOOTSTRAP
    public WaterFrames(IEventBus bus, ModContainer container) {
        WFConfig.init(bus, container);
        WFRegistry.init(bus, container);
    }

    public static ResourceLocation asResource(String id) {
        return new ResourceLocation(ID, id);
    }

    public static ResourceLocation asResource(int texture) {
        return new ResourceLocation(ID, "dynamic_texture_" + texture);
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

    public static double getDistance(DisplayTile tile, Position playerPos) {
        return getDistance(tile.level, tile.getBlockPos(), playerPos);
    }

    public static double getDistance(Level level, BlockPos pos, Position position) {
        if (VSCompat.installed() && WFConfig.vsEurekaCompat()) {
            return Math.sqrt(VSCompat.getSquaredDistance(level, pos, position));
        }
        return Math.sqrt(pos.distToLowCornerSqr(position.x(), position.y(), position.z()));
    }

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false); }

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
    public static void onClientTickEvent(ClientTickEvent.Post event) {
        ticks++;
    }
}