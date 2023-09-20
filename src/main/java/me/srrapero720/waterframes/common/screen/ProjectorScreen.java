package me.srrapero720.waterframes.common.screen;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.api.DataBlock;
import me.srrapero720.waterframes.common.packets.ActionPacket;
import me.srrapero720.waterframes.common.screen.widgets.*;
import me.srrapero720.waterframes.common.screen.widgets.custom.CustomStyles;
import me.srrapero720.waterframes.common.screen.widgets.custom.CustomIcons;
import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.common.blockentities.BEProjector;
import me.srrapero720.watermedia.api.image.ImageAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;

public class ProjectorScreen extends GuiLayer {
    public BEProjector projector;
    public GuiTextfield widgetUrl;
    public float scaleMultiplier;

    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> projector.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> projector.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> projector.stop());
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> DataBlock.Projector.sync(projector, getPlayer(), nbt));

    public ProjectorScreen(BEProjector projector) {
        super("projector_screen", 245, 235);
        this.projector = projector;
        this.scaleMultiplier = 1F / 16;
        this.align = Align.STRETCH;
        this.flow = GuiFlow.STACK_Y;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) { return CustomStyles.BACKGROUND_COLOR; }

    @OnlyIn(Dist.CLIENT)
    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) { return CustomStyles.BACKGROUND_BORDER; }

    @Override
    public void create() {
        GuiButton widgetSave = (GuiButton) new GuiButton("save", x -> SET_DATA.send(DataBlock.Projector.build(this))).setTranslate("gui.waterframes.save");
        widgetUrl = new WidgetTextField(widgetSave, "url", projector.getUrl());
        widgetUrl.setSuggestion("https://i.imgur.com/1yCDs5C.mp4");

        WidgetDoubleTable widgetUrlStatus = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(4);

        widgetUrlStatus.addOnFirst(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("URL")));
        widgetUrlStatus.addOnFirst(widgetUrl.setExpandableX());
        if (isClient()) widgetUrlStatus.addOnSecond(new WidgetStatusIcon("", 25, 25, CustomIcons.STATUS_OK, () -> projector.imageCache));

        // IMAGE SIZE
        GuiParent widgetSizeParent = new WidgetParent(GuiFlow.STACK_X).setSpacing(8).setAlign(Align.STRETCH);
        widgetSizeParent.add(new WidgetCounterDecimal("width", projector.getSizeX(), 0, (float) WaterConfig.maxWidth(), scaleMultiplier)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiButton("reX", but -> {
                    WidgetCounterDecimal sizeXField = get("width", WidgetCounterDecimal.class);
                    WidgetCounterDecimal sizeYField = get("height", WidgetCounterDecimal.class);

                    float x = sizeXField.getValue();

                    if (projector.display != null)
                        sizeYField.setValue(projector.display.height() / (projector.display.width() / x));
                }).setTitle(new TextComponent("x->y")))
        );

        widgetSizeParent.add(new WidgetCounterDecimal("height", projector.getSizeY(), 0, (float) WaterConfig.maxHeight(), scaleMultiplier)
                .expandX()
                .setSpacing(0)
                .setAlign(Align.CENTER)
                .add2(new GuiButton("reY", but -> {
                    WidgetCounterDecimal sizeXField = get("width", WidgetCounterDecimal.class);
                    WidgetCounterDecimal sizeYField = get("height", WidgetCounterDecimal.class);

                    float y = sizeYField.getValue();

                    if (projector.display != null)
                        sizeXField.setValue(projector.display.width() / (projector.display.height() / y));
                }).setTitle(new TextComponent("y->x"))));

        widgetSizeParent.add(new WidgetParent(GuiFlow.STACK_Y)
                .add2(new GuiCheckBox("flip_x", projector.data.flipX).setTranslate("gui.waterframes.flipx"))
                .add2(new GuiCheckBox("flip_y", projector.data.flipY).setTranslate("gui.waterframes.flipy")));


        WidgetDoubleTable widgetImageSettings = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2);
        widgetImageSettings.setExpandableY();

        // ROTATION
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("r_icon", 12, 12, CustomIcons.ROTATION))
                .add2(new WidgetSlider("rotation", 130, 10, projector.data.rotation, 0, 360, WidgetSlider.ANGLE)));

        // TRANSPARENCY
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("t_icon", 12, 12, CustomIcons.TRANSPARENCY))
                .add2(new WidgetSlider("alpha", 130, 10, projector.data.alpha, 0, 1, WidgetSlider.PERCENT)));

        // BRIGHTNESS
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("b_icon", 12, 12, CustomIcons.BRIGHTNESS))
                .add2(new WidgetSlider("brightness", 130, 10, projector.data.brightness, 0, 1, WidgetSlider.PERCENT)));

        // DISTANCE
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("d_icon", 12, 12, CustomIcons.DISTANCE))
                .add2(new GuiSteppedSlider("render_distance", 130, 10, projector.data.renderDistance, 5, 1024)));

        // PROJECTION DISTANCE
        widgetImageSettings.addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                .add2(new WidgetIcon("pd_icon", 12, 12, CustomIcons.PROJECTION_DISTANCE))
                .add2(new GuiSteppedSlider("projection_distance", 130, 10, projector.data.projectionDistance, 4, 128)));

        // IMAGE POSITION
        widgetImageSettings.getSecondRow().setAlign(Align.CENTER);
        widgetImageSettings.addOnSecond(new WidgetIcon("posView", 40, 35, CustomIcons.POS_CORD[projector.data.getPosX()][projector.data.getPosY()]));
        widgetImageSettings.addOnSecond(new GuiStateButton("pos_x", projector.data.getPosX(), new TextListBuilder()
                .addTranslated("gui.waterframes.posx.", "left", "center", "right")));

        widgetImageSettings.addOnSecond(new GuiStateButton("pos_y", projector.data.getPosY(), new TextListBuilder()
                .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));


        WidgetDoubleTable widgetMediaSettings = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2);
        widgetMediaSettings.getFirstRow().setExpandableX();
        widgetMediaSettings.addOnFirst(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new GuiIconButton("play", CustomIcons.PLAY, button -> PLAY.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("pause", CustomIcons.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)))
                .add2(new GuiIconButton("stop", CustomIcons.STOP, button -> STOP.send(EndTag.INSTANCE))));
        widgetMediaSettings.addOnFirst(new GuiCheckBox("loop", projector.data.loop).setTranslate("gui.waterframes.loop"));

        widgetMediaSettings.getSecondRow().setAlign(Align.RIGHT).setExpandableX();
        widgetMediaSettings.addOnSecond(new WidgetParent()
                .add2(new GuiStateButton("audio_origin", projector.data.audioOrigin, new TextListBuilder()
                        .addTranslated("gui.waterframes.audiocenter.", "block", "projection", "between")))
                .setAlign(Align.RIGHT));

        widgetMediaSettings.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME))
                .add2(new WidgetSlider("volume", 130, 10, projector.data.volume, 0, (double) WaterConfig.maxAudioVolume() / 100, WidgetSlider.PERCENT).setExpandableX())
                .setAlign(Align.RIGHT));

        GuiSteppedSlider rangeMin;
        widgetMediaSettings.addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MIN))
                .add2(rangeMin = (GuiSteppedSlider) new GuiSteppedSlider("volume_min_range", 63, 10, projector.data.minVolumeDistance, 0, 512).setExpandableX())
                .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MAX))
                .add2(new GuiSteppedSlider("volume_max_range", 63, 10, projector.data.maxVolumeDistance, 0, WaterConfig.maxAudioDistance()) {
                    @Override
                    public void setValue(double value) {
                        super.setValue((int) value);
                        rangeMin.maxValue = (int) value;

                        if (rangeMin.getValue() > this.value) rangeMin.setValue(value >= 0 ? (int) value : 0);
                    }
                }.setExpandableX())
                .setAlign(Align.RIGHT));

        WidgetDoubleTable widgetScreenActions = new WidgetDoubleTable().setSpacing(2);
        widgetScreenActions.addOnFirst(new GuiButton("reload_all", x -> ImageAPI.reloadCache())
                .setTitle(new TextComponent("Reload All")).setTooltip(new TextBuilder().translate("WARNING: reload can stun or crash your game").build()));

        widgetScreenActions.addOnSecond(widgetSave);
        widgetSave.setEnabled(WaterConfig.canUse(getPlayer(), widgetUrl.getText()));

        widgetScreenActions.getSecondRow().setAlign(Align.RIGHT);
        widgetScreenActions.addOnSecond(new GuiButton("reload", x -> projector.imageCache.reload()).setTranslate("gui.waterframes.reload"));

        this.add(widgetUrlStatus);
        this.add(widgetSizeParent);
        this.add(new WidgetLabel("tex_label", 0.8f).setTitle(new TextComponent("Texture settings")));
        this.add(widgetImageSettings);
        this.add(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("Media settings")));
        this.add(widgetMediaSettings);
        this.add(new WidgetSeekBar("seek", 150, 12, projector.data.tick, 0, projector.display != null ? projector.display.maxTick() : 1, () -> projector.data.tick)
                .addOnMouseGrab(seekBar -> projector.data.tick = (int) seekBar.value)
                .addOnMouseRelease(seekBar -> WaterFrames.NETWORK.sendToServer(new ActionPacket(projector.getBlockPos(), projector.data.playing, (int) seekBar.value)))
                .setExpandableX());
        this.add(widgetScreenActions);
    }

    @Override
    public void tick() {
        super.tick();
        WidgetIcon posIc = get("posView");
        posIc.setIcon(CustomIcons.POS_CORD[((GuiStateButton) get("pos_x")).getState()][((GuiStateButton) get("pos_y")).getState()]);

    }
}