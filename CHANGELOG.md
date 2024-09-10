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