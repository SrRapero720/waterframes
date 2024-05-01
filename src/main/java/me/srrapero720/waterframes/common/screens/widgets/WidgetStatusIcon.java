package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.watermedia.api.player.PlayerAPI;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.gui.style.Icon;

import java.util.ArrayList;
import java.util.List;

public class WidgetStatusIcon extends GuiIcon {
    Icon lastIcon;

    private final DisplayTile tile;
    public WidgetStatusIcon(String name, Icon icon, DisplayTile tile) {
        super(name, icon);
        this.tile = tile;
    }

    @Override
    public void tick() {
        if (!isClient()) return;
        this.setTooltip(getStatusTooltip());
        this.setIcon(getStatusIcon());
        super.tick();
    }

    @Environment(EnvType.CLIENT)
    public List<Component> getStatusTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!tile.data.active) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.off")));
            tooltip.add(translatable("waterframes.status.off.desc"));
            return tooltip;
        }

        if (tile.imageCache == null && !tile.data.hasUri()) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.AQUA + translate("waterframes.status.idle")));
            tooltip.add(translatable("waterframes.status.idle.desc"));
            return tooltip;
        }

        // assuming it was
        if (tile.imageCache == null) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.loading")));
            return tooltip;
        }
        var status = switch (tile.imageCache.getStatus()) {
            case READY -> {
                if (tile.imageCache.isVideo()) {
                    if (!PlayerAPI.isReady())
                        yield ChatFormatting.RED + translate("waterframes.status.failed.video");
                    if (tile.display != null && tile.display.isBuffering())
                        yield ChatFormatting.YELLOW + translate("waterframes.status.buffering");
                    if (tile.display != null && tile.display.isBroken()) {
                        yield ChatFormatting.DARK_RED + translate("waterframes.status.not_working");
                    }
                }
                yield ChatFormatting.GREEN + translate("waterframes.status.operative");
            }
            case LOADING, WAITING -> ChatFormatting.YELLOW + translate("waterframes.status.loading");
            case FAILED -> {
                Throwable e = tile.imageCache.getException();
                if (e != null) {
                    if (e.getLocalizedMessage() != null && !e.getLocalizedMessage().isEmpty())
                        yield ChatFormatting.DARK_RED + e.getLocalizedMessage();
                    if (e.getCause() != null && e.getCause().getLocalizedMessage() != null && !e.getCause().getLocalizedMessage().isEmpty())
                        yield ChatFormatting.DARK_RED + e.getCause().getLocalizedMessage();
                    if (e.getCause() != null)
                        e = e.getCause();

                    yield ChatFormatting.DARK_RED + e.getClass().getSimpleName();
                }
                yield ChatFormatting.RED + translate("waterframes.download.exception.invalid");
            }
            case FORGOTTEN -> ChatFormatting.DARK_RED + translate("waterframes.status.not_working");
        };
        tooltip.add(translatable("waterframes.status", status));
        if (tile.imageCache.isCache()) {
            tooltip.add(translatable("waterframes.status.cache").withStyle(ChatFormatting.AQUA));
        }

        return tooltip;
    }

    @Environment(EnvType.CLIENT)
    public Icon getStatusIcon() {
        if (!tile.data.active) return IconStyles.STATUS_OFF;
        if (tile.imageCache == null && !tile.data.hasUri()) return IconStyles.STATUS_IDLE;
        else if (tile.imageCache == null) return IconStyles.STATUS_LOADING; // ASSUMING IT WAS LOADING

        return switch (tile.imageCache.getStatus()) {
            case READY -> {
                if (tile.imageCache.isVideo()) {
                    if (!PlayerAPI.isReady())
                        yield IconStyles.STATUS_INTERNAL_ERROR_2;
                    if (tile.display != null && (tile.display.isBuffering()))
                        yield IconStyles.STATUS_BUFFERING;
                    if (tile.display != null && tile.display.isBroken()) {
                        yield IconStyles.STATUS_ERROR;
                    }
                }

                yield tile.imageCache.isCache() ? IconStyles.STATUS_OK_CACHE : IconStyles.STATUS_OK;
            }
            case LOADING, WAITING -> IconStyles.STATUS_LOADING;
            case FAILED -> IconStyles.STATUS_ERROR;
            case FORGOTTEN -> lastIcon == null ? IconStyles.STATUS_WARN : lastIcon;
        };
    }
}