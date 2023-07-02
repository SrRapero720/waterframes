package me.srrapero720.waterframes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class FramesConfig {
    public static final Gson GSON = new Gson();
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // BASIC
    private static final ForgeConfigSpec.DoubleValue MAX_WIDTH;
    private static final ForgeConfigSpec.DoubleValue MAX_HEIGHT;
    private static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_VIDEOS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_ADVENTURE;
    private static final ForgeConfigSpec.BooleanValue DISABLE_SURVIVAL;
    private static final ForgeConfigSpec.BooleanValue DISABLE_PLAYERS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_REDSTONE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_WHITELIST;
    private static final ForgeConfigSpec.ConfigValue<List<String>> WHITELIST;

    // PROJECTOR
    public static final ForgeConfigSpec.IntValue MAX_PROJECTION_DISTANCE;
    private static final Marker IT = MarkerFactory.getMarker("Util");

    static {
        /* waterframes -> */
        BUILDER.push("waterframes");

        MAX_WIDTH = BUILDER.defineInRange("maxWidth", 100.0D, 10.0D, 1000.0D);
        MAX_HEIGHT = BUILDER.defineInRange("maxHeight", 100.0D, 10.0D, 1000.0D);
        MAX_RENDER_DISTANCE = BUILDER.defineInRange("maxRenderDistance", 1000, 10, Integer.MAX_VALUE);

        DISABLE_ADVENTURE = BUILDER.define("disableUsageAdventure", true);
        DISABLE_SURVIVAL = BUILDER.define("disableUsageSurvival", false);
        DISABLE_PLAYERS = BUILDER.define("disableUsageForPlayers", false);

        DISABLE_VIDEOS = BUILDER.define("disableVLC", false);

        DISABLE_REDSTONE = BUILDER.comment("Disable pause trigger on redstone signal input").define("disableRedstone", true);

        DISABLE_WHITELIST = BUILDER.define("disableWhitelist", false);
        WHITELIST = BUILDER.define("whitelist", getJsonListFromRes("whitelist_url.json"));


        /* waterframes -> frames */
        BUILDER.push("frames");

        // waterframes ->
        BUILDER.pop();

        // waterframes -> projector
        BUILDER.push("projector");
        MAX_PROJECTION_DISTANCE = BUILDER.comment("Block distance of projection and projector")
                .defineInRange("maxProjectionDistance", 10, 3, 100);


        // waterframes ->
        BUILDER.pop();

        // ->
        BUILDER.pop();

        //WATERFrAMES BASIC
        SPEC = BUILDER.build();
    }

    public static double maxWidth() { return MAX_WIDTH.get(); }
    public static double maxHeight() { return MAX_HEIGHT.get(); }
    public static int maxRenderDistance() { return MAX_RENDER_DISTANCE.get(); }
    public static boolean isDisabledVideos() { return DISABLE_VIDEOS.get(); }

    @Deprecated
    public static boolean isDisabledRedstone() { return DISABLE_REDSTONE.get(); }

    public static boolean domainAllowed(@NotNull String domain) {
        if (DISABLE_WHITELIST.get()) return true;

        for (final var url: WHITELIST.get()) {
            var uri = url.trim().toLowerCase(Locale.ROOT);
            if (domain.endsWith("." + uri) || domain.equals(uri)) return true;
        }
        return false;
    }

    public static boolean canUse(@NotNull Player player, String url) {
        var level = player.level;
        var server = level.getServer();
        if (level.isClientSide) return true;
        if (server.isSingleplayer() || player.hasPermissions(server.getOperatorUserPermissionLevel())) return true;

        if (!DISABLE_WHITELIST.get()) return ThreadUtil.tryAndReturn((defaultVar) ->
                domainAllowed(new URI(url.toLowerCase(Locale.ROOT)).getHost()), false);
        return true;
    }

    public static GameType getGameType(Player player) {
        if (player instanceof ServerPlayer serverPlayer) return serverPlayer.gameMode.getGameModeForPlayer();
        return Minecraft.getInstance().gameMode.getPlayerMode();
    }

    public static boolean canInteract(Player player, Level level) {
        var server = player.getServer();
        boolean isOperator = level.isClientSide() || (server != null && player.hasPermissions( server.getOperatorUserPermissionLevel()));

        if (DISABLE_SURVIVAL.get() && !getGameType(player).equals(GameType.CREATIVE) && !isOperator) return false;
        if (DISABLE_ADVENTURE.get() && getGameType(player).equals(GameType.ADVENTURE) && !isOperator) return false;
        if (DISABLE_PLAYERS.get()) return isOperator;

        else return player.getAbilities().mayBuild;

    }

    public static List<String> getJsonListFromRes(String path) {
        try (var in = Util.class.getClassLoader().getResourceAsStream(path);
             var res = (in != null) ? new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)) : null) {
            if (res != null) return GSON.fromJson(res, new TypeToken<List<String>>() {}.getType());
            else throw new IllegalArgumentException("File not found!");
        } catch (Exception e) {
            LOGGER.error(IT, "Exception trying to read JSON from {}", path, e);
        }
        return new ArrayList<>();
    }
}