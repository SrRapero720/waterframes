package me.srrapero720.waterui.format;

import me.srrapero720.waterui.theme.Icon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a parameterless host method as the live source of a {@code var} binding in a {@code .ui}
 * document (UI-SPEC.md §5.5). The method returns one of {@code boolean}, {@code long}/{@code int},
 * {@code String}, {@code List<Component>} (tooltips) or {@link Icon}, and its {@link #value()} is
 * the variable name screens reference (e.g. {@code value=loop}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UIVar {
    String value();
}
