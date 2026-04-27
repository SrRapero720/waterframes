package me.srrapero720.waterframes;

import me.srrapero720.waterframes.client.display.DisplayList;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.compat.sable.SableCompat;
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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod(WaterFrames.ID)
@EventBusSubscriber(value = Dist.CLIENT, modid = WaterFrames.ID)
public class WaterFrames {
    static final Marker IT = MarkerManager.getMarker(WaterFrames.class.getName());
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final ResourceLocation LOADING_ANIMATION = WaterFrames.asResource("loading_animation");
    public static final long SYNC_TIME = 5000L;
    private static int ticks = 0;

    // BOOTSTRAP
    public WaterFrames(IEventBus bus, ModContainer container) {
        DisplaysConfig.init(bus, container);
        DisplaysRegistry.init(bus, container);
    }

    public static ResourceLocation asResource(String id) {
        return Objects.requireNonNull(ResourceLocation.tryBuild(ID, id));
    }

    public static ResourceLocation asResource(int texture) {
        return Objects.requireNonNull(ResourceLocation.tryBuild(ID, "dynamic_texture_" + texture));
    }

    public static boolean isInstalled(String modId) {
        return FMLLoader.getLoadingModList().getModFileById(modId) != null;
    }

    /**
     * Checks if a URL string is valid for use with MRL.
     * @param url the URL string to validate
     * @return true if valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) return false;

        // Local file check
        File f = new File(url);
        if (!f.isDirectory() && f.exists()) return true;

        // Basic URL validation - let MRL handle detailed validation
        return url.contains("://") || url.startsWith("water://");
    }

    /**
     * Composes a list of URLs into a newline-delimited string for NBT storage.
     * @param urls list of URL strings
     * @return newline-delimited string
     */
    public static String composeUrlList(List<String> urls) {
        if (urls == null || urls.isEmpty()) return "";
        return String.join("\n", urls);
    }

    /**
     * Decomposes a newline-delimited URL string into a list.
     * @param str the newline-delimited string
     * @return list of URL strings (never null)
     */
    public static List<String> decomposeUrlList(String str) {
        List<String> urls = new ArrayList<>();
        if (str == null || str.isEmpty()) return urls;

        String[] split = str.split("\n");
        for (String url : split) {
            String trimmed = url.trim();
            if (!trimmed.isEmpty()) {
                urls.add(trimmed);
            }
        }
        return urls;
    }

    public static boolean isInstalled(String... mods) {
        for (String id: mods) {
            if (FMLLoader.getLoadingModList().getModFileById(id) != null) {
                return true;
            }
        }
        return false;
    }

    public static double getDistance(DisplayTile tile, Position playerPos) {
        return getDistance(tile.level, tile.getBlockPos(), playerPos);
    }

    public static double getDistance(Level level, BlockPos pos, Position position) {
        if (SableCompat.installed() && DisplaysConfig.sableCompat()) {
            return Math.sqrt(SableCompat.getSquaredDistance(level, pos, position));
        }
        if (VSCompat.installed() && DisplaysConfig.vsEurekaCompat()) {
            return Math.sqrt(VSCompat.getSquaredDistance(level, pos, position));
        }
        return Math.sqrt(pos.distToLowCornerSqr(position.x(), position.y(), position.z()));
    }

    @OnlyIn(Dist.CLIENT)
    public static float deltaFrames() { return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false); }

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        if (++ticks == Integer.MAX_VALUE) ticks = 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getTicks() {
        return ticks;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onClientTickEvent(ClientTickEvent.Post event) {
        ticks++;
    }
}