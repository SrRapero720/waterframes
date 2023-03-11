package me.srrapero720.vlcj.binding.internal;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface libvlc_media_player_watch_time_on_update extends Callback {

    void callback(libvlc_media_player_time_point_t value, Pointer data);
}
