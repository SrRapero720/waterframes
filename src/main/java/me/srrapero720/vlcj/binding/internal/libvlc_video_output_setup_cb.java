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
public interface libvlc_video_output_setup_cb extends Callback {

    /**
     * Callback prototype called to initialize user data.
     * Setup the rendering environment.
     *
     * @param opaque private pointer passed to the @a libvlc_video_set_output_callbacks()
     *               on input. The callback can change this value on output to be
     *               passed to all the other callbacks set on @a libvlc_video_set_output_callbacks().
     *               [IN/OUT]
     * @param cfg requested configuration of the video device [IN]
     * @param out libvlc_video_setup_device_info_t* to fill [OUT]
     * @return true on success
     *
     * @since LibVLC 4.0.0 or later
     */
    int setup(Pointer opaque, libvlc_video_setup_device_cfg_t cfg, libvlc_video_setup_device_info_t out);
}
