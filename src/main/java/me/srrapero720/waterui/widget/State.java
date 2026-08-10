package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.theme.Icon;

import java.util.function.IntConsumer;

/** Cycles through a fixed list of icons, one per state, reporting the state it lands on. */
public class State extends Button {
    private Icon[] states;
    private int state;
    private IntConsumer onChange;

    public State() {
        this(new Icon[0]);
    }

    public State(Icon... states) {
        super(null);
        this.states = states;
    }

    public State states(Icon... states) {
        this.states = states;
        return this.state(state);
    }

    /** Called with the new state index every time a click advances the cycle. */
    public State onChange(IntConsumer onChange) {
        this.onChange = onChange;
        return this;
    }

    public State state(int state) {
        this.state = states.length == 0 ? 0 : Math.clamp(state, 0, states.length - 1);
        return this;
    }

    public int state() {
        return state;
    }

    @Override
    public Icon icon() {
        return states.length == 0 ? null : states[state];
    }

    @Override
    protected void click(int button) {
        if (states.length == 0) return;
        this.state = (this.state + 1) % states.length;
        if (onChange != null) onChange.accept(state);
    }
}
