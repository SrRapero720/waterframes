package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.DataListSyncPacket;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.WidgetPlaylistEntry;
import me.srrapero720.waterframes.common.screens.widgets.WidgetURLTextField;
import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiStateButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;

public class PlayListScreen extends GuiLayer {
    protected static final int WIDTH = 225;
    protected static final int HEIGHT = 210;

    // IMPORTANT
    public final DisplayTile tile;
    private final GuiButtonIcon save;
    public LinkedList<URI> uris;

    // SCROLL
    public final GuiScrollY scrollY;
    public final GuiParent list;
    public final WidgetURLTextField urlTextField;
    public final GuiButtonIcon addButton;

    public final GuiButtonIcon prevButton;
    public final GuiButtonIcon nextButton;
    public final GuiCheckButtonIcon playButton;

    public PlayListScreen(DisplayTile tile) {
        super("display_screen", WIDTH, HEIGHT);
        this.setFlow(GuiFlow.STACK_Y);
        this.tile = tile;
        this.uris = new LinkedList<>();
        this.scrollY = new GuiScrollY("parent_scroll");
        this.list = new GuiParent(GuiFlow.STACK_Y);
        this.scrollY.addControl(list);
        this.setSpacing(4);

        for (URI uri: tile.data.uris) {
            this.list.addControl(new WidgetPlaylistEntry(tile, this.uris, uri));
        }

        this.urlTextField = new WidgetURLTextField(null);
        this.addButton = new GuiButtonIcon("add", IconStyles.ADD, mouse -> {
            if (urlTextField.isUrlValid()) {
                this.list.addControl(new WidgetPlaylistEntry(tile, this.uris, urlTextField.getURI()));
                this.urlTextField.setText("");
                this.reflow();
            }
        });
        this.save = new GuiButtonIcon("save", IconStyles.SAVE, click ->
                DisplayNetwork.sendServer(new DataListSyncPacket(tile.getBlockPos(), DisplayData.build(this, tile)))
        );

        this.playButton = new GuiCheckButtonIcon("playback", IconStyles.PLAY, IconStyles.PAUSE, tile.data.paused, button -> tile.setPause(true, !tile.data.paused));
        this.nextButton = new GuiButtonIcon("next", IconStyles.NEXT_MEDIA, button -> tile.nextUri(true));
        this.prevButton = new GuiButtonIcon("prev", IconStyles.BACK_MEDIA, button -> tile.prevUri(true));
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
        this.add(new GuiParent("", GuiFlow.STACK_X, Align.STRETCH)
                .add(prevButton.setSquared(true).setDim(1, 18).setExpandableX())
                .add(playButton.setSquared(true).setDim(1, 18).setExpandableX())
                .add(nextButton.setSquared(true).setDim(1, 18).setExpandableX())
        );
        this.add(scrollY.setExpandable());
        this.add(new GuiParent(GuiFlow.STACK_X)
                .add(urlTextField.setDim(1, 12).setExpandableX())
                .add(addButton.setDim(12, 12))
                .add(save.setDim(12, 12))
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!isClient())
            return;

        // TOOLTIPS REFRESH
        if (this.playButton.getState() != tile.data.paused) {
            this.playButton.setState(tile.data.paused);
            this.playButton.setTooltip(Collections.singletonList(
                    translatable("waterframes.gui.playback", ChatFormatting.AQUA + translate("waterframes.common." + (this.playButton.value ? "paused" : "playing")))
            ));
        }
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
