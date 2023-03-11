/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2021 Caprica Software Limited.
 */

package me.srrapero720.vlcj.binding.internal;

import java.util.HashMap;
import java.util.Map;

public enum libvlc_video_orient_t {

    libvlc_video_orient_top_left(0),       /** Normal. Top line represents top, left column left. */
    libvlc_video_orient_top_right(1),      /** Flipped horizontally */
    libvlc_video_orient_bottom_left(2),    /** Flipped vertically */
    libvlc_video_orient_bottom_right(3),   /** Rotated 180 degrees */
    libvlc_video_orient_left_top(4),       /** Transposed */
    libvlc_video_orient_left_bottom(5),    /** Rotated 90 degrees clockwise (or 270 anti-clockwise) */
    libvlc_video_orient_right_top(6),      /** Rotated 90 degrees anti-clockwise */
    libvlc_video_orient_right_bottom(7);   /** Anti-transposed */

    private static final Map<Integer, libvlc_video_orient_t> map = new HashMap<>();

    static {
        for (libvlc_video_orient_t value : libvlc_video_orient_t.values()) {
            map.put(value.intValue, value);
        }
    }

    public static libvlc_video_orient_t libvlc_video_orient(int intValue) {
        return map.get(intValue);
    }

    private final int intValue;

    libvlc_video_orient_t(int intValue) {
        this.intValue = intValue;
    }

    public int intValue() {
        return intValue;
    }
}
