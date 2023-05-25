package me.srrapero720.waterframes.display.texture;

import com.mojang.logging.LogUtils;
import me.srrapero720.waterframes.WFUtil;
import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("ResultOfMethodCallIgnored")
public class PictureStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final File dir = new File(Minecraft.getInstance().gameDirectory, "cache/waterframes");
    private static final File index = new File(dir, "indexer");
    private static final Map<String, Entry> entries = new HashMap<>();

    static {
        if (!dir.exists()) dir.mkdirs();
        if (index.exists()) {
            var stream = new WFUtil.Stationary<DataInputStream>();

            ThreadUtil.trySimple(() -> {
                stream.set(new DataInputStream(new GZIPInputStream(new FileInputStream(index))));
                int length = stream.get().readInt();

                for (int i = 0; i < length; i++) {
                    var url = stream.get().readUTF();
                    var tag = stream.get().readUTF();
                    var time = stream.get().readLong();
                    var expireTime = stream.get().readLong();
                    var entry = new Entry(url, tag.length() > 0 ? tag : null, time, expireTime);
                    entries.put(entry.getUrl(), entry);
                }
            }, (e) -> LOGGER.error("Failed to load indexes", e), () -> IOUtils.closeQuietly(stream.get()));
        }
    }

    public static File getFile(String url) {
        return new File(dir, Base64.encodeBase64String(url.getBytes()));
    }

    public static void saveFile(String url, String tag, long time, long expireTime, byte[] data) {
        var entry = new Entry(url, tag, time, expireTime);
        var saved = false;
        var out = (OutputStream) null;
        var file = getFile(entry.url);

        try {
            out = new FileOutputStream(file);
            out.write(data);
            saved = true;
        } catch (Exception e) { LOGGER.error("Failed to save cache file {}", url, e);
        } finally { IOUtils.closeQuietly(out); }

        // SAVE INDEX FIST
        if (saved && refreshAllIndexOnFile()) entries.put(url, entry);
        else if (file.exists()) file.delete();
    }

    private static boolean refreshAllIndexOnFile() {
        var out = (DataOutputStream) null;
        try {
            out = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(index)));
            out.writeInt(entries.size());

            for (var mapEntry : entries.entrySet()) {
                var entry = mapEntry.getValue();
                out.writeUTF(entry.getUrl());
                out.writeUTF(entry.getTag() == null ? "" : entry.getTag());
                out.writeLong(entry.getTime());
                out.writeLong(entry.getExpireTime());
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save cache index", e);
            return false;
        } finally { IOUtils.closeQuietly(out); }
    }

    public static Entry getEntry(String url) { return entries.get(url); }
    public static void updateEntry(Entry fresh) {
        entries.put(fresh.url, fresh);
    }
    public static void deleteEntry(String url) {
        entries.remove(url);
        var file = getFile(url);
        if (file.exists()) file.delete();
    }

    public static final class Entry {
        private final String url;
        private String tag;
        private long time;
        private long expireTime;

        public Entry(String url, String tag, long time, long expireTime) {
            this.url = url;
            this.tag = tag;
            this.time = time;
            this.expireTime = expireTime;
        }

        public void setTag(String tag) { this.tag = tag; }
        public void setTime(long time) { this.time = time; }
        public void setExpireTime(long expireTime) { this.expireTime = expireTime; }
        public String getUrl() { return url; }
        public String getTag() { return tag; }
        public long getTime() { return time; }
        public long getExpireTime() { return expireTime; }
    }
}
