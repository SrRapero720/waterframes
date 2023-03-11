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

package me.srrapero720.vlcj.medialist;

import me.srrapero720.vlcj.binding.internal.libvlc_event_e;
import me.srrapero720.vlcj.binding.internal.libvlc_event_manager_t;
import me.srrapero720.vlcj.binding.internal.libvlc_event_t;
import me.srrapero720.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.vlcj.medialist.events.MediaListEventFactory;
import me.srrapero720.vlcj.support.eventmanager.EventNotification;
import me.srrapero720.vlcj.support.eventmanager.NativeEventManager;

import static me.srrapero720.vlcj.binding.lib.LibVlc.libvlc_media_list_event_manager;

final class MediaListNativeEventManager extends NativeEventManager<MediaList, MediaListEventListener> {

    MediaListNativeEventManager(libvlc_instance_t libvlcInstance, MediaList eventObject) {
        super(libvlcInstance, eventObject, libvlc_event_e.libvlc_MediaListItemAdded, libvlc_event_e.libvlc_MediaListEndReached, "media-list-events");
    }

    @Override
    protected libvlc_event_manager_t onGetEventManager(MediaList eventObject) {
        return libvlc_media_list_event_manager(eventObject.mediaListInstance());
    }

    @Override
    protected EventNotification<MediaListEventListener> onCreateEvent(libvlc_instance_t libvlcInstance, libvlc_event_t event, MediaList eventObject) {
        return MediaListEventFactory.createEvent(libvlcInstance, eventObject, event);
    }

}
