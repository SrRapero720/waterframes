package me.srrapero720.waterframes.custom.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.custom.screen.widgets.WidgetTextField;
import me.srrapero720.waterframes.custom.tiles.TileProjector;
import me.srrapero720.waterframes.display.texture.TextureCache;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.network.chat.Component;
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
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;

public class ProjectorScreen extends GuiLayer {
    public TileProjector frame;

    public float scaleMultiplier;

    public GuiTextfield url;

    public final GuiSyncLocal<EndTag> PLAY = getSyncHolder().register("play", x -> frame.play());
    public final GuiSyncLocal<EndTag> PAUSE = getSyncHolder().register("pause", x -> frame.pause());
    public final GuiSyncLocal<EndTag> STOP = getSyncHolder().register("stop", x -> frame.stop());

    public final GuiSyncLocal<CompoundTag> SET_DATA = getSyncHolder().register("set_data", nbt -> {
        String url = nbt.getString("url");
        if (WFConfig.canUse(getPlayer(), url)) {
            frame.setURL(url);
            float sizeX = (float) Math.min(WFConfig.maxWidth(), nbt.getFloat("x"));
            float sizeY = (float) Math.min(WFConfig.maxHeight(), nbt.getFloat("y"));
            int posX = nbt.getByte("posX");
            int posY = nbt.getByte("posY");
            if (posX == 0) {
                frame.min.x = 0;
                frame.max.x = sizeX;
            } else if (posX == 1) {
                float middle = sizeX / 2;
                frame.min.x = 0.5F - middle;
                frame.max.x = 0.5F + middle;
            } else {
                frame.min.x = 1 - sizeX;
                frame.max.x = 1;
            }

            if (posY == 0) {
                frame.min.y = 0;
                frame.max.y = sizeY;
            } else if (posY == 1) {
                float middle = sizeY / 2;
                frame.min.y = 0.5F - middle;
                frame.max.y = 0.5F + middle;
            } else {
                frame.min.y = 1 - sizeY;
                frame.max.y = 1;
            }

            frame.renderDistance = Math.min(WFConfig.maxRenderDistance(), nbt.getInt("render"));
            frame.rotation = nbt.getFloat("rotation");
            frame.loop = nbt.getBoolean("loop");
            frame.flipX = nbt.getBoolean("flipX");
            frame.flipY = nbt.getBoolean("flipY");
            frame.volume = nbt.getFloat("volume");
            frame.minDistance = nbt.getFloat("min");
            frame.maxDistance = nbt.getFloat("max");
            frame.alpha = nbt.getFloat("transparency");
            frame.brightness = nbt.getFloat("brightness");
        }

        frame.markDirty();
    });

    public ProjectorScreen(TileProjector frame) {
        this(frame, 16);
    }

    public ProjectorScreen(TileProjector frame, int scaleSize) {
        super("waterframe", 250, 210);
        this.frame = frame;
        this.scaleMultiplier = 1F / (scaleSize);
    }
    
    @Override
    public void create() {
        GuiButton save = new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            GuiTextfield url = get("url");
            GuiCounterDecimal sizeX = get("sizeX");
            GuiCounterDecimal sizeY = get("sizeY");

            GuiStateButton buttonPosX = get("posX");
            GuiStateButton buttonPosY = get("posY");
            GuiSlider rotation = get("rotation");

            GuiCheckBox flipX = get("flipX");
            GuiCheckBox flipY = get("flipY");
            GuiCheckBox visibleFrame = get("visibleFrame");
            GuiCheckBox bothSides = get("bothSides");

            GuiSteppedSlider renderDistance = get("distance");

            GuiSlider transparency = get("transparency");
            GuiSlider brightness = get("brightness");

            GuiCheckBox loop = get("loop");
            GuiSlider volume = get("volume");
            GuiSteppedSlider min = get("range_min");
            GuiSteppedSlider max = get("range_max");

            nbt.putByte("posX",  (byte) 1);
            nbt.putByte("posY", (byte) 1);

            nbt.putFloat("rotation", (float) rotation.value);

            nbt.putBoolean("flipX", flipX.value);
            nbt.putBoolean("flipY", flipY.value);
            nbt.putBoolean("visibleFrame", visibleFrame.value);
            nbt.putBoolean("bothSides", bothSides.value);

            nbt.putInt("render", (int) renderDistance.value);

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

        align = Align.STRETCH;
        flow = GuiFlow.STACK_Y;

        url = new WidgetTextField(save, "url", frame.getRealURL());
        url.setMaxStringLength(2048);
        add(url);
        GuiLabel error = new GuiLabel("error").setDefaultColor(ColorUtils.RED);
        if (frame.isClient() && frame.cache != null && frame.cache.getError() != null)
            error.setTranslate(frame.cache.getError());
        add(error);

        // SIZE -----------------------
        GuiParent size = new GuiParent(GuiFlow.STACK_X);
        size.spacing = 16;
        size.align = Align.STRETCH;
        add(size);
        GuiCounterDecimal counterDecimal;

        // LEFT
        size.add(counterDecimal = new GuiCounterDecimal("sizeX", frame.getSizeX(), 0, Float.MAX_VALUE) {
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

            if (frame.display != null)
                sizeYField.setValue(frame.display.getHeight() / (frame.display.getWidth() / x));
        }).setTitle(Component.literal("x->y")).setExpandableY());

        // RIGHT
        size.add(counterDecimal = new GuiCounterDecimal("sizeY", frame.getSizeY(), 0, Float.MAX_VALUE) {
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

            if (frame.display != null)
                sizeXField.setValue(frame.display.getWidth() / (frame.display.getHeight() / y));
        }).setTitle(Component.literal("y->x")).setExpandableY());

        GuiParent flips = new GuiParent(GuiFlow.STACK_X);
        flips.spacing = 4;
        add(flips);
        flips.add(new GuiCheckBox("flipX", frame.flipX).setTranslate("gui.waterframes.flipx"));
        flips.add(new GuiCheckBox("flipY", frame.flipY).setTranslate("gui.waterframes.flipy"));


        GuiTable table = new GuiTable();
        table.spacing = 2;
        add(table);
        GuiColumn left;
        GuiColumn right;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(Component.translatable("gui.waterframes.rotation").append(":")));
        right.add(new GuiSlider("rotation", 130, 10, frame.rotation, 0, 360));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("t_label").setTitle(Component.translatable("gui.waterframes.transparency").append(":")));
        right.add(new GuiSlider("transparency", 130, 10, frame.alpha, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("b_label").setTitle(Component.translatable("gui.waterframes.brightness").append(":")));
        right.add(new GuiSlider("brightness", 130, 10, frame.brightness, 0, 1));
        right.align = Align.RIGHT;

        table.addRow(new GuiRow(left = new GuiColumn(), right = new GuiColumn()));
        left.add(new GuiLabel("d_label").setTitle(Component.translatable("gui.waterframes.distance").append(":")));
        right.add(new GuiSteppedSlider("distance", 130, 10, frame.renderDistance, 5, 1024));
        right.align = Align.RIGHT;


        GuiParent rendering = new GuiParent(GuiFlow.STACK_X);
        rendering.align = Align.RIGHT;
        rendering.spacing = 8;
        add(rendering);
        rendering.add(new GuiCheckBox("loop", frame.loop).setTranslate("gui.waterframes.loop"));

        GuiTable volume = new GuiTable();
        add(volume);
        GuiColumn volume_left;
        GuiColumn volume_right;

        volume.addRow(new GuiRow(volume_left = new GuiColumn(GuiFlow.STACK_X), volume_right = new GuiColumn(GuiFlow.STACK_X)));
        volume_left.add(new GuiLabel("v_label").setTitle(Component.translatable("gui.waterframes.volume").append(":")));
        volume_right.add(new GuiSlider("volume", 130, 10, frame.volume, 0, 1));
        volume_right.align = Align.RIGHT;

        GuiParent range = new GuiParent(GuiFlow.STACK_X);
        add(range);
        range.add(new GuiLabel("range_label").setTitle(Component.translatable("gui.waterframes.range").append(" (min/max):")));
        range.add(new GuiSteppedSlider("range_min", 63, 10, (int) frame.minDistance, 0, 512).setExpandableX());
        range.add(new GuiSteppedSlider("range_max", 63, 10, (int) frame.maxDistance, 0, 512).setExpandableX());

        GuiTable playgrid = new GuiTable();
        add(playgrid);
        GuiColumn play_left;
        GuiColumn play_right;

        playgrid.addRow(new GuiRow(play_left = new GuiColumn(GuiFlow.STACK_X), play_right = new GuiColumn(GuiFlow.STACK_X)));
        play_right.align = Align.RIGHT;

        play_left.add(new GuiIconButton("play", GuiIcon.PLAY, button -> PLAY.send(EndTag.INSTANCE)));
        play_left.add(new GuiIconButton("pause", GuiIcon.PAUSE, button -> PAUSE.send(EndTag.INSTANCE)));
        play_left.add(new GuiIconButton("stop", GuiIcon.STOP, button -> STOP.send(EndTag.INSTANCE)));


        save.setEnabled(WFConfig.canUse(getPlayer(), url.getText()));
        play_right.add(save);
        play_right.add(new GuiButton("reload", x -> {
            if (Screen.hasShiftDown()) TextureCache.reloadAll();
            else if (frame.cache != null) frame.cache.reload();
        }).setTranslate("gui.waterframes.reload").setTooltip(new TextBuilder().translate("gui.waterframes.reload.tooltip").build()));


        GuiParent footer = new GuiParent(GuiFlow.STACK_X);
        add(footer);
        footer.add(new GuiSlider("seek", 150, 12, frame.tick, 0, frame.display != null ? frame.display.maxTick() : 1) {

            protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
                double percent = this.getPercentage();
                int posX = (int) (control.getContentWidth() * percent);
                GuiStyle style = this.getStyle();
                style.get(ControlFormatting.ControlStyleFace.CLICKABLE, false).render(pose, 0, 0.0, posX, rect.getHeight());
                if (this.textfield != null) {
                    this.textfield.render(pose, control, rect, rect, mouseX, mouseY);
                } else {
                    GuiRenderHelper.drawStringCentered(pose, this.getTextByValue(), (float)rect.getWidth(), (float)rect.getHeight(), -1, true);
                }

            }
        }.setExpandableX());
    }

}
