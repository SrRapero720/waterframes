package me.srrapero720.waterframes;

import me.srrapero720.watercore.api.thread.ThreadUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import java.net.URI;
import java.util.*;

public class FramesConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.DoubleValue MAX_SIZE;
    public static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VLC;
    public static final ForgeConfigSpec.BooleanValue DISABLE_ADVENTURE;
    public static final ForgeConfigSpec.BooleanValue ONLY_CREATIVE;
    public static final ForgeConfigSpec.BooleanValue ONLY_ADMINS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WHITELIST;
    public static final ForgeConfigSpec.ConfigValue<List<String>> WHITELIST;

    static {
        // waterframes ->
        BUILDER.push("waterframes");

        // waterframes -> rendering
        BUILDER.push("rendering");
        DISABLE_VLC = BUILDER.define("disableVLC", false);
        MAX_SIZE = BUILDER.defineInRange("maxSize", 100.0D, 10.0D, Double.MAX_VALUE);
        MAX_RENDER_DISTANCE = BUILDER.defineInRange("maxRenderDistance", 1000, 10, Integer.MAX_VALUE);

        // waterframes ->
        BUILDER.pop();

        // waterframes -> behavior
        BUILDER.push("behavior");
        DISABLE_ADVENTURE = BUILDER.define("disableAdventure", true);
        ONLY_CREATIVE = BUILDER.define("onlyCreative", false);
        ONLY_ADMINS = BUILDER.define("onlyAdmins", false);
        ENABLE_WHITELIST = BUILDER.define("whitelistEnabled", false);
        WHITELIST = BUILDER.define("whitelist", Arrays.asList(
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
                "githubusercontent.com",
                "googleusercontent.com",
                "googleapis.com",
                "wikimedia.org",
                "ytimg.com",
                "youtube.com",
                "drive.google.com"
        ));
        // waterframes ->
        BUILDER.pop();

        // ->
        BUILDER.pop();

        //WATERFrAMES BASIC
        SPEC = BUILDER.build();
    }

    public static boolean isDomainWhitelisted(String domain) {
        if (domain != null)
            for (final var url: WHITELIST.get()) {
                var uri = url.trim().toLowerCase(Locale.ROOT);
                if (domain.endsWith("." + uri) || domain.equals(uri)) return true;
            }
        return false;
    }

    public static boolean canUse(Player player, String url) { return canUse(player, url, false); }

    public static boolean canUse(Player player, String url, boolean ignoreToggle) {
        var level = player.level;
        var server = level.getServer();
        if (!level.isClientSide && (server.isSingleplayer() || player.hasPermissions(server.getOperatorUserPermissionLevel()))) return true;

        if (ENABLE_WHITELIST.get() || ignoreToggle) return ThreadUtil.tryAndReturn((defaultVar) ->
                isDomainWhitelisted(new URI(url.toLowerCase(Locale.ROOT)).getHost()), false);
        return true;
    }

    public static GameType getGameType(Player player) {
        if (player instanceof ServerPlayer serverPlayer) return serverPlayer.gameMode.getGameModeForPlayer();
        return Minecraft.getInstance().gameMode.getPlayerMode();
    }

    public static boolean canInteract(Player player, Level level) {
        if (ONLY_CREATIVE.get() && !player.isCreative()) return false;

        var isOperator = Objects.requireNonNull(level.getServer()).isSingleplayer() || player.hasPermissions(level.getServer().getOperatorUserPermissionLevel());
        if (ONLY_ADMINS.get()) return isOperator;
        else return isOperator || (player.getAbilities().mayBuild);
    }
}