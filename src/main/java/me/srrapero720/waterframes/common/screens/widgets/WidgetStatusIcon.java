package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.gui.style.Icon;

import java.util.ArrayList;
import java.util.List;

public class WidgetStatusIcon extends GuiIcon {

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

    @OnlyIn(Dist.CLIENT)
    public List<Component> getStatusTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!tile.data.active) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.off")));
            tooltip.add(translatable("waterframes.status.off.desc"));
            return tooltip;
        }
        if (tile.imageCache == null) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.failed")));
            return tooltip;
        }
        var status = switch (tile.imageCache.getStatus()) {
            case READY -> ChatFormatting.GREEN + translate("waterframes.status.operative");
            case LOADING -> ChatFormatting.AQUA + translate("waterframes.status.loading");
            case FAILED -> ChatFormatting.RED + translate("waterframes.download.exception.invalid");
            case WAITING, FORGOTTEN -> {
                if (tile.imageCache.uri == null)
                    yield ChatFormatting.AQUA + translate("waterframes.status.idle");
                else {
                    Exception e = tile.imageCache.getException();
                    if (e != null) {
                        yield ChatFormatting.DARK_RED + tile.imageCache.getException().getLocalizedMessage();
                    } else {
                        yield ChatFormatting.DARK_RED + translate("waterframes.status.not_working");
                    }
                }
            }
        };
        tooltip.add(translatable("waterframes.status", status));
        if (tile.imageCache.isCache()) {
            tooltip.add(translatable("waterframes.status.cache").withStyle(ChatFormatting.AQUA));
        }

        return tooltip;
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getStatusIcon() {
        if (!tile.data.active) return IconStyles.STATUS_OFF;
        if (tile.imageCache == null) return IconStyles.STATUS_ERROR;
        return switch (tile.imageCache.getStatus()) {
            case READY -> tile.imageCache.isCache() ? IconStyles.STATUS_OK_CACHE : IconStyles.STATUS_OK;
            case LOADING -> IconStyles.STATUS_WARN;
            case FAILED -> IconStyles.STATUS_ERROR;
            case WAITING, FORGOTTEN -> tile.imageCache.uri == null ? IconStyles.STATUS_IDLE : IconStyles.STATUS_ERROR;
        };
    }
}