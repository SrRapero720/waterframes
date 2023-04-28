package me.srrapero720.waterframes.watercore_supplier;

import java.util.regex.Pattern;

public class YTExtractor {
    private static final Pattern PATTERN = Pattern.compile("(?:(?:youtube\\.com\\/(?:watch\\?.*v=|user\\/\\S+|(?:v|embed)\\/)|youtu\\.be\\/)([^&\\n?#]+))");

    private final String ID;
    public YTExtractor(String url) {
        var matcher = PATTERN.matcher(url);
        ID = matcher.find() ? matcher.group(1) : null;
    }

    public boolean isValid() { return ID != null; }

    @Override
    public String toString() { return ID; }
}