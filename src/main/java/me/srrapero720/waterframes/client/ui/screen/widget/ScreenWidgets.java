package me.srrapero720.waterframes.client.ui.screen.widget;

import me.srrapero720.waterui.format.UIRegistry;
import me.srrapero720.waterframes.common.block.data.types.PositionHorizontal;
import me.srrapero720.waterframes.common.block.data.types.PositionVertical;

/**
 * Registers the waterframes-specific UI widgets against the shared {@link UIRegistry} at client
 * setup, keeping the toolkit core free of any waterframes tag. Documents resolve these tags from the
 * same global registry as the core widgets, so a Java import is optional (the tag is already known).
 */
public final class ScreenWidgets {
    private ScreenWidgets() {}

    /** Registers the url/anchor widgets; run once at client setup, before the first inflate. */
    public static void register() {
        // UrlField INHERITS InputText'S value/suggestion/maxLength AND onTextChange/onSubmit/onValueCommit (§7.3, H6)
        UIRegistry.register("UrlField", UrlField::new, false, "InputText");

        UIRegistry.register("AnchorPicker", AnchorPicker::new, false);
        // POSITION NAMES ARE BAKED (§11): pos_x/pos_y CARRY THE TILE'S CURRENT ANCHOR AS THE INITIAL CELL
        UIRegistry.property("AnchorPicker", "x", (e, v, c) -> ((AnchorPicker) e).x(PositionHorizontal.valueOf(c.string(v))));
        UIRegistry.property("AnchorPicker", "y", (e, v, c) -> ((AnchorPicker) e).y(PositionVertical.valueOf(c.string(v))));
        UIRegistry.event("AnchorPicker", "onValueChange", (e, handler) -> {
            AnchorPicker picker = (AnchorPicker) e;
            picker.onChange(() -> handler.invoke(picker.x().name(), picker.y().name()));
        });
        UIRegistry.event("AnchorPicker", "onValueCommit", (e, handler) -> {
            AnchorPicker picker = (AnchorPicker) e;
            picker.onCommit(() -> handler.invoke(picker.x().name(), picker.y().name()));
        });
    }
}
