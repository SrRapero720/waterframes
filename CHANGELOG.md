# UPDATE 2.1.22
- ✨ Added command `/waterframes edit <x> <y> <z> time <time_in_ms>` to set displays time (like the gui seekbar)
- ✨ Added command `/waterframes edit <x> <y> <z> pause <true/false>` to set the pause state
- 🛠️ Update watervision integration to support 0.1.0-alpha+
- 🛠️ Ported to fabric 1.21.5, 1.21.8 and 1.21.10
- 🐛 Fixed watervision integration wasn't working

# UPDATE 2.1.21
- 🐛 Fixed crashes when WATERViSION is not installed
- 🛠️ Marked WATERViSION as optional dependency

# UPDATE 2.1.20
- ✨ Added error status for videos (this will stop showing "working" when player fails to play)
- ✨ Update GUI textures (textures made by Kotyarendj)
- ✨ Re-enable VideoPlayer integration using [WaterVision](https://www.curseforge.com/minecraft/mc-mods/watervision)
- 🐛 Fixed `/waterframes` commands doesn't work on command blocks
- 🐛 Fixed Displays resets time and log spams mismatchs when a player reconnects or joins the chunk
- 🐛 Fixed lagTickCorrection causes time jumps on media players without tick compensation
  - This option will still cause small jumps, a proper fix will come with v3 update
- 🐛 [FABRIC] Fixed display packets are sent to players who are not tracking that chunk 

# UPDATE 2.1.15
- 🐛 Fixed race condition on texture registration (Crashing other mods)
- 🐛 Fixed ProjectionDistance rounds decimal values to integer
- 🛠️ Ported to 1.21.5 (by @linkpad)

# UPDATE 2.1.14
- ✨ Overhaul experimental playlist GUI layout and colors
  - ✨ Added a new button to "play next and previous"
  - ✨ Added a new button to "play and pause" media
  - ✨ Added a reload button on the loaded entry
  - ✨ Colorize active playlist media
- 🛠️ Using playlist, single pictures will be forced to be shown for 10 seconds
- 🐛 Fixed GUI was not rendered in non-singleplayer worlds

# UPDATE 2.1.13
- ✨ Added experimental "playListMode"
  - 🛠️ Can be enabled using `/waterframes experimentals playListMode <true/false>`
  - 🛠️ This will allow you to play a list of videos in a row
  - 🛠️ DO NOT USE IT ON EXISTING WORLDS
  - ✨ Remote buttons to "change channel" now are enabled and let you move up and down the list.
- ✨ Added shaderMode on displays gui
  - 🛠️ Enables extra-compatibility for some shaderpacks using Iris/Oculus
  - 🛠️ Its fully client-side config, it won't affect server-side
- 🛠️ [1.21.1 -> 4] Updated to the latest version of CreativeCore

# UPDATE 2.1.12
- 🛠️ Removed slavisim mode (not needed/working anymore)
- 🛠️ Ensure synchronized duration when vlc updates it (somehow for a few seconds VLC reports no duration after playing specific streams)
- 🐛 Fixed wrong distance calculation on projection distance (causes projectors to not render)

# UPDATE 2.1.11
- 🛠️ Removed hard-crash of Stellarity mod (wasn't fixed, but recent WF changes makes it immune)
- 🛠️ Ensure watermedia textures are not released by minecraft
- 🐛 Fixed rendering issues on swords and enchanted items
  - 🐛 At the cost of make images with smooth transparency get stepped-cut
- ✨ `ᔑ↸↸ᒷ↸ ⍑ᒷ∷𝙹ʖ∷╎リᒷ`

# UPDATE 2.1.10
- ✨ Remote Arrows can now switch picture position (for frames and projectors)
- 🛠️ Required watermedia 2.1.12 as minimum
- 🛠️ Removed VideoPlayer integration (until VP gets updated)
- 🐛 Fixed few rendering issues with texture transparency
- 🐛 Fixed crashes typing URLS when the whitelist is enabled
- 🐛 Fixed blocklight wasn't updated when option is disabled or media is updated

# UPDATE 2.1.9
- ✨ Added slavism mode configuration
  - ✨ Integrated in server-side config (waterframes.multimedia.slavismMode)
  - ✨ Added too a override config option on server-side config
  - ✨ Added command `/waterframes slavism <enabled/disabled>`
- 🛠️ Added `tenor.com` on the default whitelist
- 🐛 Fixed compatibility with latest version of creativecore

# UPDATE 2.1.8
- 🌐 Added translation
  - 🇧🇷 Brazil (by jmsgfhr)
- ✨ [1.19.2] Extended support for 2 months (QianFuv)
- ✨ Added compatibility with Forge Permissions API
  - ✨ Added config option to enable permission usage (disabled by default)
  - ✨ Permissions added
    - `waterframes.displays.save`: Enables you save urls or settings in any display
    - `waterframes.displays.interact`: Enables you interaction for any display (open gui)
    - `waterframes.displays.interact.frame`: Enables you interaction for frames
    - `waterframes.displays.interact.projector`: Enables you interaction for projectors
    - `waterframes.displays.interact.tv`: Enables you interaction for all TVs
    - `waterframes.remote.interact`: Enables you interact with remotes (open gui)
    - `waterframes.remote.bind`: Enables you binding remotes (open gui)
    - `waterframes.whitelist.bypass`: Enables you ability to bypass white/black list
- ✨ Added config "allowSaving", enables to users ability to save urls or settings
- ✨ Added config "blackWhitelist", inverts polarity of how a whitelist works
- ✨ Added config "usableRemote", enables to users ability to interact with the remote (open the gui)
- ✨ Added config "usableRemoteBinding", enables to users ability to bind remotes on any display
- ✨ Added sound on binding and unbinding remotes
- 🛠️ Added validator for whitelist hosts (invalid host must be removed)
- 🛠️ Added better error displaying when URL isn't able to load
- 🛠️ Set minimal required watermedia version to 2.1.6
- 🐛 Fixed users aren't able to save on any display when URL bar is empty
- 🐛 Fixed when permission to interact with displays isn't enabled, remotes aren't able to bind them
- 🐛 Fixed update display data resets time to zero when it has an URL
- 🐛 Fixed you won't need to crouch to bind unbinded remotes
- 🐛 Fixed mute button is not visible on remotes
- 🐛 Fixed audio position is displayed wronly on projectors

# UPDATE 2.1.7a
- 🐛 Fixed crashes when stellarity wasn't installed (it was supposted to be when is installed)

# UPDATE 2.1.7
- 🛠️ Updated to WaterMedia 2.1.x
- ✨ Added new status icons (Media loading error, buffering, warning, success via cache)
- ✨ Added back red URL input when URL is wrong (for local files, will appear red if file doesn't exist)
- ✨ Scrolling on the seekbar will fastfoward or rewind the time (5 seconds)
- ✨ Fixed "VLC fail to load" banner doesn't show up when VLC fails  

# UPDATE 2.1.6
- 🐛 Fixed crashes on newer versions of CreativeCore 

# UPDATE 2.1.5
- 🐛 Fixed default config regression
- 🌐 Updated translations (since 2.1.4)
  - 🍾 Russian updated by cutiegin
  - 🍾 Ukrainian created by cutiegin
  - 🍜 Japanese created by argentum-2503

# UPDATE 2.1.4
THIS UPDATE DROPS 1.18.2 AND 1.19.2 MAINTENANCE
- 🐛 Fixed wrong config validation
- 🐛 Fixed remote doesn't work on dedicated servers
- 🐛 Fixed long server boot times when WF is installed
  - 🛠️ This removes POWER and LEVEL blockstates in favor of runtime-tile calculation
  - 🛠️ Saves memory when WorldEdit is installed
- 🛠️ Breaking the TV Box with hand now gives you the TV Box
- 🛠️ **[FABRIC]** Improved stability
- 🌐 Updated translations
  - 🍜 Added simplified chinese by SanQianQVQ
  - 🍜 Traditional license was done by yichifauzi but last changelog says "simplified"

# UPDATE 2.1.3
- ✨ Click on a display using a remote (binded on that display) will pause it instead of open the Screen
  - ✨ This was added for since 2.1.0, but I forgot to add it on the changelog
- ✨ Updated compatibility for VideoPlayer 3.x
- 🐛 Fixed a small memoryleak on video textures (the small things always count)
- 🐛 Fixed displays have no sound when VSEureka is installed (my bad)
- 🐛 [1.20.1] Fixed items don't appear in creative tab
- 🛠️ Added a config option to disable VSEureka compatibility (in case of future breaking changes on VSEureka side)
- 🛠️ Removed Voxeloper texture pack
- 🛠️ Increased min size of off-screen rendering from 8 to 16
- 🛠️ Prevent duplicated entries on whitelist

# UPDATE 2.1.2
- 🐛 Fixed crashes... on both sides this time.

# UPDATE 2.1.1
- 🐛 Fixed crashes on server opening a display screen
- 🐛 Fixed changelog on modrinth
- 🛠️ Removed VideoPlayer fix mixins (the new update fixes the issue)

# UPDATE 2.1.0
It Will be a small update, I said... No many code changes will have, I said
## ✨ ENHANCEMENTS
- ✨ NEW: Box Television; Perfect to watch your local [villager news](https://www.youtube.com/watch?v=tFPcx4X9-e8)
- ✨ NEW: `/waterframes reload_all` command
  - ✨ Replaces "reload all" button in displays
- ✨ NEW: `/waterframes audit in_range` command
  - ✨ List all displays in a range of chunks
- ✨ NEW: Added option on screen to disable light on play (Closes #76)
  - ✨ Includes a config option forcing light on play (disabled by default)
- ✨ NEW: Added config option to use game master volume (Disabled by default) (Fixes #37)
- ✨ NEW: Big Television can ceil on walls horizontally
- ✨ Overhaul display screens, much smaller, compat and responsive
- ✨ Overhaul Remote control screen, even more small, compat and responsive
- ✨ Tweaked some screen icon textures
- ✨ Overhaul renderer of displays
  - ✨ Shaders (90% of them) not longer over-brights the images
  - ✨ Stellarity can't longer break pictures
    - 🛠️ Compatibility crash will stay in favor of [VideoPlayer](https://www.curseforge.com/minecraft/mc-mods/video-player) and [LittleFrames](https://www.curseforge.com/minecraft/mc-mods/video-player) which yet still breaks them
- ✨ RemoteControl now works when display is on a ValkirienSkyes ship
- ✨ Block light is now adjustable based on brightness level
- 🌐 Updated translations
  - 🍜 Added simplified chinese (by yichifauzi): was done for 2.0.14 so it might be outdated.
## 🐛 BUG FIXES
- 🐛 MINECRAFT FIX: Fixed releases the texture directly without call releaseId on AbstractTexture
  - 🛠️ This comes out by the renderer rewrite
- 🐛 FIX: Removed check for level nullability (fixes logs spam)
- 🐛 FIX: Cursed waterframes commands prefix
- 🐛 Fixed broken command responses
## 🛠️ CHANGES
- 🛠️ Moved ValkirienSkyes into a class compat (instead of a self injection Mixin)
- 🛠️ Hardcode op-permission-level check to level 4 instead of retrieve to server
- 🛠️ Volume calculations are now done by player's position in corner instead of center block
  - 🛠️ Revert of 2.0.14 calculations change.