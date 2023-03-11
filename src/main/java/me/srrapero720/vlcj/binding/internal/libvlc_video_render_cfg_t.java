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

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class libvlc_video_render_cfg_t extends Structure {

    private static final List<String> FIELD_ORDER = Collections.unmodifiableList(Arrays.asList("width", "height", "bitdepth", "full_range", "colorspace", "primaries", "transfer", "device"));

    public int width;       /** rendering video width in pixel */
    public int height;      /** rendering video height in pixel */
    public int bitdepth;    /** rendering video bit depth in bits per channel */
    public int full_range;  /** video is full range or studio/limited range */
    public int colorspace;  /** video color space */
    public int primaries;   /** video color primaries */
    public int transfer;    /** video transfer function */
    public Pointer device;  /** device used for rendering, IDirect3DDevice9* for D3D9 */

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_ORDER;
    }
}
