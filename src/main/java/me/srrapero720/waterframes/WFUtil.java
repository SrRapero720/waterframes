package me.srrapero720.waterframes;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.image.BufferedImage;
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

    public static int preRender(BufferedImage image, int width, int height) {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        boolean hasAlpha = false;

        if (image.getColorModel().hasAlpha()) for (int pixel : pixels)
            if ((pixel >> 24 & 0xFF) < 0xFF) {
                hasAlpha = true;
                break;
            }

        int bytesPerPixel = hasAlpha ? 4 : 3;
        var buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
            buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
            buffer.put((byte) (pixel & 0xFF)); // Blue component
            if (hasAlpha) buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
        }
        buffer.flip();

        int textureID = GlStateManager._genTexture(); //Generate texture ID
        RenderSystem.bindTexture(textureID); //Bind texture ID

        //Setup wrap mode
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        if (!hasAlpha) RenderSystem.pixelStore(GL11.GL_UNPACK_ALIGNMENT, 1);

        // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3315, 0);

        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);

        //Return the texture ID so we can bind it later again
        return textureID;
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
