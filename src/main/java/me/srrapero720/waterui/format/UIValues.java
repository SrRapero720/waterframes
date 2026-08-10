package me.srrapero720.waterui.format;

import me.srrapero720.waterui.core.Anchor;
import me.srrapero720.waterui.core.Spacing;
import me.srrapero720.waterui.layout.Justify;
import me.srrapero720.waterui.layout.ParentLinear;
import me.srrapero720.waterui.widget.ValueFormat;

import java.util.List;

/**
 * Pure decoders for the {@code .ui} value grammar (UI-SPEC.md §4, §8, §9): colours, anchor/align
 * masks with their axis rules, justify, spacing arity and event-payload number formatting. Anything
 * that resolves a variable reference lives in {@link UIContract}; these operate on already-known
 * {@link UIValue}s only.
 */
public final class UIValues {
    private UIValues() {}

    /** Event payload form of a number (§12.1): whole values drop the ".0" so handlers can parse ints. */
    public static String number(double value) {
        return value == Math.rint(value) && !Double.isInfinite(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    /** {@code #RRGGBB} (alpha FF) or {@code #AARRGGBB}, packed ARGB (§4). */
    public static int color(String atom) {
        String hex = atom != null && atom.startsWith("#") ? atom.substring(1) : null;
        if (hex == null || (hex.length() != 6 && hex.length() != 8)) {
            throw new UIFormatException(null, -1, "invalid color '" + atom + "'");
        }
        try {
            long parsed = Long.parseLong(hex, 16);
            return hex.length() == 6 ? (int) (0xFF000000L | parsed) : (int) parsed;
        } catch (NumberFormatException e) {
            throw new UIFormatException(null, -1, "invalid color '" + atom + "'");
        }
    }

    /** Margin/padding arity: 1 = all edges, 2 = (horizontal, vertical), 4 = CSS clockwise (§8.3). */
    public static Spacing spacing(UIValue value) {
        if (value instanceof UIValue.Num n) return Spacing.all((int) n.value());
        if (value instanceof UIValue.Tuple t) {
            List<UIValue> items = t.items();
            return switch (items.size()) {
                case 1 -> Spacing.all(px(items.get(0)));
                case 2 -> Spacing.hv(px(items.get(0)), px(items.get(1)));
                case 4 -> new Spacing(px(items.get(0)), px(items.get(1)), px(items.get(2)), px(items.get(3)));
                default -> throw new UIFormatException(null, -1, "margin/padding needs 1, 2 or 4 values");
            };
        }
        throw new UIFormatException(null, -1, "margin/padding needs a number or a tuple");
    }

    private static int px(UIValue value) {
        if (value instanceof UIValue.Num n) return (int) n.value();
        throw new UIFormatException(null, -1, "margin/padding values must be numbers");
    }

    /**
     * Free-space attachment flags with the axis-claim rule (§9.3): each side claims its axis,
     * {@code CENTER}/{@code STRETCH} fill the unclaimed axes and alone claim both, two sides on one
     * axis are contradictory.
     */
    public static int anchorMask(UIValue value) {
        int h = 0, v = 0;
        boolean center = false, stretch = false;
        for (String name: names(value)) {
            switch (name) {
                case "LEFT" -> { if (h != 0) throw contradiction("horizontal"); h = Anchor.START; }
                case "RIGHT" -> { if (h != 0) throw contradiction("horizontal"); h = Anchor.END; }
                case "TOP" -> { if (v != 0) throw contradiction("vertical"); v = Anchor.TOP; }
                case "BOTTOM" -> { if (v != 0) throw contradiction("vertical"); v = Anchor.BOTTOM; }
                case "CENTER" -> center = true;
                case "STRETCH" -> stretch = true;
                default -> throw new UIFormatException(null, -1, "unknown anchor flag '" + name + "'");
            }
        }
        if (center && stretch) throw new UIFormatException(null, -1, "anchor cannot be both CENTER and STRETCH");
        int fillH = center ? Anchor.CENTER_H : stretch ? Anchor.STRETCH_H : 0;
        int fillV = center ? Anchor.CENTER_V : stretch ? Anchor.STRETCH_V : 0;
        if (fillH != 0 || fillV != 0) {
            if (h == 0 && v == 0) { h = fillH; v = fillV; }
            else { if (h == 0) h = fillH; if (v == 0) v = fillV; }
        }
        int result = h | v;
        if (result == 0) throw new UIFormatException(null, -1, "empty anchor");
        return result;
    }

    /**
     * Outside-edge placement (§9.5), ORDER-SENSITIVE: {@code [edge, align]}. The first flag is the
     * edge the element sits outside of (a side, never CENTER/STRETCH); the optional second is the
     * flush alignment along that edge and must belong to the perpendicular axis, defaulting to the
     * edge's own centre when omitted. More than two flags, a same-axis pair, or a non-side edge are
     * compile errors. {@code RIGHT|BOTTOM} (right edge, bottom-flush) differs from {@code BOTTOM|RIGHT}.
     */
    public static int[] outsideAnchor(UIValue value) {
        List<String> names = names(value);
        if (names.isEmpty() || names.size() > 2) throw new UIFormatException(null, -1, "outsideAnchor takes one or two side flags (§9.5)");
        int edge = side(names.get(0));
        boolean edgeHorizontal = edge == Anchor.START || edge == Anchor.END;
        int align;
        if (names.size() == 2) {
            int second = side(names.get(1));
            boolean secondHorizontal = second == Anchor.START || second == Anchor.END;
            if (secondHorizontal == edgeHorizontal) throw new UIFormatException(null, -1, "outsideAnchor's second flag must be the perpendicular axis (§9.5)");
            align = second;
        } else {
            align = edgeHorizontal ? Anchor.CENTER_V : Anchor.CENTER_H;
        }
        return new int[] {edge, align};
    }

    // A SINGLE SIDE FLAG AS AN Anchor BIT; CENTER/STRETCH ARE NOT EDGES (§9.5)
    private static int side(String name) {
        return switch (name) {
            case "LEFT" -> Anchor.START;
            case "RIGHT" -> Anchor.END;
            case "TOP" -> Anchor.TOP;
            case "BOTTOM" -> Anchor.BOTTOM;
            case "CENTER", "STRETCH" -> throw new UIFormatException(null, -1, "'" + name + "' is not an edge; outsideAnchor needs a side (§9.5)");
            default -> throw new UIFormatException(null, -1, "unknown outsideAnchor flag '" + name + "'");
        };
    }

    /**
     * Cross-flow alignment (§9.2), a single value validated against the container orientation: only
     * the sides perpendicular to the flow are legal, plus {@code CENTER}/{@code STRETCH}; a
     * flow-direction side is a compile error.
     */
    public static int alignMask(UIValue value, boolean vertical) {
        List<String> names = names(value);
        if (names.size() != 1) throw new UIFormatException(null, -1, "align takes a single value");
        String name = names.get(0);
        if (vertical) {
            return switch (name) {
                case "LEFT" -> Anchor.START;
                case "RIGHT" -> Anchor.END;
                case "CENTER" -> Anchor.CENTER_H;
                case "STRETCH" -> Anchor.STRETCH_H;
                case "TOP", "BOTTOM" -> throw new UIFormatException(null, -1, "align '" + name + "' is a flow-direction side in a vertical container (use LEFT/RIGHT/CENTER/STRETCH)");
                default -> throw new UIFormatException(null, -1, "unknown align '" + name + "'");
            };
        }
        return switch (name) {
            case "TOP" -> Anchor.TOP;
            case "BOTTOM" -> Anchor.BOTTOM;
            case "CENTER" -> Anchor.CENTER_V;
            case "STRETCH" -> Anchor.STRETCH_V;
            case "LEFT", "RIGHT" -> throw new UIFormatException(null, -1, "align '" + name + "' is a flow-direction side in a horizontal container (use TOP/BOTTOM/CENTER/STRETCH)");
            default -> throw new UIFormatException(null, -1, "unknown align '" + name + "'");
        };
    }

    public static ParentLinear.Orientation orientation(UIValue value) {
        String name = keyword(value, "orientation");
        return switch (name) {
            case "HORIZONTAL" -> ParentLinear.Orientation.HORIZONTAL;
            case "VERTICAL" -> ParentLinear.Orientation.VERTICAL;
            default -> throw new UIFormatException(null, -1, "unknown orientation '" + name + "'");
        };
    }

    public static Justify justify(UIValue value) {
        String name = keyword(value, "justify");
        return switch (name) {
            case "START" -> Justify.START;
            case "CENTER" -> Justify.CENTER;
            case "END" -> Justify.END;
            case "BETWEEN" -> Justify.BETWEEN;
            default -> throw new UIFormatException(null, -1, "unknown justify '" + name + "'");
        };
    }

    public static ValueFormat format(UIValue value) {
        String name = keyword(value, "format");
        return switch (name) {
            case "RAW" -> ValueFormat.RAW;
            case "PERCENT" -> ValueFormat.PERCENT;
            case "RAW_PERCENT" -> ValueFormat.RAW_PERCENT;
            case "ANGLE" -> ValueFormat.ANGLE;
            case "BLOCKS" -> ValueFormat.BLOCKS;
            case "BLOCKS_DECIMAL" -> ValueFormat.BLOCKS_DECIMAL;
            case "DURATION" -> ValueFormat.DURATION;
            default -> throw new UIFormatException(null, -1, "unknown format '" + name + "'");
        };
    }

    // A SINGLE BARE KEYWORD; NOT A VAR REFERENCE (THESE PROPERTIES TAKE ONLY ENUM CONSTANTS)
    private static String keyword(UIValue value, String prop) {
        if (value instanceof UIValue.Id id) return id.name();
        throw new UIFormatException(null, -1, prop + " expects a keyword");
    }

    // AN Id IS ONE FLAG, A Flags IS SEVERAL; ANYTHING ELSE IS NOT AN ANCHOR/ALIGN VALUE
    private static List<String> names(UIValue value) {
        if (value instanceof UIValue.Id id) return List.of(id.name());
        if (value instanceof UIValue.Flags flags) return flags.names();
        throw new UIFormatException(null, -1, "expected placement flags");
    }

    private static UIFormatException contradiction(String axis) {
        return new UIFormatException(null, -1, "contradictory " + axis + " anchors");
    }
}
