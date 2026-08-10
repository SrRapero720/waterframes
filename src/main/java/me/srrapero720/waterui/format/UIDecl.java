package me.srrapero720.waterui.format;

import net.minecraft.resources.ResourceLocation;

/**
 * One variable declaration (UI-SPEC.md §5.1). {@link Mode#LIVE} is {@code var} (a host supplier
 * read at runtime); {@link Mode#BAKED} is {@code final var} (one value fixed at build). {@code type}
 * is null when omitted (inferred from the use site, §5.1); {@code inline} is the {@code = value}
 * default, non-null only on a baked declaration; {@code source} is the document that declared it, so
 * a contract error names the file the variable came from across an import chain.
 */
public record UIDecl(Mode mode, String type, String name, UIValue inline, ResourceLocation source, int line) {
    public enum Mode { LIVE, BAKED }
}
