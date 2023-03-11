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
 * Copyright 2009-2022 Caprica Software Limited.
 */

package me.srrapero720.vlcj.player.component;

import me.srrapero720.vlcj.factory.MediaPlayerFactory;
import me.srrapero720.vlcj.media.MediaRef;
import me.srrapero720.vlcj.medialist.MediaList;
import me.srrapero720.vlcj.medialist.MediaListEventListener;
import me.srrapero720.vlcj.player.component.callback.CallbackImagePainter;
import me.srrapero720.vlcj.player.embedded.fullscreen.FullScreenStrategy;
import me.srrapero720.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import me.srrapero720.vlcj.player.embedded.videosurface.callback.RenderCallback;
import me.srrapero720.vlcj.player.list.MediaListPlayer;
import me.srrapero720.vlcj.player.list.MediaListPlayerEventListener;

import javax.swing.*;

/**
 * Base implementation of a callback "direct-rendering" media player.
 * <p>
 * This class serves to keep the {@link CallbackMediaListPlayerComponent} concrete implementation clean and
 * un-cluttered.
 */
@SuppressWarnings("serial")
public class CallbackMediaListPlayerComponentBase extends CallbackMediaPlayerComponent implements MediaListPlayerEventListener, MediaListEventListener {

    /**
     * Create a media player component.
     * <p>
     * All constructor parameters are optional, reasonable defaults will be used as needed.
     *
     * @param mediaPlayerFactory factory used to create the component
     * @param fullScreenStrategy full-screen strategy
     * @param inputEvents required input events
     * @param bufferFormatCallback buffer format callback
     * @param lockBuffers <code>true</code> if the native video buffer should be locked; <code>false</code> if not
     * @param imagePainter image painter (video renderer)
     * @param videoSurfaceComponent lightweight video surface component
     * @param renderCallback render callback
     */
    public CallbackMediaListPlayerComponentBase(MediaPlayerFactory mediaPlayerFactory, FullScreenStrategy fullScreenStrategy, InputEvents inputEvents, BufferFormatCallback bufferFormatCallback, boolean lockBuffers, CallbackImagePainter imagePainter, JComponent videoSurfaceComponent, RenderCallback renderCallback) {
        super(mediaPlayerFactory, fullScreenStrategy, inputEvents, lockBuffers, imagePainter, renderCallback, bufferFormatCallback, videoSurfaceComponent);
    }

    // === MediaListPlayerEventListener =========================================

    @Override
    public void mediaListEndReached(MediaList mediaList) {
    }

    @Override
    public void mediaListPlayerFinished(MediaListPlayer mediaListPlayer) {
    }

    @Override
    public void nextItem(MediaListPlayer mediaListPlayer, MediaRef item) {
    }

    @Override
    public void stopped(MediaListPlayer mediaListPlayer) {
    }

    // === MediaListEventListener ===============================================

    @Override
    public void mediaListWillAddItem(MediaList mediaList, MediaRef item, int index) {
    }

    @Override
    public void mediaListItemAdded(MediaList mediaList, MediaRef item, int index) {
    }

    @Override
    public void mediaListWillDeleteItem(MediaList mediaList, MediaRef item, int index) {
    }

    @Override
    public void mediaListItemDeleted(MediaList mediaList, MediaRef item, int index) {
    }

}
