package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.text.TextBuilder;

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
        TextBuilder builder = new TextBuilder();
        if (!tile.data.active) {
            builder.translate("waterframes.status.off.1");
            builder.translate("waterframes.status.off.2");
            return builder.build();
        }
        if (tile.imageCache == null) {
            builder.translate("waterframes.download.exception.invalid");
            return builder.build();
        }
        switch (tile.imageCache.getStatus()) {
            case READY -> builder.translate("waterframes.status.operative");
            case LOADING -> builder.translate("waterframes.status.loading");
            case FAILED -> builder.translate("download.exception.invalid");
            case WAITING, FORGOTTEN -> {
                if (tile.imageCache.url.isEmpty())
                    builder.translate("waterframes.status.idle");
                else
                    builder.translate(tile.imageCache.getException().getLocalizedMessage()).build();
            }
        };

        return builder.build();
    }

    @OnlyIn(Dist.CLIENT)
    public Icon getStatusIcon() {
        if (!tile.data.active) return IconStyles.STATUS_OFF;
        if (tile.imageCache == null) return IconStyles.STATUS_ERROR;
        return switch (tile.imageCache.getStatus()) {
            case READY -> IconStyles.STATUS_OK;
            case LOADING -> IconStyles.STATUS_WARN;
            case FAILED -> IconStyles.STATUS_ERROR;
            case WAITING, FORGOTTEN -> tile.imageCache.url.isEmpty() ? IconStyles.STATUS_IDLE : IconStyles.STATUS_ERROR;
        };
    }
}