package me.srrapero720.waterframes.custom.screen;

import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.waterframes.api.data.BasicData;
import me.srrapero720.waterframes.core.WaterConfig;
import me.srrapero720.waterframes.core.WaterNet;
import me.srrapero720.waterframes.custom.block.entity.ProjectorTile;
import me.srrapero720.waterframes.custom.data.ProjectorData;
import me.srrapero720.waterframes.custom.packets.ActionPacket;
import me.srrapero720.waterframes.custom.screen.widgets.*;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomIcons;
import me.srrapero720.waterframes.custom.screen.widgets.custom.CustomStyles;
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
import team.creative.creativecore.common.util.text.TextListBuilder;

public class ProjectorScreen extends GuiLayer {
    public ProjectorTile projector;
    public GuiTextfield inputUrl;
    public float scaleMultiplier;

    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> projector.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> projector.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> projector.stop());
    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> ProjectorData.sync(projector, getPlayer(), nbt));

    public ProjectorScreen(ProjectorTile projector) {
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
        GuiButton buttonSave = (GuiButton) new GuiButton("save", x -> SET_DATA.send(ProjectorData.build(this))).setTranslate("gui.waterframes.save");
        inputUrl = new WidgetTextField(buttonSave, BasicData.URL, projector.getUrl()).setSuggest("https://i.imgur.com/1yCDs5C.mp4");

        WidgetDoubleTable parentURL = new WidgetDoubleTable(GuiFlow.STACK_Y)
                .addOnFirst(new WidgetLabel("media_label", 0.75f).setTitle(new TextComponent("URL")))
                .addOnFirst(inputUrl.setExpandableX())
                .addOnSecondIf(new WidgetStatusIcon("", 25, 25, CustomIcons.STATUS_OK, () -> projector.imageCache), isClient())
                .setSpacing(4);

        // IMAGE SIZE
        GuiParent parentSize = new WidgetParent(GuiFlow.STACK_X).setSpacing(8).setAlign(Align.STRETCH);
        parentSize.add(new WidgetCounterDecimal("width", projector.getSizeX(), 0, WaterConfig.maxWidth(), scaleMultiplier)
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

        parentSize.add(new WidgetCounterDecimal("height", projector.getSizeY(), 0, WaterConfig.maxHeight(), scaleMultiplier)
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

        parentSize.add(new WidgetParent(GuiFlow.STACK_Y)
                .add2(new GuiCheckBox(BasicData.FLIP_X, projector.data.flipX).setTranslate("gui.waterframes.flipx"))
                .add2(new GuiCheckBox(BasicData.FLIP_Y, projector.data.flipY).setTranslate("gui.waterframes.flipy")));


        WidgetDoubleTable parentTexSettings = new WidgetDoubleTable(GuiFlow.STACK_Y).setSpacing(2).expandY()
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("r_icon", 12, 12, CustomIcons.ROTATION))
                        .add2(new WidgetSlider(BasicData.ROTATION, 130, 10, projector.data.rotation, 0, 360, WidgetSlider.ANGLE)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("t_icon", 12, 12, CustomIcons.TRANSPARENCY))
                        .add2(new WidgetSlider(BasicData.ALPHA, 130, 10, projector.data.alpha, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("b_icon", 12, 12, CustomIcons.BRIGHTNESS))
                        .add2(new WidgetSlider(BasicData.BRIGHTNESS, 130, 10, projector.data.brightness, 0, 1, WidgetSlider.PERCENT)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("d_icon", 12, 12, CustomIcons.DISTANCE))
                        .add2(new GuiSteppedSlider(BasicData.RENDER_DISTANCE, 130, 10, projector.data.renderDistance, 5, 1024)))
                .addOnFirst(new WidgetParent(GuiFlow.STACK_X)
                        .add2(new WidgetIcon("pd_icon", 12, 12, CustomIcons.PROJECTION_DISTANCE))
                        .add2(new GuiSteppedSlider(ProjectorData.PROJECTION_DISTANCE, 130, 10, projector.data.projectionDistance, 4, 128)))
                // IMAGE POSITION
                .addOnSecond(new WidgetIcon("posView", 40, 35, CustomIcons.POS_CORD[projector.data.getPosX()][projector.data.getPosY()]))
                .addOnSecond(new GuiStateButton("pos_x", projector.data.getPosX(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posx.", "left", "center", "right")))
                .addOnSecond(new GuiStateButton("pos_y", projector.data.getPosY(), new TextListBuilder()
                        .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));

        parentTexSettings.getSecondRow().setAlign(Align.CENTER);

        WidgetDoubleTable parentMedia = new WidgetDoubleTable(() -> new WidgetColum(GuiFlow.STACK_Y)).setSpacing(2);
        GuiSteppedSlider rangeMin;

        parentMedia.addOnFirst(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(new GuiIconButton("play", CustomIcons.PLAY, button -> PLAY.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("pause", CustomIcons.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)))
                        .add2(new GuiIconButton("stop", CustomIcons.STOP, button -> STOP.send(EndTag.INSTANCE))))
                .addOnFirst(new GuiCheckBox("loop", projector.data.loop).setTranslate("gui.waterframes.loop"))
                .addOnSecond(new WidgetParent()
                        .add2(new GuiStateButton(ProjectorData.AUDIO_ORIGIN, projector.data.audioOrigin, new TextListBuilder()
                                .addTranslated("gui.waterframes.audiocenter.", "block", "projection", "between")))
                        .setAlign(Align.RIGHT))
                .addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME))
                        .add2(new WidgetSlider(BasicData.VOLUME, 130, 10, projector.data.volume, 0, WaterConfig.maxAudioVolume(), WidgetSlider.PERCENT).setExpandableX())
                        .setAlign(Align.RIGHT))
                .addOnSecond(new WidgetParent("", GuiFlow.STACK_X)
                        .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MIN))
                        .add2(rangeMin = (GuiSteppedSlider) new GuiSteppedSlider(BasicData.VOL_RANGE_MIN, 63, 10, projector.data.minVolumeDistance, 0, Math.min(WaterConfig.maxAudioDistance(), projector.data.maxVolumeDistance)).setExpandableX())
                        .add2(new WidgetIcon("v_icon", 12, 12, CustomIcons.VOLUME_RANGE_MAX))
                        .add2(new WidgetSteppedSlider(BasicData.VOL_RANGE_MAX, rangeMin, 63, 10, projector.data.maxVolumeDistance, 0, WaterConfig.maxAudioDistance()).setExpandableX())
                .setAlign(Align.RIGHT));

        parentMedia.getFirstRow().setExpandableX();
        parentMedia.getSecondRow().setAlign(Align.RIGHT).setExpandableX();

        WidgetDoubleTable parentControls = new WidgetDoubleTable()
                .addOnFirst(new GuiButton("reload_all", x -> ImageAPI.reloadCache()).setTitle(new TextComponent("Reload All")))
                .addOnSecond(buttonSave.setEnabled(WaterConfig.canUse(getPlayer(), inputUrl.getText())))
                .addOnSecond(new GuiButton("reload", x -> projector.imageCache.reload()).setTranslate("gui.waterframes.reload"))
                .setSpacing(2);
        parentControls.getSecondRow().setAlign(Align.RIGHT);


        this.add(parentURL);
        this.add(parentSize);
        this.add(new WidgetLabel("tex_label", 0.8f).setTitle(new TextComponent("Texture settings")));
        this.add(parentTexSettings);
        this.add(new WidgetLabel("media_label", 0.8f).setTitle(new TextComponent("Media settings")));
        this.add(parentMedia);
        this.add(new WidgetSeekBar("seek", 150, 12, projector.data.tick, 0, projector.display != null ? projector.display.maxTick() : 1, () -> projector.data.tick)
                .addOnMouseGrab(seekBar -> projector.data.tick = (int) seekBar.value)
                .addOnMouseRelease(seekBar -> WaterNet.syncPlaybackState(projector.getBlockPos(), projector.data.playing, (int) seekBar.value))
                .setExpandableX());
        this.add(parentControls);
    }

    @Override
    public void tick() {
        super.tick();
        WidgetIcon posIc = get("posView");
        posIc.setIcon(CustomIcons.POS_CORD[((GuiStateButton) get("pos_x")).getState()][((GuiStateButton) get("pos_y")).getState()]);
    }
}