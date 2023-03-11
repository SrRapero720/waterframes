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

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 *
 */
public interface libvlc_video_update_output_cb extends Callback {

    /**
     * Callback prototype called on video size changes.
     * Update the rendering output setup.
     *
     * @param opaque private pointer set on the opaque parameter of @a libvlc_video_output_setup_cb() [IN]
     * @param cfg configuration of the video that will be rendered [IN]
     * @param output configuration describing with how the rendering is setup [OUT]
     * @return
     *
     * Tone mapping, range and color conversion will be done depending on the values
     * set in the output structure.
     *
     * @since LibVLC 4.0.0 or later
     */
    int updateOutput(Pointer opaque, libvlc_video_render_cfg_t cfg, libvlc_video_output_cfg_t output);
}
