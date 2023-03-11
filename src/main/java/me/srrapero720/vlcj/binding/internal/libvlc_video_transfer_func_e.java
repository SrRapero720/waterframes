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

/**
 *
 */
public enum libvlc_video_transfer_func_e {

    libvlc_video_transfer_func_LINEAR   (1),
    libvlc_video_transfer_func_SRGB     (2),
    libvlc_video_transfer_func_BT470_BG (3),
    libvlc_video_transfer_func_BT470_M  (4),
    libvlc_video_transfer_func_BT709    (5),
    libvlc_video_transfer_func_PQ       (6),
    libvlc_video_transfer_func_SMPTE_240(7),
    libvlc_video_transfer_func_HLG      (8);

    private static final Map<Integer, libvlc_video_transfer_func_e> INT_MAP = new HashMap<Integer, libvlc_video_transfer_func_e>();

    static {
        for (libvlc_video_transfer_func_e value : libvlc_video_transfer_func_e.values()) {
            INT_MAP.put(value.intValue, value);
        }
    }

    public static libvlc_video_transfer_func_e videoTransferFunc(int intValue) {
        return INT_MAP.get(intValue);
    }

    private final int intValue;

    libvlc_video_transfer_func_e(int intValue) {
        this.intValue = intValue;
    }

    public int intValue() {
        return intValue;
    }
}
