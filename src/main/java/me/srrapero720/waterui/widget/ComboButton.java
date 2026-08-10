package me.srrapero720.waterui.widget;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Element;
import me.srrapero720.waterui.core.AbstractParent;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.theme.Drawable;
import me.srrapero720.waterui.theme.Face;

/**
 * Fuses two or more buttons into a single visual control: one connected chrome drawn over
 * the union of the segments, zero gap between them. Each segment keeps its own hover shade,
 * tooltip, content and events.
 */
public class ComboButton extends ParentLinear {

    public ComboButton() {
        this(Orientation.HORIZONTAL);
    }

    public ComboButton(Orientation orientation) {
        super(orientation);
        this.face = Face.CLICKABLE;
        this.spacing = 0;
        this.padding(0);
        this.alignChildren(Anchor.STRETCH);
    }

    @Override
    public ComboButton orientation(Orientation orientation) {
        super.orientation(orientation);
        return this;
    }

    // SPACING IS HARDWIRED TO ZERO: SEGMENTS MUST TOUCH FOR THE FUSED CHROME TO WORK
    @Override
    public ParentLinear spacing(int spacing) {
        return this;
    }

    @Override
    public AbstractParent add(Element child) {
        if (!(child instanceof Button button)) {
            throw new IllegalArgumentException("ComboButton only accepts Button subclasses");
        }
        button.fused = true;
        button.border(0);
        return super.add(child);
    }

    @Override
    public void remove(Element child) {
        if (child instanceof Button button) button.fused = false;
        super.remove(child);
    }

    @Override
    public void clear() {
        for (Element child: children) {
            if (child instanceof Button button) button.fused = false;
        }
        super.clear();
    }

    // THE UNIFIED SURFACE IS ALWAYS THE BASE FACE; SEGMENTS PAINT THEIR OWN HOVER SHADE
    @Override
    protected Drawable faceDisplay() {
        return style().face(false);
    }
}
