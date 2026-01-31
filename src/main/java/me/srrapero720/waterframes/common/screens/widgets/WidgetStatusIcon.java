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

    @OnlyIn(Dist.CLIENT)
    public List<Component> getStatusTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!tile.data.active) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.off")));
            tooltip.add(translatable("waterframes.status.off.desc"));
            return tooltip;
        }

        if (tile.mrl == null && !tile.data.hasUrl()) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.AQUA + translate("waterframes.status.idle")));
            tooltip.add(translatable("waterframes.status.idle.desc"));
            return tooltip;
        }

        // assuming it was
        if (tile.mrl == null || tile.mrl.busy()) {
            tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.loading")));
            return tooltip;
        }

        if (tile.display != null) {
            if (tile.display.isNoEngine()) {
                tooltip.add(translatable("waterframes.status", ChatFormatting.RED + translate("waterframes.status.no_engine")));
                tooltip.add(translatable("waterframes.status.no_engine.desc"));
                return tooltip;
            }

            var status = switch (tile.display.status()) {
                case PLAYING, PAUSED, STOPPED, ENDED -> ChatFormatting.GREEN + translate("waterframes.status.operative");
                case LOADING, WAITING -> ChatFormatting.YELLOW + translate("waterframes.status.loading");
                case ERROR -> ChatFormatting.RED + translate("waterframes.download.exception.invalid");
                case BUFFERING -> ChatFormatting.YELLOW + translate("waterframes.status.buffering");
            };
            tooltip.add(translatable("waterframes.status", status));
        }

//        if (tile.mrl.isCache()) {
//            tooltip.add(translatable("waterframes.status.cache").withStyle(ChatFormatting.AQUA));
//        }

        return tooltip;
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getStatusIcon() {
        if (!tile.data.active) return IconStyles.STATUS_OFF;
        if (tile.mrl == null && !tile.data.hasUrl()) return IconStyles.STATUS_IDLE;
        else if (tile.mrl == null) return IconStyles.STATUS_LOADING; // ASSUMING IT WAS LOADING
        if (tile.display == null) return IconStyles.STATUS_ERROR;
        if (tile.display.isNoEngine()) return IconStyles.STATUS_ERROR;

        return switch (tile.display.status()) {
            case LOADING, WAITING -> IconStyles.STATUS_LOADING;
            case BUFFERING -> IconStyles.STATUS_BUFFERING;
            case ERROR -> IconStyles.STATUS_ERROR;
            case PLAYING, PAUSED, STOPPED, ENDED -> IconStyles.STATUS_OK;
        };
    }
}