package me.srrapero720.waterframes.core.tools;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static me.srrapero720.waterframes.WaterFrames.LOGGER;

public class JarTool {
    private static final Gson GSON = new Gson();
    private static final Marker IT = MarkerManager.getMarker("Tools");

    public static List<String> readStringList(ClassLoader loader, String path) {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(readResource(loader, path)))) {
            result.addAll(GSON.fromJson(reader, new TypeToken<List<String>>() {}.getType()));
        } catch (Exception e) {
            LOGGER.fatal(IT, "Exception trying to read JSON from {}", path, e);
        }

        return result;
    }

    public static InputStream readResource(ClassLoader loader, String source) {
        InputStream is = loader.getResourceAsStream(source);
        if (is == null && source.startsWith("/")) is = loader.getResourceAsStream(source.substring(1));
        return is;
    }
}