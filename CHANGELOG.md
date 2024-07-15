# UPDATE 2.0.12
- 🍾 Added russian translations (by the translator Heimdallr-1)

# UPDATE 2.0.11
- 🐛 Fixed when server is lagging media playing rollbacks in time
- 🐛 Fixed pause is triggered too late when game is paused
- 🐛 [1.21] Fixed recipes aren't working
- 🐛 [1.21] Fixed `/waterframes` is not working
- 🛠️ Get blockpos from block instead of ticker (may solves VS Eureka compat issues) 
- 🛠️ Moved all packets sending into one channel

# UPDATE 2.0.10
- 🐛 Fixed crash on server-side using `/waterframes whitelist` 

# UPDATE 2.0.9
- ✨ Increased max limit of some config fields (`waterframes-server.toml`)
  - 📐 Max width: 128.0 -> 256.0
  - 📐 Max height: 128.0 -> 256.0
  - 👁️ Max render distance: 128 -> 512
  - 📽️ Max projection distance: 128 -> 256
  - 🔊 Max audio distance: 256 -> 512
- 🛠️ Delete NBT entry for model visibility in favor of the blockstate
- 🛠️ Added ``DisplayCaps`` to specify the renderBox and the capabilities of the display
  - 🛠️ This removes all abstract methods on BlockEntity classes
- 🐛 Fixed crashes by the GPU memory usage optimization (backToRAM)

# UPDATE 2.0.8
- ✨ Enhanced GPU memory usage for single-use images
  - 🛠️ This changes releases GPU memory (VRAM) and stores back to RAM 
  - 🛠️ Due to limitations on WATERMeDIA (planned to solve on v3) WF can only optimize pictures with only one usage (1 display for 1 URL)
- ✨ Image position selection box is now drag and drop
  - 🛠️ Now you can click and slide your mouse as a madman to everywhere
- 🛠️ Bumped the minimal version required of WATERMeDIA required to 2.0.54

# UPDATE 2.0.7
- ✨ Added `/waterframes give`
  - 🛠️ Can specify an player selection or nothing (to give you)
  - 🛠️ Reminder: `/waterframes` command is restricted in general just to op players (and the owner of the mod)
- ✨ Added `/waterframes whitelist`
  - 🛠️ Can toggle, add, and remove urls
  - 🛠️ Due to some Forge skill issues, users need to leave and join the server to refresh
- 🐛 Fixed wrong value type on RC
  - 🛠️ Type was change from long to int
  - 🛠️ Added a data fixer for old value type (logs a warning)
- 🛠️ Tweaked some GUI textures

# UPDATE 2.0.6
- 🐛 Fixed wrong 0.0 to 1.0 value on brightness and alpha, (now is range of 0 to 255)
  - 🛠️ Command, Gui and Data is updated
- 🐛 Fixed KeepRendering config wasn't working
- ✨ Blocklight is disabled when Displays have no URL
  - 🛠️ Light can be completely disabled on config
- 🛠️ TV GUI uses less height (feel it less empty)

# UPDATE 2.0.5
- 🐛 Fixed crashes by the lastest update (caused by making redstone working again)
- 🛠️ Bumped the minimum watermedia version to 2.0.50

# UPDATE 2.0.4
- 🐛 Fixed redstone and model hiding wasn't working (blockstates aren't updated)
- 🐛 Fixed wrong twitch url on default config (by [herronjo](https://github.com/SrRapero720/waterframes/pull/62))
- 🛠️ Renamed `waterframes-client-new.toml` to `waterframes-client.toml`
  - 🛠️ This was a horrendous mistake
- 🛠️ Renamed `waterframes-server-new.toml` to `waterframes-server.toml`
  - 🛠️ This was a horrendous mistake

# UPDATE 2.0.3
- 🐛 Fixed crashes loading worlds with older versions of the mod also with images
- 🐛 Fixed changing brightness or transparency causes other stuff getting obscured (view model arm or entities)
  (AGAIN)
- ✨ Reinforce mute state on loading worlds and on pause/resume
- 🛠️ Added better explanation for 🌌 Stellarity crash

# UPDATE 2.0.2
- 🐛 Fixed changing brightness or transparency causes other stuff getting obscured (view model arm or entities)
- 🐛 Fixed crashes when data on RC is invalid (now should show "Something goes wrong!")
- 🐛 Fixed redstone output (comparator) never got updated and always was 1 or 14
- 🐛 Fixed redstone input never unpauses the display or updates the "powered" block state
- ✨ Optimized performance on large amount of frames (get display is no longer synchronized)
- ✨ Added a "mute" icon state on the volume bar when RC mutes display
- ✨ Updated wording on en_us translation
- ✨ Updated es_mx translation to 2.0 texts
- 🛠️ Removed obsolete es_es translation

# UPDATE 2.0.1
**NON-ENGINNERS HAVE PROBLEM WITH THE 2.0**
- 🐛 Fixed hard dependency on Create Mod (by accident)

# 🎉 RELEASE 2.0 🎉
**THIS CHANGELOG CONTAINS EVERYTHING DONE FROM 1.3.x TO 2.0.0**

- ## 📽️ | NEW BLOCK: PROJECTOR
  - ✨ Let you project distanced pictures and videos
  - ✨ Can configure the audio source of the video from image or centered with the block and the image
  - 🧱 Includes a fancy model to ceiling it upward (model made by FabiAcr and J-RAP, texture by Kotyarendj)
  - 🛜 Have a max-range of 64 blocks (configurable)
- ## 📺 | NEW BLOCK: TELEVISION
  - ✨ Cannot be resized, rotated and picture can't be repositioned
  - 🧱 Includes a fancy models to ceiling it into too many sides (model made by FabiAcr and J-RAP, texture by Kotyarendj)
- ## 📺 | NEW BLOCK: BIG TELEVISION
  - ✨ Cannot be resized, rotated and picture can't be repositioned
  - 🧱 Only had one design in different horizontal rotations, but it IS HUGE (model made by J-RAP, texture by Kotyarendj)
- ## 🎮 | NEW ITEM: REMOTE CONTROL
  - ✨ Let you control any display (Frame, Projector, TV, Big TV) from far away
  - ✨ Can turn off, mute, volume up/down, pause, play, stop, rewind, fast-forward or reload the display
  - 🛜 Have a range of 32 blocks (configurable)
  - 🧱 Model made by J-RAP, texture by Kotyarendj
- ## 🎛️ | REVAMPED THE WHOLE DISPLAYS SCREEN
  - ✨ Added a small status icon indicating whatever was the state of the current display
  - ✨ Picture position is now a big selection area (no longer need to click buttons many times)
  - ✨ Now you have a seekbar to change the time for your videos or gifs
  - ✨ Loop button is now a playback action and get sync whithout save
  - ✨ Mute and Turn-Off states can be restarted by clicking on SAVE
  - ✨ Reload is now disabled when URL is different from the active media
  - ✨ Added compatibility with VideoPlayer (by Goedix) adding a new button on display screens to play the media fullscreen
  - ✨ All displays share the same GUI, making all features common to each other
- ## 🌐 | GENERAL IMPROVEMENTS
  - ✨ SNEAK + CLICK will now flip the direction of all displays except FRAMES
  - 🛠️ Now you can do your own frames! Internal rewrote the whole block system into an abstraction layer
    - It Is easier now made your own blocks and tiles having custom properties and rendering sizes
    - Data is fully abstracted, and easier to synchronize
  - ✨ Max volume can now be configurable to the max value 120 (default: 100)
  - ✨ Frame texture is revamped by Kotyarendj
  - ✨ New command: ``/waterframes``
    - ``/waterframes audit ~ ~ ~`` lets you find who put that URL in the frame (soon more audit capabilities)
    - ``/waterframes edit ~ ~ ~`` lets you modify the attributes of the frame such as the rotation, volume distances or sizes
  - ✨ Optimize performance when media is paused or turned off
  - ✨ Added a loading gif rendered when the media is buffering or loading (customizable)}
    - You can add your own loading gif placing the gif into ``config/watermedia/assets/waterframes/loading.gif`` (powered by WATERMeDIA)
  - 🛠️ Reduced stack size to 16
  - 🛠️ All displays can be removed faster using a pickaxe
  - 🛠️ Now requires the right tool to get the block
  - 🛠️ Updated crafting recipe for each display (all display requires the RC in the craft)
- ## 🟥 REDSTONE HANDLING
  - ✨ Now displays (using comparators) emits signal
    - level 0 when have not a URL
    - level 1 when have a URL and time is near to 0
    - level 1 to 15 when have a URL and the time is between 0 to the media duration
  - ✨ Redstone inputs will pause the media and can be overridden by RC and screen controls (configurable)
- ## 🛠️ | BUGS FIXED
  - 🐛 Position of the image in some directions is inverted (RIGHT position goes to the LEFT)
  - 🐛 Redstone handling never works even if it was enabled in config
  - 🐛 JEI overlaps with WaterFrames screens (fixed by CreativeMD, backported by me)
  - 🐛 **[1.20.1]** Some block entities get obscured when you change the brightness of any display
  - 🐛 Whitelist wasn't working properly on player with no OP permissions
  - 🐛 Permissions wasn't working on the client-side (letting you save but not really saving)
  - 🐛 Updating videos to another video makes it play with the time of the old video
  - 🛠️ incompatibility with Stellarity (now crashes the game instead of make picture buggy)
  - 🛠️ [EXTERNAL FIX]: fixed crashes by VideoPlayer about StackOverflowError 