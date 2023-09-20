package me.srrapero720.waterframes.common.screen.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import me.srrapero720.waterframes.core.tools.TimerTool;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import team.creative.creativecore.client.render.GuiRenderHelper;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.controls.simple.GuiSlider;
import team.creative.creativecore.common.gui.style.ControlFormatting;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.util.math.geo.Rect;

import java.util.function.Supplier;

public class WidgetSeekBar extends GuiSlider {
    final Supplier<Integer> tickValueUpdate;
    EventConsumer onMouseGrabEv;
    EventConsumer onMouseReleased;
    public WidgetSeekBar(String name, int width, int height, double value, double min, double max, Supplier<Integer> valueUpdate) {
        super(name, width, height, value, min, max);
        this.tickValueUpdate = valueUpdate;
    }

    public WidgetSeekBar(String name, double value, double min, double max, Supplier<Integer> valueUpdate) {
        super(name, value, min, max);
        this.tickValueUpdate = valueUpdate;
    }

    public WidgetSeekBar addOnMouseGrab(EventConsumer r) {
        onMouseGrabEv = r;
        return this;
    }

    public WidgetSeekBar addOnMouseRelease(EventConsumer r) {
        onMouseReleased = r;
        return this;
    }

    @Override
    public void mouseMoved(Rect rect, double x, double y) {
        super.mouseMoved(rect, x, y);
        if (grabbedSlider && onMouseGrabEv != null) onMouseGrabEv.run(this);
    }

    @Override
    public void mouseReleased(Rect rect, double x, double y, int button) {
        super.mouseReleased(rect, x, y, button);
        if (onMouseReleased != null) onMouseReleased.run(this);
    }

    protected void renderContent(PoseStack pose, GuiChildControl control, Rect rect, int mouseX, int mouseY) {
        double percent = this.getPercentage();
        int posX = (int) (control.getContentWidth() * percent);
        GuiStyle style = this.getStyle();
        style.get(ControlFormatting.ControlStyleFace.CLICKABLE, false).render(pose, 0, 0.0, posX, rect.getHeight());

        if (this.textfield != null) this.textfield.render(pose, control, rect, rect, mouseX, mouseY);
        else {
            int ticks = (int) value;
            if (ticks > maxValue) ticks %= (int) maxValue;
            GuiRenderHelper.drawStringCentered(pose, TimerTool.timestamp(WaterMediaAPI.math_ticksToMillis(ticks)) + "/" + TimerTool.timestamp(WaterMediaAPI.math_ticksToMillis((int) maxValue)), (float)rect.getWidth(), (float)rect.getHeight(), -1, true);
        }
    }

    @Override
    public void tick() {
        if (!grabbedSlider) setValue(tickValueUpdate.get());
    }

    public interface EventConsumer {
        void run(WidgetSeekBar seekBar);
    }
}