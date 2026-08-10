package me.srrapero720.waterui.format;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * One parsed element node (UI-SPEC.md §6, §8): its tag, source line, the {@code [...]} bag as a
 * key/value map of {@link UIValue}s in source order (first-defined wins) and its {@code {...}}
 * children. Events live in the same {@code props} map under their {@code on<Event>} keys. {@code source}
 * is the document the node was parsed from, so an error in an imported subtree names its own file.
 */
public record UINode(String tag, int line, LinkedHashMap<String, UIValue> props, List<UINode> children, ResourceLocation source) {}
