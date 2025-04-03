package me.srrapero720.waterframes.common.screens.widgets;

import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.style.Icon;

import java.net.URI;
import java.util.LinkedList;

public class WidgetPlaylistEntry extends GuiParent {

    public final URI uri;
    public final LinkedList<URI> list;
    public final DisplayTile tile;

    public WidgetPlaylistEntry(DisplayTile tile, LinkedList<URI> list, URI uri) {
        super("experimental_element_" + uri.toString());
        this.uri = uri;
        this.tile = tile;
        this.list = list;

        list.add(uri);
        this.add(new GuiLabel("name").setTitle(Component.literal(uri.toString())).setExpandableX());
        this.add(new GuiButtonIcon("remove", Icon.EMPTY, mouse -> {
            ((GuiParent) this.getParent()).remove(this);
            list.remove(uri);
        }));
    }
}
