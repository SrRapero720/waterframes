# WaterFrames Issue Investigation Tracker

Maintained by the bug-investigation agent. Updated continuously as issues are investigated.
Organized by root cause. Each entry: issue number, title, cause analysis, suggested fix/investigation path.

Current supported versions (as of 2026-07-12): Minecraft 1.20.1, 1.21.1, 1.21.11, 26.1.3, 26.2.x
(1.21.1 is the base version other ports derive from; 1.21.11/26.1.3/26.2.x are planned/in-progress;
1.21.5/1.21.8 are being abandoned going forward).

Code paths cited below are from the 26.2 (v2/VLC) checkout; released 2.1.x share the same logic
(line numbers differ slightly per version).

---

## WaterFrames bugs (mod's own code)

### #290 + #294 — Crash rendering/assembling a Create contraption containing a display  [CONFIRMED, WF-fixable]
- Crash (from #290 client crash report): `UnsupportedOperationException` at
  `Create VirtualRenderWorld.getChunk` <- `Level.blockEntityChanged` <- `DisplayTile.setDirty` <-
  `DisplayTile.handleUpdateTag` <- `Create ClientContraption.readBlockEntity`.
- Root cause: `DisplayTile.handleUpdateTag()` calls `this.setDirty()`, and `setDirty()` calls
  `level.blockEntityChanged()` + `level.sendBlockUpdated()` (DisplayTile.java ~357-378 here). Create
  renders/assembles contraptions inside a fake `VirtualRenderWorld` that does NOT support chunk
  lookups / block-entity change tracking -> throws -> crash. #294 "world bricks" = the assembled
  moving contraption re-reads the display BE on every load/render, re-crashing.
- Fix: `handleUpdateTag` (client receipt of synced BE data) should NOT call the full `setDirty()`.
  Just load data + request a render/model update; never call blockEntityChanged/sendBlockUpdated there.
  Optionally guard `setDirty()` against non-standard levels (isClientSide + level not a real ClientLevel).
- #294's ATTACHED mclo.gs log is a different "Mod Loading has failed / watermedia version unreadable"
  problem (likely wrong file) — but the described repro is unambiguously the #290 mechanism.

### #297 — Video playback rewinds when a player enters render distance  [CONFIRMED, reporter supplied fix]
- `Display.syncDuration()` (Display.java:168) sends `tile.syncTime(true, tile.data.tick, duration)` ->
  `TimePacket(pos, tick=local tick, tickMax=duration, bounce=true)` to the server on the first
  renderable tick. Server `TimePacket.exec()` does `tile.data.tick = this.tick` then bounces to ALL
  viewers, who `forceSeek()`. A late/lagging client's stale `tick` overwrites the authoritative server
  tick and drags everyone's playback backward.
- syncDuration's real job is to report `tickMax` (duration is only known client-side after decode); it
  must not send/overwrite `tick`. Reporter's fix (send tick placeholder -1, server ignores tick == -1)
  is valid. NOT fixed by 2.1.12 (VLC duration) or 2.1.20 (lagTick time-jumps) — different bugs.

### #279 — Audio stutter walking away from a MUTED frame  [CONFIRMED]
- `Display.tick()` recomputes distance-based `rangedVol(...)` every tick and calls
  `mediaPlayer.setVolume(...)` whenever the value changes — INDEPENDENTLY of mute state (Display.java
  185-187). Walking = volume changes every tick = continuous setVolume() calls on the VLC player, which
  glitches/stutters audio even though `muted` is set (mute only zeroes output, keeps decoding).
- Fix: when `tile.data.muted` (or master-mode), skip the rangedVol/setVolume path (or set 0 once).

### #239 — usePermissionsAPI=true crashes on right-click / while GUI open  [CONFIRMED; owner's "install LuckPerms" is WRONG]
- Crash: `NullPointerException` "PermissionAPI.activeHandler is null" at
  `PermissionAPI.getOfflinePermission` <- `DisplaysRegistry.getPermBoolean` <- `DisplaysConfig.canSave`
  <- `DisplayScreen.tick` (client thread).
- Root cause: `DisplayScreen.create()` (line 312) and `DisplayScreen.tick()` (line 395) call
  `DisplaysConfig.canSave(getPlayer(), url)` on the CLIENT. When usePermissionsAPI=true, canSave ->
  `getPermBoolean` -> NeoForge `PermissionAPI.getOfflinePermission`. The NeoForge permission handler is
  registered server-side (PermissionGatherEvent); on a client there is NO active handler ->
  activeHandler null -> NPE. LuckPerms on the server does not create a client-side handler, so the
  commenter WITH LuckPerms still crashed.
- Fix: never call the server-only PermissionAPI from client GUI code. Gate the client save-button
  enable on something client-safe (assume-allow; let the server-side `canSave` in `DisplayData.sync`
  reject), or null-check `PermissionAPI.getActiveHandler()` in `getPermBoolean` and fall back.
- Same latent trap in canInteractBlock/canInteractRemote/canBindRemote if reached client-side.

### #295 — Projector image vanishes when viewed head-on within ~9 blocks (both loaders)  [ROOT CAUSE IDENTIFIED, WF culling hooks]
- Reproduced on Fabric AND NeoForge -> core render logic. Angle-dependent disappearance = frustum
  culling. WF's `DisplayRenderer.shouldRenderOffScreen` returns false for projector-sized images
  (getWidth/getHeight are block fractions, ~never >16), so projectors rely on vanilla frustum culling
  via `getRenderBoundingBox`. The cull box is anchored at the projector block, extends along the facing
  axis by projectionDistance (default 8 ~= reported ~9), thin cross-section; when the camera aligns
  with the projection axis at ~projectionDistance the display gets culled, while off-axis it renders.
- Also `shouldRender` truncates `(int) tile.data.projectionDistance` (CHANGELOG 2.1.15 claimed to fix
  projectionDistance int-rounding but this call still truncates).
- Fix direction: make `getRenderBoundingBox` generously cover the projected quad region (or oversize
  it), and/or return true from `shouldRenderOffScreen` for projecting displays; stop truncating
  projectionDistance to int.

### #292 — [V3] waterframes 2.2.0-beta.5 fails to launch (Forge 1.20.1)  [CONFIRMED, WF build bug]
- Launch crash: `InvalidMixinException: @Shadow field pause was not located in the target class
  net.minecraft.client.Minecraft. No refMap loaded.`
- Root cause: the v3 build ships a mixin that @Shadows `Minecraft.pause` (game-pause detection, used by
  the pause / lag-tick features) but the mixin REFMAP is missing/unreferenced in the produced jar. In
  dev (Mojmap) `pause` resolves; in production (SRG names) it needs the refmap to map pause -> SRG ->
  "No refMap loaded" -> shadow fails. Pure build/packaging problem on the v3 line (mixin annotation
  processor / loom refmap not generated or not packaged). v2 mixin.json here declares no refmap and
  relies on the processor; the v3 beta build evidently produced a jar without it.
- Fix: ensure the mixin refmap is generated + packaged for the v3 build (loom/mixin AP config).

### #283 — Typing ":" or pasting in the URL field crashes (Forge 1.20.1)  [LIKELY ALREADY FIXED; needs version+log]
- No log, no version. Very likely the pre-2.1.10 "crashes typing URLs when the whitelist is enabled"
  bug (CHANGELOG 2.1.10). Mechanism: the GUI validates the URL every tick via `canSave` ->
  `WaterFrames.createURI` + `isWhiteListed`; a partial URL (e.g. "https:" right after ":") yields a
  null/host-less URI and the whitelist/permission path NPEs. Current code still has a latent variant:
  with usePermissionsAPI=true, `canSave` calls `isWhiteListed(uri)` with a possibly-null uri ->
  `uri.getScheme()` NPE.
- Action: ask for WF version + crash-report; recommend updating to latest. Harden createURI/isWhiteListed
  against null/partial input regardless.

### #270 — Freeze/crash with 10+ active displays nearby (Fabric 1.20.1)  [ROOT CAUSE PROBABLE; needs hs_err]
- No log. "GPU driver reset" on the weaker machine + hard crash = GPU/VRAM or native decoder
  exhaustion, not a Java logic bug. Each VIDEO Display spins up its own VLC player (Display.java:65) +
  its own dynamic GL texture; 10 identical videos = 10 independent decoders/uploads (no sharing for
  identical URLs on the video path; only still-image ImageCache is shared by URI). Weak GPUs reset
  drivers under the texture-upload load; strong machines OOM/crash.
- Fix direction: share decoders/textures for identical URLs, cap concurrent players, or (v3) unify.
- Action: request an hs_err_pid log to confirm OOM vs native GL crash.

---

## WaterMedia bugs (underlying media/API layer)

### #282 — SIGSEGV in Mesa libgallium after URL change on server (Fabric 1.20.1, Linux/AMD)  [CONFIRMED native crash]
- hs_err: SIGSEGV, problematic frame `C [libgallium-26.0.5-arch2.2.so]`, crashing "Render thread",
  stack = `GL11C.nglTexSubImage2D` <- watermedia `VideoPlayer` frame upload. Native GPU driver crash
  during video-frame texture upload (radeonsi/RX 5500 XT, Mesa 26.0.5). URL change (texture
  release+realloc while decoder keeps pushing frames) makes it more likely.
- Cause: watermedia VideoPlayer texture-upload path hitting a Mesa driver bug; not WF logic and not
  fixable from a Java SIGSEGV. Mitigations: try a different Mesa version, or watermedia should
  serialize texture realloc vs upload on URL swap. Env-dependent (Arch/CachyOS Mesa).

### #284 — YouTube "Sign in to confirm you're not a bot" (SignInConfirmNotBotException)  [NOT WF; dup of #255]
- Stack: `watermedia_youtube_plugin@2.1.2` shaded NewPipe `YoutubeStreamExtractor.checkPlayabilityStatus`
  -> `SignInConfirmNotBotException` LOGIN_REQUIRED. YouTube's anti-bot blocking anonymous extraction for
  that IP. Entirely upstream (YouTube) + the watermedia YT plugin / NewPipe extractor. WF just passes
  the URL. Recurs whenever YT tightens; fix depends on a YT-plugin/NewPipe update or a non-flagged IP.

### #298 — YouTube video loads forever (Forge 1.20.1, Linux/Bazzite)  [PROBABLE watermedia; log not retrievable]
- Distinct from #284 (no bot error; it just never finishes loading). Reporter uses watermedia +
  watervision (VLC VideoPlayer) + platform extension, VLC installed. Symptom = the resolved stream is
  never played (extractor hang OR VLC/ffmpeg can't open the resolved YT manifest on Linux). Points to
  watermedia/watervision playback side, not WF. Attached logs are on discord-paste (JS app) and could
  not be fetched. Action: get a plain latest.log + debug.log (or mclo.gs) to see whether the YT plugin
  resolves the URL and where playback stalls.

### #289 — Occasional silent client crash near TVs playing GIFs/videos (1.21.1 Fabric server)  [UNFIXED; diagnosis incomplete — see supplement]
- Owner diagnosed "race condition: server asks client to sync clock while the GIF is still loading."
  Plausible as a trigger, but: (1) no fix has been committed anywhere (26.2 log = porting only;
  2.1.20/2.1.22 sync work addressed time RESET/spam, not this crash) — still an OPEN diagnosis; and
  (2) the reported pattern (4-5s freeze then the JVM disappears with NO crash report) is characteristic
  of a NATIVE crash (watermedia/VLC or GL) during concurrent media-load + sync/seek, not a plain Java
  exception (which would produce a crash report). A Java-side sync guard alone may not fully fix it.
  Reporter confirmed removing WaterFrames stops the crashes, so it is WF-triggered.

---

## Third-party incompatibilities

### #281 — Dedicated-server crash opening a display GUI (Fabric 1.21.8, ABANDONED)  [CreativeCore sideness bug on abandoned MC]
- Server stack: `NoClassDefFoundError: net/minecraft/class_1113` (client SoundInstance) at
  `CreativeCore GuiClientRegistry.<clinit>` <- `GuiLayer$1.createDist` <- `GuiControl.<init>` <-
  WF `DisplayScreen.<init>` <- `DisplayBlock.create` (opening the GUI server-side via CreativeCore's
  GuiCreator.openGuiOnServer).
- Root cause is inside CreativeCore v2.14.7 (mc1.21.8): its GuiControl constructor touches the
  client-only `GuiClientRegistry` static-init (referencing a client sound class) on the dedicated
  server. WF only triggers it by opening its CreativeCore GUI (same flow works on 1.20.1/1.21.1).
- 1.21.8 is being abandoned; not actionable from WF. Report upstream to CreativeCore if it repros on a
  supported version.

---

## Needs more info from user

- #270 — request hs_err_pid log (confirm native-GL/VRAM vs OOM).
- #283 — request WaterFrames version + crash-report; likely fixed in 2.1.10, update first.
- #298 — request a fetchable latest.log/debug.log (discord-paste unreadable).

---

## Resolved / duplicate / non-actionable (kept for reference)

- #294 — duplicate root cause of #290 (Create contraption + DisplayTile.handleUpdateTag/setDirty).
- #284 — recurrence of closed #255; upstream YouTube anti-bot, watermedia YT plugin.
- #281 — abandoned MC version (1.21.8) + CreativeCore-side bug.
