package me.srrapero720.waterframes.custom.screen.widgets;

import team.creative.creativecore.common.gui.controls.simple.GuiSteppedSlider;

public class WidgetSteppedSlider extends GuiSteppedSlider {
    final GuiSteppedSlider rangeMin;

    public WidgetSteppedSlider(String name, int width, int height, int value, int min, int max) {
        this(name, null, width, height, value, min, max);
    }

    public WidgetSteppedSlider(String name, int value, int min, int max) {
        this(name, null, value, min, max);
    }

    public WidgetSteppedSlider(String name, GuiSteppedSlider rangeMin, int width, int height, int value, int min, int max) {
        super(name, width, height, value, min, max);
        this.rangeMin = rangeMin;
    }

    public WidgetSteppedSlider(String name, GuiSteppedSlider rangeMin, int value, int min, int max) {
        super(name, value, min, max);
        this.rangeMin = rangeMin;
    }

    @Override
    public void setValue(double value) {
        super.setValue(value);
        if (rangeMin != null) {
            rangeMin.maxValue = (int) value;

            if (rangeMin.getValue() > this.value) rangeMin.setValue(value >= 0 ? (int) value : 0);
        }
    }
}