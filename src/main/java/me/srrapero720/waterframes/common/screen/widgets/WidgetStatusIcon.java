package me.srrapero720.waterframes.common.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.screen.widgets.custom.CustomIcons;
import me.srrapero720.watermedia.api.image.ImageCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.text.TextBuilder;

import java.util.Collections;
import java.util.function.Supplier;

public class WidgetStatusIcon extends WidgetIcon {
    private final Supplier<ImageCache> cache;
    public WidgetStatusIcon(String name, int width, int height, GuiIcon icon, Supplier<ImageCache> cache) {
        super(name, width, height, icon);
        this.cache = cache;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl guiChildControl, Rect rect, int i, int i1) {
        icon = switch (cache.get().getStatus()) {
            case READY -> {
                setTooltip(new TextBuilder().build());
                yield  CustomIcons.STATUS_OK;
            }
            case LOADING -> {
                setTooltip(new TextBuilder("Loading media").build());
                yield CustomIcons.STATUS_ALERT;
            }
            case WAITING, FORGOTTEN -> {
                if (cache.get().url.isEmpty()) {
                    setTooltip(Collections.emptyList());
                    yield CustomIcons.STATUS_CHILL;
                } else {
                    setTooltip(new TextBuilder("Something wrong happens").build());
                    yield CustomIcons.STATUS_PEM;
                }
            }
            case FAILED -> {
                setTooltip(new TextBuilder(cache.get().getException().getMessage()).build());
                yield CustomIcons.STATUS_ERROR;
            }
        };
        super.renderContent(pose, guiChildControl, rect, i, i1);
    }
}