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

package me.srrapero720.vlcj.medialist.events;

import me.srrapero720.vlcj.binding.internal.libvlc_event_t;
import me.srrapero720.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_t;
import me.srrapero720.vlcj.binding.internal.media_list_will_delete_item;
import me.srrapero720.vlcj.medialist.MediaList;
import me.srrapero720.vlcj.medialist.MediaListEventListener;

/**
 * Encapsulation of a media list will delete item event.
 */
final class MediaListWillDeleteItemEvent extends MediaListEvent {

    /**
     * Native media instance that will deleted.
     */
    private final libvlc_media_t item;

    /**
     * Index from which the item will be deleted.
     */
    private final int index;

    /**
     * Create a media list event.
     *
     * @param libvlcInstance native library instance
     * @param mediaList media list the event relates to
     * @param event native event
     */
    MediaListWillDeleteItemEvent(libvlc_instance_t libvlcInstance, MediaList mediaList, libvlc_event_t event) {
        super(libvlcInstance, mediaList);

        media_list_will_delete_item deleteItemEvent = ((media_list_will_delete_item) event.u.getTypedValue(media_list_will_delete_item.class));

        this.item  = deleteItemEvent.item;
        this.index = deleteItemEvent.index;
    }

    @Override
    public void notify(MediaListEventListener listener) {
        listener.mediaListWillDeleteItem(component, temporaryMediaRef(item), index);
    }

}
