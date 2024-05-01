package me.srrapero720.waterframes;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.NotNull;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.api.ICreativeConfig;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;

import java.net.URL;
import java.util.*;

import static me.srrapero720.waterframes.WaterFrames.ID;

public class WFConfig implements ICreativeConfig {
    public static final Marker IT = MarkerManager.getMarker("Config");
    public static final WFConfig ROOT = new WFConfig();
    public static final WFConfig.Rendering RENDERING = new WFConfig.Rendering();
    public static final WFConfig.Multimedia MULTIMEDIA = new WFConfig.Multimedia();
    public static final WFConfig.Multimedia.WaterMedia WATERMEDIA = new WFConfig.Multimedia.WaterMedia();
    public static final WFConfig.BlockBehavior BEHAVIOR = new WFConfig.BlockBehavior();
    public static final WFConfig.RemoteControl REMOTE = new WFConfig.RemoteControl();
    public static final WFConfig.Permissions PERMISSIONS = new WFConfig.Permissions();
    public static final WFConfig.Permissions.Whitelist WHITELIST = new WFConfig.Permissions.Whitelist();

    public static class Rendering {
        // RENDERING
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.DecimalRange(min = 1, max = 256)
        public float maxWidth = 48;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.DecimalRange(min = 1, max = 256)

        public float maxHeight = 48;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.IntRange(min = 1, max = 512)
        public int maxRenderDistance = 64;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.DecimalRange(min = 1, max = 256)
        public float maxProjectionDistance = 64;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean keepRendering = true;
    }

    // MULTIMEDIA
    public static class Multimedia {
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.IntRange(min = 8, max = 512)
        public int maxVolumeDistance = 64;

        // MULTIMEDIA
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.IntRange(min = 10, max = 128)
        public int maxVolume = 100;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useMasterVolume = false;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useVSEurekaCompat = true;

        public static class WaterMedia {
            @CreativeConfig(type = ConfigSynchronization.SERVER)
            public boolean useMultimedia = true;
        }
    }

    // BEHAVIOR
    public static class BlockBehavior {
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useLightsOnPlay = true;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean forceLightsOnPlay = true;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useRedstone = true;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useMasterModeOnRedsone = false;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useLagTickCorrection = true;
    }

    // REMOTE CONTROL
    public static class RemoteControl {
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        @CreativeConfig.IntRange(min = 4, max = 128)
        public int remoteDistance = 32;
    }

    // PERMISSIONS
    public static class Permissions {
        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useInAdventure = false;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useInSurvival = true;

        @CreativeConfig(type = ConfigSynchronization.SERVER)
        public boolean useForAnyone = true;

        public static class Whitelist {
            @CreativeConfig(type = ConfigSynchronization.SERVER)
            public boolean useWhitelist = true;

            @CreativeConfig(type = ConfigSynchronization.SERVER)
            public List<String> whitelist = List.of("imgur.com",
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
                    "drive.google.com");
        }
    }

    // OVERRIDES (client)
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean overrideServerConfig = false;

    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean clientUseMultimedia = false;

    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean clientKeepsRendering = false;

    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean forceDevMode = false;



    public static void init() {
        var holder = CreativeConfigRegistry.ROOT.registerFolder(ID);
        holder.registerValue("General", ROOT);
        holder.registerValue("Rendering", RENDERING);

        var folderMultimedia = holder.registerFolder("Multimedia");
        folderMultimedia.registerValue("Multimedia", MULTIMEDIA);
        var folderWaterMedia = folderMultimedia.registerFolder("WaterMedia");
        folderWaterMedia.registerValue("WaterMedia", WATERMEDIA);

        holder.registerValue("BlockBehavior", BEHAVIOR);
        holder.registerValue("RemoteControl", REMOTE);
        var permissions = holder.registerValue("Permissions", PERMISSIONS);
        permissions.registerValue("Whitelist", WHITELIST);
    }

    public WFConfig() {

    }

    @Override
    public void configured(Side side) {

    }

    public static float maxWidth() { return RENDERING.maxWidth; }
    public static float maxHeight() { return RENDERING.maxHeight; }
    public static float maxWidth(float width) { return Math.min(width, maxWidth()); }
    public static float maxHeight(float height) { return Math.min(height, maxHeight()); }

    public static int maxRenDis() { return RENDERING.maxRenderDistance; }
    public static int maxRenDis(int value) { return Math.min(value, maxRenDis()); }

    public static float maxProjDis() { return RENDERING.maxProjectionDistance; }
    public static float maxProjDis(float value) { return Math.min(value, maxProjDis()); }

    public static boolean keepsRendering() { return ROOT.overrideServerConfig ? ROOT.clientKeepsRendering : RENDERING.keepRendering; }
    public static boolean useLightOnPlay() { return BEHAVIOR.useLightsOnPlay; }
    public static boolean forceLightOnPlay() { return BEHAVIOR.forceLightsOnPlay; }
    public static boolean useLagTickCorrection() { return BEHAVIOR.useLagTickCorrection; }

    // MULTIMEDIA
    public static int maxVolDis() { return MULTIMEDIA.maxVolumeDistance; }
    public static int maxVolDis(int value) { return Math.min(value, maxVolDis()); }
    public static boolean useMasterVolume() { return MULTIMEDIA.useMasterVolume; }
    public static boolean vsEurekaCompat() { return MULTIMEDIA.useVSEurekaCompat; }

    public static int maxVol() { return MULTIMEDIA.maxVolume; }
    public static int maxVol(int value) { return Math.max(Math.min(value, maxVol()), 0); }

    public static boolean useMultimedia() { return ROOT.overrideServerConfig ? ROOT.clientUseMultimedia : WATERMEDIA.useMultimedia; }

    // BEHAVIOR
    public static boolean useRedstone() { return BEHAVIOR.useRedstone; }
    public static boolean useMasterModeRedstone() { return useRedstone() && BEHAVIOR.useMasterModeOnRedsone; }
    public static int maxRcDis() { return REMOTE.remoteDistance; }

    // PERMISSIONS
    public static boolean useInAdv() { return PERMISSIONS.useInAdventure; }
    public static boolean useInSurv() { return PERMISSIONS.useInSurvival; }
    public static boolean useForAnyone() { return PERMISSIONS.useForAnyone; }
    public static boolean useWhitelist() { return WHITELIST.useWhitelist; }
    public static boolean useWhitelist(boolean state) {
        WHITELIST.useWhitelist = state;
        return state;
    }
    public static boolean toggleWhitelist() {
        return useWhitelist(!useWhitelist());
    }
    public static void addOnWhitelist(String url) {
        WHITELIST.whitelist.add(url);
    }
    public static boolean removeOnWhitelist(String url) {
        return WHITELIST.whitelist.remove(url);
    }

    public static boolean isWhiteListed(String url) {
        if (!useWhitelist()) return true;

        if (url.startsWith("local://")
                || url.startsWith("game://")
                || url.startsWith("user://")
                || url.startsWith("users://")) return true; // local files = anything you have

        try {
            var host = new URL(url).getHost();

            for (var s: WHITELIST.whitelist) {
                if (host.endsWith("." + s) || host.equals(s)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }
    public static <T> Set<T> mutableSet(Iterator<T> it) {
        var list = new HashSet<T>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
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
                return player.hasPermissions(4);
            }
        } else {
            return player.hasPermissions(4);
        }
    }

    public static boolean isDevMode() {
        return FabricLoader.getInstance().isDevelopmentEnvironment() || ROOT.forceDevMode;
    }
}
