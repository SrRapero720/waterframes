package me.srrapero720.waterframes.cossporting;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;

public class Crossponent {
    public static MutableComponent text(String value) {
        return new TextComponent(value);
    }

    public static MutableComponent text(String value, Style style) {
        return text(value).withStyle(style);
    }

    public static MutableComponent text(String value, ChatFormatting formatting) {
        return text(value).withStyle(formatting);
    }


    public static MutableComponent text(String value, Component ...components) {
        MutableComponent c = text(value);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent text(String value, Style style, Component ...components) {
        MutableComponent c = text(value).withStyle(style);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent text(String value, ChatFormatting formatting, Component ...components) {
        MutableComponent c = text(value).withStyle(formatting);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent text(String value, ChatFormatting... formattings) {
        return text(value).withStyle(formattings);
    }


    public static MutableComponent translatable(String key) {
        return new TranslatableComponent(key);
    }

    public static MutableComponent translatable(String key, Style style) {
        return translatable(key).withStyle(style);
    }

    public static MutableComponent translatable(String key, ChatFormatting formatting) {
        return translatable(key).withStyle(formatting);
    }


    public static MutableComponent translatable(String key, Component ...components) {
        MutableComponent c = translatable(key);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent translatable(String key, Style style, Component ...components) {
        MutableComponent c = translatable(key).withStyle(style);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent translatable(String key, ChatFormatting formatting, Component ...components) {
        MutableComponent c = translatable(key).withStyle(formatting);
        for (Component cs: components) c.append(cs);
        return c;
    }

    public static MutableComponent translatable(String key, ChatFormatting... formattings) {
        return translatable(key).withStyle(formattings);
    }
}