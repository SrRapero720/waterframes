package me.srrapero720.waterframes.common.screen;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.util.FrameNet;
import me.srrapero720.waterframes.common.block.entity.ProjectorTile;
import me.srrapero720.waterframes.common.block.data.ProjectorData;
import me.srrapero720.waterframes.common.screen.widgets.*;
import me.srrapero720.waterframes.common.screen.widgets.styles.WidgetIcons;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextListBuilder;

public class ProjectorScreen extends DisplayScreen<ProjectorTile> {
    // PARENTS
    protected WidgetDoubleTable urlValueTable;
    protected WidgetParent sizeParent;
    protected WidgetDoubleTable textureSettingsTable;
    protected WidgetDoubleTable mediaSettingsTable;
    protected WidgetDoubleTable actionsTable;

    // WIDGETS
    protected GuiTextfield urlTextField;
    protected WidgetCounterDecimal widthTextField;
    protected WidgetCounterDecimal heightTextField;
    protected WidgetSlider volumeSlider;
    protected GuiSteppedSlider volumeMinSlider;
    protected GuiStateButton posXButton;
    protected GuiStateButton posYButton;
    protected WidgetSteppedSlider volumeMaxSlider;
    protected GuiButton saveBtn;

    // ICONS
    protected WidgetIcon rotationIcon;
    protected WidgetIcon transparencyIcon;
    protected WidgetIcon alphaIcon;
    protected WidgetIcon brightnessIcon;
    protected WidgetIcon distanceIcon;
    protected WidgetIcon positionViewer;
    protected WidgetIcon volumeIcon;

    public ProjectorScreen(ProjectorTile projector) {
        super("projector_screen", projector, 245, 235);
        this.align = Align.STRETCH;
        this.flow = GuiFlow.STACK_Y;
    }

    @Override
    public void onCreate() {
        this.urlTextField = new WidgetTextFieldTrigger(() -> this.saveBtn, DisplayData.URL, tileBlock.getUrl()).setSuggest("https://i.imgur.com/1yCDs5C.mp4").expandX();
        this.urlValueTable = new WidgetDoubleTable(GuiFlow.STACK_Y)
                .addOnFirst(new WidgetLabel("media_label", 0.75f).setTitle(new TranslatableComponent("gui.waterframes.url")))
                .addOnFirst(urlTextField)
                .setSpacing(4);

        if (isClient()) {
            this.urlValueTable.addOnSecond(new WidgetStatusIcon("", 25, 25, WidgetIcons.STATUS_OK, () -> tileBlock.imageCache));
        }

        // IMAGE SIZE
        this.sizeParent = new WidgetParent(GuiFlow.STACK_X).setSpacing(4).setAlign(Align.STRETCH);
        this.sizeParent.add(this.widthTextField = new WidgetCounterDecimal("width", tileBlock.data.getWidth(), 0, DisplayConfig.maxWidth(), scale)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiIconButton("reX", 16, 16, WidgetIcons.EXPAND_X, but -> {
                    if (tileBlock.display != null)
                        heightTextField.setValue(tileBlock.display.height() / (tileBlock.display.width() / widthTextField.getValue()));
                }))
        );

        this.sizeParent.add(this.heightTextField = new WidgetCounterDecimal("height", tileBlock.data.getHeight(), 0, DisplayConfig.maxHeight(), scale)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiIconButton("reY", 16, 16, WidgetIcons.EXPAND_Y, but -> {
                    if (tileBlock.display != null)
                        widthTextField.setValue(tileBlock.display.width() / (tileBlock.display.height() / heightTextField.getValue()));
                })));

        this.sizeParent.add(new WidgetParent(GuiFlow.STACK_Y)
                .add2(new GuiCheckBox(DisplayData.FLIP_X, tileBlock.data.flipX).setTranslate("gui.waterframes.flip_x"))
                .add2(new GuiCheckBox(DisplayData.FLIP_Y, tileBlock.data.flipY).setTranslate("gui.waterframes.flip_y")));


        this.textureSettingsTable = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2).expandY()
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(this.rotationIcon = new WidgetIcon("r_icon", 12, 12, WidgetIcons.ROTATION))
                        .add2(new WidgetSlider(DisplayData.ROTATION, 130, 10, tileBlock.data.rotation, 0, 360, WidgetSlider.ANGLE)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(this.transparencyIcon = new WidgetIcon("t_icon", 12, 12, WidgetIcons.TRANSPARENCY))
                        .add2(new WidgetSlider(DisplayData.ALPHA, 130, 10, tileBlock.data.alpha, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(this.brightnessIcon = new WidgetIcon("b_icon", 12, 12, WidgetIcons.BRIGHTNESS))
                        .add2(new WidgetSlider(DisplayData.BRIGHTNESS, 130, 10, tileBlock.data.brightness, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(this.distanceIcon = new WidgetIcon("d_icon", 12, 12, WidgetIcons.DISTANCE))
                        .add2(new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, 130, 10, tileBlock.data.renderDistance, 5, 1024)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("pd_icon", 12, 12, WidgetIcons.PROJECTION_DISTANCE))
                        .add2(new GuiSteppedSlider(ProjectorData.PROJECTION_DISTANCE, 130, 10, tileBlock.data.projectionDistance, 4, 128)))
                // IMAGE POSITION
                .addOnSecond(positionViewer = new WidgetIcon("posView", 40, 40, WidgetIcons.POS_CORD[tileBlock.data.getPosX()][tileBlock.data.getPosY()]))
                .addOnSecond(posXButton = new GuiStateButton("pos_x", tileBlock.data.getPosX(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posx.", "left", "center", "right")))
                .addOnSecond(posYButton = new GuiStateButton("pos_y", tileBlock.data.getPosY(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));
        this.textureSettingsTable.getSecondRow().setAlign(Align.CENTER);

        this.mediaSettingsTable = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(4);
        this.mediaSettingsTable.addOnFirst(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(new GuiIconButton("play", WidgetIcons.PLAY, button -> playAction.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("pause", WidgetIcons.PAUSE, button -> pauseAction.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("stop", WidgetIcons.STOP, button -> stopAction.send(EndTag.INSTANCE))))
                .addOnFirst(new GuiCheckBox("loop", tileBlock.data.loop).setTranslate("gui.waterframes.loop"))
                .addOnSecond(new WidgetParent()
                        .add2(new GuiStateButton(ProjectorData.AUDIO_OFFSET, tileBlock.data.getOffsetMode(), new TextListBuilder()
                                .addTranslated("gui.waterframes.audio_offset.", "block", "between", "projection")))
                        .setAlign(Align.RIGHT))
                .addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(this.volumeIcon = new WidgetIcon("v_icon", 12, 12, WidgetIcons.getVolumeIcon(tileBlock.data.volume)))
                        .add2(this.volumeSlider = (WidgetSlider) new WidgetSlider(DisplayData.VOLUME, 100, 10, tileBlock.data.volume, 0, DisplayConfig.maxVolume(), WidgetSlider.PERCENT).setExpandableX())
                        .setAlign(Align.RIGHT))
                .addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(new WidgetIcon("v_min_icon", 12, 12, WidgetIcons.VOLUME_RANGE_MIN))
                        .add2(this.volumeMinSlider = (GuiSteppedSlider) new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, 63, 10, tileBlock.data.minVolumeDistance, 0, Math.min(DisplayConfig.maxVolumeDistance(), tileBlock.data.maxVolumeDistance)).setExpandableX())
                        .add2(new WidgetIcon("v_max_icon", 12, 12, WidgetIcons.VOLUME_RANGE_MAX))
                        .add2(this.volumeMaxSlider = (WidgetSteppedSlider) new WidgetSteppedSlider(DisplayData.VOL_RANGE_MAX, volumeMinSlider, 63, 10, tileBlock.data.maxVolumeDistance, 0, DisplayConfig.maxVolumeDistance()).setExpandableX())
                .setAlign(Align.RIGHT));

        this.mediaSettingsTable.getFirstRow().setExpandableX();
        this.mediaSettingsTable.getSecondRow().setAlign(Align.RIGHT).setExpandableX();

        this.saveBtn = (GuiButton) new GuiButton("save", x -> syncAction.send(ProjectorData.build(this))).setTranslate("gui.waterframes.save");
        this.actionsTable = new WidgetDoubleTable().setSpacing(2)
                .addOnFirst(new GuiButton("reload_all", x -> ImageAPI.reloadCache()).setTitle(new TranslatableComponent("gui.waterframes.reload.all")))
                .addOnSecond(saveBtn.setEnabled(DisplayConfig.canSave(getPlayer(), urlTextField.getText())))
                .addOnSecond(new GuiButton("reload", x -> tileBlock.imageCache.reload()).setTranslate("gui.waterframes.reload"))
                .setSpacing(2);
        this.actionsTable.getSecondRow().setAlign(Align.RIGHT);


        this.add(urlValueTable);
        this.add(sizeParent);
        this.add(new WidgetLabel("tex_label", 0.8f).setTitle(new TranslatableComponent("label.waterframes.texture_settings")));
        this.add(textureSettingsTable);
        this.add(new WidgetLabel("media_label", 0.8f).setTitle(new TranslatableComponent("label.waterframes.media_settings")));
        this.add(mediaSettingsTable);
        if (isClient()) {
            this.add(new WidgetSeekBar("seek", 150, 12, tileBlock.data.tick, 0, tileBlock.display != null ? tileBlock.display.durationInTicks() : 1, () -> tileBlock.data.tick)
                    .addOnMouseGrab(seekBar -> tileBlock.data.tick = (int) seekBar.value)
                    .addOnMouseRelease(seekBar -> FrameNet.syncPlaybackState(tileBlock.getBlockPos(), tileBlock.data.playing, tileBlock.data.tick = (int) seekBar.value))
                    .setExpandableX());
        }
        this.add(actionsTable);
    }

    @Override
    protected void syncData(ProjectorTile tileBlock, Player player, CompoundTag tag) {
        ProjectorData.sync(tileBlock, player, tag);
    }

    @Override
    public void tick() {
        super.tick();
        if (isClient()) {
            positionViewer.setIcon(WidgetIcons.POS_CORD[posXButton.getState()][posYButton.getState()]);
            volumeIcon.setIcon(WidgetIcons.getVolumeIcon((int) volumeSlider.value));
        }
    }
}