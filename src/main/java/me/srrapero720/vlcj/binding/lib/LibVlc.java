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
 * Copyright 2009-2019 Caprica Software Limited.
 */

package me.srrapero720.vlcj.binding.lib;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_output_mixmode_t;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_output_stereomode_t;
import me.srrapero720.vlcj.binding.support.runtime.RuntimeUtil;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_cleanup_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_drain_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_flush_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_output_device_t;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_output_t;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_pause_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_play_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_resume_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_set_volume_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_audio_setup_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_callback_t;
import me.srrapero720.vlcj.binding.internal.libvlc_dialog_cbs;
import me.srrapero720.vlcj.binding.internal.libvlc_dialog_display_error_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_dialog_id;
import me.srrapero720.vlcj.binding.internal.libvlc_display_callback_t;
import me.srrapero720.vlcj.binding.internal.libvlc_equalizer_t;
import me.srrapero720.vlcj.binding.internal.libvlc_event_e;
import me.srrapero720.vlcj.binding.internal.libvlc_event_manager_t;
import me.srrapero720.vlcj.binding.internal.libvlc_event_u;
import me.srrapero720.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.vlcj.binding.internal.libvlc_lock_callback_t;
import me.srrapero720.vlcj.binding.internal.libvlc_log_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_log_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_close_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_media_discoverer_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_list_player_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_list_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_open_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_media_player_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_player_time_point_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_player_watch_time_on_discontinuity;
import me.srrapero720.vlcj.binding.internal.libvlc_media_player_watch_time_on_update;
import me.srrapero720.vlcj.binding.internal.libvlc_media_read_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_media_seek_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_media_stats_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_thumbnail_request_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_track_t;
import me.srrapero720.vlcj.binding.internal.libvlc_media_tracklist_t;
import me.srrapero720.vlcj.binding.internal.libvlc_module_description_t;
import me.srrapero720.vlcj.binding.internal.libvlc_picture_list_t;
import me.srrapero720.vlcj.binding.internal.libvlc_picture_t;
import me.srrapero720.vlcj.binding.internal.libvlc_player_program_t;
import me.srrapero720.vlcj.binding.internal.libvlc_player_programlist_t;
import me.srrapero720.vlcj.binding.internal.libvlc_renderer_discoverer_t;
import me.srrapero720.vlcj.binding.internal.libvlc_renderer_item_t;
import me.srrapero720.vlcj.binding.internal.libvlc_unlock_callback_t;
import me.srrapero720.vlcj.binding.internal.libvlc_video_format_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_frameMetadata_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_getProcAddress_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_makeCurrent_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_output_cleanup_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_output_select_plane_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_output_set_resize_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_output_setup_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_swap_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_update_output_cb;
import me.srrapero720.vlcj.binding.internal.libvlc_video_viewpoint_t;
import me.srrapero720.vlcj.binding.support.types.size_tByReference;
import me.srrapero720.vlcj.binding.support.types.size_t;

/**
 * JNA interface to the libvlc native library.
 * <p>
 * This is <strong>not a complete</strong> interface to libvlc, although most functions are present.
 * <p>
 * This interface specifies the exposed methods only, the types and structures are all factored out separately in the
 * "internal" sub-package.
 * <p>
 * This code and that in the internal sub-package is structured out of necessity to interoperate with the libvlc native
 * library. This code was originally derived (but has now been completely re-written) from the original JVLC source
 * code, the copyright of which belongs to the VideoLAN team, which was distributed under GPL version 2 or later.
 * <p>
 * This system property may be useful for debugging:
 * <pre>
 * -Djna.dump_memory=true
 * </pre>
 * In the native header file, generally "char*" types must be freed, but "const char*" need (must) not.
 * <p>
 * This interface is essentially a translation of the LibVLC header files to Java, with changes for JNA/Java types. The
 * documentation in that VLC header file is reproduced here for convenience, with the appropriate Javadoc documentation
 * convention changes, the copyright of which (mostly) belongs to the VLC authors.
 */
public final class LibVlc {

    static {
        Native.register(RuntimeUtil.getLibVlcLibraryName());
    }

    private LibVlc() {
    }

    // === libvlc.h =============================================================

    /**
     * A human-readable error message for the last LibVLC error in the calling thread. The resulting
     * string is valid until another error occurs (at least until the next LibVLC call).
     * <p>
     * This will be NULL if there was no error.
     *
     * @return error message, or <code>NULL</code>
     */
    public static native String libvlc_errmsg();

    /**
     * Clears the LibVLC error status for the current thread. This is optional. By default, the
     * error status is automatically overridden when a new error occurs, and destroyed when the
     * thread exits.
     */
    public static native void libvlc_clearerr();

    /**
     * Create and initialize a libvlc instance.
     *
     * @param argc the number of arguments
     * @param argv command-line-type arguments
     * @return the libvlc instance or NULL in case of error
     */
    public static native libvlc_instance_t libvlc_new(int argc, StringArray argv);

    /**
     * Decrement the reference count of a libvlc instance, and destroy it if it reaches zero.
     *
     * @param p_instance the instance to destroy
     */
    public static native void libvlc_release(libvlc_instance_t p_instance);

    /**
     * Get the ABI version of the libvlc library.
     * <p>
     * This is different than the VLC version, which is the version of the whole
     * VLC package. The value is the same as LIBVLC_ABI_VERSION_INT used when
     * compiling.
     * <p>
     * Note: This the same value as the .so version but cross platform.
     *
     * @return a value with the following mask in hexadecimal
     *  0xFF000000: major VLC version, similar to VLC major version,
     *  0x00FF0000: major ABI version, incremented incompatible changes are added,
     *  0x0000FF00: minor ABI version, incremented when new functions are added
     *  0x000000FF: micro ABI version, incremented with new release/builds
     */
    public static native int libvlc_abi_version();

    /**
     * Increments the reference count of a libvlc instance. The initial reference count is 1 after
     * libvlc_new() returns.
     *
     * @param p_instance the instance to reference
     */
    public static native void libvlc_retain(libvlc_instance_t p_instance);

    /**
     * Try to start a user interface for the libvlc instance.
     *
     * @param p_instance the instance
     * @param name interface name, or NULL for default
     * @return 0 on success, -1 on error.
     */
    public static native int libvlc_add_intf(libvlc_instance_t p_instance, String name);

    /**
     * Sets the application name. LibVLC passes this as the user agent string when a protocol
     * requires it.
     *
     * @param p_instance LibVLC instance
     * @param name human-readable application name, e.g. "FooBar player 1.2.3"
     * @param http HTTP User Agent, e.g. "FooBar/1.2.3 Python/2.6.0"
     * @since LibVLC 1.1.1
     */
    public static native void libvlc_set_user_agent(libvlc_instance_t p_instance, String name, String http);

    /**
     * Sets some meta-informations about the application.
     * <p>
     * See also {@link #libvlc_set_user_agent(libvlc_instance_t, String, String)}.
     *
     * @param p_instance LibVLC instance
     * @param id Java-style application identifier, e.g. "com.acme.foobar"
     * @param version application version numbers, e.g. "1.2.3"
     * @param icon application icon name, e.g. "foobar"
     * @since LibVLC 2.1.0
     */
    public static native void libvlc_set_app_id(libvlc_instance_t p_instance, String id, String version, String icon);

    /**
     * Retrieve libvlc version. Example: "1.1.0-git The Luggage"
     *
     * @return a string containing the libvlc version
     */
    public static native String libvlc_get_version();

    /**
     * Retrieve libvlc compiler version. Example: "gcc version 4.2.3 (Ubuntu 4.2.3-2ubuntu6)"
     *
     * @return a string containing the libvlc compiler version
     */
    public static native String libvlc_get_compiler();

    /**
     * Retrieve libvlc changeset. Example: "aa9bce0bc4"
     *
     * @return a string containing the libvlc changeset
     */
    public static native String libvlc_get_changeset();

    /**
     * Frees an heap allocation returned by a LibVLC function. If you know you're using the same
     * underlying C run-time as the LibVLC implementation, then you can call ANSI C free() directly
     * instead.
     *
     * @param ptr the pointer
     */
    public static native void libvlc_free(Pointer ptr);

    /**
     * Register for an event notification.
     *
     * @param p_event_manager the event manager to which you want to attach to. Generally it is
     *            obtained by vlc_my_object_event_manager() where my_object is the object you want
     *            to listen to.
     * @param i_event_type the desired event to which we want to listen
     * @param f_callback the function to call when i_event_type occurs
     * @param user_data user provided data to carry with the event
     * @return 0 on success, ENOMEM on error
     */
    public static native int libvlc_event_attach(libvlc_event_manager_t p_event_manager, int i_event_type, libvlc_callback_t f_callback, Pointer user_data);

    /**
     * Unregister an event notification.
     *
     * @param p_event_manager the event manager
     * @param i_event_type the desired event to which we want to unregister
     * @param f_callback the function to call when i_event_type occurs
     * @param p_user_data user provided data to carry with the event
     */
    public static native void libvlc_event_detach(libvlc_event_manager_t p_event_manager, int i_event_type, libvlc_callback_t f_callback, Pointer p_user_data);

    /**
     * Gets debugging informations about a log message: the name of the VLC module
     * emitting the message and the message location within the source code.
     * <p>
     * The returned module name and file name will be NULL if unknown.
     * <p>
     * The returned line number will similarly be zero if unknown.
     * <p>
     * <strong>The returned module name and source code file name, if non-NULL,
     * are only valid until the logging callback returns.* </strong>
     *
     * @param ctx message context (as passed to the {@link libvlc_log_cb})
     * @param module module name storage (or NULL) [OUT]
     * @param file source code file name storage (or NULL) [OUT]
     * @param line source code file line number storage (or NULL) [OUT]
     *
     * @since LibVLC 2.1.0 or later
     */
    public static native void libvlc_log_get_context(libvlc_log_t ctx, PointerByReference module, PointerByReference file, IntByReference line);

    /**
     * Gets VLC object informations about a log message: the type name of the VLC
     * object emitting the message, the object header if any and a temporaly-unique
     * object identifier. These informations are mainly meant for <b>manual</b>
     * troubleshooting.
     * <p>
     * The returned type name may be "generic" if unknown, but it cannot be NULL.
     * <p>
     * The returned header will be NULL if unset; in current versions, the header
     * is used to distinguish for VLM inputs.
     * <p>
     * The returned object ID will be zero if the message is not associated with
     * any VLC object.
     * <p>
     * <strong>The returned module name and source code file name, if non-NULL,
     * are only valid until the logging callback returns.</strong>
     *
     * @param ctx message context (as passed to the {@link libvlc_log_cb})
     * @param name object name storage (or NULL) [OUT]
     * @param header object header (or NULL) [OUT]
     * @param id source code file line number storage (or NULL) [OUT]
     *
     * @since LibVLC 2.1.0 or later
     */
    public static native void libvlc_log_get_object(libvlc_log_t ctx, PointerByReference name, PointerByReference header, LongByReference id);

    /**
     * Unsets the logging callback for a LibVLC instance. This is rarely needed:
     * the callback is implicitly unset when the instance is destroyed.
     * <p>
     * This function will wait for any pending callbacks invocation to complete
     * (causing a deadlock if called from within the callback).
     *
     * @param p_instance the instance
     *
     * @since LibVLC 2.1.0 or later
     */
    public static native void libvlc_log_unset(libvlc_instance_t p_instance);

    /**
     * Sets the logging callback for a LibVLC instance.
     * <p>
     * This function is thread-safe: it will wait for any pending callbacks
     * invocation to complete.
     * <p>
     * <em>Some log messages (especially debug) are emitted by LibVLC while
     * is being initialized. These messages cannot be captured with this interface.</em>
     * <p>
     * <strong>A deadlock may occur if this function is called from the callback.</strong>
     *
     * @param p_instance the instance
     * @param cb callback function pointer
     * @param data opaque data pointer for the callback function
     *
     * @since LibVLC 2.1.0 or later
     */
    public static native void libvlc_log_set(libvlc_instance_t p_instance, libvlc_log_cb cb, Pointer data);

    /**
     * Release a list of module descriptions.
     *
     * @param p_list the list to be released
     */
    public static native void libvlc_module_description_list_release(Pointer p_list);

    /**
     * Returns a list of audio filters that are available.
     *
     * @param p_instance libvlc instance
     * @return a list of module descriptions. It should be freed with
     *         libvlc_module_description_list_release(). In case of an error, NULL is returned.
     * @see libvlc_module_description_t
     * @see #libvlc_module_description_list_release(Pointer)
     */
    public static native libvlc_module_description_t libvlc_audio_filter_list_get(libvlc_instance_t p_instance);

    /**
     * Returns a list of video filters that are available.
     *
     * @param p_instance libvlc instance
     * @return a list of module descriptions. It should be freed with
     *         libvlc_module_description_list_release(). In case of an error, NULL is returned.
     * @see libvlc_module_description_t
     * @see #libvlc_module_description_list_release(Pointer)
     */
    public static native libvlc_module_description_t libvlc_video_filter_list_get(libvlc_instance_t p_instance);

    /**
     * Return the current time as defined by LibVLC. The unit is the microsecond. Time increases
     * monotonically (regardless of time zone changes and RTC adjustments). The origin is arbitrary
     * but consistent across the whole system (e.g. the system uptime, the time since the system was
     * booted). Note: On systems that support it, the POSIX monotonic clock is used.
     *
     * @return clock value
     */
    public static native long libvlc_clock();

    // === libvlc.h =============================================================

    // === libvlc_media.h =======================================================

    /**
     * Create a media with a certain given media resource location.
     *
     * @see #libvlc_media_release(libvlc_media_t)
     * @param psz_mrl the MRL to read
     * @return the newly created media or NULL on error
     */
    public static native libvlc_media_t libvlc_media_new_location(String psz_mrl);

    /**
     * Create a media with a certain file path.
     *
     * @see #libvlc_media_release(libvlc_media_t)
     * @param path local filesystem path
     * @return the newly created media or NULL on error
     */
    public static native libvlc_media_t libvlc_media_new_path(String path);

    /**
     * Create a media with custom callbacks to read the data from.
     * <p>
     * If open_cb is NULL, the opaque pointer will be passed to read_cb,
     * seek_cb and close_cb, and the stream size will be treated as unknown.
     * <p>
     * The callbacks may be called asynchronously (from another thread).
     * A single stream instance need not be reentrant. However the open_cb needs to
     * be reentrant if the media is used by multiple player instances.
     * <p>
     * <strong>The callbacks may be used until all or any player instances
     * that were supplied the media item are stopped.</strong>
     * <p>
     * @see #libvlc_media_release(libvlc_media_t)
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param open_cb callback to open the custom bitstream input media
     * @param read_cb callback to read data (must not be NULL)
     * @param seek_cb callback to seek, or NULL if seeking is not supported
     * @param close_cb callback to close the media, or NULL if unnecessary
     * @param opaque data pointer for the open callback
     * @return the newly created media or NULL on error
     */
    public static native libvlc_media_t libvlc_media_new_callbacks(libvlc_media_open_cb open_cb, libvlc_media_read_cb read_cb, libvlc_media_seek_cb seek_cb, libvlc_media_close_cb close_cb, Pointer opaque);

    /**
     * Create a media as an empty node with a given name.
     *
     * @see #libvlc_media_release(libvlc_media_t)
     * @param psz_name the name of the node
     * @return the new empty media or NULL on error
     */
    public static native libvlc_media_t libvlc_media_new_as_node(String psz_name);

    /**
     * Add an option to the media. This option will be used to determine how the media_player will
     * read the media. This allows to use VLC's advanced reading/streaming options on a per-media
     * basis. The options are detailed in vlc --long-help, for instance "--sout-all"
     *
     * @param p_md the media descriptor
     * @param ppsz_options the options (as a string)
     */
    public static native void libvlc_media_add_option(libvlc_media_t p_md, String ppsz_options);

    /**
     * Add an option to the media with configurable flags. This option will be used to determine how
     * the media_player will read the media. This allows to use VLC's advanced reading/streaming
     * options on a per-media basis. The options are detailed in vlc --long-help, for instance
     * "--sout-all"
     *
     * @param p_md the media descriptor
     * @param ppsz_options the options (as a string)
     * @param i_flags the flags for this option
     */
    public static native void libvlc_media_add_option_flag(libvlc_media_t p_md, String ppsz_options, int i_flags);

    /**
     * Retain a reference to a media descriptor object (libvlc_media_t). Use libvlc_media_release()
     * to decrement the reference count of a media descriptor object.
     *
     * @param p_md the media descriptor
     */
    public static native void libvlc_media_retain(libvlc_media_t p_md);

    /**
     * Decrement the reference count of a media descriptor object. If the reference count is 0, then
     * libvlc_media_release() will release the media descriptor object. It will send out an
     * libvlc_MediaFreed event to all listeners. If the media descriptor object has been released it
     * should not be used again.
     *
     * @param p_md the media descriptor
     */
    public static native void libvlc_media_release(libvlc_media_t p_md);

    /**
     * Get the media resource locator (mrl) from a media descriptor object
     *
     * @param p_md a media descriptor object
     * @return string with mrl of media descriptor object
     */
    public static native Pointer libvlc_media_get_mrl(libvlc_media_t p_md);

    /**
     * Duplicate a media descriptor object.
     *
     * @param p_md a media descriptor object.
     * @return duplicated media descriptor
     */
    public static native libvlc_media_t libvlc_media_duplicate(libvlc_media_t p_md);

    /**
     * Read the meta of the media.
     *
     * Note, you need to call {@link #libvlc_media_parse_request(libvlc_instance_t, libvlc_media_t, int, int)}() or play
     * the media at least once before calling this function.
     *
     * If the media has not yet been parsed this will return NULL.
     *
     * @see #libvlc_media_parse_request(libvlc_instance_t, libvlc_media_t, int, int)
     * @see libvlc_event_e#libvlc_MediaMetaChanged
     * @param p_md the media descriptor
     * @param e_meta the meta to read
     * @return the media's meta
     */
    public static native Pointer libvlc_media_get_meta(libvlc_media_t p_md, int e_meta);

    /**
     * Set the meta of the media (this function will not save the meta, call libvlc_media_save_meta
     * in order to save the meta)
     *
     * @param p_md the media descriptor
     * @param e_meta the meta to write
     * @param psz_value the media's meta
     */
    public static native void libvlc_media_set_meta(libvlc_media_t p_md, int e_meta, String psz_value);

    /**
     * Save the meta previously set
     *
     * @param inst LibVLC instance
     * @param p_md the media desriptor
     * @return true if the write operation was successfull
     */
    public static native int libvlc_media_save_meta(libvlc_instance_t inst, libvlc_media_t p_md);

    /**
     * get the current statistics about the media
     *
     * @param p_md media descriptor object
     * @param p_stats structure that contain the statistics about the media (this structure must be
     *            allocated by the caller)
     * @return true if the statistics are available, false otherwise
     */
    public static native int libvlc_media_get_stats(libvlc_media_t p_md, libvlc_media_stats_t p_stats);

    /**
     * Get subitems of media descriptor object. This will increment the reference count of supplied
     * media descriptor object. Use libvlc_media_list_release() to decrement the reference counting.
     *
     * @param p_md media descriptor object
     * @return list of media descriptor subitems or NULL This method uses libvlc_media_list_t,
     *         however, media_list usage is optional and this is here for convenience
     */
    public static native libvlc_media_list_t libvlc_media_subitems(libvlc_media_t p_md);

    /**
     * Get event manager from media descriptor object. NOTE: this function doesn't increment
     * reference counting.
     *
     * @param p_md a media descriptor object
     * @return event manager object
     */
    public static native libvlc_event_manager_t libvlc_media_event_manager(libvlc_media_t p_md);

    /**
     * Get duration (in ms) of media descriptor object item.
     *
     * @param p_md media descriptor object
     * @return duration of media item or -1 on error
     */
    public static native long libvlc_media_get_duration(libvlc_media_t p_md);

    /**
     * Get a 'stat' value of media descriptor object item.
     *
     * Note 'stat' values are currently only parsed by directory accesses. This means that only sub medias of a
     * directory media, parsed with {@link #libvlc_media_parse_request(libvlc_instance_t, libvlc_media_t, int, int)}()
     * can have valid 'stat' properties.
     *
     * @param p_md media descriptor object
     * @param type a valid libvlc_media_stat_ define
     * @param out field in which the value will be stored
     * @return 1 on success, 0 if not found, -1 on error
     * @since LibVLC 4.0.0 and later.
     */
    public static native int libvlc_media_get_filestat(libvlc_media_t p_md, int type, LongByReference out);

    /**
     * Parse the media asynchronously with options.
     *
     * This fetches (local or network) art, meta data and/or tracks information.
     *
     * To track when this is over you can listen to libvlc_MediaParsedChanged
     * event. However if this functions returns an error, you will not receive any
     * events.
     *
     * It uses a flag to specify parse options. All
     * these flags can be combined. By default, media is parsed if it's a local
     * file.
     *
     * Parsing can be aborted with libvlc_media_parse_stop().
     *
     * @see libvlc_event_e#libvlc_MediaParsedChanged
     * @see #libvlc_media_get_meta(libvlc_media_t, int)
     *
     * @param inst the instance to use to parse the media
     * @param p_md media descriptor object
     * @param parse_flag parse options
     * @param timeout maximum time allowed to preparse the media. If -1, the default
     *                "preparse-timeout" option will be used as a timeout. If 0, it
     *                will wait indefinitely. If &gt; 0, the timeout will be used (in
     *                milliseconds).
     * @return -1 in case of error, 0 otherwise
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_parse_request(libvlc_instance_t inst, libvlc_media_t p_md, int parse_flag, int timeout);

    /**
     * Stop the parsing of the media
     *
     * When the media parsing is stopped, the libvlc_MediaParsedChanged event will
     * be sent with the libvlc_media_parsed_status_timeout status.
     *
     * @see #libvlc_media_parse_request(libvlc_instance_t, libvlc_media_t, int, int)
     *
     * @param inst the instance used to parse the media
     * @param p_md media descriptor object
     *
     * @since version LibVLC 3.0.0 or later
     */
    public static native void libvlc_media_parse_stop(libvlc_instance_t inst, libvlc_media_t p_md);

    /**
     * Get Parsed status for media descriptor object.
     *
     * @param p_md media descriptor object
     * @return a value of the libvlc_media_parsed_status_t enum
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_get_parsed_status(libvlc_media_t p_md);

    /**
     * Sets media descriptor's user_data. user_data is specialized data accessed by the host
     * application, VLC.framework uses it as a pointer to an native object that references a
     * libvlc_media_t pointer
     *
     * @param p_md media descriptor object
     * @param p_new_user_data pointer to user data
     */
    public static native void libvlc_media_set_user_data(libvlc_media_t p_md, Pointer p_new_user_data);

    /**
     * Get media descriptor's user_data. user_data is specialized data accessed by the host
     * application, VLC.framework uses it as a pointer to an native object that references a
     * libvlc_media_t pointer
     *
     * @param p_md media descriptor object
     * @return user-data pointer
     */
    public static native Pointer libvlc_media_get_user_data(libvlc_media_t p_md);

    /**
     * Get the track list for one type
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Note you need to call libvlc_media_parse_request or play the media
     * at least once before calling this function.  Not doing this will result in
     * an empty list.
     *
     * @see #libvlc_media_parse_request
     * @see #libvlc_media_tracklist_count
     * @see #libvlc_media_tracklist_at
     *
     * @param p_md media descriptor object
     * @param type type of the track list to request
     *
     * @return a valid libvlc_media_tracklist_t or NULL in case of error, if there
     * is no track for a category, the returned list will have a size of 0, delete
     * with libvlc_media_tracklist_delete()
     */
    public static native libvlc_media_tracklist_t libvlc_media_get_tracklist(libvlc_media_t p_md, int type);

    /**
     * Get the media type of the media descriptor object.
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_md media descriptor object
     * @return media type
     */
    public static native int libvlc_media_get_type(libvlc_media_t p_md);

    /**
     * Start an asynchronous thumbnail generation
     *
     * If the request is successfuly queued, the libvlc_MediaThumbnailGenerated
     * is guaranteed to be emited.
     *
     * The returned request object must be released via {@link #libvlc_media_thumbnail_request_destroy(libvlc_media_thumbnail_request_t)}.
     *
     * @param inst the instance to use to generate the thumbnail
     * @param md media descriptor object
     * @param time The time at which the thumbnail should be generated
     * @param speed The seeking speed \sa{libvlc_thumbnailer_seek_speed_t}
     * @param width The thumbnail width
     * @param height the thumbnail height
     * @param crop non-zero if the thumbnail should be cropped
     * @param picture_type The thumbnail picture type \sa{libvlc_picture_type_t}
     * @param timeout A timeout value in ms, or 0 to disable timeout
     *
     * @return A valid opaque request object, or NULL in case of failure.
     *
     * @since libvlc 4.0 or later
     *
     * @see libvlc_picture_t
     */
    public static native libvlc_media_thumbnail_request_t libvlc_media_thumbnail_request_by_time(libvlc_instance_t inst, libvlc_media_t md, long time, int speed, int width, int height, int crop, int picture_type, long timeout);

    /**
     * Start an asynchronous thumbnail generation
     *
     * If the request is successfuly queued, the libvlc_MediaThumbnailGenerated
     * is guaranteed to be emited.
     *
     * The returned request object must be released via {@link #libvlc_media_thumbnail_request_destroy(libvlc_media_thumbnail_request_t)}.
     *
     * @param inst the instance to use to generate the thumbnail
     * @param md media descriptor object
     * @param pos The position at which the thumbnail should be generated
     * @param speed The seeking speed \sa{libvlc_thumbnailer_seek_speed_t}
     * @param width The thumbnail width
     * @param height the thumbnail height
     * @param picture_type The thumbnail picture type \sa{libvlc_picture_type_t}
     * @param crop non-zero if the thumbnail should be cropped
     * @param timeout A timeout value in ms, or 0 to disable timeout
     *
     * @return A valid opaque request object, or NULL in case of failure.
     *
     * @since libvlc 4.0 or later
     *
     * @see libvlc_picture_t
     */
    public static native libvlc_media_thumbnail_request_t libvlc_media_thumbnail_request_by_pos(libvlc_instance_t inst, libvlc_media_t md, double pos, int speed, int width, int height, int crop, int picture_type, long timeout);

    /**
     * Destroy a thumbnail request.
     *
     * If the request has not completed or hasn't been cancelled yet, the behavior
     * is undefined.
     *
     * This will also cancel the thumbnail request, no events will be emitted after
     * this call.
     *
     * @param p_req An opaque thumbnail request object.
     *
     * @since libvlc 4.0 or later
     */
    public static native void libvlc_media_thumbnail_request_destroy( libvlc_media_thumbnail_request_t p_req);

    /**
     * Get codec description from media elementary stream.
     *
     * @param i_type i_type from libvlc_media_track_t
     * @param i_codec i_codec or i_original_fourcc from libvlc_media_track_t
     *
     * @return codec description
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native String libvlc_media_get_codec_description(int i_type, int i_codec);

    /**
     * Add a slave to the current media.
     * <p>
     * A slave is an external input source that may contains an additional subtitle
     * track (like a .srt) or an additional audio track (like a .ac3).
     * <p>
     * This function must be called before the media is parsed (via
     * libvlc_media_parse_request()) or before the media is played (via
     * libvlc_media_player_play())
     *
     * @param p_md media descriptor object
     * @param i_type subtitle or audio
     * @param i_priority from 0 (low priority) to 4 (high priority)
     * @param psz_uri Uri of the slave (should contain a valid scheme).
     * @return 0 on success, -1 on error.
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native int libvlc_media_slaves_add(libvlc_media_t p_md, int i_type, int i_priority, String psz_uri);

    /**
     * Clear all slaves previously added by libvlc_media_slaves_add() or
     * internally.
     *
     * @param p_md media descriptor object
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native void libvlc_media_slaves_clear(libvlc_media_t p_md);

    /**
     * Get a media descriptor's slave list
     *
     * The list will contain slaves parsed by VLC or previously added by
     * libvlc_media_slaves_add(). The typical use case of this function is to save
     * a list of slave in a database for a later use.
     *
     * @param p_md media descriptor object
     * @param ppp_slaves address to store an allocated array of slaves (must be freed with libvlc_media_slaves_release()) [OUT]
     *
     * @return the number of slaves (zero on error)
     * @since LibVLC 3.0.0 and later.
     */
    public static native int libvlc_media_slaves_get(libvlc_media_t p_md, PointerByReference ppp_slaves);

    /**
     * Release a media descriptor's slave list
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param pp_slaves slave array to release
     * @param i_count number of elements in the array
     */
    public static native void libvlc_media_slaves_release(Pointer pp_slaves, int i_count);

    // === libvlc_media.h =======================================================

    // === libvlc_media_player.h ================================================

    /**
     * Create an empty Media Player object
     *
     * @param p_libvlc_instance the libvlc instance in which the Media Player should be created.
     * @return a new media player object, or NULL on error.
     */
    public static native libvlc_media_player_t libvlc_media_player_new(libvlc_instance_t p_libvlc_instance);

    /**
     * Create a Media Player object from a Media
     *
     * @param p_libvlc_instance libvlc instance
     * @param p_md the media. Afterwards the p_md can be safely destroyed.
     * @return a new media player object, or NULL on error.
     */
    public static native libvlc_media_player_t libvlc_media_player_new_from_media(libvlc_instance_t p_libvlc_instance, libvlc_media_t p_md);

    /**
     * Release a media_player after use Decrement the reference count of a media player object. If
     * the reference count is 0, then libvlc_media_player_release() will release the media player
     * object. If the media player object has been released, then it should not be used again.
     *
     * @param p_mi the Media Player to free
     */
    public static native void libvlc_media_player_release(libvlc_media_player_t p_mi);

    /**
     * Retain a reference to a media player object. Use libvlc_media_player_release() to decrement
     * reference count.
     *
     * @param p_mi media player object
     */
    public static native void libvlc_media_player_retain(libvlc_media_player_t p_mi);

    /**
     * Set the media that will be used by the media_player. If any, previous md will be released.
     *
     * @param p_mi the Media Player
     * @param p_md the Media. Afterwards the p_md can be safely destroyed.
     */
    public static native void libvlc_media_player_set_media(libvlc_media_player_t p_mi, libvlc_media_t p_md);

    /**
     * Get the media used by the media_player.
     * <p>
     * You do <strong>not</strong> need to invoke libvlc_media_player_release().
     *
     * @param p_mi the Media Player
     * @return the media associated with p_mi, or NULL if no media is associated
     */
    public static native libvlc_media_t libvlc_media_player_get_media(libvlc_media_player_t p_mi);

    /**
     * Get the Event Manager from which the media player send event.
     *
     * @param p_mi the Media Player
     * @return the event manager associated with p_mi
     */
    public static native libvlc_event_manager_t libvlc_media_player_event_manager(libvlc_media_player_t p_mi);

    /**
     * is_playing
     *
     * @param p_mi the Media Player
     * @return 1 if the media player is playing, 0 otherwise
     */
    public static native int libvlc_media_player_is_playing(libvlc_media_player_t p_mi);

    /**
     * Play
     *
     * @param p_mi the Media Player
     * @return 0 if playback started (and was already started), or -1 on error.
     */
    public static native int libvlc_media_player_play(libvlc_media_player_t p_mi);

    /**
     * Pause or resume (no effect if there is no media)
     *
     * @param mp the Media Player
     * @param do_pause play/resume if zero, pause if non-zero
     * @since LibVLC 1.1.1
     */
    public static native void libvlc_media_player_set_pause(libvlc_media_player_t mp, int do_pause);

    /**
     * Toggle pause (no effect if there is no media)
     *
     * @param p_mi the Media Player
     */
    public static native void libvlc_media_player_pause(libvlc_media_player_t p_mi);

    /**
     * Stop (no effect if there is no media)
     *
     * @param p_mi the Media Player
     * @return 0 if the player is being stopped; -1 otherwise
     *
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_media_player_stop_async(libvlc_media_player_t p_mi);

    /**
     * Set a renderer to the media player
     *
     * Must be called before the first call of libvlc_media_player_play() to
     * take effect.
     *
     * @see #libvlc_renderer_discoverer_new(libvlc_instance_t, String)
     *
     * @param p_mi the Media Player
     * @param p_item an item discovered by libvlc_renderer_discoverer_start()
     * @return 0 on success, -1 on error.
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_player_set_renderer(libvlc_media_player_t p_mi, libvlc_renderer_item_t p_item);

    /**
     * Set callbacks and private data to render decoded video to a custom area in memory.
     * <p>
     * Use libvlc_video_set_format() or libvlc_video_set_format_callbacks() to configure the decoded
     * format.
     *
     * @param mp the media player
     * @param lock callback to allocate video memory
     * @param unlock callback to release video memory
     * @param display callback when ready to display a video frame
     * @param opaque private pointer for the three callbacks (as first parameter)
     * @since LibVLC 1.1.1
     */
    public static native void libvlc_video_set_callbacks(libvlc_media_player_t mp, libvlc_lock_callback_t lock, libvlc_unlock_callback_t unlock, libvlc_display_callback_t display, Pointer opaque);

    /**
     * Set decoded video chroma and dimensions.
     * <p>
     * This only works in combination with libvlc_video_set_callbacks(), and is mutually exclusive
     * with libvlc_video_set_format_callbacks().
     *
     * @param mp the media player
     * @param chroma a four-characters string identifying the chroma (e.g. "RV32" or "YUYV")
     * @param width pixel width
     * @param height pixel height
     * @param pitch line pitch (in bytes)
     * @since LibVLC 1.1.1
     *
     * bug: All pixel planes are expected to have the same pitch. To use the YCbCr color space with
     *      chrominance subsampling, consider using libvlc_video_set_format_callback() instead.
     */
    public static native void libvlc_video_set_format(libvlc_media_player_t mp, String chroma, int width, int height, int pitch);

    /**
     * Set decoded video chroma and dimensions. This only works in combination with
     * libvlc_video_set_callbacks().
     *
     * @param mp the media player
     * @param setup callback to select the video format (cannot be NULL)
     * @param cleanup callback to release any allocated resources (or NULL)
     * @since LibVLC 2.0.0 or later
     */
    public static native void libvlc_video_set_format_callbacks(libvlc_media_player_t mp, libvlc_video_format_cb setup, libvlc_video_output_cleanup_cb cleanup);

    /**
     * Set callbacks and data to render decoded video to a custom texture
     *
     * Warning: VLC will perform video rendering in its own thread and at its own rate,
     * You need to provide your own synchronisation mechanism.
     *
     * OpenGL context need to be created before playing a media.
     *
     * @param mp the media player
     * @param engine the GPU engine to use
     * @param setup_cb callback called to initialize user data
     * @param cleanup_cb callback called to clean up user data
     * @param resize_cb callback to set the resize callback
     * @param update_output_cb callback called to get the size of the video
     * @param swap_cb callback called after rendering a video frame (cannot be NULL)
     * @param makeCurrent_cb callback called to enter/leave the opengl context (cannot be NULL for \ref libvlc_video_engine_opengl and for \ref libvlc_video_engine_gles2)
     * @param getProcAddress_cb opengl function loading callback (cannot be NULL for \ref libvlc_video_engine_opengl and for \ref libvlc_video_engine_gles2)
     * @param metadata_cb callback to provide frame metadata (D3D11 only)
     * @param select_plane_cb callback to select different D3D11 rendering targets
     * @param opaque private pointer passed to callbacks
     * @return 0 on success; -1 on error
     *
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_video_set_output_callbacks(
        libvlc_media_player_t mp,
        int engine,
        libvlc_video_output_setup_cb setup_cb,
        libvlc_video_output_cleanup_cb cleanup_cb,
        libvlc_video_output_set_resize_cb resize_cb,
        libvlc_video_update_output_cb update_output_cb,
        libvlc_video_swap_cb swap_cb,
        libvlc_video_makeCurrent_cb makeCurrent_cb,
        libvlc_video_getProcAddress_cb getProcAddress_cb,
        libvlc_video_frameMetadata_cb metadata_cb,
        libvlc_video_output_select_plane_cb select_plane_cb,
        Pointer opaque);

    /**
     * Set the NSView handler where the media player should render its video output. Use the vout
     * called "macosx". The drawable is an NSObject that follow the VLCOpenGLVideoViewEmbedding
     * protocol:
     * <pre>
     *     \@protocol VLCOpenGLVideoViewEmbedding &lt;NSObject&gt; - (void)addVoutSubview:(NSView*)view; - (void)removeVoutSubview:(NSView *)view; \@end
     * </pre>
     * Or it can be an NSView object. If you want to use it along with Qt4 see the
     * QMacCocoaViewContainer. Then the following code should work:
     * <pre>
     * {
     *     NSView *video = [[NSView alloc] init];
     *     QMacCocoaViewContainer *container = new QMacCocoaViewContainer(video, parent);
     *     libvlc_media_player_set_nsobject(mp, video);
     *     [video release];
     * }
     * </pre>
     * You can find a live example in VLCVideoView in VLCKit.framework.
     *
     * @param p_mi the Media Player
     * @param drawable the drawable that is either an NSView or an object following the
     *            VLCOpenGLVideoViewEmbedding protocol.
     */
    // FIXME should be a pointer not a long (makes no difference really)
    public static native void libvlc_media_player_set_nsobject(libvlc_media_player_t p_mi, long drawable);

    /**
     * Get the NSView handler previously set with libvlc_media_player_set_nsobject().
     *
     * @param p_mi the Media Player
     * @return the NSView handler or 0 if none where set
     */
    public static native Pointer libvlc_media_player_get_nsobject(libvlc_media_player_t p_mi);

    /**
     * Set an X Window System drawable where the media player should render its video output. If
     * LibVLC was built without X11 output support, then this has no effects. The specified
     * identifier must correspond to an existing Input/Output class X11 window. Pixmaps are
     * <b>not</b> supported. The caller shall ensure that the X11 server is the same as the one the
     * VLC instance has been configured with.
     *
     * @param p_mi the Media Player
     * @param drawable the ID of the X window
     */
    public static native void libvlc_media_player_set_xwindow(libvlc_media_player_t p_mi, int drawable);

    /**
     * Get the X Window System window identifier previously set with
     * libvlc_media_player_set_xwindow(). Note that this will return the identifier even if VLC is
     * not currently using it (for instance if it is playing an audio-only input).
     *
     * @param p_mi the Media Player
     * @return an X window ID, or 0 if none where set.
     */
    public static native int libvlc_media_player_get_xwindow(libvlc_media_player_t p_mi);

    /**
     * Set a Win32/Win64 API window handle (HWND) where the media player should render its video
     * output. If LibVLC was built without Win32/Win64 API output support, then this has no effects.
     *
     * @param p_mi the Media Player
     * @param drawable windows handle of the drawable
     */
    public static native void libvlc_media_player_set_hwnd(libvlc_media_player_t p_mi, Pointer drawable);

    /**
     * Get the Windows API window handle (HWND) previously set with libvlc_media_player_set_hwnd().
     * The handle will be returned even if LibVLC is not currently outputting any video to it.
     *
     * @param p_mi the Media Player
     * @return a window handle or NULL if there are none.
     */
    public static native Pointer libvlc_media_player_get_hwnd(libvlc_media_player_t p_mi);

    /**
     * Set callbacks and private data for decoded audio.
     * <p>
     * Use libvlc_audio_set_format() or libvlc_audio_set_format_callbacks() to configure the decoded
     * audio format.
     *
     * @param mp the media player
     * @param play callback to play audio samples (must not be NULL)
     * @param pause callback to pause playback (or NULL to ignore)
     * @param resume callback to resume playback (or NULL to ignore)
     * @param flush callback to flush audio buffers (or NULL to ignore)
     * @param drain callback to drain audio buffers (or NULL to ignore)
     * @param opaque private pointer for the audio callbacks (as first parameter)
     * @since LibVLC 2.0.0 or later
     */
    public static native void libvlc_audio_set_callbacks(libvlc_media_player_t mp, libvlc_audio_play_cb play, libvlc_audio_pause_cb pause, libvlc_audio_resume_cb resume, libvlc_audio_flush_cb flush, libvlc_audio_drain_cb drain, Pointer opaque);

    /**
     * Set callbacks and private data for decoded audio. Use libvlc_audio_set_format() or
     * libvlc_audio_set_format_callbacks() to configure the decoded audio format.
     *
     * @param mp the media player
     * @param set_volume callback to apply audio volume, or NULL to apply volume in software
     * @since LibVLC 2.0.0 or later
     */
    public static native void libvlc_audio_set_volume_callback(libvlc_media_player_t mp, libvlc_audio_set_volume_cb set_volume);

    /**
     * Set decoded audio format. This only works in combination with libvlc_audio_set_callbacks().
     *
     * @param mp the media player
     * @param setup callback to select the audio format (cannot be NULL)
     * @param cleanup callback to release any allocated resources (or NULL)
     * @since LibVLC 2.0.0 or later
     */
    public static native void libvlc_audio_set_format_callbacks(libvlc_media_player_t mp, libvlc_audio_setup_cb setup, libvlc_audio_cleanup_cb cleanup);

    /**
     * Set decoded audio format. This only works in combination with libvlc_audio_set_callbacks(),
     * and is mutually exclusive with libvlc_audio_set_format_callbacks().
     *
     * @param mp the media player
     * @param format a four-characters string identifying the sample format (e.g. "S16N" or "f32l")
     * @param rate sample rate (expressed in Hz)
     * @param channels channels count
     * @since LibVLC 2.0.0 or later
     */
    public static native void libvlc_audio_set_format(libvlc_media_player_t mp, String format, int rate, int channels);

    /** bug This might go away ... to be replaced by a broader system */

    /**
     * Get the current movie length (in ms).
     *
     * @param p_mi the Media Player
     * @return the movie length (in ms), or -1 if there is no media.
     */
    public static native long libvlc_media_player_get_length(libvlc_media_player_t p_mi);

    /**
     * Get the current movie time (in ms).
     *
     * @param p_mi the Media Player
     * @return the movie time (in ms), or -1 if there is no media.
     */
    public static native long libvlc_media_player_get_time(libvlc_media_player_t p_mi);

    /**
     * Set the movie time (in ms).
     * <p>
     * This has no effect if no media is being played.
     * <p>
     * Not all formats and protocols support this.
     *
     * @param p_mi the Media Player
     * @param i_time the movie time (in ms).
     * @param b_fast prefer fast seeking or precise seeking
     * @return 0 on success, -1 on error
     */
    public static native int libvlc_media_player_set_time(libvlc_media_player_t p_mi, long i_time, int b_fast);

    /**
     * Get movie position.
     *
     * @param p_mi the Media Player
     * @return movie position, or -1. in case of error
     */
    public static native double libvlc_media_player_get_position(libvlc_media_player_t p_mi);

    /**
     * Set movie position as percentage between 0.0 and 1.0.
     * <p>
     * This has no effect if playback is not enabled.
     * <p>
     * This might not work depending on the underlying input format and protocol.
     *
     * @param p_mi the Media Player
     * @param f_pos the position
     * @param b_fast prefer fast seeking or precise seeking
     * @return 0 on success, -1 on error
     */
    public static native int libvlc_media_player_set_position(libvlc_media_player_t p_mi, double f_pos, int b_fast);

    /**
     * Set movie chapter (if applicable).
     *
     * @param p_mi the Media Player
     * @param i_chapter chapter number to play
     */
    public static native void libvlc_media_player_set_chapter(libvlc_media_player_t p_mi, int i_chapter);

    /**
     * Get movie chapter.
     *
     * @param p_mi the Media Player
     * @return chapter number currently playing, or -1 if there is no media.
     */
    public static native int libvlc_media_player_get_chapter(libvlc_media_player_t p_mi);

    /**
     * Get movie chapter count
     *
     * @param p_mi the Media Player
     * @return number of chapters in movie, or -1.
     */
    public static native int libvlc_media_player_get_chapter_count(libvlc_media_player_t p_mi);

    /**
     * Get title chapter count
     *
     * @param p_mi the Media Player
     * @param i_title title
     * @return number of chapters in title, or -1
     */
    public static native int libvlc_media_player_get_chapter_count_for_title(libvlc_media_player_t p_mi, int i_title);

    /**
     * Set movie title
     *
     * @param p_mi the Media Player
     * @param i_title title number to play
     */
    public static native void libvlc_media_player_set_title(libvlc_media_player_t p_mi, int i_title);

    /**
     * Get movie title
     *
     * @param p_mi the Media Player
     * @return title number currently playing, or -1
     */
    public static native int libvlc_media_player_get_title(libvlc_media_player_t p_mi);

    /**
     * Get movie title count
     *
     * @param p_mi the Media Player
     * @return title number count, or -1
     */
    public static native int libvlc_media_player_get_title_count(libvlc_media_player_t p_mi);

    /**
     * Set previous chapter (if applicable)
     *
     * @param p_mi the Media Player
     */
    public static native void libvlc_media_player_previous_chapter(libvlc_media_player_t p_mi);

    /**
     * Set next chapter (if applicable)
     *
     * @param p_mi the Media Player
     */
    public static native void libvlc_media_player_next_chapter(libvlc_media_player_t p_mi);

    /**
     * Get the requested movie play rate.
     * <p>
     * Depending on the underlying media, the requested rate may be different from the real
     *          playback rate.
     * @param p_mi the Media Player
     * @return movie play rate
     */
    public static native float libvlc_media_player_get_rate(libvlc_media_player_t p_mi);

    /**
     * Set movie play rate
     *
     * @param p_mi the Media Player
     * @param rate movie play rate to set
     * @return -1 if an error was detected, 0 otherwise (but even then, it might not actually work
     *         depending on the underlying media protocol)
     */
    public static native int libvlc_media_player_set_rate(libvlc_media_player_t p_mi, float rate);

    /**
     * Get current movie state
     *
     * @param p_mi the Media Player
     * @return the current state of the media player (playing, paused, ...) @see State
     */
    public static native int libvlc_media_player_get_state(libvlc_media_player_t p_mi);

    /**
     * How many video outputs does this media player have?
     *
     * @param p_mi the media player
     * @return the number of video outputs
     */
    public static native int libvlc_media_player_has_vout(libvlc_media_player_t p_mi);

    /**
     * Is this media player seekable?
     *
     * @param p_mi the media player
     * @return true if the media player can seek
     */
    public static native int libvlc_media_player_is_seekable(libvlc_media_player_t p_mi);

    /**
     * Can this media player be paused?
     *
     * @param p_mi the media player
     * @return true if the media player can pause
     */
    public static native int libvlc_media_player_can_pause(libvlc_media_player_t p_mi);

    /**
     * Is the current program scrambled?
     *
     * @param p_mi the media player
     * @return true if the current program is scrambled
     * @since libVLC 2.2.0
     */
    public static native int libvlc_media_player_program_scrambled(libvlc_media_player_t p_mi);

    /**
     * Display the next frame (if supported)
     *
     * @param p_mi the media player
     */
    public static native void libvlc_media_player_next_frame(libvlc_media_player_t p_mi);

    /**
     * Navigate through DVD Menu
     *
     * @param p_mi the Media Player
     * @param navigate the Navigation mode
     * @since libVLC 2.0.0
     */
    public static native void libvlc_media_player_navigate(libvlc_media_player_t p_mi, int navigate);

    /**
     * Set if, and how, the video title will be shown when media is played.
     *
     * @param p_mi the media player
     * @param position position at which to display the title, or libvlc_position_disable to prevent the title from being displayed
     * @param timeout title display timeout in milliseconds (ignored if libvlc_position_disable)
     * @since libVLC 2.1.0 or later
     */
    public static native void libvlc_media_player_set_video_title_display(libvlc_media_player_t p_mi, int position, int timeout);

    /**
     * Get the track list for one type
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Note: You need to call libvlc_media_parse_request() or play the media
     * at least once before calling this function.  Not doing this will result in
     * an empty list.
     *
     * Note: This track list is a snapshot of the current tracks when this function
     * is called. If a track is updated after this call, the user will need to call
     * this function again to get the updated track.
     *
     * The track list can be used to get track informations and to select specific
     * tracks.
     *
     * @param p_mi the media player
     * @param type type of the track list to request
     * @param selected filter only selected tracks if true (return all tracks, even selected ones if false)
     *
     * @return a valid libvlc_media_tracklist_t or NULL in case of error, if there
     * is no track for a category, the returned list will have a size of 0, delete
     * with libvlc_media_tracklist_delete()
     */
    public static native libvlc_media_tracklist_t libvlc_media_player_get_tracklist(libvlc_media_player_t p_mi, int type, int selected);

    /**
     * Get the selected track for one type
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Warning: More than one tracks can be selected for one type. In that case,
     * libvlc_media_player_get_tracklist() should be used.
     *
     * @param p_mi the media player
     * @param type type of the selected track
     *
     * @return a valid track or NULL if there is no selected tracks for this type,
     * release it with libvlc_media_track_release().
     */
    public static native libvlc_media_track_t libvlc_media_player_get_selected_track(libvlc_media_player_t p_mi, int type);

    /**
     * Get a track from a track id
     *
     * @since LibVLC 4.0.0 and later.
     *
     * This function can be used to get the last updated informations of a track.
     *
     * @param p_mi the media player
     * @param psz_id valid string representing a track id (cf. psz_id from \ref
     * libvlc_media_track_t)
     *
     * @return a valid track or NULL if there is currently no tracks identified by
     * the string id, release it with libvlc_media_track_release().
     */
    public static native libvlc_media_track_t libvlc_media_player_get_track_from_id(libvlc_media_player_t p_mi, String psz_id);

    /**
     * Select a track.
     *
     * This will unselect the current track.
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Note: Use libvlc_media_player_select_tracks() for multiple selection
     *
     * @param p_mi the media player
     * @param track track to select or NULL to unselect all tracks of for this type
     */
    public static native void libvlc_media_player_select_track(libvlc_media_player_t p_mi, libvlc_media_track_t track);

    /**
     * Unselect all tracks for a given type.
     *
     * @since LibVLC 4.0.0 and later.
     *
     * @param p_mi the media player
     * @param type type to unselect
     */
    public static native void libvlc_media_player_unselect_track_type(libvlc_media_player_t p_mi, int type);

    /**
     * Select multiple tracks for one type
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Note: The internal track list can change between the calls of
     * libvlc_media_player_get_tracklist() and
     * libvlc_media_player_set_tracks(). If a track selection change but the
     * track is not present anymore, the player will just ignore it.
     *
     * Note: selecting multiple audio tracks is currently not supported.
     *
     * @param p_mi the media player
     * @param type type of the selected track
     * @param tracks pointer to the track array
     * @param track_count number of tracks in the track array
     */
    public static native void libvlc_media_player_select_tracks(libvlc_media_player_t p_mi, int type, Pointer tracks, size_t track_count);

    /**
     * Select tracks by their string identifier
     *
     * @since LibVLC 4.0.0 and later.
     *
     * This function can be used pre-select a list of tracks before starting the
     * player. It has only effect for the current media. It can also be used when
     * the player is already started.
     *
     * 'str_ids' can contain more than one track id, delimited with ','. "" or any
     * invalid track id will cause the player to unselect all tracks of that
     * category. NULL will disable the preference for newer tracks without
     * unselecting any current tracks.
     *
     * Example:
     * - (libvlc_track_video, "video/1,video/2") will select these 2 video tracks.
     * If there is only one video track with the id "video/0", no tracks will be
     * selected.
     * - (libvlc_track_type_t, "${slave_url_md5sum}/spu/0) will select one spu
     * added by an input slave with the corresponding url.
     *
     * Note: The string identifier of a track can be found via psz_id from \ref
     * libvlc_media_track_t
     *
     * Note: selecting multiple audio tracks is currently not supported.
     *
     * @param p_mi the media player
     * @param type type to select
     * @param psz_ids list of string identifier or NULL
     */
    public static native void libvlc_media_player_select_tracks_by_ids(libvlc_media_player_t p_mi, int type, String psz_ids);

    /**
     * Add a slave to the current media player.
     *
     * If the player is playing, the slave will be added directly. This call
     * will also update the slave list of the attached libvlc_media_t.
     *
     * @see #libvlc_media_slaves_add(libvlc_media_t, int, int, String)
     *
     * @param p_mi the media player
     * @param i_type subtitle or audio
     * @param psz_uri Uri of the slave (should contain a valid scheme).
     * @param b_select True if this slave should be selected when it's loaded
     *
     * @return 0 on success, -1 on error.
     * @since LibVLC 3.0.0 and later.
     */
    public static native int libvlc_media_player_add_slave(libvlc_media_player_t p_mi, int i_type, String psz_uri, int b_select);

    /**
     * Delete a program struct.
     *
     * @param program returned by libvlc_media_player_get_selected_program() or libvlc_media_player_get_program_from_id()
     * @since LibVLC 4.0.0 and later.
     */
    public static native void libvlc_player_program_delete(libvlc_player_program_t program);

    /**
     * Get the number of programs in a programlist.
     *
     * @param list valid programlist
     * @return number of programs, or 0 if the list is empty
     * @since LibVLC 4.0.0 and later.
     */
    public static native size_t libvlc_player_programlist_count(libvlc_player_programlist_t list);

    /**
     * Get a program at a specific index
     *
     * Warning: the behaviour is undefined if the index is not valid.
     *
     * @param list valid programlist
     * @param index valid index in the range [0; count[
     * @return a valid program (can't be NULL if libvlc_player_programlist_count() returned a valid count)
     * @since LibVLC 4.0.0 and later.
     */
    public static native libvlc_player_program_t libvlc_player_programlist_at(libvlc_player_programlist_t list, size_t index);

    /**
     * Release a programlist
     *
     * @param list valid programlist
     * @since LibVLC 4.0.0 and later.
     *
     * @see #libvlc_media_player_get_programlist(libvlc_media_player_t)
     */
    public static native void libvlc_player_programlist_delete(libvlc_player_programlist_t list);

     /**
      * Select program with a given program id.
      *
      * Note program ids are sent via the libvlc_MediaPlayerProgramAdded event or
      * can be fetched via libvlc_media_player_get_programlist().
      *
      * @param p_mi opaque media player handle
      * @param program_id
      * @since LibVLC 4.0.0 or later
      */
     public static native void libvlc_media_player_select_program_id(libvlc_media_player_t p_mi, int program_id);

     /**
      * Get the selected program.
      *
      * @param p_mi opaque media player handle
      * @return a valid program struct or NULL if no programs are selected. The program need to be freed with libvlc_player_program_delete().
      * @since LibVLC 4.0.0 or later
      */
     public static native libvlc_player_program_t libvlc_media_player_get_selected_program(libvlc_media_player_t p_mi);

     /**
      * Get a program struct from a program id
      *
      * @param p_mi opaque media player handle
      * @param i_group_id program id
      * @return a valid program struct or NULL if the group_id is not found. The program need to be freed with libvlc_player_program_delete().
      * @since LibVLC 4.0.0 or later
      */
     public static native libvlc_player_program_t libvlc_media_player_get_program_from_id(libvlc_media_player_t p_mi, int i_group_id);

     /**
      * Get the program list
      *
      * Note this program list is a snapshot of the current programs when this
      * function is called. If a program is updated after this call, the user will
      * need to call this function again to get the updated program.
      *
      * The program list can be used to get program informations and to select
      * specific programs.
      *
      * @param p_mi the media player
      * @return a valid libvlc_media_programlist_t or NULL in case of error or empty list, delete with libvlc_media_programlist_delete()
      * @since LibVLC 4.0.0 and later.
      */
    public static native libvlc_player_programlist_t libvlc_media_player_get_programlist(libvlc_media_player_t p_mi);

    /**
     * Toggle fullscreen status on non-embedded video outputs.
     * <p>
     * The same limitations applies to this function as to libvlc_set_fullscreen().
     *
     * @param p_mi the media player
     */
    public static native void libvlc_toggle_fullscreen(libvlc_media_player_t p_mi);

    /**
     * Enable or disable fullscreen.
     * <p>
     * With most window managers, only a top-level windows can be in full-screen mode.
     * Hence, this function will not operate properly if libvlc_media_player_set_xid() was
     * used to embed the video in a non-top-level window. In that case, the embedding
     * window must be reparented to the root window <b>before</b> fullscreen mode is
     * enabled. You will want to reparent it back to its normal parent when disabling
     * fullscreen.
     *
     * @param p_mi the media player
     * @param b_fullscreen boolean for fullscreen status
     */
    public static native void libvlc_set_fullscreen(libvlc_media_player_t p_mi, int b_fullscreen);

    /**
     * Get current fullscreen status.
     *
     * @param p_mi the media player
     * @return the fullscreen status (boolean)
     */
    public static native int libvlc_get_fullscreen(libvlc_media_player_t p_mi);

    /**
     * Enable or disable key press events handling, according to the LibVLC hotkeys configuration.
     * By default and for historical reasons, keyboard events are handled by the LibVLC video
     * widget.
     * <p>
     * On X11, there can be only one subscriber for key press and mouse click events
     * per window. If your application has subscribed to those events for the X window ID of the
     * video widget, then LibVLC will not be able to handle key presses and mouse clicks in any
     * case.
     * <p>
     * This function is only implemented for X11 and Win32 at the moment.
     *
     * @param p_mi the media player
     * @param on true to handle key press events, false to ignore them.
     */
    public static native void libvlc_video_set_key_input(libvlc_media_player_t p_mi, int on);

    /**
     * Enable or disable mouse click events handling. By default, those events are handled. This is
     * needed for DVD menus to work, as well as a few video filters such as "puzzle".
     * <p>
     * See also libvlc_video_set_key_input().
     * <p>
     * This function is only implemented for X11 and Win32 at the moment.
     *
     * @param p_mi the media player
     * @param on true to handle mouse click events, false to ignore them.
     */
    public static native void libvlc_video_set_mouse_input(libvlc_media_player_t p_mi, int on);

    /**
     * Get the pixel dimensions of a video.
     *
     * @param p_mi media player
     * @param num number of the video (starting from, and most commonly 0)
     * @param px pointer to get the pixel width [OUT]
     * @param py pointer to get the pixel height [OUT]
     * @return 0 on success, -1 if the specified video does not exist
     */
    public static native int libvlc_video_get_size(libvlc_media_player_t p_mi, int num, IntByReference px, IntByReference py);

    /**
     * Get the mouse pointer coordinates over a video. Coordinates are expressed in terms of the
     * decoded video resolution, <b>not</b> in terms of pixels on the screen/viewport (to get the
     * latter, you can query your windowing system directly). Either of the coordinates may be
     * negative or larger than the corresponding dimension of the video, if the cursor is outside
     * the rendering area.
     * <p>
     * The coordinates may be out-of-date if the pointer is not located on the video
     * rendering area. LibVLC does not track the pointer if it is outside of the video
     * widget.
     * <p>
     * LibVLC does not support multiple pointers (it does of course support multiple input
     * devices sharing the same pointer) at the moment.
     *
     * @param p_mi media player
     * @param num number of the video (starting from, and most commonly 0)
     * @param px pointer to get the abscissa [OUT]
     * @param py pointer to get the ordinate [OUT]
     * @return 0 on success, -1 if the specified video does not exist
     */
    public static native int libvlc_video_get_cursor(libvlc_media_player_t p_mi, int num, Pointer px, Pointer py);

    /**
     * Get the current video scaling factor. See also libvlc_video_set_scale().
     *
     * @param p_mi the media player
     * @return the currently configured zoom factor, or 0. if the video is set to fit to the output
     *         window/drawable automatically.
     */
    public static native float libvlc_video_get_scale(libvlc_media_player_t p_mi);

    /**
     * Set the video scaling factor. That is the ratio of the number of pixels on screen to the
     * number of pixels in the original decoded video in each dimension. Zero is a special value; it
     * will adjust the video to the output window/drawable (in windowed mode) or the entire screen.
     * Note that not all video outputs support scaling.
     *
     * @param p_mi the media player
     * @param f_factor the scaling factor, or zero
     */
    public static native void libvlc_video_set_scale(libvlc_media_player_t p_mi, float f_factor);

    /**
     * Get current video aspect ratio.
     *
     * @param p_mi the media player
     * @return the video aspect ratio or NULL if unspecified (the result must be released with
     *         free()).
     */
    public static native Pointer libvlc_video_get_aspect_ratio(libvlc_media_player_t p_mi);

    /**
     * Set new video aspect ratio.
     *
     * Note: invalid aspect ratios are ignored.
     *
     * @param p_mi the media player
     * @param psz_aspect new video aspect-ratio or NULL to reset to default
     */
    public static native void libvlc_video_set_aspect_ratio(libvlc_media_player_t p_mi, String psz_aspect);

    /**
     * Create a video viewpoint structure.
     *
     * @return video viewpoint or NULL (the result must be released with free()).
     * @since LibVLC 3.0.0 and later
     */
    public static native libvlc_video_viewpoint_t libvlc_video_new_viewpoint();

    /**
     * Update the video viewpoint information.
     * <p>
     * It is safe to call this function before the media player is started.
     * <p>
     * The values are set asynchronously, it will be used by the next frame displayed.
     *
     * @param p_mi the media player
     * @param p_viewpoint video viewpoint allocated via libvlc_video_new_viewpoint()
     * @param b_absolute if true replace the old viewpoint with the new one. If false, increase/decrease it.
     * @return -1 in case of error, 0 otherwise
     *
     * @since LibVLC 3.0.0 and later
     */
    public static native int libvlc_video_update_viewpoint(libvlc_media_player_t p_mi, libvlc_video_viewpoint_t p_viewpoint, int b_absolute);

    /**
     * Get the current subtitle delay. Positive values means subtitles are being displayed later,
     * negative values earlier.
     *
     * @param p_mi media player
     * @return time (in microseconds) the display of subtitles is being delayed
     * @since LibVLC 2.0.0 or later
     */
    public static native long libvlc_video_get_spu_delay(libvlc_media_player_t p_mi);

    /**
     * Set the subtitle delay. This affects the timing of when the subtitle will be displayed.
     * Positive values result in subtitles being displayed later, while negative values will result
     * in subtitles being displayed earlier.
     *
     * The subtitle delay will be reset to zero each time the media changes.
     *
     * @param p_mi media player
     * @param i_delay time (in microseconds) the display of subtitles should be delayed
     * @return 0 on success, -1 on error
     * @since LibVLC 2.0.0 or later
     */
    public static native int libvlc_video_set_spu_delay(libvlc_media_player_t p_mi, long i_delay);

    /**
      * Get the current subtitle text scale.
      *
      * The scale factor is expressed as a percentage of the default size, where
      * 1.0 represents 100 percent.
      *
      * @param p_mi media player
      * @since LibVLC 4.0.0 or later
      */
    public static native float libvlc_video_get_spu_text_scale(libvlc_media_player_t p_mi);

    /**
     * Set the subtitle text scale.
     * <p>
     * The scale factor is expressed as a percentage of the default size, where
     * 1.0 represents 100 percent.
     * <p>
     * A value of 0.5 would result in text half the normal size, and a value of 2.0
     * would result in text twice the normal size.
     * <p>
     * The minimum acceptable value for the scale factor is 0.1.
     * <p>
     * The maximum is 5.0 (five times normal size).
     *
     * @param p_mi media player
     * @param f_scale scale factor in the range [0.1;5.0] (default: 1.0)
     * @since LibVLC 4.0.0 or later
     */
    public static native void libvlc_video_set_spu_text_scale(libvlc_media_player_t p_mi, float f_scale);

    /**
     * Get the full description of available titles
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_mi the media player
     * @param titles address to store an allocated array of title descriptions
     *        descriptions (must be freed with libvlc_title_descriptions_release()
     *        by the caller) [OUT]
     *
     * @return the number of titles (-1 on error)
     */
    public static native int libvlc_media_player_get_full_title_descriptions(libvlc_media_player_t p_mi, PointerByReference titles);

    /**
     * Release title descriptions.
     *
     * @param p_titles title description array to release
     * @param i_count number of title descriptions to release
     *
     * @since LibVLC 3.0.0 and later
     */
    public static native void libvlc_title_descriptions_release(Pointer p_titles, int i_count);

    /**
     * Get the full description of available chapters.
     *
     * @param p_mi the media player
     * @param i_chapters_of_title index of the title to query for chapters (uses current title if set to -1)
     * @param pp_chapters address to store an allocated array of chapter descriptions
     *        descriptions (must be freed with libvlc_chapter_descriptions_release()
     *        by the caller) [OUT]
     *
     * @return the number of chapters (-1 on error)
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native int libvlc_media_player_get_full_chapter_descriptions(libvlc_media_player_t p_mi, int i_chapters_of_title, PointerByReference pp_chapters);

    /**
     * Release chapter descriptions.
     *
     * @param p_chapters chapter description array to release
     * @param i_count number of chapter descriptions to release
     *
     * @since LibVLC 3.0.0 and later
     */
    public static native void libvlc_chapter_descriptions_release(Pointer p_chapters, int i_count);

    /**
     * Set/unset the video crop ratio.
     * <p>
     * This function forces a crop ratio on any and all video tracks rendered by
     * the media player. If the display aspect ratio of a video does not match the
     * crop ratio, either the top and bottom, or the left and right of the video
     * will be cut out to fit the crop ratio.
     * <p>
     * For instance, a ratio of 1:1 will force the video to a square shape.
     * <p>
     * To disable video crop, set a crop ratio with zero as denominator.
     * <p>
     * A call to this function overrides any previous call to any of
     * libvlc_video_set_crop_ratio(), libvlc_video_set_crop_border() and/or
     * libvlc_video_set_crop_window().
     *
     * @param mp the media player
     * @param num crop ratio numerator (ignored if denominator is 0)
     * @param den crop ratio denominator (or 0 to unset the crop ratio)
     *
     * @since LibVLC 4.0.0 and later
     *
     * @see #libvlc_video_set_aspect_ratio(libvlc_media_player_t, String)
     */
    public static native void libvlc_video_set_crop_ratio(libvlc_media_player_t mp, int num, int den);

    /**
     * Set the video crop window.
     * <p>
     * This function selects a sub-rectangle of video to show. Any pixels outside
     * the rectangle will not be shown.
     * <p>
     * To unset the video crop window, use libvlc_video_set_crop_ratio() or
     * libvlc_video_set_crop_border().
     * <p>
     * A call to this function overrides any previous call to any of
     * libvlc_video_set_crop_ratio(), libvlc_video_set_crop_border() and/or
     * libvlc_video_set_crop_window().
     *
     * @param mp the media player
     * @param x abscissa (i.e. leftmost sample column offset) of the crop window
     * @param y ordinate (i.e. topmost sample row offset) of the crop window
     * @param width sample width of the crop window (cannot be zero)
     * @param height sample height of the crop window (cannot be zero)
     *
     * @since LibVLC 4.0.0 and later
     */
    public static native void libvlc_video_set_crop_window(libvlc_media_player_t mp, int x, int y, int width, int height);

    /**
     * Set the video crop borders.
     * <p>
     * This function selects the size of video edges to be cropped out.
     * <p>
     * To unset the video crop borders, set all borders to zero.
     * <p>
     * A call to this function overrides any previous call to any of
     * libvlc_video_set_crop_ratio(), libvlc_video_set_crop_border() and/or
     * libvlc_video_set_crop_window().
     *
     * @param mp the media player
     * @param left number of sample columns to crop on the left
     * @param right number of sample columns to crop on the right
     * @param top number of sample rows to crop on the top
     * @param bottom number of sample rows to corp on the bottom
     *
     * @since LibVLC 4.0.0 and later
     */
    public static native void libvlc_video_set_crop_border(libvlc_media_player_t mp, int left, int right, int top, int bottom);

    /**
     * Get current teletext page requested or 0 if it's disabled.
     *
     * @param p_mi the media player
     * @return the current teletext page requested.
     */
    public static native int libvlc_video_get_teletext(libvlc_media_player_t p_mi);

    /**
     * Set new teletext page to retrieve.
     *
     * @param p_mi the media player
     * @param i_page teletex page number requested.
     *               This value can be 0 to disable teletext or a number in the range 0 to 1000 to show the requested
     *               page or a TeletextKey. 100 is the default teletext page.
     */
    public static native void libvlc_video_set_teletext(libvlc_media_player_t p_mi, int i_page);

    /**
     * Take a snapshot of the current video window. If i_width AND i_height is 0, original size is
     * used. If i_width XOR i_height is 0, original aspect-ratio is preserved.
     *
     * @param p_mi media player instance
     * @param num number of video output (typically 0 for the first/only one)
     * @param psz_filepath the path where to save the screenshot to
     * @param i_width the snapshot's width
     * @param i_height the snapshot's height
     * @return 0 on success, -1 if the video was not found
     */
    public static native int libvlc_video_take_snapshot(libvlc_media_player_t p_mi, int num, String psz_filepath, int i_width, int i_height);

    /**
     * Enable or disable deinterlace filter
     *
     * @param p_mi libvlc media player
     * @param deinterlace state -1: auto (default), 0: disabled, 1: enabled
     * @param psz_mode type of deinterlace filter, NULL to disable
     */
    public static native void libvlc_video_set_deinterlace(libvlc_media_player_t p_mi, int deinterlace, String psz_mode);

    /**
     * Get an integer marquee option value
     *
     * @param p_mi libvlc media player
     * @param option marq option to get @see libvlc_video_marquee_int_option_t
     * @return marquee option value
     */
    public static native int libvlc_video_get_marquee_int(libvlc_media_player_t p_mi, int option);

    /**
     * Enable, disable or set an integer marquee option Setting libvlc_marquee_Enable has the side
     * effect of enabling (arg !0) or disabling (arg 0) the marq filter.
     *
     * @param p_mi libvlc media player
     * @param option marq option to set @see libvlc_video_marquee_int_option_t
     * @param i_val marq option value
     */
    public static native void libvlc_video_set_marquee_int(libvlc_media_player_t p_mi, int option, int i_val);

    /**
     * Set a marquee string option
     *
     * @param p_mi libvlc media player
     * @param option marq option to set @see libvlc_video_marquee_string_option_t
     * @param psz_text marq option value
     */
    public static native void libvlc_video_set_marquee_string(libvlc_media_player_t p_mi, int option, String psz_text);

    /**
     * Get integer logo option.
     *
     * @param p_mi libvlc media player instance
     * @param option logo option to get, values of libvlc_video_logo_option_t
     * @return logo option value
     */
    public static native int libvlc_video_get_logo_int(libvlc_media_player_t p_mi, int option);

    /**
     * Set logo option as integer. Options that take a different type value are ignored. Passing
     * libvlc_logo_enable as option value has the side effect of starting (arg !0) or stopping (arg
     * 0) the logo filter.
     *
     * @param p_mi libvlc media player instance
     * @param option logo option to set, values of libvlc_video_logo_option_t
     * @param value logo option value
     */
    public static native void libvlc_video_set_logo_int(libvlc_media_player_t p_mi, int option, int value);

    /**
     * Set logo option as string. Options that take a different type value are ignored.
     *
     * @param p_mi libvlc media player instance
     * @param option logo option to set, values of libvlc_video_logo_option_t
     * @param psz_value logo option value
     */
    public static native void libvlc_video_set_logo_string(libvlc_media_player_t p_mi, int option, String psz_value);

    /**
     * Get integer adjust option.
     *
     * @param p_mi libvlc media player instance
     * @param option adjust option to get, values of libvlc_video_adjust_option_t
     * @return value
     * @since LibVLC 1.1.1
     */
    public static native int libvlc_video_get_adjust_int(libvlc_media_player_t p_mi, int option);

    /**
     * Set adjust option as integer. Options that take a different type value are ignored. Passing
     * libvlc_adjust_enable as option value has the side effect of starting (arg !0) or stopping
     * (arg 0) the adjust filter.
     *
     * @param p_mi libvlc media player instance
     * @param option adust option to set, values of libvlc_video_adjust_option_t
     * @param value adjust option value
     * @since LibVLC 1.1.1
     */
    public static native void libvlc_video_set_adjust_int(libvlc_media_player_t p_mi, int option, int value);

    /**
     * Get float adjust option.
     *
     * @param p_mi libvlc media player instance
     * @param option adjust option to get, values of libvlc_video_adjust_option_t
     * @return value
     * @since LibVLC 1.1.1
     */
    public static native float libvlc_video_get_adjust_float(libvlc_media_player_t p_mi, int option);

    /**
     * Set adjust option as float. Options that take a different type value are ignored.
     *
     * @param p_mi libvlc media player instance
     * @param option adust option to set, values of libvlc_video_adjust_option_t
     * @param value adjust option value
     * @since LibVLC 1.1.1
     */
    public static native void libvlc_video_set_adjust_float(libvlc_media_player_t p_mi, int option, float value);

    /**
     * Gets the list of available audio outputs
     *
     * @param p_instance libvlc instance
     * @return list of available audio outputs. It must be freed it with
     *         {@link #libvlc_audio_output_list_release(Pointer)}. In case of error,
     *         NULL is returned.
     */
    public static native libvlc_audio_output_t libvlc_audio_output_list_get(libvlc_instance_t p_instance);

    /**
     * Frees the list of available audio outputs
     *
     * @param p_list list with audio outputs for release
     */
    public static native void libvlc_audio_output_list_release(Pointer p_list);

    /**
     * Sets the audio output.
     * <p>
     * Note: Any change will take be effect only after playback is stopped and
     * restarted. Audio output cannot be changed while playing.
     *
     * @param p_mi media player
     * @param psz_name name of audio output, use psz_name of @see libvlc_audio_output_t
     * @return 0 if function succeded, -1 on error
     */
    public static native int libvlc_audio_output_set(libvlc_media_player_t p_mi, String psz_name);

    /**
     * Gets a list of potential audio output devices.
     * <p>
     * <em>Not all audio outputs support enumerating devices. The audio output may be functional
     * even if the list is empty (NULL).</em>
     * <p>
     * <em>The list may not be exhaustive.</em>
     * <p>
     * <strong>Some audio output devices in the list might not actually work in some circumstances.
     * By default, it is recommended to not specify any explicit audio device.</strong>
     *
     * @param mp media player
     * @return NULL-terminated linked list of potential audio output devices. It must be freed
     *         with {@link #libvlc_audio_output_device_list_release(Pointer)}.
     * @since LibVLC 2.2.0 or later.
     */
    public static native libvlc_audio_output_device_t libvlc_audio_output_device_enum(libvlc_media_player_t mp);

    /**
     * Frees a list of available audio output devices.
     *
     * @param p_list list with audio outputs for release
     * @since LibVLC 2.1.0 or later.
     */
    public static native void libvlc_audio_output_device_list_release(Pointer p_list);

    /**
     * Configures an explicit audio output device.
     *
     * A list of adequate potential device strings can be obtained with
     * libvlc_audio_output_device_enum().
     *
     * This function does not select the specified audio output plugin.
     * libvlc_audio_output_set() is used for that purpose.
     *
     * The syntax for the device parameter depends on the audio output.
     *
     * Some audio output modules require further parameters (e.g. a channels map
     * in the case of ALSA).
     *
     * @param p_mi media player
     * @param psz_device_id device identifier string (see libvlc_audio_output_device_t#psz_device)
     */
    public static native void libvlc_audio_output_device_set(libvlc_media_player_t p_mi, String psz_device_id);

    /**
     * Get the current audio output device identifier.
     *
     * This complements libvlc_audio_output_device_set().
     *
     * <em>The initial value for the current audio output device identifier
     * may not be set or may be some unknown value. A LibVLC application should
     * compare this value against the known device identifiers (e.g. those that
     * were previously retrieved by a call to libvlc_audio_output_device_enum or
     * libvlc_audio_output_device_list_get) to find the current audio output device.
     *
     * It is possible that the selected audio output device changes (an external
     * change) without a call to libvlc_audio_output_device_set. That may make this
     * method unsuitable to use if a LibVLC application is attempting to track
     * dynamic audio device changes as they happen.</em>
     *
     * @param mp media player
     * @return the current audio output device identifier
     *         NULL if no device is selected or in case of error
     *         (the result must be released with free()).
     * @since LibVLC 3.0.0 or later.
     */
    public static native Pointer libvlc_audio_output_device_get(libvlc_media_player_t mp);

    /**
     * Toggle mute status.
     *
     * @param p_mi media player
     */
    public static native void libvlc_audio_toggle_mute(libvlc_media_player_t p_mi);

    /**
     * Get current mute status.
     *
     * @param p_mi media player
     * @return the mute status (boolean)
     */
    public static native int libvlc_audio_get_mute(libvlc_media_player_t p_mi);

    /**
     * Set mute status.
     *
     * @param p_mi media player
     * @param status If status is true then mute, otherwise unmute
     */
    public static native void libvlc_audio_set_mute(libvlc_media_player_t p_mi, int status);

    /**
     * Get current software audio volume.
     *
     * @param p_mi media player
     * @return the software volume in percents (0 = mute, 100 = nominal / 0dB)
     */
    public static native int libvlc_audio_get_volume(libvlc_media_player_t p_mi);

    /**
     * Set current software audio volume.
     *
     * @param p_mi media player
     * @param i_volume the volume in percents (0 = mute, 100 = 0dB)
     * @return 0 if the volume was set, -1 if it was out of range
     */
    public static native int libvlc_audio_set_volume(libvlc_media_player_t p_mi, int i_volume);

    /**
     * Get current audio stereo-mode.
     *
     * @param p_mi media player
     * @return the audio stereo-mode, \see libvlc_audio_output_stereomode_t
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_audio_get_stereomode(libvlc_media_player_t p_mi);

    /**
     * Set current audio stereo-mode.
     *
     * @param p_mi media player
     * @param mode the audio stereo-mode
     * @return 0 on success, -1 on error
     * @see libvlc_audio_output_stereomode_t
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_audio_set_stereomode( libvlc_media_player_t p_mi, int mode);

    /**
     * Get current audio mix-mode.
     *
     * @param p_mi media player
     * @return the audio mix-mode
     * @see libvlc_audio_output_mixmode_t
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_audio_get_mixmode(libvlc_media_player_t p_mi);

    /**
     * Set current audio mix-mode.
     * <p>
     * By default, (libvlc_AudioMixMode_Unset), the audio output will keep its
     * original channel configuration (play stereo as stereo, or 5.1 as 5.1). Yet,
     * the OS and Audio API might refuse a channel configuration and asks VLC to
     * adapt (Stereo played as 5.1 or vice-versa).
     * <p>
     * This function allows to force a channel configuration, it will only work if
     * the OS and Audio API accept this configuration (otherwise, it won't have any
     * effects). Here are some examples:
     * <ul>
     *   <li>Play multi-channels (5.1, 7.1...) as stereo (libvlc_AudioMixMode_Stereo)</li>
     *   <li>Play Stereo or 5.1 as 7.1 (libvlc_AudioMixMode_7_1)</li>
     *   <li>Play multi-channels as stereo with a binaural effect
     *     (libvlc_AudioMixMode_Binaural). It might be selected automatically if the
     *     OS and Audio API can detect if a headphone is plugged.</li>
     * </ul>
     *
     * @param p_mi media player
     * @param mode the audio mix-mode, @
     * @return 0 on success, -1 on error
     * @see libvlc_audio_output_mixmode_t
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_audio_set_mixmode(libvlc_media_player_t p_mi, int mode);

    /**
     * Get current audio delay.
     *
     * @param p_mi media player
     * @return amount audio is being delayed by, in microseconds
     * @since LibVLC 1.1.1
     */
    public static native long libvlc_audio_get_delay(libvlc_media_player_t p_mi);

    /**
     * Set current audio delay. The delay is only active for the current media item and will be
     * reset to zero each time the media changes.
     *
     * @param p_mi media player
     * @param i_delay amount to delay audio by, in microseconds
     * @return 0 on success, -1 on error
     * @since LibVLC 1.1.1
     */
    public static native int libvlc_audio_set_delay(libvlc_media_player_t p_mi, long i_delay);

    /**
     * Get the number of equalizer presets.
     *
     * @return number of presets
     * @since LibVLC 2.2.0 or later
     */
    public static native int libvlc_audio_equalizer_get_preset_count();

    /**
     * Get the name of a particular equalizer preset.
     * <p>
     * This name can be used, for example, to prepare a preset label or menu in a user
     * interface.
     *
     * @param u_index index of the preset, counting from zero
     * @return preset name, or NULL if there is no such preset
     * @since LibVLC 2.2.0 or later
     */
    public static native String libvlc_audio_equalizer_get_preset_name(int u_index);

    /**
     * Get the number of distinct frequency bands for an equalizer.
     *
     * @return number of frequency bands
     * @since LibVLC 2.2.0 or later
     */
    public static native int libvlc_audio_equalizer_get_band_count();

    /**
     * Get a particular equalizer band frequency.
     * <p>
     * This value can be used, for example, to create a label for an equalizer band control
     * in a user interface.
     *
     * @param u_index index of the band, counting from zero
     * @return equalizer band frequency (Hz), or -1 if there is no such band
     * @since LibVLC 2.2.0 or later
     */
    public static native float libvlc_audio_equalizer_get_band_frequency(int u_index);

    /**
     * Create a new default equalizer, with all frequency values zeroed.
     * <p>
     * The new equalizer can subsequently be applied to a media player by invoking
     * libvlc_media_player_set_equalizer().
     * <p>
     * The returned handle should be freed via libvlc_audio_equalizer_release() when
     * it is no longer needed.
     *
     * @return opaque equalizer handle, or NULL on error
     * @since LibVLC 2.2.0 or later
     */
    public static native libvlc_equalizer_t libvlc_audio_equalizer_new();

    /**
     * Create a new equalizer, with initial frequency values copied from an existing
     * preset.
     * <p>
     * The new equalizer can subsequently be applied to a media player by invoking
     * libvlc_media_player_set_equalizer().
     * <p>
     * The returned handle should be freed via libvlc_audio_equalizer_release() when
     * it is no longer needed.
     *
     * @param u_index index of the preset, counting from zero
     * @return opaque equalizer handle, or NULL on error
     * @since LibVLC 2.2.0 or later
     */
    public static native libvlc_equalizer_t libvlc_audio_equalizer_new_from_preset(int u_index);

    /**
     * Release a previously created equalizer instance.
     * <p>
     * The equalizer was previously created by using libvlc_audio_equalizer_new() or
     * libvlc_audio_equalizer_new_from_preset().
     * <p>
     * It is safe to invoke this method with a NULL p_equalizer parameter for no effect.
     *
     * @param p_equalizer opaque equalizer handle, or NULL
     * @since LibVLC 2.2.0 or later
     */
    public static native void libvlc_audio_equalizer_release(libvlc_equalizer_t p_equalizer);

    /**
     * Set a new pre-amplification value for an equalizer.
     * <p>
     * The new equalizer settings are subsequently applied to a media player by invoking
     * libvlc_media_player_set_equalizer().
     *
     * @param p_equalizer valid equalizer handle, must not be NULL
     * @param f_preamp preamp value (-20.0 to 20.0 Hz)
     * @return zero on success, -1 on error
     * @since LibVLC 2.2.0 or later
     */
    public static native int libvlc_audio_equalizer_set_preamp(libvlc_equalizer_t p_equalizer, float f_preamp);

    /**
     * Get the current pre-amplification value from an equalizer.
     *
     * @param p_equalizer valid equalizer handle, must not be NULL
     * @return preamp value (Hz)
     * @since LibVLC 2.2.0 or later
     */
    public static native float libvlc_audio_equalizer_get_preamp(libvlc_equalizer_t p_equalizer);

    /**
     * Set a new amplification value for a particular equalizer frequency band.
     * <p>
     * The new equalizer settings are subsequently applied to a media player by invoking
     * libvlc_media_player_set_equalizer().
     *
     * @param p_equalizer valid equalizer handle, must not be NULL
     * @param f_amp amplification value (-20.0 to 20.0 Hz)
     * @param u_band index, counting from zero, of the frequency band to set
     * @return zero on success, -1 on error
     * @since LibVLC 2.2.0 or later
     */
    public static native int libvlc_audio_equalizer_set_amp_at_index( libvlc_equalizer_t p_equalizer, float f_amp, int u_band);

    /**
     * Get the amplification value for a particular equalizer frequency band.
     *
     * @param p_equalizer valid equalizer handle, must not be NULL
     * @param u_band index, counting from zero, of the frequency band to get
     * @return amplification value (Hz); zero if there is no such frequency band
     * @since LibVLC 2.2.0 or later
     */
    public static native float libvlc_audio_equalizer_get_amp_at_index(libvlc_equalizer_t p_equalizer, int u_band);

    /**
     * Apply new equalizer settings to a media player.
     * <p>
     * The equalizer is first created by invoking libvlc_audio_equalizer_new() or
     * libvlc_audio_equalizer_new_from_preset().
     * <p>
     * It is possible to apply new equalizer settings to a media player whether the media
     * player is currently playing media or not.
     * <p>
     * Invoking this method will immediately apply the new equalizer settings to the audio
     * output of the currently playing media if there is any.
     * <p>
     * If there is no currently playing media, the new equalizer settings will be applied
     * later if and when new media is played.
     * <p>
     * Equalizer settings will automatically be applied to subsequently played media.
     * <p>
     * To disable the equalizer for a media player invoke this method passing NULL for the
     * p_equalizer parameter.
     * <p>
     * The media player does not keep a reference to the supplied equalizer so it is safe
     * for an application to release the equalizer reference any time after this method
     * returns.
     *
     * @param p_mi opaque media player handle
     * @param p_equalizer opaque equalizer handle, or NULL to disable the equalizer for this media player
     * @return zero on success, -1 on error
     * @since LibVLC 2.2.0 or later
     */
    public static native int libvlc_media_player_set_equalizer(libvlc_media_player_t p_mi, libvlc_equalizer_t p_equalizer);

    /**
     * Gets the media role.
     *
     * @param p_mi opaque media player handle
     * @return the media player role (MediaPlayerRole)
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_player_get_role(libvlc_media_player_t p_mi);

    /**
     * Sets the media role.
     *
     * @param p_mi opaque media player handle
     * @param role the media player role (MediaPlayerRole)
     * @return 0 on success, -1 on error
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_player_set_role(libvlc_media_player_t p_mi, int role);

    /**
     * Start/stop recording.
     * <p>
     * Listen to the libvlc_MediaPlayerRecordChanged event to monitor the recording state.
     *
     * @param p_mi media player
     * @param enable true to start recording, false to stop
     * @param dir_path path of the recording directory or NULL (use default path), only has an effect when first enabling recording.
     *
     * @since LibVLC 4.0.0 and later.
    */
    public static native void libvlc_media_player_record(libvlc_media_player_t p_mi, int enable, String dir_path);

    /**
     * Watch for time updates.
     * <p>
     * Warning: only one watcher can be registered at a time. Calling this function
     * a second time (if libvlc_media_player_unwatch_time() was not called
     * in-between) will fail.
     *
     * @param p_mi the media player
     * @param min_period_us corresponds to the minimum period, in us, between each update, use it to avoid flood from too many source updates, set it to 0 to receive all updates.
     * @param on_update callback to listen to update events (must not be NULL)
     * @param on_discontinuity callback to listen to discontinuity events (can be NULL)
     * @param cbs_data opaque pointer used by the callbacks
     * @return 0 on success, -1 on error (allocation error, or if already watching)
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_media_player_watch_time(libvlc_media_player_t p_mi, long min_period_us, libvlc_media_player_watch_time_on_update on_update, libvlc_media_player_watch_time_on_discontinuity on_discontinuity, Pointer cbs_data);

    /**
     * Unwatch time updates.
     *
     * @param p_mi the media player
     * @since LibVLC 4.0.0 or later
     */
    public static native void libvlc_media_player_unwatch_time(libvlc_media_player_t p_mi);

    /**
     * Interpolate a timer value to now.

     * @param point time update obtained via the
     * libvlc_media_player_watch_time_on_update() callback
     * @param system_now_us current system date, in us, returned by libvlc_clock()
     * @param out_ts_us pointer where to set the interpolated ts, in_us
     * @param out_pos pointer where to set the interpolated position
     * @return 0 in case of success, -1 if the interpolated ts is negative (could
     * happen during the buffering step)
     * @since LibVLC 4.0.0 or later
     */
    public static native int libvlc_media_player_time_point_interpolate(libvlc_media_player_time_point_t point, long system_now_us, LongByReference out_ts_us, DoubleByReference out_pos);

    /**
     * Get the date of the next interval
     *
     * Can be used to setup an UI timer in order to update some widgets at specific
     * interval. A next_interval_us of VLC_TICK_FROM_SEC(1) can be used to update a
     * time widget when the media reaches a new second.
     *
     * Note: The media time doesn't necessarily correspond to the system time, that
     * is why this function is needed and uses the rate of the current point.
     *
     * @param point time update obtained via the libvlc_media_player_watch_time_on_update()
     * @param system_now_us same system date used by libvlc_media_player_time_point_interpolate()
     * @param interpolated_ts_us ts returned by libvlc_media_player_time_point_interpolate()
     * @param next_interval_us next interval
     * @return the absolute system date, in us, of the next interval, use libvlc_delay() to get a relative delay.
     * @since LibVLC 4.0.0 or later
     */
    public static native long libvlc_media_player_time_point_get_next_date(libvlc_media_player_time_point_t point, long system_now_us, long interpolated_ts_us, long next_interval_us);

    // === libvlc_media_player.h ================================================

    // === libvlc_media_list.h ==================================================

    /**
     * Create an empty media list.
     *
     * @param p_instance libvlc instance
     * @return empty media list, or NULL on error
     */
    public static native libvlc_media_list_t libvlc_media_list_new(libvlc_instance_t p_instance);

    /**
     * Release media list created with libvlc_media_list_new().
     *
     * @param p_ml a media list created with libvlc_media_list_new()
     */
    public static native void libvlc_media_list_release(libvlc_media_list_t p_ml);

    /**
     * Retain reference to a media list
     *
     * @param p_ml a media list created with libvlc_media_list_new()
     */
    public static native void libvlc_media_list_retain(libvlc_media_list_t p_ml);

    /**
     * Associate media instance with this media list instance. If another media instance was present
     * it will be released. The libvlc_media_list_lock should NOT be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     * @param p_md media instance to add
     */
    public static native void libvlc_media_list_set_media(libvlc_media_list_t p_ml, libvlc_media_t p_md);

    /**
     * Get media instance from this media list instance. This action will increase the refcount on
     * the media instance. The libvlc_media_list_lock should NOT be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     * @return media instance
     */
    public static native libvlc_media_t libvlc_media_list_media(libvlc_media_list_t p_ml);

    /**
     * Add media instance to media list The libvlc_media_list_lock should be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     * @param p_md a media instance
     * @return 0 on success, -1 if the media list is read-only
     */
    public static native int libvlc_media_list_add_media(libvlc_media_list_t p_ml, libvlc_media_t p_md);

    /**
     * Insert media instance in media list on a position The libvlc_media_list_lock should be held
     * upon entering this function.
     *
     * @param p_ml a media list instance
     * @param p_md a media instance
     * @param i_pos position in array where to insert
     * @return 0 on success, -1 if the media list si read-only
     */
    public static native int libvlc_media_list_insert_media(libvlc_media_list_t p_ml, libvlc_media_t p_md, int i_pos);

    /**
     * Remove media instance from media list on a position The libvlc_media_list_lock should be held
     * upon entering this function.
     *
     * @param p_ml a media list instance
     * @param i_pos position in array where to insert
     * @return 0 on success, -1 if the list is read-only or the item was not found
     */
    public static native int libvlc_media_list_remove_index(libvlc_media_list_t p_ml, int i_pos);

    /**
     * Get count on media list items The libvlc_media_list_lock should be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     * @return number of items in media list
     */
    public static native int libvlc_media_list_count(libvlc_media_list_t p_ml);

    /**
     * List media instance in media list at a position The libvlc_media_list_lock should be held
     * upon entering this function.
     *
     * @param p_ml a media list instance
     * @param i_pos position in array where to insert
     * @return media instance at position i_pos, or NULL if not found. In case of success,
     *         libvlc_media_retain() is called to increase the refcount on the media.
     */
    public static native libvlc_media_t libvlc_media_list_item_at_index(libvlc_media_list_t p_ml, int i_pos);

    /**
     * Find index position of List media instance in media list. Warning: the function will return
     * the first matched position. The libvlc_media_list_lock should be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     * @param p_md media list instance
     * @return position of media instance
     */
    public static native int libvlc_media_list_index_of_item(libvlc_media_list_t p_ml, libvlc_media_t p_md);

    /**
     * This indicates if this media list is read-only from a user point of view
     *
     * @param p_ml media list instance
     * @return 0 on readonly, 1 on readwrite
     *
     * FIXME I am pretty sure the documented return values are the wrong way around
     */
    public static native int libvlc_media_list_is_readonly(libvlc_media_list_t p_ml);

    /**
     * Get lock on media list items
     *
     * @param p_ml a media list instance
     */
    public static native void libvlc_media_list_lock(libvlc_media_list_t p_ml);

    /**
     * Release lock on media list items The libvlc_media_list_lock should be held upon entering this
     * function.
     *
     * @param p_ml a media list instance
     */
    public static native void libvlc_media_list_unlock(libvlc_media_list_t p_ml);

    /**
     * Get libvlc_event_manager from this media list instance. The p_event_manager is immutable, so
     * you don't have to hold the lock
     *
     * @param p_ml a media list instance
     * @return libvlc_event_manager
     */
    public static native libvlc_event_manager_t libvlc_media_list_event_manager(libvlc_media_list_t p_ml);

    // === libvlc_media_list.h ==================================================

    // === libvlc_media_list_player.h ===========================================

    /**
     * Create new media_list_player.
     *
     * @param p_instance libvlc instance
     * @return media list player instance or NULL on error
     */
    public static native libvlc_media_list_player_t libvlc_media_list_player_new(libvlc_instance_t p_instance);

    /**
     * Release a media_list_player after use.
     *
     * Decrement the reference count of a* media player object. If the reference count is 0, then
     * libvlc_media_list_player_release() will release the media player object. If the media player
     * object has been released, then it should not be used again.
     *
     * @param p_mlp media list player instance
     */
    public static native void libvlc_media_list_player_release(libvlc_media_list_player_t p_mlp);

    /**
     * Retain a reference to a media player list object.
     *
     * Use libvlc_media_list_player_release() to decrement reference count.
     *
     * @param p_mlp media player list object
     */
    public static native void libvlc_media_list_player_retain(libvlc_media_list_player_t p_mlp);

    /**
     * Return the event manager of this media_list_player.
     *
     * @param p_mlp media list player instance
     * @return the event manager
     */
    public static native libvlc_event_manager_t libvlc_media_list_player_event_manager(libvlc_media_list_player_t p_mlp);

    /**
     * Replace media player in media_list_player with this instance.
     *
     * @param p_mlp media list player instance
     * @param p_mi media player instance
     */
    public static native void libvlc_media_list_player_set_media_player(libvlc_media_list_player_t p_mlp, libvlc_media_player_t p_mi);

    /**
     * Get media player of the media_list_player instance.
     * <p>
     * Note: the caller is responsible for releasing the returned instance.
     *
     * @param p_mlp media list player instance
     * @return media player instance
     * @since LibVLC 3.0.0
     */
    public static native libvlc_media_player_t libvlc_media_list_player_get_media_player(libvlc_media_list_player_t p_mlp);

    /**
     * Set the media list associated with the player
     *
     * @param p_mlp media list player instance
     * @param p_mlist list of media
     */
    public static native void libvlc_media_list_player_set_media_list(libvlc_media_list_player_t p_mlp, libvlc_media_list_t p_mlist);

    /**
     * Play media list
     *
     * @param p_mlp media list player instance
     */
    public static native void libvlc_media_list_player_play(libvlc_media_list_player_t p_mlp);

    /**
     * Pause media list
     *
     * @param p_mlp media list player instance
     */
    public static native void libvlc_media_list_player_pause(libvlc_media_list_player_t p_mlp);

    /**
     * Pause or resume media list
     *
     * @param p_mlp media list player instance
     * @param do_pause play/resume if zero, pause if non-zero
     * @since LibVLC 3.0.0 or later
     */
    public static native void libvlc_media_list_player_set_pause(libvlc_media_list_player_t p_mlp, int do_pause);

    /**
     * Is media list playing?
     *
     * @param p_mlp media list player instance
     * @return true for playing and false for not playing
     */
    public static native int libvlc_media_list_player_is_playing(libvlc_media_list_player_t p_mlp);

    /**
     * Get current libvlc_state of media list player
     *
     * @param p_mlp media list player instance
     * @return State for media list player
     */
    public static native int libvlc_media_list_player_get_state(libvlc_media_list_player_t p_mlp);

    /**
     * Play media list item at position index
     *
     * @param p_mlp media list player instance
     * @param i_index index in media list to play
     * @return 0 upon success -1 if the item wasn't found
     */
    public static native int libvlc_media_list_player_play_item_at_index(libvlc_media_list_player_t p_mlp, int i_index);

    /**
     * Play the given media item
     *
     * @param p_mlp media list player instance
     * @param p_md the media instance
     * @return 0 upon success, -1 if the media is not part of the media list
     */
    public static native int libvlc_media_list_player_play_item(libvlc_media_list_player_t p_mlp, libvlc_media_t p_md);

    /**
     * Stop playing media list
     *
     * @param p_mlp media list player instance
     */
    public static native void libvlc_media_list_player_stop_async(libvlc_media_list_player_t p_mlp);

    /**
     * Play next item from media list
     *
     * @param p_mlp media list player instance
     * @return 0 upon success -1 if there is no next item
     */
    public static native int libvlc_media_list_player_next(libvlc_media_list_player_t p_mlp);

    /**
     * Play previous item from media list
     *
     * @param p_mlp media list player instance
     * @return 0 upon success -1 if there is no previous item
     */
    public static native int libvlc_media_list_player_previous(libvlc_media_list_player_t p_mlp);

    /**
     * Sets the playback mode for the playlist
     *
     * @param p_mlp media list player instance
     * @param e_mode playback mode specification
     */
    public static native void libvlc_media_list_player_set_playback_mode(libvlc_media_list_player_t p_mlp, int e_mode);

    // === libvlc_media_list_player.h ===========================================

    // === libvlc_dialog.h ======================================================

    /**
     * Register callbacks in order to handle VLC dialogs.
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_instance the instance
     * @param p_cbs a pointer to callbacks, or NULL to unregister callbacks.
     * @param p_data opaque pointer for the callback
     */
    public static native void libvlc_dialog_set_callbacks(libvlc_instance_t p_instance, libvlc_dialog_cbs p_cbs, Pointer p_data);

    /**
     * Register callback in order to handle VLC error messages.
     *
     * @since LibVLC 4.0.0 and later.
     *
     * @param p_instance the instance
     * @param p_cbs a pointer to callback, or NULL to unregister callback.
     * @param p_data opaque pointer for the callback
     */
    public static native void libvlc_dialog_set_error_callback(libvlc_instance_t p_instance, libvlc_dialog_display_error_cb p_cbs, Pointer p_data);

    /**
     * Associate an opaque pointer with the dialog id.
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_id id of the dialog
     * @param p_context opaque pointer associated with the dialog id
     */
    public static native void libvlc_dialog_set_context(libvlc_dialog_id p_id, Pointer p_context);

    /**
     * Return the opaque pointer associated with the dialog id.
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_id id of the dialog
     * @return opaque pointer associated with the dialog id
     */
    public static native Pointer libvlc_dialog_get_context(libvlc_dialog_id p_id);

    /**
     * Post a login answer.
     * <p>
     * After this call, p_id won't be valid anymore
     *
     * @see libvlc_dialog_cbs#pf_display_login
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_id id of the dialog
     * @param psz_username valid and non empty string
     * @param psz_password valid string (can be empty)
     * @param b_store if true, store the credentials
     * @return 0 on success, or -1 on error
     */
    public static native int libvlc_dialog_post_login(libvlc_dialog_id p_id, String psz_username, String psz_password, int b_store);

    /**
     * Post a question answer.
     * <p>
     * After this call, p_id won't be valid anymore
     *
     * @see libvlc_dialog_cbs#pf_display_question
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_id id of the dialog
     * @param i_action 1 for action1, 2 for action2
     * @return 0 on success, or -1 on error
     */
    public static native int libvlc_dialog_post_action(libvlc_dialog_id p_id, int i_action);

    /**
     * Dismiss a dialog.
     * <p>
     * After this call, p_id won't be valid anymore
     *
     * @see libvlc_dialog_cbs#pf_cancel
     *
     * @since LibVLC 3.0.0 and later.
     *
     * @param p_id id of the dialog
     * @return 0 on success, or -1 on error
     */
    public static native int libvlc_dialog_dismiss(libvlc_dialog_id p_id);

    // === libvlc_dialog.h ======================================================

    // === libvlc_media_discoverer.h ============================================

    /**
     * Create a media discoverer object by name.
     *
     * After this object is created, you should attach to events in order to be
     * notified of the discoverer state.
     *
     * You should also attach to media_list events in order to be notified of new
     * items discovered.
     *
     * You need to call {@link #libvlc_media_discoverer_start(libvlc_media_discoverer_t)}
     * in order to start the discovery.
     *
     * @see #libvlc_media_discoverer_media_list(libvlc_media_discoverer_t)
     * @see #libvlc_media_discoverer_start(libvlc_media_discoverer_t)
     *
     * @param p_inst libvlc instance
     * @param psz_name service name
     * @return media discover object or NULL in case of error
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native libvlc_media_discoverer_t libvlc_media_discoverer_new(libvlc_instance_t p_inst, String psz_name);

    /**
     * Start media discovery.
     *
     * To stop it, call libvlc_media_discoverer_stop() or
     * libvlc_media_discoverer_release() directly.
     *
     * @see #libvlc_media_discoverer_stop(libvlc_media_discoverer_t)
     *
     * @param p_mdis media discover object
     * @return -1 in case of error, 0 otherwise
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_media_discoverer_start(libvlc_media_discoverer_t p_mdis);

    /**
     * Stop media discovery.
     *
     * @see #libvlc_media_discoverer_start(libvlc_media_discoverer_t)
     *
     * @param p_mdis media discover object
     *
     * @since LibVLC 3.0.0 or later
     */
    public static native void libvlc_media_discoverer_stop(libvlc_media_discoverer_t p_mdis);

    /**
     * Release media discover object. If the reference count reaches 0, then the object will be
     * released.
     *
     * @param p_mdis media service discover object
     */
    public static native void libvlc_media_discoverer_release(libvlc_media_discoverer_t p_mdis);

    /**
     * Get media service discover media list.
     *
     * @param p_mdis media service discover object
     * @return list of media items
     */
    public static native libvlc_media_list_t libvlc_media_discoverer_media_list(libvlc_media_discoverer_t p_mdis);

    /**
     * Query if media service discover object is running.
     *
     * @param p_mdis media service discover object
     * @return true if running, false if not
     */
    public static native int libvlc_media_discoverer_is_running(libvlc_media_discoverer_t p_mdis);

    /**
     * Get media discoverer services by category
     *
     * @param p_inst libvlc instance
     * @param i_cat category of services to fetch
     * @param ppp_services address to store an allocated array of media discoverer services (must be freed with libvlc_media_discoverer_list_release() by the caller) [OUT]
     * @return the number of media discoverer services (0 on error)
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native size_t libvlc_media_discoverer_list_get(libvlc_instance_t p_inst, int i_cat, PointerByReference ppp_services);

    /**
     * Release an array of media discoverer services
     *
     * @see #libvlc_media_discoverer_list_get(libvlc_instance_t, int, PointerByReference)
     *
     * @param pp_services array to release
     * @param i_count number of elements in the array
     *
     * @since LibVLC 3.0.0 and later.
     */
    public static native void libvlc_media_discoverer_list_release(Pointer pp_services, size_t i_count);

    // === libvlc_media_discoverer.h ============================================

    // === libvlc_renderer_discoverer.h =========================================

    /**
     * Hold a renderer item, i.e. creates a new reference
     *
     * This functions need to called from the libvlc_RendererDiscovererItemAdded
     * callback if the libvlc user wants to use this item after. (for display or
     * for passing it to the mediaplayer for example).
     *
     * @param p_item renderer item handle
     * @return the current item
     * @since LibVLC 3.0.0 or later
     */
    public static native libvlc_renderer_item_t libvlc_renderer_item_hold(libvlc_renderer_item_t p_item);

    /**
     * Releases a renderer item, i.e. decrements its reference counter
     *
     * @param p_item renderer item handle
     * @since LibVLC 3.0.0 or later
     */
    public static native void libvlc_renderer_item_release(libvlc_renderer_item_t p_item);

    /**
     * Get the human readable name of a renderer item
     *
     * @param p_item renderer item handle
     * @return the name of the item (can't be NULL, must *not* be freed)
     * @since LibVLC 3.0.0 or later
     */
    public static native String libvlc_renderer_item_name(libvlc_renderer_item_t p_item);

    /**
     * Get the type (not translated) of a renderer item. For now, the type can only
     * be "chromecast" ("upnp", "airplay" may come later).
     *
     * @param p_item renderer item handle
     * @return the type of the item (can't be NULL, must *not* be freed)
     * @since LibVLC 3.0.0 or later
     */
    public static native String libvlc_renderer_item_type(libvlc_renderer_item_t p_item);

    /**
     * Get the icon uri of a renderer item
     *
     * @param p_item renderer item handle
     * @return the uri of the item's icon (can be NULL, must *not* be freed)
     * @since LibVLC 3.0.0 or later
     */
    public static native String libvlc_renderer_item_icon_uri(libvlc_renderer_item_t p_item);

    /**
     * Get the flags of a renderer item
     *
     * @param p_item renderer item handle
     * @return bitwise flag: capabilities of the renderer, see
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_renderer_item_flags(libvlc_renderer_item_t p_item);

    /**
     * Create a renderer discoverer object by name
     *
     * After this object is created, you should attach to events in order to be
     * notified of the discoverer events.
     *
     * You need to call libvlc_renderer_discoverer_start() in order to start the
     * discovery.
     *
     * @see #libvlc_renderer_discoverer_event_manager(libvlc_renderer_discoverer_t)
     * @see #libvlc_renderer_discoverer_start(libvlc_renderer_discoverer_t)
     *
     * @param p_inst libvlc instance
     * @param psz_name service name; use libvlc_renderer_discoverer_list_get() to
     * get a list of the discoverer names available in this libVLC instance
     * @return media discover object or NULL in case of error
     * @since LibVLC 3.0.0 or later
     */
    public static native libvlc_renderer_discoverer_t libvlc_renderer_discoverer_new(libvlc_instance_t p_inst, String psz_name);

    /**
     * Release a renderer discoverer object
     *
     * @param p_rd renderer discoverer object
     * @since LibVLC 3.0.0 or later
     */
    public static native void libvlc_renderer_discoverer_release(libvlc_renderer_discoverer_t p_rd);

    /**
     * Start renderer discovery
     *
     * To stop it, call libvlc_renderer_discoverer_stop() or
     * libvlc_renderer_discoverer_release() directly.
     *
     * @see #libvlc_renderer_discoverer_stop(libvlc_renderer_discoverer_t)
     *
     * @param p_rd renderer discoverer object
     * @return -1 in case of error, 0 otherwise
     * @since LibVLC 3.0.0 or later
     */
    public static native int libvlc_renderer_discoverer_start(libvlc_renderer_discoverer_t p_rd);

    /**
     * Stop renderer discovery.
     *
     * @see #libvlc_renderer_discoverer_start(libvlc_renderer_discoverer_t)
     *
     * @param p_rd renderer discoverer object
     * @since LibVLC 3.0.0 or later
     */
    public static native void libvlc_renderer_discoverer_stop(libvlc_renderer_discoverer_t p_rd);

    /**
     * Get the event manager of the renderer discoverer
     *
     * The possible events to attach are @ref libvlc_RendererDiscovererItemAdded
     * and @ref libvlc_RendererDiscovererItemDeleted.
     *
     * The @ref libvlc_renderer_item_t struct passed to event callbacks is owned by
     * VLC, users should take care of holding/releasing this struct for their
     * internal usage.
     *
     * @see libvlc_event_u#renderer_discoverer_item_added
     * @see libvlc_event_u#renderer_discoverer_item_deleted
     *
     * @param p_rd renderer discoverer handle
     * @return a valid event manager (can't fail)
     * @since LibVLC 3.0.0 or later
     */
    public static native libvlc_event_manager_t libvlc_renderer_discoverer_event_manager(libvlc_renderer_discoverer_t p_rd);

    /**
     * Get media discoverer services
     *
     * @see #libvlc_renderer_discoverer_list_release(Pointer, size_t)
     *
     * @param p_inst libvlc instance
     * @param ppp_services address to store an allocated array of renderer
     * discoverer services (must be freed with libvlc_renderer_list_release() by
     * the caller) [OUT]
     * @return the number of media discoverer services (0 on error)
     * @since LibVLC 3.0.0 and later
     */
    public static native size_t libvlc_renderer_discoverer_list_get(libvlc_instance_t p_inst, PointerByReference ppp_services);

    /**
     * Release an array of media discoverer services
     *
     * @see #libvlc_renderer_discoverer_list_get(libvlc_instance_t, PointerByReference)
     *
     * @param pp_services array to release
     * @param i_count number of elements in the array
     * @since LibVLC 3.0.0 and later
     */
    public static native void libvlc_renderer_discoverer_list_release(Pointer pp_services, size_t i_count);

    // === libvlc_renderer_discoverer.h =========================================

    // === libvlc_picture.h =====================================================

    /**
     * Increment the reference count of this picture.
     *
     * @see #libvlc_picture_release(libvlc_picture_t)
     * @param pic A picture object
     *
     * @since libvlc 4.0 or later
     */
    public static native void libvlc_picture_retain(libvlc_picture_t pic);

    /**
     * Decrement the reference count of this picture.
     * When the reference count reaches 0, the picture will be released.
     * The picture must not be accessed after calling this function.
     *
     * @see #libvlc_picture_retain(libvlc_picture_t)
     * @param pic A picture object
     *
     * @since libvlc 4.0 or later
     */
    public static native void libvlc_picture_release(libvlc_picture_t pic);

    /**
     * Saves this picture to a file. The image format is the same as the one
     * returned by \link libvlc_picture_type \endlink
     *
     * @param pic A picture object
     * @param path The path to the generated file
     * @return 0 in case of success, -1 otherwise
     *
     * @since libvlc 4.0 or later
     */
    public static native int libvlc_picture_save(libvlc_picture_t pic, String path);

    /**
     * Returns the image internal buffer, including potential padding.
     * The libvlc_picture_t owns the returned buffer, which must not be modified nor
     * freed.
     *
     * @param pic A picture object
     * @param size A pointer to a size_t that will hold the size of the buffer [required]
     * @return A pointer to the internal buffer.
     *
     * @since libvlc 4.0 or later
     */
    public static native Pointer libvlc_picture_get_buffer(libvlc_picture_t pic, size_tByReference size);

    /**
     * Returns the picture type
     *
     * @param pic A picture object
     * @return picture type
     * @since libvlc 4.0 or later
     */
    public static native int libvlc_picture_type(libvlc_picture_t pic);

    /**
     * Returns the image stride, ie. the number of bytes per line.
     * This can only be called on images of type libvlc_picture_Argb
     *
     * @param pic A picture object
     * @return stride
     * @since libvlc 4.0 or later
     */
    public static native int libvlc_picture_get_stride(libvlc_picture_t pic);

    /**
     * Returns the width of the image in pixels
     *
     * @param pic A picture object
     * @return width
     * @since libvlc 4.0 or later
     */
    public static native int libvlc_picture_get_width(libvlc_picture_t pic);

    /**
     * Returns the height of the image in pixels
     *
     * @param pic A picture object
     * @return height
     * @since libvlc 4.0 or later
     */
    public static native int libvlc_picture_get_height(libvlc_picture_t pic);

    /**
     * Returns the time at which this picture was generated, in milliseconds
     * @param pic A picture object
     * @return timestamp
     * @since libvlc 4.0 or later
     */
    public static native long libvlc_picture_get_time(libvlc_picture_t pic);

    /**
     * Returns the number of pictures in the list
     *
     * @since libvlc 4.0 or later
     */
    public static native size_t libvlc_picture_list_count(libvlc_picture_list_t list);

    /**
     * Returns the picture at the provided index.
     *
     * If the index is out of bound, the result is undefined.
     *
     * @since libvlc 4.0 or later
     */
    public static native libvlc_picture_t libvlc_picture_list_at(libvlc_picture_list_t list, size_t index);

    /**
     * Destroys a picture list and releases the pictures it contains
     *
     * Calling this function with a NULL list is safe and will return immediately.
     *
     * @param list The list to destroy
     * @since libvlc 4.0 or later
     */
    public static native void libvlc_picture_list_destroy(libvlc_picture_list_t list);

    // === libvlc_picture.h =====================================================

    // === libvlc_media_track.h =================================================

    /**
     * Get the number of tracks in a tracklist
     *
     * @since LibVLC 4.0.0 and later.
     *
     * @param list valid tracklist
     *
     * @return number of tracks, or 0 if the list is empty
     */
    public static native size_t libvlc_media_tracklist_count(libvlc_media_tracklist_t list);

    /**
     * Get a track at a specific index
     *
     * Warning: The behaviour is undefined if the index is not valid.
     *
     * @since LibVLC 4.0.0 and later.
     *
     * @param list valid tracklist
     * @param index valid index in the range [0; count[
     *
     * @return a valid track (can't be NULL if libvlc_media_tracklist_count()
     * returned a valid count)
     */
    public static native libvlc_media_track_t libvlc_media_tracklist_at(libvlc_media_tracklist_t list, size_t index);

    /**
     * Release a tracklist
     *
     * @since LibVLC 4.0.0 and later.
     *
     * @param list valid tracklist
     */
    public static native void libvlc_media_tracklist_delete(libvlc_media_tracklist_t list);

    /**
     * Hold a single track reference
     *
     * @since LibVLC 4.0.0 and later.
     *
     * This function can be used to hold a track from a tracklist. In that case,
     * the track can outlive its tracklist.
     *
     * @param track valid track
     * @return the same track, need to be released with libvlc_media_track_release()
     */
    public static native libvlc_media_track_t libvlc_media_track_hold(libvlc_media_track_t track);

    /**
     * Release a single track
     *
     * @since LibVLC 4.0.0 and later.
     *
     * Warning: Tracks from a tracklist are released alongside the list with
     * libvlc_media_tracklist_delete().
     *
     * \note You only need to release tracks previously held with
     * libvlc_media_track_hold() or returned by
     * libvlc_media_player_get_selected_track() and
     * libvlc_media_player_get_track_from_id()
     *
     * @param track valid track
     */
    public static native void libvlc_media_track_release(libvlc_media_track_t track);

    // === libvlc_media_track.h =================================================
}
