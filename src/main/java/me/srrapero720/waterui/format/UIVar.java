package me.srrapero720.waterui.format;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameterless host method as the live source of a {@code var} binding in a {@code .ui}
 * document (UI-SPEC.md §5.5). The method returns one of {@code boolean}, {@code long}/{@code int},
 * {@code double}/{@code float}, {@code String}, {@code List<Component>} (tooltips),
 * {@link me.srrapero720.waterui.theme.Icon} or {@link net.minecraft.world.item.ItemStack}, and its
 * {@link #value()} is the variable name screens reference (e.g. {@code value=loop}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UIVar {
    String value();
}
