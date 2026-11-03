package me.srrapero720.waterframes;

import me.srrapero720.waterframes.common.block.DisplayBlock;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.item.RemoteControl;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

public class WaterFramesConfig {
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
    private static final BooleanValue useSableCompat;
    private static final BooleanValue keepRendering;
    // BEHAVIOR
    private static final BooleanValue useLightsOnPlay;
    private static final BooleanValue forceLightsOnPlay;
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
    private static final BooleanValue overrideServerConfig;
    private static final BooleanValue clientKeepsRendering;
    private static final BooleanValue clientShaderMode;
    private static final BooleanValue soundEngineIntegration;

    private static final ModConfigSpec SERVER_SPEC;
    private static final ModConfigSpec CLIENT_SPEC;

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
                .comment("Max volume value")
                .defineInRange("maxVolume", 100, 10, 100);

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

        useSableCompat = SERVER
                .comment(
                        "Enables compatibility with Sable (used by Create: Simulated / Aeronautics contraptions)",
                        "Projects sub-level block positions to global space so audio attenuates correctly from contraption-mounted displays",
                        "Disable if sable misbehaves or audio stops working while using it"
                )
                .define("sableCompat", true);

        // WATERFRAMES -> block_behavior
        SERVER.comment("Configuration related to interactions with vanilla and modded features");
        SERVER.pop().push("block_behavior");

        useLightsOnPlay = SERVER
                .comment("Enable light feature on frames while is playing")
                .define("lightOnPlay", true);

        forceLightsOnPlay = SERVER
                .comment("Forces light feature on frames while is playing", "Requires lightOnPlay be true")
                .define("forceLightOnPlay", false);

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

        clientKeepsRendering = CLIENT
                .comment(
                        "overrides 'waterframes.rendering.keepRendering'",
                        "Enables media processing and rendering, disabling it will not render nothing, you can still hear videos"
                )
                .define("keepRendering", false);

        clientShaderMode = CLIENT
                .comment(
                        "Enables compatibility with shaders",
                        "This is not enabled by default because i am tired"
                )
                .define("shaderMode", false);

        CLIENT.pop();

        soundEngineIntegration = CLIENT
                .comment(
                        "Routes display audio through Minecraft's sound engine",
                        "Displays become regular game sounds: Jukebox/Note Blocks volume applies and other mods can detect and customize them (e.g. Sound Physics)",
                        "Disable to drive the OpenAL source directly like older versions"
                )
                .define("soundEngineIntegration", true);

        CLIENT.pop();

        // BULDING
        CLIENT_SPEC = CLIENT.build();
        SERVER_SPEC = SERVER.build();
    }

    public static void init(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        if (FMLLoader.getDist().isClient()) // SKIPS TRASH CONFIG FILES ON SERVERS
            container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }

    public static float maxWidth() { return (float) (double) maxWidth.get(); }
    public static float maxHeight() { return (float) (double) maxHeight.get(); }
    // EVERY CLAMP ALSO REFUSES NaN, INFINITIES AND NEGATIVES: A FORGED VALUE MUST NEVER MAKE A DISPLAY VANISH
    public static float maxWidth(float width) { return Float.isFinite(width) ? Math.clamp(width, 0.1f, maxWidth()) : maxWidth(); }
    public static float maxHeight(float height) { return Float.isFinite(height) ? Math.clamp(height, 0.1f, maxHeight()) : maxHeight(); }

    public static int maxRenDis() { return maxRenderDistance.get(); }
    public static int maxRenDis(int value) { return Math.clamp(value, 0, maxRenDis()); }

    public static float maxProjDis() { return (float) (double) maxProjectionDistance.get(); }
    public static float maxProjDis(float value) { return Float.isFinite(value) ? Math.clamp(value, 0f, maxProjDis()) : maxProjDis(); }

    public static boolean keepsRendering() { return overrideServerConfig.get() ? clientKeepsRendering.get() : keepRendering.get(); }
    public static boolean useLightOnPlay() { return useLightsOnPlay.get(); }
    public static boolean forceLightOnPlay() { return forceLightsOnPlay.get(); }

    // MULTIMEDIA
    public static int maxVolDis() { return maxVolumeDistance.get(); }
    public static int maxVolDis(int value) { return Math.clamp(value, 0, maxVolDis()); }
    public static boolean useMasterVolume() { return useMasterVolume.get(); }
    public static boolean soundIntegration() { return soundEngineIntegration.get(); }
    public static boolean vsEurekaCompat() { return useVSEurekaCompat.get(); }
    public static boolean sableCompat() { return useSableCompat.get(); }

    public static int maxVol() { return maxVolume.get(); }
    public static int maxVol(int value) { return Math.max(Math.min(value, maxVol()), 0); }

    // BEHAVIOR
    public static boolean useRedstone() { return useRedstone.get(); }
    public static boolean useMasterModeRedstone() { return useRedstone() && useMasterModeOnRedstone.get(); }
    public static int maxRcDis() { return remoteDistance.get(); }
    public static boolean shaderMode() { return clientShaderMode.get(); }
    public static void shaderMode(boolean value) {
        clientShaderMode.set(value);
        clientShaderMode.save();
    }

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
    /** Adds a host to the whitelist; false when it is not a bare host name, the same rule the config file enforces. */
    public static boolean addOnWhitelist(String url) {
        if (!HOSTS_PATTERN.matcher(url).find()) return false;
        Set<String> w = new HashSet<>(whitelist.get());
        w.add(url);
        whitelist.set(new ArrayList<>(w));
        whitelist.save();
        return true;
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

    public static boolean isWhiteListed(String url) {
        if (!useWhitelist()) return true;
        if (url == null || url.isEmpty()) return false;

        // watermedia driven protocol
        if (url.startsWith("water://")) return true;

        // Parse URL to extract scheme and host
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null) return false;

            var host = uri.getHost();
            if (host == null) return false;

            for (var s: whitelist.get()) {
                if (host.endsWith("." + s) || host.equals(s)) {
                    return !blackWhitelist.get();
                }
            }
            return blackWhitelist.get();
        } catch (Exception e) {
            return false;
        }
    }
    public static <T> Set<T> mutableSet(Iterator<T> it) {
        var list = new HashSet<T>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    /**
     * Whether a player may use a display from where they stand: the display's own permission, or a
     * remote bound to it in hand, and either way within remote range. Every command and data packet
     * from a client goes through here before it touches the block.
     */
    public static boolean canControl(final ServerPlayer player, final DisplayTile tile) {
        // REMOTE RANGE OR ARM REACH, WHICHEVER IS LONGER: A SHORT REMOTE RANGE MUST NOT REFUSE A HAND ON THE BLOCK
        final double reach = Math.max(maxRcDis(), player.blockInteractionRange() + 3);
        if (tile.getLevel() != player.level() || WaterFrames.getDistance(tile, player.position()) >= reach) return false;
        if (canInteractBlock(player, tile.getDisplayBlock())) return true;
        final BlockPos pos = tile.getBlockPos();
        return canInteractRemote(player) && (RemoteControl.bound(player.getMainHandItem(), player.level(), pos) || RemoteControl.bound(player.getOffhandItem(), player.level(), pos));
    }

    public static boolean canSave(Player player, String url) {
        boolean valid = WaterFrames.isValidUrl(url) || url.isEmpty();
        if (usePermissionsAPI.get()) {
            // THE PERMISSION HANDLER ONLY EXISTS ON THE SERVER; A CLIENT ASKING IS OPTIMISTIC AND THE SERVER DECIDES
            if (player.level().isClientSide) return valid;
            boolean canSave = WaterFrames.getPermBoolean(player.getUUID(), WaterFrames.PERM_DISPLAYS_EDIT);
            boolean canBypass = WaterFrames.getPermBoolean(player.getUUID(), WaterFrames.PERM_WHITELIST_BYPASS);
            // CLEARING A DISPLAY NEEDS NO WHITELIST, THERE IS NO HOST TO JUDGE
            boolean whitelisted = url.isEmpty() || isWhiteListed(url);

            if (canSave && (whitelisted || canBypass)) {
                return valid;
            }

            return false;
        } else {
            boolean canSave = allowSaving.get();
            if (isAdmin(player)) return valid;
            if (url.isEmpty()) return true;
            return valid && canSave && isWhiteListed(url);
        }
    }

    public static boolean canInteractBlock(Player player, DisplayBlock block) {
        if (usePermissionsAPI.get()) {
            if (player.level().isClientSide) return true;
            return WaterFrames.getPermBoolean(player.getUUID(), WaterFrames.PERM_DISPLAYS_INTERACT) || WaterFrames.getPermBoolean(player.getUUID(), block.caps.permission());
        } else {
            GameType gameType = (player instanceof final ServerPlayer serverPlayer)
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
            if (player.level().isClientSide) return true;
            return WaterFrames.getPermBoolean(player.getUUID(), WaterFrames.PERM_REMOTE_INTERACT) || isOwner(player);
        } else {
            if (isAdmin(player)) return true;
            return useRemotes.get();
        }
    }

    public static boolean canBindRemote(Player player) {
        if (usePermissionsAPI.get()) {
            if (player.level().isClientSide) return true;
            return WaterFrames.getPermBoolean(player.getUUID(), WaterFrames.PERM_REMOTE_BIND) || isOwner(player);
        } else {
            if (isAdmin(player)) return true;
            return useBindingRemotes.get();
        }
    }

    /** Operators and the owner of a singleplayer world, judged the same way on both sides so the screen never promises what the server refuses. */
    public static boolean isAdmin(Player player) {
        if (isOwner(player)) return true;
        if (player instanceof final ServerPlayer server) return server.hasPermissions(4) || server.server.isSingleplayerOwner(server.getGameProfile());
        IntegratedServer integrated = Minecraft.getInstance().getSingleplayerServer();
        return integrated != null ? integrated.isSingleplayerOwner(player.getGameProfile()) : player.hasPermissions(4);
    }

    public static boolean isOwner(Player player) {
        // OWNER
        String name = player.getGameProfile().getName();
        return name.equals("SrRaapero720") || name.equals("SrRapero720");
    }
}
