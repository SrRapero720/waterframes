package me.srrapero720.waterframes.common.screen.widgets;

import team.creative.creativecore.common.gui.controls.simple.GuiSlider;

public class WidgetSlider extends GuiSlider {
    public static final ValueParser NONE = (v, max) -> (float) Math.round(v * 100.0) / 100 + "";
    public static final ValueParser PERCENT = (v, max) -> (int) ((v / max) * 100.0d) + "%";
    public static final ValueParser PERCENT_100 = (v, max) -> (int) ((v / 100d) * 100.0d) + "%";
    public static final ValueParser ANGLE = (v, max) -> Math.round(v) + "°";

    public final ValueParser parser;

    public WidgetSlider(String name, int width, int height, double value, double min, double max, ValueParser valueParser) {
        super(name, width, height, value, min, max);
        this.parser = valueParser;
    }

    public WidgetSlider(String name, double value, double min, double max, ValueParser valueParser) {
        super(name, value, min, max);
        this.parser = valueParser;
    }

    @Override
    public String getTextByValue() {
        return parser.get(value, maxValue);
    }

    @Override
    public String getTextfieldValue() {
        float var10000 = (float)Math.round(this.value * 100.0);
        return "" + var10000 / 100.0F;
    }

    public interface ValueParser {
        String get(double value, double max);
    }
}