package me.srrapero720.waterframes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LittleConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    private static final Map<String, ForgeConfigSpec.ConfigValue<?>> CONFIGS = new HashMap<>();

    static {
        // Initial config
        BUILDER.push("LittleFrames");

        CONFIGS.put("sizeLimitation",
                BUILDER.defineInRange("sizeLimitation", 1000.0D, 10.0D, Double.MAX_VALUE));
        CONFIGS.put("maxRenderDistance",
                BUILDER.defineInRange("maxRenderDistance", 1000, 10, Integer.MAX_VALUE));

        CONFIGS.put("useVLC", BUILDER.define("useVLC", true));
//        CONFIGS.put("disableAdventure", BUILDER.define("disableAdventure", true));
        CONFIGS.put("onlyCreative", BUILDER.define("onlyCreative", false));
        CONFIGS.put("onlyOps", BUILDER.define("onlyOps", false));
        CONFIGS.put("whitelistEnabled", BUILDER.define("whitelistEnabled", false));
        CONFIGS.put("whitelist", BUILDER.define("whitelist", new String[] { "imgur.com", "gyazo.com", "prntscr.com", "tinypic.com", "puu.sh", "pinimg.com", "photobucket.com", "staticflickr.com", "flic.kr", "tenor.co", "gfycat.com", "giphy.com", "gph.is", "gifbin.com", "i.redd.it", "media.tumblr.com", "twimg.com", "discordapp.com", "images.discordapp.net", "githubusercontent.com", "googleusercontent.com", "googleapis.com", "wikimedia.org", "ytimg.com" }));


        BUILDER.pop();

        //Watercore Basics
        SPEC = BUILDER.build();
    }

    @SuppressWarnings("UncheckedCast")
    public static <T> T get(String name) {
        return (T) CONFIGS.get(name).get();
    }

    public static boolean getBoolean(String name) throws RuntimeException {
        var data = CONFIGS.get(name).get();
        if (data instanceof Boolean) {
            return (boolean) data;
        } else throw new RuntimeException("Failed to cast server config in LittleFrames");
    }

    public static boolean canUse(Player player, String url) {
        return canUse(player, url, false);
    }

    public static boolean canUse(Player player, String url, boolean ignoreToggle) {
        Level level = player.level;
        if (!level.isClientSide && (level.getServer().isSingleplayer() || player.hasPermissions(level.getServer().getOperatorUserPermissionLevel()))) {
            return true;
        }
        if ((Boolean) get("whitelistEnabled") || ignoreToggle) {
            try {
                return isDomainWhitelisted(new URI(url.toLowerCase(Locale.ROOT)).getHost());
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isDomainWhitelisted(String domain) {
        if (domain != null) {
            for (String url : (String[]) get("whitelist")) {
                String formattedUrl = url.trim().toLowerCase(Locale.ROOT);
                if (domain.endsWith("." + formattedUrl) || domain.equals(formattedUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean canInteract(Player player, Level level) {
        if (getBoolean("onlyCreative") && !player.isCreative()) return false;

        var isOperator = level.getServer().isSingleplayer() || player.hasPermissions(level.getServer().getOperatorUserPermissionLevel());
        if (getBoolean("onlyOps")) return isOperator;
        else return isOperator || (player.getAbilities().mayBuild);
    }
}
