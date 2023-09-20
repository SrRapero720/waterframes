package me.srrapero720.waterframes.core;

import me.srrapero720.waterframes.core.tools.JarTool;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.*;

public class WaterConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // BASIC
    private static final ForgeConfigSpec.DoubleValue MAX_WIDTH;
    private static final ForgeConfigSpec.DoubleValue MAX_HEIGHT;
    private static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;
    private static final ForgeConfigSpec.IntValue MAX_AUDIO_VOLUME;
    private static final ForgeConfigSpec.IntValue MAX_AUDIO_DISTANCE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_VLC;
    private static final ForgeConfigSpec.BooleanValue DISABLE_LAVA;

    private static final ForgeConfigSpec.BooleanValue DISABLE_ADVENTURE;
    private static final ForgeConfigSpec.BooleanValue DISABLE_SURVIVAL;
    private static final ForgeConfigSpec.BooleanValue DISABLE_PLAYERS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_REDSTONE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_WHITELIST;
    private static final ForgeConfigSpec.ConfigValue<List<String>> WHITELIST;

    static {
        /* waterframes -> */
        BUILDER.push("waterframes");

        MAX_WIDTH = BUILDER.defineInRange("maxWidth", 100.0D, 8.0D, 1024.0D);
        MAX_HEIGHT = BUILDER.defineInRange("maxHeight", 100.0D, 8.0D, 1024.0D);
        MAX_RENDER_DISTANCE = BUILDER.defineInRange("maxRenderDistance", 128, 8, 1024);
        MAX_AUDIO_DISTANCE = BUILDER.defineInRange("maxAudioDistance", 512, 20, 1024);
        MAX_AUDIO_VOLUME = BUILDER.defineInRange("maxAudioVolume", 100, 10, 200);

        DISABLE_ADVENTURE = BUILDER.define("disableUsageAdventure", true);
        DISABLE_SURVIVAL = BUILDER.define("disableUsageSurvival", false);
        DISABLE_PLAYERS = BUILDER.comment("Only admins mode").define("disableUsageForPlayers", false);

        DISABLE_VLC = BUILDER.define("disableVLC", false);
        DISABLE_LAVA = BUILDER.comment("Soon").define("disableLavaPlayer", false);

        DISABLE_REDSTONE = BUILDER.comment("Disable pause trigger on redstone signal input").define("disableRedstone", true);

        DISABLE_WHITELIST = BUILDER.define("disableWhitelist", false);
        WHITELIST = BUILDER.define("whitelist", JarTool.readStringList(WaterConfig.class.getClassLoader(), "whitelist_url.json"));

        // ->
        BUILDER.pop();

        //WATERFrAMES BASIC
        SPEC = BUILDER.build();
    }

    public static void init(IEventBus bus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, WaterConfig.SPEC, "waterframes.toml");
    }

    public static float maxWidth() { return (float) (double) MAX_WIDTH.get(); }
    public static float maxHeight() { return (float) (double) MAX_HEIGHT.get(); }
    public static int maxAudioDistance() { return MAX_AUDIO_DISTANCE.get(); }
    public static int maxAudioVolume() { return MAX_AUDIO_VOLUME.get(); }
    public static int maxRenderDistance() { return MAX_RENDER_DISTANCE.get(); }
    public static boolean isDisabledVLC() { return DISABLE_VLC.get(); }
    public static boolean isDisabledLavaPlayer() { return DISABLE_LAVA.get(); }
    public static boolean isDisabledRedstone() { return DISABLE_REDSTONE.get(); }

    public static boolean domainAllowed(@NotNull String domain) {
        if (DISABLE_WHITELIST.get()) return true;

        for (final var url: WHITELIST.get()) {
            var uri = url.trim().toLowerCase(Locale.ROOT);
            if (domain.endsWith("." + uri) || domain.equals(uri)) return true;
        }
        return false;

    }

    public static boolean canUse(Player player, String url) {
        var level = player.level;
        var server = level.getServer();
        if (level.isClientSide()) return true;
        if (server.isSingleplayer() || player.hasPermissions(server.getOperatorUserPermissionLevel())) return true;

        if (!DISABLE_WHITELIST.get()) {
            try {
                return domainAllowed(new URL(url.toLowerCase(Locale.ROOT)).getHost());
            } catch (Exception e) {
                return false;
            }
        }
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
}