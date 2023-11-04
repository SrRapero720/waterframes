package me.srrapero720.waterframes.common.screen;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.entity.TvTile;
import me.srrapero720.waterframes.common.data.DisplayData;
import me.srrapero720.waterframes.common.data.TvData;
import me.srrapero720.waterframes.common.screen.widgets.*;
import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetIcons;
import me.srrapero720.waterframes.util.FrameNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiScrollY;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;

import java.util.List;

public class TvScreen extends DisplayScreen<TvTile> {
    int customWidth = 230;
    int customHeight = 210;

    // PARENTS
    private WidgetDoubleTable textureSettingsTable;
    private WidgetDoubleTable actionsTable;

    private GuiScrollY elementsList;

    // WIDGETS
    private WidgetTextFieldTrigger urlTextField;
    private WidgetSlider volumeSlider;
    private GuiSteppedSlider volumeMinSlider;
    private WidgetSteppedSlider volumeMaxSlider;
    private GuiButton saveBtn;

    // ICONS
    private WidgetIcon rotationIcon;
    private WidgetIcon transparencyIcon;
    private WidgetIcon brightnessIcon;
    private WidgetIcon distanceIcon;
    private WidgetIcon volumeIcon;
    private WidgetParent mediaControlsParent;

    public TvScreen(TvTile tileBlock) {
        super("frame_screen", tileBlock, 380, 180);
    }

    @Override
    protected void onCreate() {
        this.urlTextField = new WidgetTextFieldTrigger(() -> this.saveBtn, DisplayData.URL, tileBlock.getUrl()).expandX();

        this.textureSettingsTable = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_X, Align.STRETCH).setSpacing(4)).setSpacing(4)
                .addOnFirstNewParent(GuiFlow.STACK_X,
                        this.rotationIcon = new WidgetIcon("r_icon", 12, 12, WidgetIcons.ROTATION),
                        new WidgetSlider(DisplayData.ROTATION, 90, 10, tileBlock.data.rotation, 0, 360, WidgetSlider.ANGLE))
                .addOnSecondNewParent(GuiFlow.STACK_X,
                        this.brightnessIcon = new WidgetIcon("b_icon", 12, 12, WidgetIcons.BRIGHTNESS),
                        new WidgetSlider(DisplayData.BRIGHTNESS, 90, 10, tileBlock.data.brightness, 0, 1, WidgetSlider.PERCENT))
                .createRow()
                .addOnFirstNewParent(GuiFlow.STACK_X,
                        this.transparencyIcon = new WidgetIcon("t_icon", 12, 12, WidgetIcons.TRANSPARENCY),
                        new WidgetSlider(DisplayData.ALPHA, 90, 10, tileBlock.data.alpha, 0, 1, WidgetSlider.PERCENT))
                .addOnSecondNewParent(GuiFlow.STACK_X,
                        this.distanceIcon = new WidgetIcon("d_icon", 12, 12, WidgetIcons.DISTANCE),
                        new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, 90, 10, tileBlock.data.renderDistance, 5, 1024))
                .createRow()
                .expandY();


        this.mediaControlsParent = new WidgetParent("media_controls", GuiFlow.STACK_Y, Align.CENTER, VAlign.CENTER).setSpacing(4);
        this.mediaControlsParent
                .add2(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(this.volumeIcon = new WidgetIcon("v_icon", 12, 12, WidgetIcons.getVolumeIcon(tileBlock.data.volume)))
                        .add2(this.volumeSlider = new WidgetSlider(DisplayData.VOLUME, 50, 10, tileBlock.data.volume, 0, DisplayConfig.maxVolume(), WidgetSlider.PERCENT))
                        .add2(new WidgetIcon("v_min_icon", 12, 12, WidgetIcons.VOLUME_RANGE_MIN))
                        .add2(this.volumeMinSlider = new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, 50, 10, tileBlock.data.minVolumeDistance, 0, Math.min(DisplayConfig.maxVolumeDistance(), tileBlock.data.maxVolumeDistance)))
                        .add2(new WidgetIcon("v_max_icon", 12, 12, WidgetIcons.VOLUME_RANGE_MAX))
                        .add2(this.volumeMaxSlider = new WidgetSteppedSlider(DisplayData.VOL_RANGE_MAX, volumeMinSlider, 50, 10, tileBlock.data.maxVolumeDistance, 0, DisplayConfig.maxVolumeDistance()))
                        .setAlign(Align.STRETCH))
                .add2(new WidgetSeekBar("seek", 200, 12, tileBlock.data.tick, 0, tileBlock.display != null ? tileBlock.display.durationInTicks() : 1, () -> tileBlock.data.tick)
                        .addOnMouseGrab(seekBar -> tileBlock.data.tick = (int) seekBar.value)
                        .addOnMouseRelease(seekBar -> FrameNet.syncPlaybackState(tileBlock.getBlockPos(), tileBlock.data.playing, tileBlock.data.tick = (int) seekBar.value)))
                .add2(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new GuiCheckBox(DisplayData.LOOP, tileBlock.data.loop).setTranslate("gui.waterframes.loop"))
                        .add2(new GuiCheckBox(DisplayData.FLIP_X, tileBlock.data.flipX).setTranslate("gui.waterframes.flipx"))
                        .add2(new GuiCheckBox(DisplayData.FLIP_Y, tileBlock.data.flipY).setTranslate("gui.waterframes.flipy")))
                .add2(new WidgetParent("media_controls_btn", GuiFlow.STACK_X, Align.CENTER, VAlign.CENTER)
                        .add2(new GuiIconButton("back_media", WidgetIcons.BACK_MEDIA, button -> fastBackwards.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("fast_backward", WidgetIcons.BACK_10, button -> fastBackwards.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("pause", WidgetIcons.PAUSE, button -> pauseAction.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("play", WidgetIcons.PLAY, button -> playAction.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("fast_forward", WidgetIcons.SKIP_10, button -> fastForward.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("next_media", WidgetIcons.NEXT_MEDIA, button -> fastForward.send(EndTag.INSTANCE)))
        );

        WidgetDoubleTable baseTable = new WidgetDoubleTable(GuiFlow.STACK_Y).setSpacing(4);
        baseTable
                .addOnFirst(textureSettingsTable)
                .addOnFirst(mediaControlsParent)
                .addOnSecond(new WidgetParent("", GuiFlow.STACK_Y)
                        .add2(new WidgetParent(GuiFlow.STACK_X)
                                .add2(this.urlTextField.setHeight(12))
                                .add2(new GuiIconButton("", WidgetIcons.ADD, x -> {}))
                        )
                        .add2(this.elementsList = (GuiScrollY) new GuiScrollY("elements_list").setExpandableX().setExpandableY())
                        .add2(new WidgetParent("", GuiFlow.STACK_X)
                                .add2(this.saveBtn = (GuiButton) new GuiButton("save", x -> syncAction.send(TvData.build(this))).setTranslate("gui.waterframes.save"))
                        )
                        .setSpacing(2)
                        .setAlign(Align.RIGHT)
                        .setExpandableY()
                );
        baseTable.getFirstRow().setAlign(Align.STRETCH).setWidth(230);
        baseTable.getSecondRow().setAlign(Align.CENTER).setWidth(140);
        this.add(baseTable);


        // DATA PROCESSOR
        List<String> urls = tileBlock.data.url_list;
        for (int i = 0; i < urls.size(); i++) {
            int finalI = i;
            String url = urls.get(finalI);
            elementsList.add(new GuiButton("element_index_" + finalI, btn -> {
                tileBlock.data.url_list.remove(finalI);
                elementsList.remove("element_index_" + finalI);
                if (tileBlock.data.url_list.size() >= tileBlock.data.url_index) {
                    tileBlock.data.url_index = tileBlock.data.url_list.size() - 1;
                }
                FrameNet.syncUrlList(tileBlock.getBlockPos(), tileBlock.data.url_list, tileBlock.data.url_index);
            }).setTitle(new TextComponent(url)).setExpandableX());
        }
    }

    @Override
    protected void syncData(TvTile tileBlock, Player player, CompoundTag tag) {
        TvData.sync(tileBlock, player, tag);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClient()) {
            volumeIcon.setIcon(WidgetIcons.getVolumeIcon((int) volumeSlider.value));
        }
    }
}