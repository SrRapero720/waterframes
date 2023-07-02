package me.srrapero720.waterframes.custom.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.FramesConfig;
import me.srrapero720.waterframes.custom.screen.widgets.WidgetTextField;
import me.srrapero720.waterframes.custom.tiles.TileProjectorTile;
import me.srrapero720.waterframes.display.texture.TextureData;
import me.srrapero720.waterframes.displays.Display;
import me.srrapero720.waterframes.displays.VideoDisplay;
import me.srrapero720.watermedia.api.images.PictureFetcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.parent.GuiColumn;
import team.creative.creativecore.common.gui.controls.parent.GuiRow;
import team.creative.creativecore.common.gui.controls.parent.GuiTable;
import team.creative.creativecore.common.gui.controls.simple.*;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.creativecore.common.util.text.TextListBuilder;

public class ProjectorScreen extends GuiLayer {
    public TileProjectorTile projector;

    public float scaleMultiplier;

    public GuiTextfield url;

    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> projector.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> projector.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> projector.stop());

    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (FramesConfig.canUse(getPlayer(), url)) {
            projector.setURL(url);
            float sizeX = (float) Math.min(FramesConfig.maxWidth(), nbt.getFloat("x"));
            float sizeY = (float) Math.min(FramesConfig.maxHeight(), nbt.getFloat("y"));

            int posX = nbt.contains("posX", 99) ? nbt.getByte("posX") : 1;
            int posY = nbt.contains("posY", 99) ? nbt.getByte("posY") : 1;
            if (posX == 0) {
                projector.min.x = 0;
                projector.max.x = sizeX;
            } else if (posX == 1) {
                float middle = sizeX / 2;
                projector.min.x = 0.5F - middle;
                projector.max.x = 0.5F + middle;
            } else {
                projector.min.x = 1 - sizeX;
                projector.max.x = 1;
            }

            if (posY == 0) {
                projector.min.y = 0;
                projector.max.y = sizeY;
            } else if (posY == 1) {
                float middle = sizeY / 2;
                projector.min.y = 0.5F - middle;
                projector.max.y = 0.5F + middle;
            } else {
                projector.min.y = 1 - sizeY;
                projector.max.y = 1;
            }

            projector.renderDistance = Math.min(FramesConfig.maxRenderDistance(), nbt.getInt("render"));
            projector.projectionDistance = nbt.getInt("projection_distance");
            projector.rotation = nbt.getFloat("rotation");
            projector.loop = nbt.getBoolean("loop");
            projector.flipX = nbt.getBoolean("flipX");
            projector.flipY = nbt.getBoolean("flipY");
            projector.volume = nbt.getFloat("volume");
            projector.minDistance = nbt.getFloat("min");
            projector.maxDistance = nbt.getFloat("max");
            projector.alpha = nbt.getFloat("transparency");
            projector.brightness = nbt.getFloat("brightness");
        }

        projector.markDirty();
    });

    public ProjectorScreen(TileProjectorTile projector) {
        this(projector, 16);
    }

    public ProjectorScreen(TileProjectorTile projector, int scaleSize) {
        super("waterframe", 260, 220);
        this.projector = projector;
        this.scaleMultiplier = 1F / (scaleSize);
    }

    @Override
    public void render(PoseStack pose, GuiChildControl control, Rect controlRect, Rect realRect, int mouseX, int mouseY) {
        super.render(pose, control, controlRect, realRect, mouseX, mouseY);

        GuiSlider seek = get("seek");
        if (projector.display instanceof VideoDisplay display) {
            seek.maxValue = display.getGameTickDuration();
            seek.value = display.getGameTickTime();
        }
    }

    @Override
    public void create() {
        var isVideo = projector.display != null && projector.display.getType().equals(Display.Type.VIDEO);

        GuiButton save = new GuiButton("save", x -> {
            var nbt = new CompoundTag();
            GuiTextfield url = get("url");
            GuiCounterDecimal sizeX = get("sizeX");
            GuiCounterDecimal sizeY = get("sizeY");

            GuiStateButton buttonPosX = get("posX");
            GuiStateButton buttonPosY = get("posY");
            GuiSlider rotation = get("rotation");

            GuiCheckBox flipX = get("flipX");
            GuiCheckBox flipY = get("flipY");

            GuiSteppedSlider renderDistance = get("distance");
            GuiSteppedSlider projection_distance = get("projection_distance");

            GuiSlider transparency = get("transparency");
            GuiSlider brightness = get("brightness");

            GuiCheckBox loop = get("loop");
            GuiSlider volume = get("volume");
            GuiSteppedSlider min = get("range_min");
            GuiSteppedSlider max = get("range_max");

            nbt.putByte("posX", (byte) buttonPosX.getState());
            nbt.putByte("posY", (byte) buttonPosY.getState());

            nbt.putFloat("rotation", (float) rotation.value);

            nbt.putBoolean("flipX", flipX.value);
            nbt.putBoolean("flipY", flipY.value);

            nbt.putInt("render", (int) renderDistance.value);
            nbt.putInt("projection_distance", (int) projection_distance.value);

            nbt.putFloat("transparency", (float) transparency.value);
            nbt.putFloat("brightness", (float) brightness.value);

            nbt.putBoolean("loop", loop.value);
            nbt.putFloat("volume", (float) volume.value);
            nbt.putFloat("min", min.getValue());
            nbt.putFloat("max", max.getValue());

            nbt.putString("url", url.getText());
            nbt.putFloat("x", Math.max(0.1F, sizeX.getValue()));
            nbt.putFloat("y", Math.max(0.1F, sizeY.getValue()));
            SET_DATA.send(nbt);
        });
        save.setTranslate("gui.waterframes.save");

        // Gui config
        this.align = Align.STRETCH;
        this.flow = GuiFlow.STACK_Y;

        // URL
        this.url = new WidgetTextField(save, "url", projector.getRealURL());
        this.url.setMaxStringLength(2048);
        this.add(url);

        // LABEL
        var error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (projector.isClient() && projector.texture != null && projector.texture.getError() != null)
            error.setTranslate(projector.texture.getError());
        this.add(error);

        // SIZE (PARENT9
        var sizeParent = new GuiParent(GuiFlow.STACK_X);
        sizeParent.spacing = 16;
        sizeParent.align = Align.STRETCH;
        add(sizeParent);
        GuiCounterDecimal counterDecimal;

        // SIZE (LEFT)
        sizeParent.add(counterDecimal = new GuiCounterDecimal("sizeX", projector.getSizeX(), 0, Float.MAX_VALUE) {
            @Override
            public float stepUp(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled++;
                return Math.min(max, scaled * scaleMultiplier);
            }

            @Override
            public float stepDown(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled--;
                return Math.max(min, scaled * scaleMultiplier);
            }
        });
        counterDecimal.setExpandableX();
        counterDecimal.spacing = 2;
        counterDecimal.align = Align.CENTER;
        counterDecimal.add(new GuiButton("reX", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);


            float x = sizeXField.getValue();

            if (projector.display != null)
                sizeYField.setValue(projector.display.getHeight() / (projector.display.getWidth() / x));
        }).setTitle(new TextComponent("x->y")).setExpandableY());

        // SIZE (RIGHT)
        sizeParent.add(counterDecimal = new GuiCounterDecimal("sizeY", projector.getSizeY(), 0, Float.MAX_VALUE) {
            @Override
            public float stepUp(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled++;
                return Math.min(max, scaled * scaleMultiplier);
            }

            @Override
            public float stepDown(float value) {
                int scaled = (int) (value / scaleMultiplier);
                scaled--;
                return Math.max(min, scaled * scaleMultiplier);
            }
        });


        counterDecimal.setExpandableX();
        counterDecimal.spacing = 2;
        counterDecimal.align = Align.CENTER;
        counterDecimal.add(new GuiButton("reY", but -> {
            GuiCounterDecimal sizeXField = get("sizeX", GuiCounterDecimal.class);
            GuiCounterDecimal sizeYField = get("sizeY", GuiCounterDecimal.class);

            float y = sizeYField.getValue();

            if (projector.display != null)
                sizeXField.setValue(projector.display.getWidth() / (projector.display.getHeight() / y));
        }).setTitle(new TextComponent("y->x")).setExpandableY());

        GuiTable flip_grid = new GuiTable();
        GuiColumn flip_left;
        GuiColumn flip_right;
        this.add(flip_grid);

        flip_grid.addRow(new GuiRow(flip_left = new GuiColumn(GuiFlow.STACK_X), flip_right = new GuiColumn(GuiFlow.STACK_Y)));
        flip_grid.align = Align.CENTER;
        flip_left.align = Align.LEFT;
        flip_right.align = Align.RIGHT;

        flip_left.add(new GuiStateButton("posX", projector.min.x == 0 ? 0 : projector.max.x == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posx.", "left", "center", "right")));

        flip_left.add(new GuiStateButton("posY", projector.min.y == 0 ? 0 : projector.max.y == 1 ? 2 : 1, new TextListBuilder()
                .addTranslated("gui.waterframes.posy.", "top", "center", "bottom")));

        flip_right.add(new GuiCheckBox("flipX", projector.flipX).setTranslate("gui.waterframes.flipx"));
        flip_right.add(new GuiCheckBox("flipY", projector.flipY).setTranslate("gui.waterframes.flipy"));


        // MAIN OPTIONS
        GuiTable table = new GuiTable();
        table.spacing = 2;
        add(table);
        GuiColumn left;
        GuiColumn right;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("r_label").setTitle(new TranslatableComponent("gui.waterframes.rotation")));
        right.add(new GuiSlider("rotation", 120, 10, projector.rotation, 0, 360));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(new TranslatableComponent("gui.waterframes.transparency")));
        right.add(new GuiSlider("transparency", 120, 10, projector.alpha, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(new TranslatableComponent("gui.waterframes.brightness")));
        right.add(new GuiSlider("brightness", 120, 10, projector.brightness, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(new TranslatableComponent("gui.waterframes.visible_distance")));
        right.add(new GuiSteppedSlider("distance", 120, 10, projector.renderDistance, 5, 1024));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(right = new GuiColumn()));
        right.align = Align.RIGHT;
        right.add(new GuiCheckBox("loop", projector.loop).setTranslate("gui.waterframes.loop"));

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("p_label").setTitle(new TranslatableComponent("gui.waterframes.projection_distance")));
        right.add(new GuiSteppedSlider("projection_distance", 120, 10, projector.projectionDistance, 4, 128));
        right.align = Align.RIGHT;

        // SEEK BAR
        var seekParent = new GuiParent(GuiFlow.STACK_X);
        seekParent.add(new GuiIconButton("play", GuiIcon.PLAY, button -> PLAY.send(EndTag.INSTANCE)));
        seekParent.add(new GuiIconButton("pause", GuiIcon.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)));
        seekParent.add(new GuiIconButton("stop", GuiIcon.STOP, button -> STOP.send(EndTag.INSTANCE)));


        GuiSlider slider;
        seekParent.add(slider = new GuiSlider("seek", 150, 12, 0, 0, projector.display != null ? projector.display.maxTick() : 1) {
                protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
                    double percent = this.getPercentage();
                    int posX = (int) (control.getContentWidth() * percent);
                    this.getStyle().get(ControlFormatting.ControlStyleFace.CLICKABLE, false).render(pose, 0, 0.0, posX, rect.getHeight());

                    if (this.textfield != null) this.textfield.render(pose, control, rect, rect, mouseX, mouseY);
                    else GuiRenderHelper.drawStringCentered(pose, this.getTextByValue(), (float) rect.getWidth(), (float) rect.getHeight(), -1, true);
                }

            @Override
            public void setValue(double value) {
                super.setValue(value);
                if (projector.display instanceof VideoDisplay display) {
                    display.player.seekGameTicksTo((int) value);
                }
            }
        });
        slider.setExpandableX();
        slider.setEnabled(isVideo);
        this.add(seekParent);

        // VIDEO CONTROLLING
        var vidVolume = new GuiParent(GuiFlow.STACK_X);
        vidVolume.spacing = 4;
        vidVolume.add(new GuiLabel("v_label").setTitle(new TranslatableComponent("gui.waterframes.volume")));
        vidVolume.add(new GuiSlider("volume", 130, 10, projector.volume, 0, 1).setExpandableX());
        vidVolume.add(new GuiLabel("range_label").setTitle(new TranslatableComponent("gui.waterframes.range")));
        vidVolume.add(new GuiSteppedSlider("range_min", 70, 10, (int) projector.minDistance, 0, 512));
        vidVolume.add(new GuiSteppedSlider("range_max", 70, 10, (int) projector.maxDistance, 0, 512));
        this.add(vidVolume);
        vidVolume.setEnabled(isVideo);

        // SAVE, RELOAD
        var vidSaving = new GuiParent(GuiFlow.STACK_X);
        vidSaving.align = Align.RIGHT;
        save.setEnabled(FramesConfig.canUse(getPlayer(), url.getText()));
        vidSaving.add(save);
        vidSaving.add(new GuiButton("reload", x -> {
            if (PictureFetcher.canSeek()) {
                if (Screen.hasShiftDown()) TextureData.reloadAll();
                else if (projector.texture != null) projector.texture.reload();
            }
        }).setTranslate("gui.waterframes.reload").setTooltip(new TextBuilder().translate("gui.waterframes.reload.tooltip").build()));

        this.add(vidSaving);
    }

}
