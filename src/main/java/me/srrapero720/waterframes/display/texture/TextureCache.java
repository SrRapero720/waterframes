package me.srrapero720.waterframes.display.texture;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.srrapero720.waterframes.watercore_supplier.GifDecoder;
import me.srrapero720.waterframes.WFConfig;
import me.srrapero720.waterframes.display.IDisplay;
import me.srrapero720.waterframes.display.MediaDisplay;
import me.srrapero720.waterframes.display.PictureDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import team.creative.creativecore.common.util.math.vec.Vec3d;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    private static final Map<String, TextureCache> cached = new HashMap<>();

    public static void renderInternal() {
        for (var iterator = cached.values().iterator(); iterator.hasNext();) {
            var type = iterator.next();
            if (!type.isUsed()) {
                type.remove();
                iterator.remove();
            }
        }
    }

    public static void tick() { MediaDisplay.tick(); }

    public static void reloadAll() {
        for (var cache : cached.values())
            cache.reload();
    }
    
    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            for (TextureCache cache : cached.values())
                cache.remove();
            cached.clear();
            MediaDisplay.unload();
        }
    }
    
    public static TextureCache get(String url) {
        TextureCache cache = cached.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureCache(url);
        cached.put(url, cache);
        return cache;
    }
    
    public final String url;
    private int[] textures;
    private int width;
    private int height;
    private long[] delay;
    private long duration;
    private boolean isVideo;
    
    private TextureSeeker seeker;
    private boolean ready = false;
    private String error;
    
    private int usage = 0;
    
    private GifDecoder decoder;
    private int remaining;
    
    public TextureCache(String url) {
        this.url = url;
        use();
        trySeek();
    }
    
    private void trySeek() {
        if (seeker != null) return;
        synchronized (TextureSeeker.LOCK) {
            if (TextureSeeker.activeDownloads < TextureSeeker.MAXIMUM_ACTIVE_DOWNLOADS && !this.url.isEmpty())
                this.seeker = new TextureSeeker(this);
        }
    }
    
    private int getTexture(int index) {
        if (textures[index] == -1 && decoder != null) {
            textures[index] = uploadFrame(decoder.getFrame(index), width, height);
            remaining--;
            if (remaining <= 0)
                decoder = null;
        }
        return textures[index];
    }
    
    public int getTexture(long time) {
        if (textures == null)
            return -1;
        if (textures.length == 1)
            return getTexture(0);
        int last = getTexture(0);
        for (int i = 1; i < delay.length; i++) {
            if (delay[i] > time)
                break;
            last = getTexture(i);
        }
        return last;
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return createDisplay(pos, url, volume, minDistance, maxDistance, loop, false);
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (textures == null && !noVideo && !WFConfig.DISABLE_VLC.get()) return MediaDisplay.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
        return new PictureDisplay(this);
    }
    
    public String getError() {
        return error;
    }
    
    public void processVideo() {
        this.textures = null;
        this.error = null;
        this.isVideo = true;
        this.ready = true;
        this.seeker = null;
    }
    
    public void processFailed(String error) {
        this.textures = null;
        this.error = error;
        this.ready = true;
        this.seeker = null;
    }
    
    public void process(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        textures = new int[] { uploadFrame(image, width, height) };
        delay = new long[] { 0 };
        duration = 0;
        seeker = null;
        ready = true;
    }
    
    public void process(GifDecoder decoder) {
        Dimension frameSize = decoder.getFrameSize();
        width = (int) frameSize.getWidth();
        height = (int) frameSize.getHeight();
        textures = new int[decoder.getFrameCount()];
        delay = new long[decoder.getFrameCount()];
        
        this.decoder = decoder;
        this.remaining = decoder.getFrameCount();
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            textures[i] = -1;
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
        seeker = null;
        ready = true;
    }
    
    public boolean ready() {
        if (ready)
            return true;
        trySeek();
        return false;
    }
    
    public boolean isVideo() {
        return isVideo;
    }
    
    public void reload() {
        remove();
        error = null;
        trySeek();
    }
    
    public void use() {
        usage++;
    }
    
    public void unuse() {
        usage--;
    }
    
    public boolean isUsed() {
        return usage > 0;
    }
    
    public void remove() {
        ready = false;
        if (textures != null) for (int texture: textures) GlStateManager._deleteTexture(texture);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public long[] getDelay() {
        return delay;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public boolean isAnimated() {
        return textures.length > 1;
    }
    
    public int getFrameCount() {
        return textures.length;
    }
    
    private static int uploadFrame(BufferedImage image, int width, int height) {
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        boolean hasAlpha = false;
        if (image.getColorModel().hasAlpha()) {
            for (int pixel : pixels) {
                if ((pixel >> 24 & 0xFF) < 0xFF) {
                    hasAlpha = true;
                    break;
                }
            }
        }
        int bytesPerPixel = hasAlpha ? 4 : 3;
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bytesPerPixel);
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
            buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
            buffer.put((byte) (pixel & 0xFF)); // Blue component
            if (hasAlpha) {
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component. Only for RGBA
            }
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
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10241, 9729);
        
        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, hasAlpha ? GL11.GL_RGBA8 : GL11.GL_RGB8, width, height, 0, hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buffer);
        
        //Return the texture ID so we can bind it later again
        return textureID;
    }
}
