package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.IGuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.net.URI;
import java.util.LinkedList;

public class WidgetPlaylistEntry extends GuiParent {

    public final URI uri;
    public final LinkedList<URI> list;
    public final DisplayTile tile;
    private final GuiButtonIcon reload;
    private boolean added = false;

    public WidgetPlaylistEntry(DisplayTile tile, LinkedList<URI> list, URI uri) {
        super((IGuiParent) null, "experimental_element_" + uri.toString());
        this.uri = uri;
        this.tile = tile;
        this.list = list;

        this.setDim(0, 40);
        this.setSpacing(4);
        this.setExpandableX();
        this.setVAlign(VAlign.CENTER);

        this.reload = new GuiButtonIcon(this, "reload", IconStyles.RELOAD, mouse -> {
            if (mouse != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
            tile.imageCache.reload();
        });

        list.add(uri);
        this.add(new GuiParent(this, "").setDim(4, 1));
        this.add(new GuiLabel(this, "name").setTitle(Component.literal(uri.toString().substring(uri.toString().indexOf(uri.getScheme())))).setExpandableX());
        this.add(this.checkReload(), () -> reload.setDim(12, 12));
        this.add(new GuiButtonIcon(this, "remove", IconStyles.REMOVE, mouse -> {
            if (mouse != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
            ((GuiParent) this.getParent()).remove(this);
            list.remove(uri);
            this.getParent().reflow();
        }).setDim(12, 12));
        this.add(new GuiParent(this, "").setDim(4, 1));
    }

    @Override
    public void tick() {
        super.tick();

        if (!isClient())
            return;

        this.reload.setEnabled(this.checkReload());
        this.reload.setVisible(this.checkReload());
    }

    private boolean checkReload() {
        return tile.data.hasUri() && tile.data.getUri().equals(uri);
    }
}
