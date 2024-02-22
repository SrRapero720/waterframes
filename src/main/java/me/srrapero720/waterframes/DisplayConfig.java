package me.srrapero720.waterframes;

import me.srrapero720.waterframes.util.FrameTools;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.Locale;

public class DisplayConfig {
    private static final ForgeConfigSpec server_spec;
    private static final ForgeConfigSpec client_spec;

    // RENDERING
    private static ForgeConfigSpec.DoubleValue maxWidth;
    private static ForgeConfigSpec.DoubleValue maxHeight;
    private static ForgeConfigSpec.IntValue maxRenderDistance;
    private static ForgeConfigSpec.IntValue maxProjectionDistance;
    // MULTIMEDIA
    private static ForgeConfigSpec.IntValue maxVolumeDistance;
    private static ForgeConfigSpec.IntValue maxVolume;
    private static ForgeConfigSpec.BooleanValue useVideoLan;
    private static ForgeConfigSpec.BooleanValue keepsRendering;
    // BEHAVIOR
    private static ForgeConfigSpec.BooleanValue useRedstone;
    private static ForgeConfigSpec.EnumValue<RedstoneMode> redstoneMode;
    private static ForgeConfigSpec.BooleanValue overridingPlayback;
    // PERMISSIONS
    private static ForgeConfigSpec.BooleanValue usableOnAdventure;
    private static ForgeConfigSpec.BooleanValue usableOnSurvival;
    private static ForgeConfigSpec.BooleanValue usableForAny;
    private static ForgeConfigSpec.BooleanValue useWhitelist;
    private static ForgeConfigSpec.ConfigValue<List<String>> whitelist;

    // OVERRIDES (client)
    private final static ForgeConfigSpec.BooleanValue overrideServerConfig;
    private static ForgeConfigSpec.BooleanValue overrideUseVideolan;
    private static ForgeConfigSpec.BooleanValue overrideKeepsRendering;

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, server_spec, "waterframes-server.toml");
        if (FMLLoader.getDist().isClient()) // SKIPS TRASH CONFIG FILES ON SERVERS
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, client_spec, "waterframes-client.toml");
    }

    static {
        LazyBuilder serverBuilder = new LazyBuilder("waterframes");
        serverBuilder.group("rendering", b -> {
            maxWidth = b.defineInRange("maxWidth", 40d, 1d, 128d);
            maxHeight = b.defineInRange("maxHeight", 40d, 1d, 128d);
            maxRenderDistance = b.defineInRange("maxRenderDistance", 128, 8, 512);
            maxProjectionDistance = b.defineInRange("maxProjectionDistance", 32, 8, 128);
        }, "Configures all rendering limits, size, distance");

        serverBuilder.group("multimedia", b -> {
            maxVolumeDistance = b.defineInRange("maxVolumeDistance", 64, 8, 512);
            maxVolume =
                    b.comment("Values over 100 uses ÜberVolume of VLC")
                            .defineInRange("maxVolume", 100, 10, 200);

            useVideoLan = serverBuilder.booleanGroup("videoLan", true, b2 -> {
                keepsRendering = b2
                        .comment("Enables VLC media processing and game rendering for videos, disabling only plays audio")
                        .define("keepsRendering", true);
            }, "Enables VideoLAN usage for multimedia processing (support added by WATERMeDIA)");
        }, "Configures all settings related with multimedia player");

        serverBuilder.group("blockBehavior", b -> {
            useRedstone = serverBuilder.booleanGroup("redstonePlayback", true, b2 -> {
                redstoneMode = b2
                        .comment("Select what redstone behavior should have",
                                "IN: Input redstone changes playback",
                                "OUT: Emits restone with playback")
                        .defineEnum("redstoneMode", RedstoneMode.INPUT);

                overridingPlayback = b2
                        .comment(
                                "Redstone gets more powerful and forces playback to ignore any other sources of controlling",
                                "Only works when redstone behavior is on 'IN' mode"
                        )
                        .define("overridingPlayback", true);
            }, "Enables redstone playback for all DisplayBlocks");
        }, "Configures block behavior");

        serverBuilder.group("permissions", b -> {
            usableOnAdventure = b.comment("Changes if players in Adventure mode can use displays").define("usableOnAdventureMode", false);
            usableOnSurvival = b.comment("Changes if players in Survival mode can use displays").define("usableOnSurvivalMode", true);
            usableForAny = b.comment("Changes if any player can use displays, otherwise only admins can use it").define("usableForAnyone", true);

            useWhitelist = serverBuilder.booleanGroup("whitelist", true, b2 -> {
                whitelist = b2.define("url", FrameTools.readStringList("whitelist_url.json"));
            }, "Whitelist config");

        }, "Configures player permissions and url restrictions");

        LazyBuilder clientBuilder = new LazyBuilder("waterframes");
        overrideServerConfig = clientBuilder.booleanGroup("serverOverride", false, b -> {
            overrideUseVideolan = b.define("overrideUseVideoLan", true);
            overrideKeepsRendering = b.define("overrideKeepsRendering", true);
        }, "Overrides all configuration from current server. helpfully for custom changes");

        server_spec = serverBuilder.end();
        client_spec = clientBuilder.end();
    }

    // RENDERING
    public static float maxWidth() { return (float) (double) maxWidth.get(); }
    public static float maxHeight() { return (float) (double) maxHeight.get(); }

    public static int maxRenderDistance() { return maxRenderDistance.get(); }
    public static int maxRenderDistance(int value) { return Math.min(value, maxRenderDistance()); }

    public static int maxProjectionDistance() { return maxProjectionDistance.get(); }
    public static int maxProjectionDistance(int value) { return Math.min(value, maxProjectionDistance()); }

    // MULTIMEDIA
    public static int maxVolumeDistance() { return maxVolumeDistance.get(); }
    public static int maxVolumeDistance(int value) { return Math.min(value, maxVolumeDistance()); }

    public static int maxVolume() { return maxVolume.get(); }
    public static int maxVolume(int value) { return Math.max(Math.min(value, maxVolume()), 0); }

    public static boolean useVideoLan() { return overridesServerConfig() ? useVideoLan.get() : overridesUseVideoLan(); }
    public static boolean keepsRendering() { return overridesServerConfig() ? keepsRendering.get() : overridesKeepsRendering(); }

    // BEHAVIOR
    public static boolean useRedstone() { return useRedstone.get(); }
    public static RedstoneMode redstoneMode() { return redstoneMode.get(); }
    public static boolean overridingPlayback() { return overridingPlayback.get(); }
    // PERMISSIONS
    public static boolean usableOnAdventure() { return usableOnAdventure.get(); }
    public static boolean usableOnSurvival() { return usableOnSurvival.get(); }
    public static boolean usableForAny() { return usableForAny.get(); }
    public static boolean useWhitelist() { return useWhitelist.get(); }
    public static String[] whitelist() { return whitelist.get().toArray(new String[0]); }
    // OVERRIDES (client)
    private static boolean overridesServerConfig() { return overrideServerConfig.get(); }
    private static boolean overridesUseVideoLan() { return overrideUseVideolan.get(); }
    private static boolean overridesKeepsRendering() { return overrideKeepsRendering.get(); }

    public static boolean isWhiteListed(@NotNull String domain) {
        if (useWhitelist()) return true;

        String[] wl = whitelist();
        for (int i = 0; i < wl.length; i++) {
            String url = wl[i].trim().toLowerCase(Locale.ROOT);
            if (domain.endsWith("." + url) || domain.equals(url)) return true;
        }
        return false;
    }

    public static boolean canSave(Player player, String url) {
        Level level = player.getLevel();
        MinecraftServer server = level.getServer();
        if (level.isClientSide()) return true; // FIXME: on client breaks screens
        assert server != null;
        if (server.isSingleplayer() || player.hasPermissions(server.getOperatorUserPermissionLevel())) return true;

        try {
            return isWhiteListed(new URI(url).toURL().getHost().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    public static GameType getGameType(Player player) {
        if (player instanceof ServerPlayer serverPlayer) return serverPlayer.gameMode.getGameModeForPlayer();
        return Minecraft.getInstance().gameMode.getPlayerMode();
    }

    public static boolean canInteract(Player player, Level level) {
        var server = player.getServer();
        boolean isOperator = level.isClientSide() || (server != null && player.hasPermissions(server.getOperatorUserPermissionLevel()));

        if (!usableOnSurvival() && getGameType(player).equals(GameType.SURVIVAL) && !isOperator) return false;
        if (!usableOnAdventure() && getGameType(player).equals(GameType.ADVENTURE) && !isOperator) return false;
        if (!usableForAny()) return isOperator;

        return player.getAbilities().mayBuild;
    }


    public enum RedstoneMode {
        INPUT, OUTPUT;
    }

    public static final class LazyBuilder {
        private final ForgeConfigSpec.Builder BUILDER;

        public LazyBuilder(String name) {
            BUILDER = new ForgeConfigSpec.Builder();
            BUILDER.push(name);
        }

        public ForgeConfigSpec.BooleanValue booleanGroup(String name, boolean def, Group groupCreator, String... comments) {
            BUILDER.comment(comments);
            BUILDER.push(name);
            ForgeConfigSpec.BooleanValue result = BUILDER.comment("Toggle the option").define("enable", def);
            groupCreator.create(BUILDER);
            BUILDER.pop();
            return result;
        }

        public void group(String name, Group groupCreator, String... comments) {
            BUILDER.comment(comments);
            BUILDER.push(name);
            groupCreator.create(BUILDER);
            BUILDER.pop();
        }

        public ForgeConfigSpec end() {
            BUILDER.pop();
            return BUILDER.build();
        }

        public interface Group {
            void create(ForgeConfigSpec.Builder supplier);
        }
    }
}