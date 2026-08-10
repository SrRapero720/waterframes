package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.theme.Theme;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.DoubleConsumer;

/**
 * Numeric field with the increment and decrement buttons stacked on its right side. Every
 * piece runs without padding so the row stays as short as the readout needs and the two
 * buttons split its full height between them.
 */
public class Stepper extends ParentLinear {
    private static final int BUTTON_WIDTH = 9;

    private double min;
    private double max;
    private double step;
    private double value;
    private final InputNumber readout;
    private DoubleConsumer onChange;
    private DoubleConsumer onCommit;

    public Stepper() {
        this(0, 0, 100, 1);
    }

    public Stepper(double value, double min, double max, double step) {
        super(Orientation.HORIZONTAL);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(value);
        this.spacing = 0;
        this.alignChildren(Anchor.STRETCH);

        ParentLinear buttons = ParentLinear.column();
        buttons.spacing = 0;
        buttons.alignChildren(Anchor.STRETCH);
        buttons.width(BUTTON_WIDTH);
        buttons.add(button(Component.literal("+"), 1));
        buttons.add(button(Component.literal("-"), -1));

        this.readout = new InputNumber("value", this.value, this::value);
        // THE EDITOR DEFAULTS TO NO PADDING BECAUSE IT ALSO SERVES AS A SLIDER OVERLAY; HERE IT IS
        // A FIELD OF ITS OWN AND THE DIGITS NEED ROOM AWAY FROM THE FRAME
        this.readout.padding(Theme.SPACE_XS, 0);
        this.add(readout.width(FILL));
        this.add(buttons);
    }

    // HALF HEIGHT EACH: FILL ON THE COLUMN MAKES THE TWO BUTTONS SPLIT IT EVENLY. THE SIGN IS
    // BAKED IN AND THE STEP IS READ ON CLICK, SO A LATER step() STILL REACHES BOTH BUTTONS
    private Button button(Component label, int sign) {
        Button button = Button.of(label, click -> this.value(this.value + sign * this.step));
        button.padding(0).height(FILL);
        return button;
    }

    private double clamp(double raw) {
        int scale = stepScale();
        return BigDecimal.valueOf(Math.clamp(raw, min, max)).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    // SCALE DERIVED FROM THE STEP: 0.0625 HAS 4 DECIMALS, 1 HAS 0, MATCHING THE GRID THE USER SET
    private int stepScale() {
        if (step <= 0 || step == Math.rint(step)) return 0;
        String s = BigDecimal.valueOf(step).stripTrailingZeros().toPlainString();
        int dot = s.indexOf('.');
        return dot < 0 ? 0 : s.length() - dot - 1;
    }

    /** Moves the bounds and pulls the current value back inside them. */
    public Stepper range(double min, double max) {
        this.min = min;
        this.max = max;
        return this.value(this.value);
    }

    public Stepper min(double min) {
        this.min = min;
        return this.value(this.value);
    }

    public Stepper max(double max) {
        this.max = max;
        return this.value(this.value);
    }

    public Stepper step(double step) {
        this.step = step;
        return this;
    }

    /** Called with the new value on every edit, wherever it came from: buttons, typing or code. */
    public Stepper onChange(DoubleConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    /** Fires when an edit lands; on a stepper every change is discrete, so it tracks onChange. */
    public Stepper onCommit(DoubleConsumer onCommit) {
        this.onCommit = onCommit;
        return this;
    }

    public double value() {
        return value;
    }

    public Stepper value(double value) {
        double next = clamp(value);
        if (readout != null && !readout.text().equals(ValueFormat.decimal(next))) readout.value(next);
        if (next != this.value) {
            this.value = next;
            if (onChange != null) onChange.accept(next);
            if (onCommit != null) onCommit.accept(next);
        }
        return this;
    }
}
