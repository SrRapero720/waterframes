package me.srrapero720.waterui.format;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a host method as the handler an {@code on<Event>} spec resolves to, decoupling the
 * {@code .ui} handler id from the Java method name. The {@link #value()} is the id screens
 * reference (e.g. {@code onClick=toggleLoop}); resolution falls back to by-name reflection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UIEvent {
    String value();
}
