package me.srrapero720.waterui.widget;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Turns a slider value into the label drawn on top of it. */
@FunctionalInterface
public interface ValueFormat {
    String format(double value, double max);

    ValueFormat RAW = (value, max) -> String.valueOf(Math.round(value));
    ValueFormat PERCENT = (value, max) -> Math.round((max == 0 ? 0 : value / max) * 100) + "%";
    // THE RAW VALUE WITH A PERCENT SIGN (NOT value/max), FOR A CONTROL WHOSE UNIT ALREADY IS A PERCENT
    ValueFormat RAW_PERCENT = (value, max) -> (int) value + "%";
    ValueFormat ANGLE = (value, max) -> decimal(value) + "°";
    ValueFormat BLOCKS = (value, max) -> Component.translatable("waterframes.gui.blocks", Math.round(value)).getString();
    ValueFormat BLOCKS_DECIMAL = (value, max) -> Component.translatable("waterframes.gui.blocks", decimal(value)).getString();

    /** Two decimals, and no trailing ".00" when the value is whole. */
    static String decimal(double value) {
        // ROOT LOCALE: A COMMA SEPARATOR WOULD BE REJECTED BY THE NUMERIC FIELD PARSING IT BACK
        return value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.2f", value);
    }

    /** Milliseconds in, {@code mm:ss/mm:ss} out, growing to hours only when the media needs it. */
    ValueFormat DURATION = (value, max) -> timestamp((long) value) + "/" + timestamp((long) max);

    static String timestamp(long millis) {
        if (millis < 0) millis = 0;
        long seconds = millis / 1000L;
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        return hours > 0
                ? String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs)
                : String.format(Locale.ROOT, "%02d:%02d", minutes, secs);
    }
}
