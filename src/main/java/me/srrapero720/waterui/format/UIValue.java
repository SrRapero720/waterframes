package me.srrapero720.waterui.format;

import java.util.List;

/**
 * Parsed value node of the {@code .ui} grammar (UI-SPEC.md §4). The parser keeps values as this
 * small tree instead of raw strings, so the builder decodes structure once instead of re-splitting
 * text. {@link Num} keeps its source text to tell {@code int}/{@code long} from {@code float}/
 * {@code double} and to format event payloads; {@link Str} keeps its {@code {name}} spans for the
 * string-interpolation pass (§5.4).
 */
public sealed interface UIValue {
    /** A number literal; {@code raw} is the exact source text, {@code value} its double form. */
    record Num(double value, String raw) implements UIValue {}

    /** A packed ARGB colour literal (§4). */
    record Hex(int argb) implements UIValue {}

    /** A string literal, still carrying any {@code {name}} interpolation spans (§5.4). */
    record Str(String text) implements UIValue {}

    /** A bare identifier: a keyword the property expects, or a declared variable reference (§4). */
    record Id(String name) implements UIValue {}

    /** A {@code {a, b, ...}} tuple; commas inside never leak into the enclosing bag (§4). */
    record Tuple(List<UIValue> items) implements UIValue {}

    /** A {@code name(a, b, ...)} call: a handler, {@code loadAtlas(...)} or an atlas handle (§4, §5.7). */
    record Call(String name, List<UIValue> args) implements UIValue {}

    /** A {@code A|B|C} flag set of keywords (§4). */
    record Flags(List<String> names) implements UIValue {}
}
