package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.layout.FrameLayout;
import me.srrapero720.waterui.layout.ParentLinear;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * A tabbed container: a strip of {@link Tab}s over a body that shows one tab at a
 * time. Each tab is a button face plus its own content; clicking a button reveals that content
 * and marks its button selected. The active index is exposed so a host can tick or persist it.
 */
public class ParentTab extends ParentLinear {
    // HEIGHT OF THE ICON STRIP; THE TAB BUTTONS AUTHORED AGAINST THIS 18px BAND
    private static final int STRIP_HEIGHT = 18;
    // INSET AROUND THE BODY SO THE ACTIVE TAB CLEARS THE STRIP AND THE PANEL EDGE
    private static final int BODY_PAD = 4;

    private final ParentLinear strip;
    private final FrameLayout body;
    private final List<Tab> buttons = new ArrayList<>();
    private final List<Element> contents = new ArrayList<>();
    private final List<Runnable> activate = new ArrayList<>();
    private final List<Runnable> deactivate = new ArrayList<>();
    private int active;
    private final List<IntConsumer> onChange = new ArrayList<>();

    public ParentTab() {
        super(Orientation.VERTICAL);
        this.spacing = 0;
        this.alignChildren(Anchor.STRETCH);

        this.strip = new ParentLinear(Orientation.HORIZONTAL);
        this.strip.spacing(0).alignChildren(Anchor.STRETCH).width(Element.FILL).height(STRIP_HEIGHT);

        this.body = new FrameLayout();
        this.body.width(Element.FILL).height(Element.FILL).padding(BODY_PAD);

        this.add(strip);
        this.add(body);
    }

    /** Adds one tab: its button joins the strip, its content the body, shown only when active. */
    public void tab(Element content, Tab button) {
        this.tab(content, button, null, null);
    }

    /** Adds one tab plus its lifecycle hooks, fired when a switch shows or hides it. */
    public void tab(Element content, Tab button, Runnable onActivate, Runnable onDeactivate) {
        int index = buttons.size();
        button.width(Element.FILL);
        button.selected(index == active).onClick(glfw -> { if (glfw == 0) this.select(index); });
        content.visible(index == active);
        this.buttons.add(button);
        this.contents.add(content);
        this.activate.add(onActivate);
        this.deactivate.add(onDeactivate);
        this.strip.add(button);
        this.body.add(content);
    }

    /** Index of the tab currently shown. */
    public int active() {
        return active;
    }

    /** Index of the tab whose content carries {@code id}, or -1; how a host finds a page regardless of pack order. */
    public int indexOf(ResourceLocation id) {
        for (int i = 0; i < contents.size(); i++) if (id.equals(contents.get(i).id())) return i;
        return -1;
    }

    /** Shows tab {@code index} without notifying, e.g. to restore the open page after a rebuild. */
    public ParentTab active(int index) {
        if (index != active && index >= 0 && index < contents.size()) this.show(index);
        return this;
    }

    /** Adds a listener notified with the new index whenever the shown tab changes by a click. */
    public ParentTab onChange(IntConsumer onChange) {
        this.onChange.add(onChange);
        return this;
    }

    // CLICK PATH: SWITCH THE PAGE THEN LET THE HOSTS REACT (TICK IT, PERSIST THE INDEX)
    private void select(int index) {
        if (index == active || index < 0 || index >= contents.size()) return;
        this.show(index);
        for (IntConsumer listener: onChange) listener.accept(index);
    }

    // REVEAL ONE PAGE AND ITS BUTTON, FIRING THE OUTGOING DEACTIVATE THEN THE INCOMING ACTIVATE.
    // THE visible() FLIP QUEUES THE REFLOW ON ITS OWN
    private void show(int index) {
        int previous = this.active;
        this.active = index;
        for (int i = 0; i < contents.size(); i++) {
            this.buttons.get(i).selected(i == index);
            this.contents.get(i).visible(i == index);
        }
        if (previous != index && deactivate.get(previous) != null) deactivate.get(previous).run();
        if (activate.get(index) != null) activate.get(index).run();
    }
}
