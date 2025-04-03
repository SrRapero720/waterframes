package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.DataListSyncPacket;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetPlaylistEntry;
import me.srrapero720.waterframes.common.screens.widgets.WidgetURLTextField;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.net.URI;
import java.util.LinkedList;

public class PlayListScreen extends GuiLayer {
    protected static final int WIDTH = 225;
    protected static final int HEIGHT = 210;

    // IMPORTANT
    public final DisplayTile tile;
    private final GuiButtonIcon save;
    public LinkedList<URI> uris;
    public int uri_index = 0;

    // SCROLL
    public final GuiScrollY scrollY;
    public final GuiParent list;
    public final WidgetURLTextField urlTextField;
    public final GuiButtonIcon addButton;

    public PlayListScreen(DisplayTile tile) {
        super("display_screen", WIDTH, HEIGHT);
        this.setFlow(GuiFlow.STACK_Y);
        this.tile = tile;
        this.uris = new LinkedList<>();
        this.scrollY = new GuiScrollY("parent_scroll");
        this.list = new GuiParent(GuiFlow.STACK_Y);
        this.scrollY.addControl(list);

        for (URI uri: tile.data.uris) {
            this.list.addControl(new WidgetPlaylistEntry(tile, this.uris, uri));
        }

        this.urlTextField = new WidgetURLTextField(null);
        this.addButton = new GuiButtonIcon("add", IconStyles.ADD, mouse -> {
            if (urlTextField.isUrlValid()) {
                this.list.addControl(new WidgetPlaylistEntry(tile, this.uris, urlTextField.getURI()));
                this.urlTextField.setText("");
            }
        });
        this.save = new GuiButtonIcon("save", IconStyles.SAVE, click ->
                DisplayNetwork.sendServer(new DataListSyncPacket(tile.getBlockPos(), DisplayData.build(this, tile)))
        );
    }

    public LinkedList<URI> getUris() {
        LinkedList<URI> uris = new LinkedList<>();
        for (GuiChildControl control: this.list) {
            if (control.control instanceof WidgetPlaylistEntry element) {
                uris.add(element.uri);
            }
        }
        return uris;
    }

    @Override
    public void create() {
        this.add(scrollY.setExpandable());
        this.add(new GuiParent(GuiFlow.STACK_X)
                .add(urlTextField.setExpandableX())
                .add(addButton)
                .add(save)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public GuiStyle getStyle() { return ScreenStyles.DISPLAYS; }
}
