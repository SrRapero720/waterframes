package me.srrapero720.waterframes.common.screens;

import me.srrapero720.waterframes.DisplayConfig;
import me.srrapero720.waterframes.common.block.data.DisplayData;
import me.srrapero720.waterframes.common.block.entity.DisplayTile;
import me.srrapero720.waterframes.common.network.DisplaysNet;
import me.srrapero720.waterframes.common.screens.styles.IconStyles;
import me.srrapero720.waterframes.common.screens.styles.ScreenStyles;
import me.srrapero720.waterframes.common.screens.widgets.*;
import me.srrapero720.waterframes.util.FrameTools;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.*;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.event.GuiTextUpdateEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.parser.DoubleValueParser;
import team.creative.creativecore.common.gui.parser.IntValueParser;
import team.creative.creativecore.common.gui.parser.LongValueParser;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;
import team.creative.creativecore.common.util.type.Color;

import java.util.Collections;
import java.util.List;

public class DisplayScreen extends GuiLayer {
    protected static final float SCALE = 1F / 16;

    // IMPORTANT
    public final DisplayTile tile;

    // LABELS
    private final WidgetLabel url_l = new WidgetLabel("media_label", 0.75f).setTranslate("waterframes.gui.label.url");
    private final WidgetLabel tex_l = new WidgetLabel("tex_label", 0.75f).setTranslate("waterframes.gui.label.texture_settings");
    private final WidgetLabel media_l = new WidgetLabel("media_label", 0.75f).setTranslate("waterframes.gui.label.media_settings");

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
    public final WidgetURLTextField urlField;

    public final GuiCounterDecimal widthField;
    public final GuiCounterDecimal heightField;

    public final GuiSlider rotation;
    public final GuiSlider visibility;
    public final GuiSlider brightness;
    public final GuiSlider render_distance;
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

    // WIDGETS
    public final GuiCheckBox flip_x;
    public final GuiCheckBox flip_y;

    public final GuiStateButton pos_x;
    public final GuiStateButton pos_y;
    public final GuiSlider volume;
    public final GuiSteppedSlider volume_min;
    public final GuiSteppedSlider volume_max;

    // ICONS
    private final GuiIcon pos_view;

    public DisplayScreen(DisplayTile tile) {
        super("display_screen", 260, 245);
        this.setAlign(Align.STRETCH);
        this.flow = GuiFlow.STACK_Y;
        this.tile = tile;

        this.save = new GuiButton("save", x -> DisplaysNet.updateDataServer(tile, this));
        this.save.setTranslate("waterframes.gui.save");

        this.urlField = new WidgetURLTextField(this.tile);

        this.widthField = new GuiCounterDecimal("width", tile.data.getWidth(), 0.1, DisplayConfig.maxWidth(), ControlFormatting.CLICKABLE_NO_PADDING);
        this.widthField.setSpacing(0).setStep(SCALE).setAlign(Align.STRETCH).setVAlign(VAlign.STRETCH);

        this.widthField.buttons.setVAlign(VAlign.STRETCH);
        this.widthField.get("value").setTooltip("waterframes.common.width");

        this.heightField = new GuiCounterDecimal("height", tile.data.getHeight(), 0.1, DisplayConfig.maxHeight(), ControlFormatting.CLICKABLE_NO_PADDING);
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
                this.widthField.setValue((float) (tile.display.width() / (tile.display.height() / widthField.getValue())));
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
        this.render_distance = new GuiSteppedSlider(DisplayData.RENDER_DISTANCE, tile.data.renderDistance, 4, DisplayConfig.maxRenderDistance(), IntValueParser.BLOCKS.BLOCKS);
        this.projection_distance = new GuiSteppedSlider(DisplayData.PROJECTION_DISTANCE, tile.data.projectionDistance, 4, DisplayConfig.maxProjectionDistance(), IntValueParser.BLOCKS);
        this.audioOffset = new GuiStateButtonIcon(DisplayData.AUDIO_OFFSET, IconStyles.AUDIO_POS_BLOCK, IconStyles.AUDIO_POS_PICTURE, IconStyles.AUDIO_POS_CENTER) {
            @Override
            public List<Component> getTooltip() {
                return new TextBuilder()
                        .translate("waterframes.gui.audio_pos.1")
                        .newLine()
                        .translate("waterframes.gui.audio_pos.2",
                                ChatFormatting.BLUE + translate("waterframes.gui.audio_pos.states." + getState()))
                        .build();
            }
        }.setControlFormatting(ControlFormatting.CLICKABLE_NO_PADDING).setState(tile.data.getOffsetMode());
        this.audioOffset.setShadow(Color.NONE);

        this.show_model = new GuiCheckBox(DisplayData.VISIBLE_FRAME, tile.data.frameVisibility);
        this.render_behind = new GuiCheckBox(DisplayData.RENDER_BOTH_SIDES, tile.data.renderBothSides);
        this.show_model.setTranslate("waterframes.gui.show_model");
        this.render_behind.setTranslate("waterframes.gui.render_behind");

        this.pos_x = new GuiStateButton("pos_x", tile.data.getPosX().ordinal(), FrameTools.translatable("waterframes.gui.pos_x.", "left", "right", "center"));
        this.pos_y = new GuiStateButton("pos_y", tile.data.getPosY().ordinal(), FrameTools.translatable("waterframes.gui.pos_y.", "top", "bottom", "center"));
        this.pos_view = new GuiIcon("posView", IconStyles.POS_CORD[pos_x.getState()][pos_y.getState()]);

        this.playback = new GuiCheckButtonIcon("playback", IconStyles.PAUSE, IconStyles.PLAY, tile.data.paused, button ->
                DisplaysNet.sendPlaybackServer(tile, !tile.data.paused, tile.data.tick)
        );
        this.stop = new GuiButtonIcon("stop", IconStyles.STOP, button -> DisplaysNet.sendPlaybackServer(tile, false, 0));
        this.loop = new GuiCheckButtonIcon(DisplayData.LOOP, IconStyles.REPEAT_ON, IconStyles.REPEAT_OFF, tile.data.loop, button ->
                DisplaysNet.sendLoopServer(tile, !tile.data.loop)
        ) {
            @Override
            public List<Component> getTooltip() {
                return Collections.singletonList(
                        translatable("waterframes.gui.loop",
                                (this.value ? ChatFormatting.GREEN : ChatFormatting.RED) + translate("waterframes.common." + this.value))
                );
            }
        };

        this.volume = new GuiSlider(DisplayData.VOLUME, tile.data.volume, 0, DisplayConfig.maxVolume(), DoubleValueParser.PERCENT);
        this.volume_min = new GuiSteppedSlider(DisplayData.VOL_RANGE_MIN, tile.data.minVolumeDistance, 0, Math.min(tile.data.maxVolumeDistance, DisplayConfig.maxVolumeDistance()));
        this.volume_max = new GuiSteppedSlider(DisplayData.VOL_RANGE_MAX, tile.data.maxVolumeDistance, 0, DisplayConfig.maxVolumeDistance());
        this.volume_max.setMinSlider(this.volume_min);

        this.reload_all = new GuiButton("reload_all", x -> ImageAPI.reloadCache());
        this.reload = new GuiButton("reload", x -> tile.imageCache.reload());
        this.reload_all.setTranslate("waterframes.gui.reload.all").setTooltip("waterframes.gui.reload.all.warning");
        this.reload.setTranslate("waterframes.gui.reload");

        this.registerEvent(GuiTextUpdateEvent.class, guiTextUpdateEvent -> {
            if (guiTextUpdateEvent.control.name.equals(DisplayData.URL)) {
                save.setEnabled(DisplayConfig.canSave(getPlayer(), this.urlField.getText()));
                reload.setEnabled(!this.urlField.getText().isEmpty() && this.urlField.getText().equals(tile.data.url));
            }
        });

        this.seekbar = new GuiSeekBar("seek", () -> tile.data.tick, () -> tile.data.tickMax != -1 ? tile.data.tickMax : 1, LongValueParser.TIME_DURATION_TICK)
                .setOnTimeUpdate(v -> tile.data.tick = (int) v)
                .setOnLastTimeUpdate(v -> DisplaysNet.sendPlaytimeServer(tile, tile.data.tick = (int) v, tile.data.tickMax));
    }

    @Override
    public void create() {
        if (!isClient()) return;
        // URL FIELD
        final var table = new WidgetPairTable(GuiFlow.STACK_Y, 4)
                .addLeft(url_l)
                .addLeft(urlField.setExpandableX())
                .addRight(new WidgetStatusIcon("", IconStyles.STATUS_OK, tile).setDim(30, 30));
        this.add(table);


        // IMAGE SIZE
        var sizeTable = new GuiParent("", GuiFlow.STACK_X, Align.STRETCH).setSpacing(4);
        if (tile.canResize()) {
            sizeTable.addWidget(this.widthField.setExpandableX())
                    .addWidget(this.heightField.setExpandableX())
                    .addWidget(new GuiParent(GuiFlow.STACK_Y)
                            .addWidget(flip_x)
                            .addWidget(flip_y)
                    );
            this.add(sizeTable);
        }

        // PICTURE PROPERTIES
        final var basicOptions = new GuiParent("", GuiFlow.STACK_X, Align.STRETCH).setSpacing(1)
                .addWidget(tile.canHideModel(), () -> show_model)
                .addWidget(tile.canRenderBackside(), () -> render_behind)
                .addWidget(!tile.canResize(), () -> flip_x)
                .addWidget(!tile.canResize(), () -> flip_y);

        this.add(tex_l);
        this.add(new WidgetPairTable(GuiFlow.STACK_Y, 2)
                .addLeft(new GuiParent(GuiFlow.STACK_X).addWidget(rot_i).addWidget(rotation.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).addWidget(vis_i).addWidget(visibility.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).addWidget(bright_i).addWidget(brightness.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeft(new GuiParent(GuiFlow.STACK_X).addWidget(render_i).addWidget(render_distance.setDim(130, 12)).setVAlign(VAlign.CENTER))
                .addLeftIf(tile.canProject(), new GuiParent(GuiFlow.STACK_X).addWidget(project_i).addWidget(projection_distance.setDim(100, 13)).addWidget(audioOffset.setDim(26, 13)).setVAlign(VAlign.CENTER))
                .addLeftIf(!basicOptions.isEmpty(), basicOptions)
                .addRightIf(tile.canResize(), pos_view.setDim(40, 40), pos_x, pos_y)
                .setAlignRight(Align.CENTER)
                .setExpandableY()
        );

        // MEDIA SETTINGS
        this.add(media_l);
        final var mediaSettingsTable = new WidgetPairTable(GuiFlow.STACK_X, 4)
                .addRight(this.vol_i, this.volume.setDim(100, 16).setExpandableX())
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
        this.add(new GuiParent(GuiFlow.STACK_X).addWidget(
                this.loop.setDim(14, 14),
                this.playback.setSquared(true).setDim(20, 14),
                this.stop.setDim(14, 14),
                this.seekbar.setDim(150, 18).setExpandableX())
        );

        // SAVE BUTTONS
        this.add(new WidgetPairTable(GuiFlow.STACK_X, Align.RIGHT, 2)
                .addLeft(this.reload_all)
                .addRight(this.save.setEnabled(DisplayConfig.canSave(getPlayer(), urlField.getText())))
                .addRight(this.reload)
                .setAlignRight(Align.RIGHT)
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (!isClient()) return;
        this.vol_i.setIcon(IconStyles.getVolumeIcon((int) volume.getValue()));
        this.pos_view.setIcon(IconStyles.POS_CORD[pos_x.getState()][pos_y.getState()]);
        this.playback.setState(tile.data.paused);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BACKGROUND; }

    @Override
    @OnlyIn(Dist.CLIENT)
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return ScreenStyles.SCREEN_BORDER; }
}
