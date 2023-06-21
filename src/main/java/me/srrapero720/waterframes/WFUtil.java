package me.srrapero720.waterframes;

import me.srrapero720.waterframes.watercore_supplier.ThreadUtil;

import java.net.URI;

@Deprecated
public class WFUtil {

    @Deprecated
    public static boolean validUrl(String url) {
        return ThreadUtil.tryAndReturn(defaultVar -> {
            new URI(url);
            return true;
        }, false);
    }

}