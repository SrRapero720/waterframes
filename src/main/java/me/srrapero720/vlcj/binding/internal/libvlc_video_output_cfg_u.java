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
import com.sun.jna.Union;

/**
 *
 */
public class libvlc_video_output_cfg_u extends Union {

    public static class ByValue extends libvlc_video_output_cfg_u implements Union.ByValue {}

    public int dxgi_format;    /** the rendering DXGI_FORMAT for \ref libvlc_video_engine_d3d11*/
    public int d3d9_format;    /** the rendering D3DFORMAT for \ref libvlc_video_engine_d3d9 */
    public int opengl_format;  /** the rendering GLint GL_RGBA or GL_RGB for \ref libvlc_video_engine_opengl and for \ref libvlc_video_engine_gles2 */
    public Pointer p_surface;  /** currently unused */
}
