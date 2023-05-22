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
    public static String getUserAgentBasedOnOS() {
        var winLegacy = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";
        var winEdge = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36 Edg/112.0.1722.68";
//        var macEdge = "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_3_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/112.0.1722.71";
//        var linuxFirefox = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/113.0.0.0 Chrome/113.0.0.0 Safari/537.36";
        return winEdge;
    }


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
