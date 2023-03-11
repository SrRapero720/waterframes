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

/**
 *
 */
public enum libvlc_audio_output_mixmode_t {

    libvlc_AudioMixMode_Unset(0),
    libvlc_AudioMixMode_Stereo(1),
    libvlc_AudioMixMode_Binaural(2),
    libvlc_AudioMixMode_4_0(3),
    libvlc_AudioMixMode_5_1(4),
    libvlc_AudioMixMode_7_1(5);

    private final int intValue;
    libvlc_audio_output_mixmode_t(int intValue) {
        this.intValue = intValue;
    }

    public int intValue() {
        return intValue;
    }
}
