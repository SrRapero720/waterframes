package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.block.DisplayBlock;
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
import net.minecraftforge.server.permission.nodes.PermissionNode;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

public class WFConfig {
    private static final Pattern HOSTS_PATTERN = Pattern.compile("^(?!-)([a-zA-Z0-9-]{1,63}\\.)+[a-zA-Z]{2,63}$");

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
            "tenor.com",
            "gfycat.com",
            "giphy.com",
            "gph.is",
            "gifbin.com",
            "i.redd.it",
            "media.tumblr.com",
            "twimg.com",
            "githubusercontent.com",
            "googleusercontent.com",
            "googleapis.com",
            "wikimedia.org",
            "ytimg.com",
            "youtube.com",
            "youtu.be",
            "twitch.tv",
            "twitter.com",
            "x.com",
            "soundcloud.com",
            "kick.com",
            "streamable.com",
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
    private static final BooleanValue useMasterVolume;
    private static final BooleanValue useVSEurekaCompat;
    private static final BooleanValue useMultimedia;
    private static final BooleanValue useSlavismMode;
    private static final BooleanValue keepRendering;
    // BEHAVIOR
    private static final BooleanValue useLightsOnPlay;
    private static final BooleanValue forceLightsOnPlay;
    private static final BooleanValue useLagTickCorrection;
    private static final BooleanValue useRedstone;
    private static final BooleanValue useMasterModeOnRedstone;
    // REMOTE CONTROL
    private static final IntValue remoteDistance;

    // PERMISSIONS
    private static final BooleanValue usePermissionsAPI;
    private static final BooleanValue useInAdventure;
    private static final BooleanValue useInSurvival;
    private static final BooleanValue useForAnyone;
    private static final BooleanValue useBindingRemotes;
    private static final BooleanValue useRemotes;
    private static final BooleanValue useWhitelist;
    private static final BooleanValue blackWhitelist;
    private static final BooleanValue allowSaving;

    private static final ConfigValue<List<? extends String>> whitelist;

    // OVERRIDES (client)
    private final static BooleanValue overrideServerConfig;
    private static final BooleanValue clientUseMultimedia;
    private static final BooleanValue clientKeepsRendering;
    private static final BooleanValue clientSlavistMode;
    private static final BooleanValue forceDevMode;

    private static final ForgeConfigSpec SERVER_SPEC;
    private static final ForgeConfigSpec CLIENT_SPEC;

    static {
        // WATERFRAMES -> rendering
        SERVER.comment("All configurations about rendering");
        SERVER.push("waterframes.rendering");

        maxWidth = SERVER
                .comment("Width limit of displays in blocks")
                .defineInRange("maxWidth", 40d, 1d, 256d);

        maxHeight = SERVER
                .comment("Height limit of displays in blocks")
                .defineInRange("maxHeight", 40d, 1d, 256d);

        maxRenderDistance = SERVER
                .comment("Max Radius of rendering distance in blocks")
                .defineInRange("maxRenderDistance", 64, 4, 512);

        maxProjectionDistance = SERVER
                .comment("Max distance of projections in blocks")
                .defineInRange("maxProjectionDistance", 64d, 4d, 512d);

        keepRendering = SERVER
                .comment("Enables media processing and rendering, disabling it will not render nothing, you can still hear videos")
                .define("keepRendering", true);

        // WATERFRAMES -> multimedia
        SERVER.comment("Configuration related to multimedia sources like Videos or Music");
        SERVER.pop().push("multimedia");

        maxVolumeDistance = SERVER
                .comment("Max volume distance radius")
                .defineInRange("maxVolumeDistance", 64, 8, 512);

        maxVolume = SERVER
                .comment("Max volume value", "values over 100 uses VLC überVolume")
                .defineInRange("maxVolume", 100, 10, 120);

        useMasterVolume = SERVER
                .comment("Makes Minecraft master volume affects waterframes volume")
                .define("masterVolume", false);

        useVSEurekaCompat = SERVER
                .comment(
                        "Enables compatibility with VSEureka",
                        "In case VS breaks something on their side, this option should stop client/server crashing",
                        "Or if the audio isn't working, disable this option should help",
                        "(This option is called VSEureka because valkirienskies is too long, and VS may be misleading)"
                )
                .define("vsEurekaCompat", true);

        // WATERFRAMES -> multimedia -> watermedia
        SERVER.push("watermedia");

        useMultimedia = SERVER
                .comment("Enables VLC/FFMPEG usage for multimedia processing like videos and music (support added by WATERMeDIA)")
                .define("enable", true);

        useSlavismMode = SERVER
                .comment(
                        "Force-enable WATERMeDIA's slavism mode, slavism enhances youtube quality",
                        "This option comes with 3 downsides:",
                        "1: Playback gets way unstable",
                        "2: Sync time is increased to 10 seconds (less sync for all players)",
                        "3: You can only manually sync every 10 seconds after a seek"
                )
                .define("slavismMode", false);

        // WATERFRAMES -> block_behavior
        SERVER.comment("Configuration related to interactions with vanilla and modded features");
        SERVER.pop(2).push("block_behavior");

        useLightsOnPlay = SERVER
                .comment("Enable light feature on frames while is playing")
                .define("lightOnPlay", true);

        forceLightsOnPlay = SERVER
                .comment("Forces light feature on frames while is playing", "Requires lightOnPlay be true")
                .define("forceLightOnPlay", false);

        useLagTickCorrection = SERVER
                .comment("Enable lag tick time correction", "Helps when server is too laggy and playback is regressing in time", "Disable if causes problems")
                .define("lagTickCorrection", true);

        SERVER.comment("Redstone interaction options");
        SERVER.push("redstone");

        useRedstone = SERVER
                .comment("Enable the feature")
                .define("enable", true);

        useMasterModeOnRedstone = SERVER
                .comment("Redstone inputs forces paused playback and ignores any other control sources")
                .define("masterMode", false);

        SERVER.pop();

        // WATERFRAMES -> remote_control
        SERVER.comment("Configuration related to remote control");
        SERVER.pop().push("remote_control");

        remoteDistance = SERVER
                .comment("Distance in blocks of RC range")
                .defineInRange("distance", 32, 4, 256);

        // WATERFRAMES -> permissions
        SERVER.comment("Configurations related to permissions");
        SERVER.pop().push("permissions");

        usePermissionsAPI = SERVER
                .comment(
                        "Enables Permission API integration [(Neo)Forge only]",
                        "(Neo)Forge provides its own permissions API compatible with permissions mods like Luckperms or FTB Ranks",
                        "This config enables integrations and ignores all below settings"
                )
                .define("usePermissionsAPI", false);

        useInAdventure = SERVER
                .comment("Changes if players in Adventure mode can use displays")
                .define("usableInAdventureMode", false);

        useInSurvival = SERVER
                .comment("Changes if players in Survival mode can use displays")
                .define("usableInSurvivalMode", true);
        useForAnyone = SERVER
                .comment("Changes if any player can use displays, otherwise only admins can use it")
                .define("usableForAnyone", true);

        allowSaving = SERVER
                .comment("Allow saving for anyone without OP permissions")
                .define("allowSaving", true);

        useRemotes = SERVER
                .comment("Allow interacting (open gui) for all remotes")
                .define("usableRemote", true);

        useBindingRemotes = SERVER
                .comment("Allow binding remotes on any display")
                .define("usableRemoteBinding", true);

        SERVER.comment("Whitelist configuration: please stop bugging me with this :(");
        SERVER.push("whitelist");

        useWhitelist = SERVER
                .comment(
                        "Enables whitelist feature",
                        "[WARNING]: THE AUTHOR OF THE MOD (SRRAPERO720) IS NOT RESPONSIBLE IF IN YOUR SERVER SOMEONE PUTS NSFW MEDIA",
                        "WATERMEDIA HAVE SUPPORT FOR ADULT PAGES AND WHITELIST WAS DESIGNED TO PREVENT THAT",
                        "MIMIMIMI THE KIDS, SHUT UP, I GIVE YOU THE OPTIONS ENABLED _BY DEFAULT_ TO PREVENT THAT"
                )
                .define("enable", true);

        blackWhitelist = SERVER
                .comment("Moves the whitelist into the darkside")
                .define("blacklistMode", false);

        whitelist = SERVER.defineList("urls", Arrays.asList(WHITELIST), o -> HOSTS_PATTERN.matcher((String) o).find());

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

        clientSlavistMode = CLIENT
                .comment(
                        "overrides 'waterframes.multimedia.slavistMode'",
                        "Force-enable WATERMeDIA's slavist mode, slavism enhances youtube quality at the cost of have unstable playback"
                )
                .define("slavistMode", false);

        forceDevMode = CLIENT
                .comment(
                        "WARNING: DO NOT CHANGE IT EXCEPT IF YOU KNOW WHAT ARE YOU DOING, TOGGLING IT ON MAY CAUSE CORRUPTIONS, UNEXPECTED BEHAVIORS OR WORLD DESTRUCTION",
                        "forces WATERMeDIA and WATERFrAMES to run in developer mode",
                        "This is was done for developers who has mods that causes compatibilities with waterframes (or watermedia)",
                        "Let those modders test waterframes x incompatible mods (i see you stellarity owner)"
                )
                .define("forceDevMode", false);

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
    public static boolean forceLightOnPlay() { return forceLightsOnPlay.get(); }
    public static boolean useLagTickCorrection() { return useLagTickCorrection.get(); }

    // MULTIMEDIA
    public static int maxVolDis() { return maxVolumeDistance.get(); }
    public static int maxVolDis(int value) { return Math.min(value, maxVolDis()); }
    public static boolean useMasterVolume() { return useMasterVolume.get(); }
    public static boolean vsEurekaCompat() { return useVSEurekaCompat.get(); }

    public static int maxVol() { return maxVolume.get(); }
    public static int maxVol(int value) { return Math.max(Math.min(value, maxVol()), 0); }

    public static boolean useMultimedia() { return overrideServerConfig.get() ? clientUseMultimedia.get() : useMultimedia.get(); }
    public static boolean useSlavismMode() { return overrideServerConfig.get() ? clientSlavistMode.get() : useSlavismMode.get(); }
    public static void useSlavismMode(boolean value) { useSlavismMode.set(value); }

    // BEHAVIOR
    public static boolean useRedstone() { return useRedstone.get(); }
    public static boolean useMasterModeRedstone() { return useRedstone() && useMasterModeOnRedstone.get(); }
    public static int maxRcDis() { return remoteDistance.get(); }

    // PERMISSIONS
    public static boolean useInAdv() { return useInAdventure.get(); }
    public static boolean useInSurv() { return useInSurvival.get(); }
    public static boolean useForAnyone() { return useForAnyone.get(); }
    public static boolean useWhitelist() { return useWhitelist.get(); }
    public static boolean useWhitelist(boolean state) {
        useWhitelist.set(state);
        useWhitelist.save();
        return state;
    }
    public static boolean toggleWhitelist() {
        return useWhitelist(!useWhitelist());
    }
    public static void addOnWhitelist(String url) {
        @SuppressWarnings("unchecked")
        var w = (Set<String>) mutableSet(whitelist.get().iterator());
        w.add(url);
        whitelist.set(new ArrayList<>(w));
        whitelist.save();
    }
    public static boolean removeOnWhitelist(String url) {
        var w = mutableSet(whitelist.get().iterator());
        boolean removed = false;
        try {
            return removed = w.remove(url);
        } finally {
            if (removed) {
                whitelist.set(new ArrayList<>(w));
                whitelist.save();
            }
        }
    }

    public static boolean isWhiteListed(URI uri) {
        if (!useWhitelist()) return true;

        // watermedia driven protocol
        String scheme = uri.getScheme();
        if (scheme == null)
            return false; // no scheme, no url, in the best case this must never happend

        if (scheme.equals("water")) return true;

        var host = uri.getHost();
        if (host == null) return false;

        for (var s: whitelist.get()) {
            if (host.endsWith("." + s) || host.equals(s)) {
                return !blackWhitelist.get();
            }
        }
        return blackWhitelist.get();
    }
    public static <T> Set<T> mutableSet(Iterator<T> it) {
        var list = new HashSet<T>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    public static boolean canSave(Player player, String url) {
        URI uri = WaterFrames.createURI(url);
        boolean valid = uri != null || url.isEmpty();
        if (usePermissionsAPI.get()) {
            boolean canSave = WFRegistry.getPermBoolean(player.getUUID(), WFRegistry.PERM_DISPLAYS_EDIT);
            boolean canBypass = WFRegistry.getPermBoolean(player.getUUID(), WFRegistry.PERM_WHITELIST_BYPASS);
            boolean whitelisted = isWhiteListed(uri);

            if (canSave && (whitelisted || canBypass)) {
                return valid;
            }

            return false;
        } else {
            boolean canSave = allowSaving.get();
            if (isAdmin(player)) return valid;
            if (url.isEmpty()) return true;
            return valid && canSave && isWhiteListed(uri);
        }
    }

    public static boolean canInteractBlock(Player player, DisplayBlock block) {
        if (usePermissionsAPI.get()) {
            PermissionNode<Boolean> NODE = block.getPermissionNode();

            return WFRegistry.getPermBoolean(player.getUUID(), WFRegistry.PERM_DISPLAYS_INTERACT) || WFRegistry.getPermBoolean(player.getUUID(), NODE);
        } else {
            GameType gameType = (player instanceof ServerPlayer serverPlayer)
                    ? serverPlayer.gameMode.getGameModeForPlayer()
                    : Minecraft.getInstance().gameMode.getPlayerMode();

            if (isAdmin(player)) return true;
            if (!useInSurv() && gameType.equals(GameType.SURVIVAL)) return false;
            if (!useInAdv() && gameType.equals(GameType.ADVENTURE)) return false;
        }

        return useForAnyone();
    }

    public static boolean canInteractRemote(Player player) {
        if (usePermissionsAPI.get()) {
            return WFRegistry.getPermBoolean(player.getUUID(), WFRegistry.PERM_REMOTE_INTERACT) || isOwner(player);
        } else {
            if (isAdmin(player)) return true;
            return useRemotes.get();
        }
    }

    public static boolean canBindRemote(Player player) {
        if (usePermissionsAPI.get()) {
            return WFRegistry.getPermBoolean(player.getUUID(), WFRegistry.PERM_REMOTE_BIND) || isOwner(player);
        } else {
            if (isAdmin(player)) return true;
            return useBindingRemotes.get();
        }
    }

    public static boolean isAdmin(Player player) {
        Level level = player.level;

        // OWNER
        boolean owner = isOwner(player);
        if (owner) return true;

        if (level.isClientSide()) { // validate if was singleplayer and if was the admin
            IntegratedServer integrated = Minecraft.getInstance().getSingleplayerServer();
            if (integrated != null) {
                return integrated.isSingleplayerOwner(player.getGameProfile()) || player.hasPermissions(integrated.getOperatorUserPermissionLevel());
            } else { // its a guest, check perms
                return player.hasPermissions(4);
            }
        } else {
            return player.hasPermissions(4);
        }
    }

    public static boolean isOwner(Player player) {
        // OWNER
        String name = player.getGameProfile().getName();
        return name.equals("SrRaapero720") || name.equals("SrRapero720");
    }

    public static boolean isDevMode() {
        return !FMLLoader.isProduction() || forceDevMode.get();
    }
}
