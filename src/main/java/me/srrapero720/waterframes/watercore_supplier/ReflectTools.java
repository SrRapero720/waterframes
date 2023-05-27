package me.srrapero720.waterframes.watercore_supplier;

import java.lang.reflect.Field;

public class ReflectTools {
    public static <T> Field field(Class<? super T> clazz, String offical, String obfuscated) {
        try {
            return field(clazz, obfuscated);
        } catch (RuntimeException var6) {
            try {
                return field(clazz, offical);
            } catch (Exception var5) {
                throw new RuntimeException("Unable to locate field " + clazz.getSimpleName() + "." + offical + " (" + obfuscated + ")", var5);
            }
        } catch (Exception var7) {
            throw new RuntimeException("Unable to locate field " + clazz.getSimpleName() + "." + offical + " (" + obfuscated + ")", var7);
        }
    }

    public static <T> Field field(Class<? super T> clazz, String offical) {
        try {
            Field f = clazz.getDeclaredField(offical);
            f.setAccessible(true);
            return f;
        } catch (Exception var3) {
            throw new RuntimeException("Unable to locate field " + clazz.getSimpleName() + "." + offical, var3);
        }
    }
}