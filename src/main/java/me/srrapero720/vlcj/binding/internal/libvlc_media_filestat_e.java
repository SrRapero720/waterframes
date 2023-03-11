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
public enum libvlc_media_filestat_e {

    libvlc_media_filestat_mtime(0),
    libvlc_media_filestat_size(1);

    private static final Map<Integer, libvlc_media_filestat_e> INT_MAP = new HashMap<Integer, libvlc_media_filestat_e>();

    static {
        for (libvlc_media_filestat_e value : libvlc_media_filestat_e.values()) {
            INT_MAP.put(value.intValue, value);
        }
    }

    public static libvlc_media_filestat_e mediaStatType(int intValue) {
        return INT_MAP.get(intValue);
    }

    private final int intValue;

    libvlc_media_filestat_e(int intValue) {
        this.intValue = intValue;
    }

    public int intValue() {
        return intValue;
    }
}
