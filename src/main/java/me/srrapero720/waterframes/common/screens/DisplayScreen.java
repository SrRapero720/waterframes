package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.compat.videoplayer.VPCompat;
import me.srrapero720.waterframes.common.network.DisplayNetwork;
import me.srrapero720.waterframes.common.network.packets.DataSyncPacket;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.*;
import me.srrapero720.waterframes.common.helpers.ScalableText;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.parser.DoubleValueParser;
import team.creative.creativecore.common.gui.parser.IntValueParser;
import team.creative.creativecore.common.gui.parser.LongValueParser;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.type.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisplayScreen extends GuiLayer {
    protected static final float SCALE = 1F / 16;

    // IMPORTANT
    public final DisplayTile tile;

    // LABELS
    private final GuiLabel url_l = new GuiLabel("media_label").setTranslate("waterframes.gui.label.url");
    private final GuiLabel tex_l = new GuiLabel("tex_label").setTranslate("waterframes.gui.label.texture_settings");
    private final GuiLabel media_l = new GuiLabel("media_label").setTranslate("waterframes.gui.label.media_settings");

    // ICONS
    private final GuiIcon rot_i = (GuiIcon) new GuiIcon("r_icon", IconStyles.ROTATION).setTooltip("waterframes.gui.icon.rotation");
    private final GuiIcon vis_i = (GuiIcon) new GuiIcon("t_icon", IconStyles.TRANSPARENCY).setTooltip("waterframes.gui.icon.transparency");
    private final GuiIcon bright_i = (GuiIcon) new GuiIcon("b_icon", IconStyles.BRIGHTNESS).setTooltip("waterframes.gui.icon.brightness");
    private final GuiIcon render_i = (GuiIcon) new GuiIcon("r_icon", IconStyles.DISTANCE).setTooltip("waterframes.gui.icon.render_distance");
    private final GuiIcon project_i = (GuiIcon) new GuiIcon("pd_icon",  IconStyles.PROJECTION_DISTANCE).setTooltip("waterframes.gui.icon.projection_distance");
    private final GuiIcon vol_i = (GuiIcon) new GuiIcon("v_icon", IconStyles.VOLUME).setTooltip("waterframes.gui.icon.volume");
    private final GuiIcon vol_min_i = (GuiIcon) new GuiIcon("v_min_icon", IconStyles.VOLUME_RANGE_MIN).setTooltip("waterframes.gui.icon.volume_min");
    private final GuiIcon vol_max_i = (GuiIcon) new GuiIcon("v_max_icon", IconStyles.VOLUME_RANGE_MAX).setTooltip("waterframes.gui.icon.volume_max");

    // WIDGETS INSTANCES
    public final GuiButton save;
    public final WidgetURLTextField url;

    public final GuiCounterDecimal widthField;
    public final GuiCounterDecimal heightField;

    public final GuiSlider rotation;
    public final GuiSlider visibility;
    public final GuiSlider brightness;
    public final GuiSteppedSlider render_distance;
    public final GuiSlider projection_distance;

    public final GuiCheckBox show_model;
    public final GuiCheckBox render_behind;
    public final GuiCheckButtonIcon loop;

    public final GuiCheckButtonIcon playback;
    public final GuiControl stop;

    public final GuiStateButtonIcon audioOffset;

    public final GuiButton reload_all;
    public final GuiButton reload;
    public GuiControl seekbar;

    public GuiButtonIcon videoplayer;

    // WIDGETS
    public final GuiCheckBox flip_x;
    public final GuiCheckBox flip_y;

    public final GuiSteppedSlider volume;
    public final GuiSteppedSlider volume_min;
    public final GuiSteppedSlider volume_max;

    // ICONS
    public final WidgetClickableArea pos_view;

    public DisplayScreen(DisplayTile tile) {
        super("display_screen", 260, 245);
        this.setAlign(Align.STRETCH);
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.save = new GuiButton("save", x -> DisplayNetwork.sendServer(new DataSyncPacket(tile.getBlockPos(), DisplayData.build(this, tile))));
        this.save.setTranslate("waterframes.gui.save");

        this.url = new WidgetURLTextField(this.tile);

        this.widthField = new GuiCounterDecimal("width", tile.data.getWidth(), 0.1, WFConfig.maxWidth(), ControlFormatting.CLICKABLE_NO_PADDING);
        this.widthField.setSpacing(0).setStep(SCALE).setAlign(Align.STRETCH).setVAlign(VAlign.STRETCH);

        this.widthField.buttons.setVAlign(VAlign.STRETCH);
        this.widthField.get("value").setTooltip("waterframes.common.width");

        this.heightField = new GuiCounterDecimal("height", tile.data.getHeight(), 0.1, WFConfig.maxHeight(), ControlFormatting.CLICKABLE_NO_PADDING);
        this.heightField.setSpacing(0).setStep(SCALE).setAlign(Align.STRETCH).setVAlign(VAlign.STRETCH);

        this.heightField.buttons.setVAlign(VAlign.STRETCH);
        this.heightField.get("value").setTooltip("waterframes.common.height");

        var resize_y = new GuiButtonIcon("resize_y", IconStyles.EXPAND_Y, integer -> {
            if (tile.display != null) {
                this.heightField.setValue((float) (tile.display.height() / (tile.display.width() / widthField.getValue())));
            }
        }).setTooltip("waterframes.gui.resize_y");

        var resize_x = new GuiButtonIcon("resize_x", IconStyles.EXPAND_X, integer -> {
            if (tile.display != null) {
                this.widthField.setValue((float) (tile.display.width() / (tile.display.height() / heightField.getValue())));
            }
        }).setTooltip("waterframes.gui.resize_x");

        this.widthField.addControl(resize_y.setDim(16, 16));
        this.heightField.addControl(resize_x.setDim(16, 16));

        this.flip_x = new GuiCheckBox(DisplayData.FLIP_X, tile.data.flipX);
        this.flip_y = new GuiCheckBox(DisplayData.FLIP_Y, tile.data.flipY);
        this.flip_x.setTranslate("waterframes.gui.flip_x");
        this.flip_y.setTranslate("waterframes.gui.flip_y");

        this.rotation = new GuiSlider(DisplayData.ROTATION, tile.data.rotation, 0, 360, DoubleValueParser.ANGLE);
        this.visibility = new GuiSlider(DisplayData.ALPHA, tile.data.alpha, 0, 1, DoubleValueParser.PERCENT);
        this.brightness = new GuiSlider(DisplayData.BRIGHTNESS, tile.data.brightness, 0, 1, DoubleValueParser.PERCENT);
        this.render_distance = new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, tile.data.renderDistance, 4, WFConfig.maxRenDis(), IntValueParser.BLOCKS.BLOCKS);
        this.projection_distance = new GuiSlider(DisplayData.PROJECTION_DISTANCE, tile.data.projectionDistance, 4, WFConfig.maxProjDis(), DoubleValueParser.BLOCKS);
        this.audioOffset = new GuiStateButtonIcon(DisplayData.AUDIO_OFFSET, IconStyles.AUDIO_POS_BLOCK, IconStyles.AUDIO_POS_PICTURE, IconStyles.AUDIO_POS_CENTER) {
            @Override
            public List<Component> getTooltip() {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(translatable("waterframes.gui.audio_pos.1"));
                tooltip.add(translatable("waterframes.gui.audio_pos.2",
                        ChatFormatting.AQUA + translate("waterframes.gui.audio_pos.states." + getState())
                ));
                return tooltip;
            }
        }.setControlFormatting(ControlFormatting.CLICKABLE_NO_PADDING).setState(tile.data.getAudioPosition().ordinal());
        this.audioOffset.setShadow(Color.NONE);

        this.show_model = new GuiCheckBox(DisplayData.VISIBLE_FRAME, tile.data.frameVisibility);
        this.render_behind = new GuiCheckBox(DisplayData.RENDER_BOTH_SIDES, tile.data.renderBothSides);
        this.show_model.setTranslate("waterframes.gui.show_model");
        this.render_behind.setTranslate("waterframes.gui.render_behind");

        this.pos_view = new WidgetClickableArea("pos_area", tile.data.getPosX(), tile.data.getPosY());

        this.playback = new GuiCheckButtonIcon("playback", IconStyles.PLAY, IconStyles.PAUSE, tile.data.paused, button ->
                tile.setPause(true, !tile.data.paused)
        );
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> tile.setStop(true));
        this.loop = new GuiCheckButtonIcon(DisplayData.LOOP, IconStyles.REPEAT_ON, IconStyles.REPEAT_OFF, tile.data.loop, button -> tile.loop(true, !tile.data.loop)) {
            @Override
            public List<Component> getTooltip() {
                return Collections.singletonList(
                        translatable("waterframes.gui.loop",
                                (this.value ? ChatFormatting.GREEN : ChatFormatting.RED) + translate("waterframes.common." + this.value))
                );
            }
        };

        this.volume = new GuiSteppedSlider(DisplayData.VOLUME, tile.data.volume, 0, WFConfig.maxVol(), (v, max) -> v + "%");
        this.volume_min = new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, tile.data.minVolumeDistance, 0, Math.min(tile.data.maxVolumeDistance, WFConfig.maxVolDis()), IntValueParser.BLOCKS);
        this.volume_max = new GuiSteppedSlider(DisplayData.VOL_RANGE_MAX, tile.data.maxVolumeDistance, 0, WFConfig.maxVolDis(), IntValueParser.BLOCKS);
        this.volume_max.setMinSlider(this.volume_min);

        this.reload_all = new GuiButton("reload_all", x -> ImageAPI.reloadCache());
        this.reload = new GuiButton("reload", x -> tile.imageCache.reload());
        this.reload_all.setTranslate("waterframes.gui.reload.all").setTooltip("waterframes.gui.reload.all.warning");
        this.reload.setTranslate("waterframes.gui.reload");

        if (VPCompat.installed()) {
            this.videoplayer = new GuiButtonIcon("", IconStyles.VIDEOPLAYER_PLAY, button -> {
                VPCompat.playVideo(tile.data.url, tile.data.volume, true);
                tile.setPause(true, true);
            });
            this.videoplayer.setTooltip("waterframes.gui.videoplayer");
        }

        this.seekbar = new GuiSeekBar("seek", () -> tile.data.tick, () -> tile.data.tickMax, LongValueParser.TIME_DURATION_TICK)
                .setOnTimeUpdate(v -> tile.data.tick = (int) v)
                .setOnLastTimeUpdate(v -> tile.syncTime(true, (int) v, tile.data.tickMax));

        ((ScalableText) url_l).wf$setScale(0.75f);
        ((ScalableText) tex_l).wf$setScale(0.75f);
        ((ScalableText) media_l).wf$setScale(0.75f);
    }

    @Override
    public void create() {
        if (!isClient()) return;
        // URL FIELD
        final var table = new WidgetPairTable(GuiFlow.STACK_Y, 4)
                .addLeft(url_l)
                .addLeft(url.setExpandableX())
                .addRight(new WidgetStatusIcon("", IconStyles.STATUS_OK, tile).setDim(30, 30));
        this.add(table);


        // IMAGE SIZE
        var sizeTable = new GuiParent("", GuiFlow.STACK_X, Align.STRETCH).setSpacing(4);
        if (tile.canResize()) {
            sizeTable.add(this.widthField.setExpandableX())
                    .add(this.heightField.setExpandableX())
                    .add(new GuiParent(GuiFlow.STACK_Y)
                            .add(flip_x)
                            .add(flip_y)
                    );
            this.add(sizeTable);
        }

        // PICTURE PROPERTIES
        final var basicOptions = new GuiParent("", GuiFlow.STACK_X, Align.STRETCH).setSpacing(1)
                .add(tile.canHideModel(), () -> show_model)
                .add(tile.canRenderBackside(), () -> render_behind)
                .add(!tile.canResize(), () -> flip_x)
                .add(!tile.canResize(), () -> flip_y);

        this.add(tex_l);
        this.add(new WidgetPairTable(GuiFlow.STACK_Y, 2)
                .addLeft(tile.canResize(), () -> new GuiParent(GuiFlow.STACK_X).add(rot_i).add(rotation.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).add(vis_i).add(visibility.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).add(bright_i).add(brightness.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).add(render_i).add(render_distance.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(tile.canProject(), () -> new GuiParent(GuiFlow.STACK_X).add(project_i).add(projection_distance.setDim(100, 13)).add(audioOffset.setDim(26, 13)).setVAlign(VAlign.CENTER))
                .addLeft(!basicOptions.isEmpty(), () -> basicOptions)
                .addRight(tile.canResize(), () -> pos_view.setDim(80, 80))
                .setAlignRight(Align.CENTER)
                .setExpandableY()
        );

        // MEDIA SETTINGS
        this.add(media_l);
        final var mediaSettingsTable = new WidgetPairTable(GuiFlow.STACK_X, 2)
                .addRight(this.vol_i.setIcon(IconStyles.getVolumeIcon(tile.data.volume, tile.data.muted)), this.volume.setDim(100, 15).setExpandableX())
                .addLeft(VPCompat.installed(), () -> this.videoplayer.setDim(16, 12))
                .setAlignRight(Align.RIGHT)
                .setVAlignRight(VAlign.CENTER)
                .setLeftExpandableX()
                .setRightExpandableX()
                .createRow()
                .addRight(
                        this.vol_min_i,
                        this.volume_min.setDim(63, 10).setExpandableX(),
                        this.vol_max_i,
                        this.volume_max.setDim(63, 10).setExpandableX())
                .setAlignRight(Align.RIGHT)
                .setVAlignRight(VAlign.CENTER)
                .setLeftExpandableX()
                .setRightExpandableX();

        this.add(mediaSettingsTable);

        // SEEKBAR + buttons
        this.add(new GuiParent(GuiFlow.STACK_X).add(
                this.loop.setDim(14, 14),
                this.playback.setSquared(true).setDim(20, 14),
                this.stop.setDim(14, 14),
                this.seekbar.setDim(150, 18).setExpandableX())
        );

        // SAVE BUTTONS
        this.add(new WidgetPairTable(GuiFlow.STACK_X, Align.RIGHT, 2)
                .addLeft(this.reload_all.setAlign(Align.CENTER).setVAlign(VAlign.CENTER).setDim(70, 10))
                .addRight(this.save.setAlign(Align.CENTER).setVAlign(VAlign.CENTER).setDim(60, 10).setEnabled(WFConfig.canSave(getPlayer(), url.getText())))
                .addRight(this.reload.setAlign(Align.CENTER).setVAlign(VAlign.CENTER).setDim(50, 10))
                .setAlignRight(Align.RIGHT)
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (!isClient()) return;
        this.vol_i.setIcon(IconStyles.getVolumeIcon((int) volume.getValue(), tile.data.muted));
        this.playback.setState(tile.data.paused);
        var text = this.url.getText();
        save.setEnabled(WFConfig.canSave(getPlayer(), text));
        reload.setEnabled(tile.imageCache != null && !text.isEmpty() && !tile.data.url.isEmpty() && text.equals(tile.data.url));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }
}
