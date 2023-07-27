package me.srrapero720.waterframes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WFUtil {
    private static final Gson GSON = new Gson();

    public static List<String> getJsonArrayStringResource(String path) {
        var LOGGER = LogUtils.getLogger();

        List<String> result = null;
        try (InputStream res = WFUtil.class.getClassLoader().getResourceAsStream(path); BufferedReader reader = res != null ? new BufferedReader(new InputStreamReader(res)) : null) {
            if (reader == null) throw new NullPointerException("Failed to read resource (not found)");
            return result = GSON.fromJson(reader, new TypeToken<List<String>>() {}.getType());
        } catch (Exception e) {
            LOGGER.error("Exception trying to read JSON from {}", path, e);
        } finally {
            if (result != null) result.forEach(LOGGER::debug);
        }

        return result == null ? new ArrayList<>() : result;
    }

    public static boolean validUrl(String url) {
        return ThreadUtil.tryAndReturn(defaultVar -> {
            new URI(url);
            return true;
        }, false);
    }
}
