package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.net.URI;
import java.util.LinkedList;

public class WidgetPlaylistEntry extends GuiParent {

    public final URI uri;
    public final LinkedList<URI> list;
    public final DisplayTile tile;
    private final GuiButtonIcon reload;
    private boolean added = false;

    public WidgetPlaylistEntry(DisplayTile tile, LinkedList<URI> list, URI uri) {
        super("experimental_element_" + uri.toString());
        this.uri = uri;
        this.tile = tile;
        this.list = list;

        this.setDim(0, 40);
        this.setSpacing(4);
        this.setExpandableX();
        this.setVAlign(VAlign.CENTER);

        this.reload = new GuiButtonIcon("reload", IconStyles.RELOAD, mouse -> {
            if (mouse != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
            tile.imageCache.reload();
        });

        list.add(uri);
        this.add(new GuiParent("").setDim(4, 1));
        this.add(new GuiLabel("name").setTitle(Component.literal(uri.toString().substring(uri.toString().indexOf(uri.getScheme())))).setExpandableX());
        this.add(this.checkReload(), () -> reload.setDim(12, 12));
        this.add(new GuiButtonIcon("remove", IconStyles.REMOVE, mouse -> {
            if (mouse != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
            ((GuiParent) this.getParent()).remove(this);
            list.remove(uri);
            this.getParent().reflow();
        }).setDim(12, 12));
        this.add(new GuiParent("").setDim(4, 1));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        return tile.data.hasUri() && tile.data.getUri().equals(uri) ? ScreenStyles.DARK_BLUE_HIGHLIGHT : ScreenStyles.DARK_BLUE_BACKGROUND;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(Rect rect, double x, double y, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            Util.getPlatform().openUri(this.uri);
        }
        return super.mouseClicked(rect, x, y, button);
    }

    @Override
    public void tick() {
        super.tick();

        if (!isClient())
            return;

        this.reload.setEnabled(this.checkReload());
        this.reload.setVisible(this.reload.enabled);
    }

    private boolean checkReload() {
        return tile.data.hasUri() && tile.data.getUri().equals(uri);
    }
}
