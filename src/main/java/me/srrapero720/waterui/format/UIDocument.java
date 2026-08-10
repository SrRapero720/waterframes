package me.srrapero720.waterui.format;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * A parsed {@code .ui} document (UI-SPEC.md §6), in strict statement order: an optional theme
 * reference, the imports, the variable declarations, an optional {@code root[...]} preference bag
 * and the top-level element tree. {@code themeId} is null when no {@code theme} was declared, which
 * the theme resolver tells apart from a declared-but-broken reference (§2).
 */
public record UIDocument(ResourceLocation source, ResourceLocation themeId,
                         List<UIImport> imports, List<UIDecl> decls,
                         LinkedHashMap<String, UIValue> rootProps, List<UINode> tree) {}
