package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import me.srrapero720.waterframes.common.compat.valkyrienskies.VSCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.Objects;

public class WaterFrames implements ModInitializer {
    // TOOLS
    public static final String ID = "waterframes";
    public static final String NAME = "WATERFrAMES";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    private static final Marker IT = MarkerManager.getMarker(ID);
    public static final ResourceLocation LOADING_ANIMATION = WaterFrames.asResource("loading_animation");
    public static final long SYNC_TIME = 2000L;
    private static int ticks = 0;

    @Override
    public void onInitialize() {
        DisplaysConfig.init();
        DisplaysRegistry.init();
        DisplayTile.initCommon();

        LOGGER.info(IT, "Running WATERFrAMES v{}", FabricLoader.getInstance().getModContainer(ID).get().getMetadata().getVersion());
    }

    public static ResourceLocation asResource(String id) {
        return Objects.requireNonNull(ResourceLocation.tryBuild(ID, id));
    }

    public static ResourceLocation asResource(int texture) {
        return Objects.requireNonNull(ResourceLocation.tryBuild(ID, "dynamic_texture_" + texture));
    }

    public static boolean isInstalled(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public static URI createURI(String s) {
        File f = new File(s);
        // accept local paths as file uris
        if (!f.isDirectory() && f.exists())
            return new File(s).toURI();

        try {
            return new URI(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static String composeURIString(LinkedList<URI> s) {
        final StringBuilder sb = new StringBuilder();
        for (URI uri: s) {
            sb.append(uri.toString());
            if (uri != s.getLast()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static LinkedList<URI> decomposeURIString(String s) {
        LinkedList<URI> uris = new LinkedList<>();
        String[] split = s.split("\n");
        for (String uri: split) {
            if (uri.isEmpty()) continue;
            URI u = createURI(uri);
            if (u != null) uris.add(u);
        }
        return uris;
    }

    public static boolean isInstalled(String... mods) {
        for (String id: mods) {
            if (FabricLoader.getInstance().isModLoaded(id)) {
                return true;
            }
        }
        return false;
    }

    public static double getDistance(DisplayTile tile, Position playerPos) {
        return getDistance(tile.level, tile.getBlockPos(), playerPos);
    }

    public static double getDistance(Level level, BlockPos pos, Position position) {
        if (VSCompat.installed() && DisplaysConfig.vsEurekaCompat()) {
            return Math.sqrt(VSCompat.getSquaredDistance(level, pos, position));
        }
        return Math.sqrt(pos.distToLowCornerSqr(position.x(), position.y(), position.z()));
    }

    @Environment(EnvType.CLIENT)
    public static float deltaFrames() {
        return Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getFrameTime();
    }

    @Environment(EnvType.CLIENT)
    public static void tick() {
        if (++ticks == Integer.MAX_VALUE) ticks = 0;
    }

    @Environment(EnvType.CLIENT)
    public static int getTicks() {
        return ticks;
    }
}