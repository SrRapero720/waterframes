package me.srrapero720.vlcj.binding.internal;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Media Player timer point
 *
 * Note ts and system_date values should not be used directly by the user.
 * <p>
 * libvlc_media_player_time_point_interpolate() will read these values and
 * return an interpolated ts.
 *
 * @see libvlc_media_player_watch_time_on_update
 */
public class libvlc_media_player_time_point_t extends Structure {

    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList("position", "rate", "ts_us", "length_us", "system_date_us"));

    public static class ByValue extends libvlc_audio_track_t implements Structure.ByValue {}

    /** Position in the range [0.0f;1.0] */
    public double position;

    /** Rate of the player */
    public double rate;

    /** Valid time in us >= 0 or -1 */
    public long ts_us;

    /** Valid length in us >= 1 or 0 */
    public long length_us;

    /**
     * System date, in us, of this record (always valid).
     * <p>
     * Based on libvlc_clock(). This date can be in the future or in the past.
     * <p>
     * The special value of INT64_MAX mean that the clock was paused when this
     * point was updated. In that case,
     * <p>
     * libvlc_media_player_time_point_interpolate() will return the current
     * ts/pos of this point (there is nothing to interpolate).
     */
    public long system_date_us;

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_ORDER;
    }
}
