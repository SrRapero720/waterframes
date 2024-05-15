package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class WFConfig {
    private static final Builder SERVER = new Builder();
    private static final Builder CLIENT = new Builder();

    private static final String[] WHITELIST = new String[] {
            "imgur.com",
            "gyazo.com",
            "prntscr.com",
            "tinypic.com",
            "puu.sh",
            "pinimg.com",
            "photobucket.com",
            "staticflickr.com",
            "flic.kr",
            "tenor.co",
            "gfycat.com",
            "giphy.com",
            "gph.is",
            "gifbin.com",
            "i.redd.it",
            "media.tumblr.com",
            "twimg.com",
            "discordapp.com",
            "images.discordapp.net",
            "discord.com",
            "githubusercontent.com",
            "googleusercontent.com",
            "googleapis.com",
            "wikimedia.org",
            "ytimg.com",
            "youtube.com",
            "youtu.be",
            "twitch.tv",
            "twitter.com",
            "soundcloud.com",
            "kick.com",
            "srrapero720.me",
            "fbcdn.net",
            "drive.google.com",
    };

    // RENDERING
    private static final DoubleValue maxWidth;
    private static final DoubleValue maxHeight;
    private static final IntValue maxRenderDistance;
    private static final DoubleValue maxProjectionDistance;
    // MULTIMEDIA
    private static final IntValue maxVolumeDistance;
    private static final IntValue maxVolume;
    private static final BooleanValue useMultimedia;
    private static final BooleanValue keepRendering;
    // BEHAVIOR
    private static final BooleanValue useLightsOnPlay;
    private static final BooleanValue useRedstone;
    private static final BooleanValue useMasterModeOnRedsone;
    // REMOTE CONTROL
    private static final IntValue remoteDistance;

    // PERMISSIONS
    private static final BooleanValue useInAdventure;
    private static final BooleanValue useInSurvival;
    private static final BooleanValue useForAnyone;
    private static final BooleanValue useWhitelist;
    private static final ConfigValue<List<String>> whitelist;

    // OVERRIDES (client)
    private final static ForgeConfigSpec.BooleanValue overrideServerConfig;
    private static final BooleanValue clientUseMultimedia;
    private static final BooleanValue clientKeepsRendering;

    private static final ForgeConfigSpec SERVER_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;

    static {
        // WATERFRAMES -> rendering
        SERVER.comment("All configurations about rendering");
        SERVER.push("waterframes.rendering");

        maxWidth = SERVER
                .comment("Width limit of displays in blocks")
                .defineInRange("maxWidth", 40d, 1d, 128d);

        maxHeight = SERVER
                .comment("Height limit of displays in blocks")
                .defineInRange("maxHeight", 40d, 1d, 128d);

        maxRenderDistance = SERVER
                .comment("Max Radius of rendering distance in blocks")
                .defineInRange("maxRenderDistance", 64, 4, 128);

        maxProjectionDistance = SERVER
                .comment("Max distance of projections in blocks")
                .defineInRange("maxProjectionDistance", 64d, 4d, 128d);

        keepRendering = SERVER
                .comment("Enables media processing and rendering, disabling it will not render nothing, you can still hear videos")
                .define("keepRendering", true);

        // WATERFRAMES -> multimedia
        SERVER.comment("Configuration related to multimedia sources like Videos or Music");
        SERVER.pop().push("multimedia");

        maxVolumeDistance = SERVER
                .comment("Max volume distance radius")
                .defineInRange("maxVolumeDistance", 64, 8, 256);

        maxVolume = SERVER
                .comment("Max volume value", "values over 100 uses VLC überVolume")
                .defineInRange("maxVolume", 100, 10, 120);

        // WATERFRAMES -> multimedia -> watermedia
        SERVER.push("watermedia");

        useMultimedia = SERVER
                .comment("Enables VLC/FFMPEG usage for multimedia processing like videos and music (support added by WATERMeDIA)")
                .define("enable", true);

        // WATERFRAMES -> block_behavior
        SERVER.comment("Configuration related to interactions with vanilla and modded features");
        SERVER.pop(2).push("block_behavior");

        useLightsOnPlay = SERVER
                .comment("Enable light feature on frames while is playing")
                .define("lightOnPlay", true);

        SERVER.comment("Redstone interaction options");
        SERVER.push("redstone");

        useRedstone = SERVER
                .comment("Enable the feature")
                .define("enable", true);

        useMasterModeOnRedsone = SERVER
                .comment("Redstone inputs forces paused playback and ignores any other control sources")
                .define("masterMode", false);

        SERVER.pop();

        // WATERFRAMES -> remote_control
        SERVER.comment("Configuration related to remote control");
        SERVER.pop().push("remote_control");

        remoteDistance = SERVER
                .comment("Distance in blocks of RC range")
                .defineInRange("distance", 32, 4, 128);

        // WATERFRAMES -> permissions
        SERVER.comment("Configurations related to permissions");
        SERVER.pop().push("permissions");

        useInAdventure = SERVER
                .comment("Changes if players in Adventure mode can use displays")
                .define("usableInAdventureMode", false);

        useInSurvival = SERVER
                .comment("Changes if players in Survival mode can use displays")
                .define("usableInSurvivalMode", true);
        useForAnyone = SERVER
                .comment("Changes if any player can use displays, otherwise only admins can use it")
                .define("usableForAnyone", true);

        SERVER.comment("Whitelist configuration: please stop bugging me with this :(");
        SERVER.push("whitelist");

        useWhitelist = SERVER
                .comment(
                        "Enables whitelist feature",
                        "[WARNING]: THE AUTHOR OF THE MOD IS NOT RESPONSIBLE IF IN YOUR SERVER SOMEONE PUTS NSFW MEDIA",
                        "WATERMEDIA HAVE SUPPORT FOR ADULT PAGES AND WHITELIST WAS DESIGNED TO PREVENT THAT"
                )
                .define("enable", true);

        whitelist = SERVER.define("urls", () -> Arrays.asList(WHITELIST), o -> false);

        SERVER.pop();

        SERVER.pop();

        // ###################
        // ### CLIENTSIDE ###
        // #################
        CLIENT.push("waterframes");

        CLIENT.comment("Configurations to override server config");
        CLIENT.push("overrideConfig");

        overrideServerConfig = CLIENT
                .comment("Enables the option")
                .define("enable", false);

        clientUseMultimedia = CLIENT
                .comment(
                        "Overrides 'waterframes.watermedia.enable' option",
                        "Enables VLC/FFMPEG usage for multimedia processing (support added by WATERMeDIA)"
                )
                .define("useMultimedia", false);

        clientKeepsRendering = CLIENT
                .comment(
                        "overrides 'waterframes.rendering.keepRendering'",
                        "Enables media processing and rendering, disabling it will not render nothing, you can still hear videos"
                )
                .define("keepRendering", false);

        CLIENT.pop();

        CLIENT.pop();

        // BULDING
        CLIENT_SPEC = CLIENT.build();
        SERVER_SPEC = SERVER.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        if (FMLLoader.getDist().isClient()) // SKIPS TRASH CONFIG FILES ON SERVERS
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static float maxWidth() { return (float) (double) maxWidth.get(); }
    public static float maxHeight() { return (float) (double) maxHeight.get(); }
    public static float maxWidth(float width) { return Math.min(width, maxWidth()); }
    public static float maxHeight(float height) { return Math.min(height, maxHeight()); }

    public static int maxRenDis() { return maxRenderDistance.get(); }
    public static int maxRenDis(int value) { return Math.min(value, maxRenDis()); }

    public static float maxProjDis() { return (float) (double) maxProjectionDistance.get(); }
    public static float maxProjDis(float value) { return Math.min(value, maxProjDis()); }

    public static boolean keepsRendering() { return overrideServerConfig.get() ? clientKeepsRendering.get() : keepRendering.get(); }
    public static boolean useLightOnPlay() { return useLightsOnPlay.get(); }

    // MULTIMEDIA
    public static int maxVolDis() { return maxVolumeDistance.get(); }
    public static int maxVolDis(int value) { return Math.min(value, maxVolDis()); }

    public static int maxVol() { return maxVolume.get(); }
    public static int maxVol(int value) { return Math.max(Math.min(value, maxVol()), 0); }

    public static boolean useMultimedia() { return overrideServerConfig.get() ? clientUseMultimedia.get() : useMultimedia.get(); }

    // BEHAVIOR
    public static boolean useRedstone() { return useRedstone.get(); }
    public static boolean useMasterModeRedstone() { return useRedstone() && useMasterModeOnRedsone.get(); }
    public static int maxRcDis() { return remoteDistance.get(); }

    // PERMISSIONS
    public static boolean useInAdv() { return useInAdventure.get(); }
    public static boolean useInSurv() { return useInSurvival.get(); }
    public static boolean useForAnyone() { return useForAnyone.get(); }
    public static boolean useWhitelist() { return useWhitelist.get(); }

    public static boolean isWhiteListed(@NotNull String url) {
        if (!useWhitelist()) return true;

        if (url.startsWith("local://")
                || url.startsWith("game://")
                || url.startsWith("user://")
                || url.startsWith("users://")) return true; // local files = anything you have

        try {
            var host = new URL(url).getHost();

            for (var s: whitelist.get()) {
                if (host.endsWith("." + s) || host.equals(s)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean canSave(Player player, String url) {
        if (isAdmin(player)) return true;

        try {
            return url.isEmpty() || isWhiteListed(url);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canInteractBlock(Player player) {
        GameType gameType = (player instanceof ServerPlayer serverPlayer)
                ? serverPlayer.gameMode.getGameModeForPlayer()
                : Minecraft.getInstance().gameMode.getPlayerMode();

        if (isAdmin(player)) return true;
        if (!useInSurv() && gameType.equals(GameType.SURVIVAL)) return false;
        if (!useInAdv() && gameType.equals(GameType.ADVENTURE)) return false;

        return useForAnyone();
    }

    public static boolean canInteractItem(Player player) {
        if (isAdmin(player)) return true;
        return useForAnyone();
    }

    public static boolean isAdmin(Player player) {
        Level level = player.level;

        // OWNER
        String name = player.getGameProfile().getName();
        if (name.equals("SrRaapero720") || name.equals("SrRapero720")) {
            return true;
        }

        if (level.isClientSide()) { // validate if was singleplayer and if was the admin
            IntegratedServer integrated = Minecraft.getInstance().getSingleplayerServer();
            if (integrated != null) {
                return integrated.isSingleplayerOwner(player.getGameProfile()) || player.hasPermissions(integrated.getOperatorUserPermissionLevel());
            } else { // is a guest, check perms
                return player.hasPermissions(WaterFrames.getServerOpPermissionLevel(level));
            }
        } else {
            return player.hasPermissions(player.getServer().getOperatorUserPermissionLevel());
        }
    }
}
