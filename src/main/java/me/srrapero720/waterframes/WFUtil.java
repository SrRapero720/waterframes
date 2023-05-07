package me.srrapero720.waterframes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WFUtil {
    public static List<String> getJsonArrayStringResource(String path) {
        var LOGGER = LogUtils.getLogger();

        var inputStream = new Stationary<InputStreamReader>();
        var bufferedReader = new Stationary<BufferedReader>();

        return ThreadUtil.tryAndReturn(defaultVar -> {
            var res = WFUtil.class.getClassLoader().getResourceAsStream(path);
            if (res != null) {
                inputStream.set(new InputStreamReader(res, StandardCharsets.UTF_8));
                bufferedReader.set(new BufferedReader(inputStream.get()));

                return new Gson().fromJson(bufferedReader.get(), new TypeToken<List<String>>() {}.getType());
            } else throw new IllegalArgumentException("File not found!");

        }, e -> LOGGER.error("Exception trying to read JSON from {}", path, e), returnedVar -> {
            returnedVar.forEach(LOGGER::debug);

            // ENSURE NO MEMORY LEAKS
            ThreadUtil.trySimple(() -> {
                if (!bufferedReader.isEmpty()) bufferedReader.get().close();
                if (!inputStream.isEmpty()) inputStream.get().close();
            });
        }, (List<String>) new ArrayList<String>());
    }

    public static boolean validUrl(String url) {
        return ThreadUtil.tryAndReturn(defaultVar -> {
            new URI(url);
            return true;
        }, false);
    }

    public static class Stationary<T> {
        protected T object;
        public Stationary() { object = null; }
        public Stationary(T obj) { object = obj; }

        public void set(T obj) { object = obj; }
        public T get() { return object; }
        public boolean isEmpty() { return object == null; }

        public boolean valid() { return true; }
    }
}
