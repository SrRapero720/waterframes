package me.srrapero720.waterframes.display.texture;

import me.srrapero720.waterframes.FramesConfig;
import me.srrapero720.waterframes.WaterFrames;
import me.srrapero720.gifs.GifDecoder;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class TextureSeeker extends Thread {
    
    public static final Logger LOGGER = LogManager.getLogger(WaterFrames.class);
    
    public static final TextureStorage TEXTURE_STORAGE = new TextureStorage();
    public static final DateFormat FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    public static final Object LOCK = new Object();
    public static final int MAXIMUM_ACTIVE_DOWNLOADS = 5;
    
    public static int activeDownloads = 0;
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    private TextureCache cache;
    
    public TextureSeeker(TextureCache cache) {
        this.cache = cache;
        synchronized (TextureSeeker.LOCK) {
            TextureSeeker.activeDownloads++;
        }
        setName("WF Seeker \"" + cache.url + "\"");
        setDaemon(true);
        start();
    }
    
    @Override
    public void run() {
        Exception exception = null;
        boolean isVideo = false;
        boolean processed = false;
        try {
            byte[] data = load(cache.url);
            String type = readType(data);
            ByteArrayInputStream in = null;
            try {
                in = new ByteArrayInputStream(data);
                if (type != null && type.equalsIgnoreCase("gif")) {
                    GifDecoder gif = new GifDecoder();
                    int status = gif.read(in);
                    if (status == GifDecoder.STATUS_OK) {
                        mc.executeBlocking(() -> cache.process(gif));
                        processed = true;
                    } else {
                        LOGGER.error("Failed to read gif: {}", status);
                    }
                } else {
                    try {
                        BufferedImage image = ImageIO.read(in);
                        if (image != null) {
                            mc.executeBlocking(() -> cache.process(image));
                            processed = true;
                        }
                    } catch (IOException e1) {
                        exception = e1;
                        LOGGER.error("Failed to parse BufferedImage from stream", e1);
                    }
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (FoundVideoException e) {
            if (!FramesConfig.DISABLE_VLC.get()) {
                cache.processVideo();
                isVideo = true;
            } else
                exception = e;
        } catch (NoConnectionException e) {
            exception = e;
        } catch (Exception e) {
            exception = e;
            LOGGER.error("An exception occurred while loading Waterframes image", e);
        }
        if (!isVideo && !processed) {
            if (exception == null)
                cache.processFailed("download.exception.gif");
            else if (exception instanceof FoundVideoException)
                cache.processFailed("No image found");
            else if (exception.getMessage().startsWith("Server returned HTTP response code: 403"))
                cache.processFailed("download.exception.forbidden");
            else if (exception.getMessage().startsWith("Server returned HTTP response code: 404"))
                cache.processFailed("download.exception.notfound");
            else
                cache.processFailed("download.exception.invalid");
            TEXTURE_STORAGE.deleteEntry(cache.url);
        }
        
        synchronized (TextureSeeker.LOCK) {
            TextureSeeker.activeDownloads--;
        }
    }
    
    public static byte[] load(String url) throws IOException, FoundVideoException, NoConnectionException {
        TextureStorage.CacheEntry entry = TEXTURE_STORAGE.getEntry(url);
        long requestTime = System.currentTimeMillis();
        URLConnection connection = new URL(url).openConnection();
        try {
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            int responseCode = -1;
            if (connection instanceof HttpURLConnection httpConnection) {
                if (entry != null && entry.getFile().exists()) {
                    if (entry.getEtag() != null)
                        httpConnection.setRequestProperty("If-None-Match", entry.getEtag());
                    else if (entry.getTime() != -1)
                        httpConnection.setRequestProperty("If-Modified-Since", FORMAT.format(new Date(entry.getTime())));
                }
                responseCode = httpConnection.getResponseCode();
            }
            InputStream in = null;
            try {
                in = connection.getInputStream();
                if (responseCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    String type = connection.getContentType();
                    if (type == null)
                        throw new NoConnectionException();
                    if (!type.startsWith("image"))
                        throw new FoundVideoException();
                }
                
                String etag = connection.getHeaderField("ETag");
                long lastModifiedTimestamp;
                long expireTimestamp = -1;
                String maxAge = connection.getHeaderField("max-age");
                if (maxAge != null && !maxAge.isEmpty()) {
                    try {
                        expireTimestamp = requestTime + Long.parseLong(maxAge) * 1000;
                    } catch (NumberFormatException e) {}
                }
                String expires = connection.getHeaderField("Expires");
                if (expires != null && !expires.isEmpty()) {
                    try {
                        expireTimestamp = FORMAT.parse(expires).getTime();
                    } catch (ParseException e) {}
                }
                String lastModified = connection.getHeaderField("Last-Modified");
                if (lastModified != null && !lastModified.isEmpty()) {
                    try {
                        lastModifiedTimestamp = FORMAT.parse(lastModified).getTime();
                    } catch (ParseException e) {
                        lastModifiedTimestamp = requestTime;
                    }
                } else {
                    lastModifiedTimestamp = requestTime;
                }
                if (entry != null) {
                    if (etag != null && !etag.isEmpty()) {
                        entry.setEtag(etag);
                    }
                    entry.setTime(lastModifiedTimestamp);
                    if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        File file = entry.getFile();
                        if (file.exists()) {
                            FileInputStream fileStream = new FileInputStream(file);
                            try {
                                return IOUtils.toByteArray(fileStream);
                            } finally {
                                fileStream.close();
                            }
                            
                        }
                    }
                }
                byte[] data = IOUtils.toByteArray(in);
                if (readType(data) == null)
                    throw new FoundVideoException();
                TEXTURE_STORAGE.save(url, etag, lastModifiedTimestamp, expireTimestamp, data);
                return data;
            } finally {
                IOUtils.closeQuietly(in);
            }
        } finally {
            if (connection instanceof HttpURLConnection httpConnection)
                httpConnection.disconnect();

        }
    }
    
    private static String readType(byte[] input) throws IOException {
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(input);
            return readType(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
    
    private static String readType(InputStream input) throws IOException {
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        Iterator iter = ImageIO.getImageReaders(stream);
        if (!iter.hasNext())
            return null;
        
        ImageReader reader = (ImageReader) iter.next();
        
        if (reader.getFormatName().equalsIgnoreCase("gif"))
            return "gif";
        
        ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);
        try {
            reader.read(0, param);
        } catch (IOException e) {
            LOGGER.error("Failed to parse input format", e);
        } finally {
            reader.dispose();
            IOUtils.closeQuietly(stream);
        }
        input.reset();
        return reader.getFormatName();
    }
    
    public static class FoundVideoException extends Exception {}
    
    public static class NoConnectionException extends Exception {
        
        public NoConnectionException() {
            super("");
        }
        
    }
    
}
