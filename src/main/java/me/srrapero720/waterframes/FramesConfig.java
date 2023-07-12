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
    private static final Gson GSON = new Gson();
    private static final Marker IT = MarkerFactory.getMarker("Util");
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // BASIC
    private static final ForgeConfigSpec.IntValue MAX_WIDTH;
    private static final ForgeConfigSpec.IntValue MAX_HEIGHT;
    private static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;
    private static final ForgeConfigSpec.IntValue MAX_SOUND_DISTANCE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_MASTER_CONTROL;
    private static final ForgeConfigSpec.BooleanValue DISABLE_VIDEOS;
    private static final ForgeConfigSpec.BooleanValue DISABLE_ADVENTURE;
    private static final ForgeConfigSpec.BooleanValue DISABLE_SURVIVAL;
    private static final ForgeConfigSpec.BooleanValue DISABLE_PLAYERS;
    private static final ForgeConfigSpec.BooleanValue DISABLE_WHITELIST;

    private static final ForgeConfigSpec.ConfigValue<List<String>> WHITELIST;

    // FRAMES
    private static final ForgeConfigSpec.BooleanValue DISABLE_REDSTONE;

    // PROJECTOR
    private static final ForgeConfigSpec.IntValue MAX_PROJECTION_DISTANCE;

    // TV
    private static final ForgeConfigSpec.IntValue MAX_TV_SAVES;
    private static final ForgeConfigSpec.IntValue CHANNEL_SWITCH_COOLDOWN;

    // BOOMBOX
    private static final ForgeConfigSpec.BooleanValue DISABLE_SYNC;

    // UNIVERSAL REMOTE CONTROL
    private static final ForgeConfigSpec.IntValue MAX_REMOTE_RADIUS;

    static {
        /* WaterFrames -> */
        BUILDER.push("WaterFrames");
        /* WaterFrames -> General */
        BUILDER.push("General");

        MAX_WIDTH = BUILDER
                .comment("Define max width for all displays in blocks")
                .defineInRange("maxWidth", 100, 5, 1024);

        MAX_HEIGHT = BUILDER
                .comment("Define max height for all displays in blocks")
                .defineInRange("maxHeight", 100, 5, 1024);

        MAX_RENDER_DISTANCE = BUILDER
                .comment(
                        "Define max rendering distance for all displays in blocks",
                        "Note: on public multiplayer servers keep this value extremely low, just to prevent FPS drops"
                )
                .defineInRange("maxRenderDistance", 1000, 10, 1024);
        MAX_SOUND_DISTANCE = BUILDER
                .comment(
                        "Define max sound distance for all displays in blocks",
                        "Note: on public multiplayer servers keep this value extremly low, just to prevent global rickrolls or earrapes"
                )
                .defineInRange("maxSoundDistance", 100, 5, 1024);

        DISABLE_MASTER_CONTROL = BUILDER
                .comment(
                        "Disables Minecraft master volume can control WF video or audio players volume",
                        "If is enabled displays volume can't be controlled by Minecraft master volume"
                )
                .define("disableMasterVolumeControl", true);

        DISABLE_ADVENTURE = BUILDER
                .comment(
                        "Disable all WF blocks usage in adventure mode",
                        "If true, people in adventure mode CAN'T use frames, overwise can use it"
                )
                .define("disableOnAdventure", true);

        DISABLE_SURVIVAL = BUILDER
                .comment(
                        "Disable all WF blocks usage in survival mode",
                        "If true, people in survival mode CAN'T use frames, overwise can use it"
                )
                .define("disableOnSurvival", false);
        DISABLE_PLAYERS = BUILDER
                .comment(
                        "Disable usage only for admins",
                        "If true, everyone can use frames, overwise only admins can use it"
                )
                .define("disableOnlyAdmins", true);


        DISABLE_VIDEOS = BUILDER
                .comment(
                        "Disable VLC usage for all clients connected in your server",
                        "This disables all video/audio features, but keeps picture and gif features"
                )
                .define("disableVideoFeatures", false);

        DISABLE_WHITELIST = BUILDER
                .comment(
                        "Disable whitelist for URLs. when whitelist is enabled and any non listed URL",
                        "is trying to be added then frame rejects saving. Use this on public servers"
                )
                .define("disableWhitelist", false);

        WHITELIST = BUILDER
                .comment("All permitted URLs")
                .define("whitelist", getJsonListFromRes("whitelist_url.json"));

        /* WaterFrames -> */
        BUILDER.pop();

        /* WaterFrames -> Frames */
        BUILDER.push("Frames");
        DISABLE_REDSTONE = BUILDER
                .comment(
                        "Disable redstone trigger for frames",
                        "When is enabled, any powered redstone signal can pause frames",
                        "if doesn't detect any signal then resume playing"
                )
                .define("disableRedstone", false);

        /* WaterFrames -> */
        BUILDER.pop();

        /* WaterFrames -> Projector */
        BUILDER.push("Projector");
        MAX_PROJECTION_DISTANCE = BUILDER.comment("Block distance of projection and projector")
                .defineInRange("maxProjectionDistance", 10, 3, 100);


        /* WaterFrames -> */
        BUILDER.pop();

        /* WaterFrames -> TV */
        BUILDER.push("TV");
        MAX_TV_SAVES = BUILDER.defineInRange("maxTVSaves", 10, 3, 100);
        CHANNEL_SWITCH_COOLDOWN = BUILDER.defineInRange("channelSwitchCooldown", 3, 1, 10);


        /* WaterFrames -> */
        BUILDER.pop();

        /* WaterFrames -> Boombox */
        BUILDER.push("Boombox");
        DISABLE_SYNC = BUILDER
                .comment(
                        "Disables passive media sync when multimedia is already playing",
                        "This option doesn't disable boombox starting/booting sync"
                )
                .define("disableSync", false);

        /* WaterFrames -> */
        BUILDER.pop();

        /* WaterFrames -> Universal Control Remote*/
        BUILDER.comment("Universal Control Remote").push("UCR");
        MAX_REMOTE_RADIUS = BUILDER.defineInRange("maxRemoteRadius",  24, 5, 1024);

        // WaterFrames ->
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