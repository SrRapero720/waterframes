package me.srrapero720.waterui.format;

/**
 * One {@code import} statement (UI-SPEC.md §7). The reference form decides the {@link Kind}: a
 * {@code :} marks another mod's document (alias required), a {@code .} marks a Java element class,
 * anything else is a local document path. An import is an implicit {@code final var} named by its
 * {@code alias} (§7.1).
 */
public record UIImport(Kind kind, String ref, String alias, int line) {
    public enum Kind { LOCAL, CROSSMOD, JAVA }
}
