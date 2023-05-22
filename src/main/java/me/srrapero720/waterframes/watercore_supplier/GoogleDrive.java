package me.srrapero720.waterframes.watercore_supplier;

import java.net.URL;

public class GoogleDrive {
    private final String URI;
    private static final String apiKey = "AIzaSyBiFNT6TTo506kCYYwA2NHqs36TlXC1DMo";

    public GoogleDrive(String googleDriveLink) {
        this.URI = constructApiUrl(googleDriveLink, apiKey);
    }

    public String get() { return URI; }

    public static String constructApiUrl(String googleDriveLink, String apiKey) {
        return ThreadUtil.tryAndReturn(defaultVar -> {
            var url = new URL(googleDriveLink);
            if (isValidGoogleDriveUrl(url)) {
                String fileId = extractFileIdFromUrl(url);
                return String.format("https://www.googleapis.com/drive/v3/files/%s?alt=media&key=%s", fileId, apiKey);
            }

            return defaultVar;
        }, Throwable::printStackTrace, null);
    }

    // UTIL

    private static boolean isValidGoogleDriveUrl(URL url) {
        return url.getHost().equals("drive.google.com") && url.getPath().startsWith("/file/d/");
    }

    private static String extractFileIdFromUrl(URL url) {
        String path = url.getPath();
        int startIndex = path.indexOf("/file/d/") + 8;
        int endIndex = path.indexOf('/', startIndex);
        if (endIndex == -1) {
            endIndex = path.length();
        }
        return path.substring(startIndex, endIndex);
    }
}