package me.srrapero720.waterframes;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import team.creative.creativecore.common.config.api.CreativeConfig;
import team.creative.creativecore.common.config.sync.ConfigSynchronization;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Deprecated(forRemoval = true)
//TODO: Replace this class with server.LittleConfig
public class LittleFramesConfig {
    
    @CreativeConfig
    public double sizeLimitation = 1000.0;
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public int maxRenderDistance = 10000;
    
    @CreativeConfig
    public boolean onlyOps = false;
    
    @CreativeConfig
    public boolean disableAdventure = true;
    
    @CreativeConfig
    public boolean onlyCreative = false;
    
    @CreativeConfig
    public boolean whitelistEnabled = false;
    
    @CreativeConfig(type = ConfigSynchronization.CLIENT)
    public boolean useVLC = true;
    
    @CreativeConfig
    public List<String> whitelist = Arrays
            .asList("imgur.com", "gyazo.com", "prntscr.com", "tinypic.com", "puu.sh", "pinimg.com", "photobucket.com", "staticflickr.com", "flic.kr", "tenor.co", "gfycat.com", "giphy.com", "gph.is", "gifbin.com", "i.redd.it", "media.tumblr.com", "twimg.com", "discordapp.com", "images.discordapp.net", "githubusercontent.com", "googleusercontent.com", "googleapis.com", "wikimedia.org", "ytimg.com");
    
    public boolean canUse(Player player, String url) {
        return canUse(player, url, false);
    }
    
    public boolean canUse(Player player, String url, boolean ignoreToggle) {
        Level level = player.level;
        if (!level.isClientSide && (level.getServer().isSingleplayer() || player.hasPermissions(level.getServer().getOperatorUserPermissionLevel()))) {
            return true;
        }
        if (whitelistEnabled || ignoreToggle) {
            try {
                return isDomainWhitelisted(new URI(url.toLowerCase(Locale.ROOT)).getHost());
            } catch (URISyntaxException e) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isDomainWhitelisted(String domain) {
        if (domain != null) {
            for (String url : whitelist) {
                String formattedUrl = url.trim().toLowerCase(Locale.ROOT);
                if (domain.endsWith("." + formattedUrl) || domain.equals(formattedUrl)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static GameType getGameType(Player player) {
        if (player instanceof ServerPlayer)
            return ((ServerPlayer) player).gameMode.getGameModeForPlayer();
        return Minecraft.getInstance().gameMode.getPlayerMode();
    }
    
    public boolean canInteract(Player player, Level level) {
        if (disableAdventure && getGameType(player) == GameType.ADVENTURE)
            return false;
        if (onlyCreative && !player.isCreative())
            return false;
        boolean isOperator = level.getServer().isSingleplayer() || player.hasPermissions(level.getServer().getOperatorUserPermissionLevel());
        if (onlyOps)
            return isOperator;
        else
            return isOperator || (!disableAdventure || player.getAbilities().mayBuild);
        
    }
}
