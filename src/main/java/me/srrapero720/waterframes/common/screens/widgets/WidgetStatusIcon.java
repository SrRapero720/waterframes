package me.srrapero720.waterframes.common.screens.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.cossporting.Crossponent;
import me.srrapero720.watermedia.api.image.ImageCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiIcon;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.text.TextBuilder;

import java.util.function.Supplier;

public class WidgetStatusIcon extends GuiIcon {
    private final Supplier<ImageCache> cacheSupplier;
    public WidgetStatusIcon(String name, int width, int height, Icon icon, Supplier<ImageCache> cacheSupplier) {
        super(name, width, height, icon);
        this.cacheSupplier = cacheSupplier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void renderContent(PoseStack pose, GuiChildControl guiChildControl, Rect rect, int i, int i1) {
        ImageCache cache = cacheSupplier.get();
        icon = switch (cache != null ? cache.getStatus() : ImageCache.Status.FAILED) {
            case READY -> {
                setTooltip(new TextBuilder().add(Crossponent.translatable("waterframes.status.operative")).build());
                yield  IconStyles.STATUS_OK;
            }
            case LOADING -> {
                setTooltip(new TextBuilder().add(Crossponent.translatable("waterframes.status.loading")).build());
                yield IconStyles.STATUS_ALERT;
            }
            case WAITING, FORGOTTEN -> {
                if (cacheSupplier.get().url.isEmpty()) {
                    setTooltip(new TextBuilder().add(Crossponent.translatable("waterframes.status.idle")).build());
                    yield IconStyles.STATUS_IDLE;
                } else {
                    setTooltip(new TextBuilder().add(Crossponent.translatable("waterframes.status.wrong")).build());
                    yield IconStyles.STATUS_PEM;
                }
            }
            case FAILED -> {
                if (cache != null) {
                    setTooltip(new TextBuilder(cacheSupplier.get().getException().getMessage()).build());
                } else {
                    setTooltip(new TextBuilder().add(Crossponent.translatable("download.exception.invalid")).build());
                }
                yield IconStyles.STATUS_ERROR;
            }
        };
        super.renderContent(pose, guiChildControl, rect, i, i1);
    }
}