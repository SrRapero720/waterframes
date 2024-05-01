# WaterFrames Feature Feasibility Tracker

Maintained by the feature-feasibility investigation agent. Tracks the technical feasibility of
planned/requested features against WaterMedia's actual public API surface, Minecraft's engine
constraints, and platform (Fabric/Forge/NeoForge) limitations. Updated continuously as new
feature-request issues are investigated.

Current supported versions (confirmed by maintainer, 2026-07-12): Minecraft 1.20.1, 1.21.1,
1.21.11, 26.1.3, 26.2.x (1.21.1 is the base port target; 1.21.11/26.1.3/26.2.x are planned but
not fully released yet; 1.21.5/1.21.8 are being abandoned). Requests for any other MC version
are a scoping decision by the maintainer, not a technical feasibility question.

Source of truth for the v3.0.0 roadmap: GitHub issue #73 ("[WF v3.0.0] Planned features").

**IMPORTANT CAVEAT — watermedia API generation varies per branch. CONFIRMED FINAL TABLE** (full
per-branch `git worktree` validation, complete):

| Branch | Loader/MC | watermedia generation |
|---|---|---|
| 1.20.1 (default) | Forge, 1.20.1 | **v3/FFmpeg** — vendored 3.0.0.18, WF modversion 2.2.0-beta.6 — **unreleased WIP** |
| 1.20.1-v3 | Forge, 1.20.1 | v2/VLC (2.0.70) — "v3" in the branch name is a red herring |
| 1.20.1-v3-yett | Forge, 1.20.1 | v2/VLC (2.1.22) — same red-herring naming |
| 1.20.1-fabric | Fabric, 1.20.1 | v2/VLC (2.1.34) |
| 1.21.1-fabric | Fabric, 1.21.1 | v2/VLC (2.1.34) |
| 1.21.1-neo | NeoForge, 1.21.1 | v2/VLC (2.1.34) |
| 1.21.5 | NeoForge, 1.21.5 | v2/VLC (2.1.34) |
| 1.21.8-fabric | Fabric, 1.21.8 | v2/VLC (2.1.34) |
| 26.2 (current) | NeoForge, 26.2 | v2/VLC (2.1.34) currently, mid-port to v3 |

`1.20.1-v3`/`1.20.1-v3-yett` are old, low-modversion, side/abandoned-looking branches — not part
of the maintainer's current supported set (1.20.1, 1.21.1, 1.21.11, 26.1.3, 26.2.x) — don't weight
them in feasibility calls.

**THE PRACTICAL RULE THIS DOCUMENT FOLLOWS: every actually-released, stable watermedia line is
uniformly v2/VLC, regardless of loader or MC version.** The only v3/FFmpeg build anywhere is the
unreleased 2.2.0-beta.6 WIP on the 1.20.1 default branch. **Default assumption for any incoming
feature-request issue that doesn't explicitly name a 2.2.0-beta build or the post-port 26.2 line:
assume the reporter is on v2/VLC.** A category verdict of "POSSIBLE" below describes feasibility
against the **v3 target architecture** (the correct lens for issue #73 itself, which is explicitly
the v3.0.0 roadmap) — but when triaging an individual issue that depends on a v3-only capability,
**that "POSSIBLE" is conditional on the reporter eventually being on a v3 branch, and should be
communicated as PARTIALLY POSSIBLE** ("yes, once the v3 update ships for your version — not yet on
what you're running today"), **not a flat "yes."**

Not every feature depends on a v3-only capability, though. Many verdicts below are pure
WaterFrames-side/Minecraft-side logic, or lean on functionality (play/pause/seek/stop, audio-only
playback, a texture/frame handle to render) that VLC-backed v2 almost certainly already provides
too — VLC has done these since long before this rewrite, and the WaterFrames architectural survey
directly confirmed several of them already working today on the (v2) 26.2 codebase. Those are
tagged **[GENERATION-INDEPENDENT]** below and keep a clean, unconditional POSSIBLE regardless of
which branch an issue targets. Anything tagged **[V3-DEPENDENT]** should get the conditional
downgrade above whenever an issue's target version/branch isn't confirmed to already be on v3.

I have not independently indexed the old v2/VLC API's exact capabilities/limits (no old-watermedia
source is checked out anywhere visible to me) — "almost certainly provides too" below is inference
from VLC's known long-standing feature set and from what the survey found already working on v2
today, not a line-by-line comparison against old watermedia's actual source. Treat
[GENERATION-INDEPENDENT] tags as high-confidence inference, not directly-verified fact, when it
matters for a specific issue.

Legend (feasibility verdict — the main POSSIBLE/NOT POSSIBLE tag on each heading):
- **POSSIBLE** — nothing in WaterMedia/Minecraft blocks it; may still be significant WaterFrames-side work.
- **PARTIALLY POSSIBLE** — blocked on a real prerequisite (not impossible, not buildable today).
- **NOT POSSIBLE** — a hard technical or third-party-architecture wall.
- **NOT PROMISED** — a scope decision, not a feasibility verdict.

Two more tags layer on top of that, both orthogonal to the feasibility verdict:
- **Implementation-state tag** (from the WaterFrames code survey): **ALREADY (PARTIALLY)
  IMPLEMENTED** (some/most already shipped, often behind a flag) vs. **NOT STARTED (confirmed)**
  (verified absent from the repo).
- **Generation-dependency tag** (from the branch table above): **[GENERATION-INDEPENDENT]** (safe
  regardless of v2/VLC vs v3/FFmpeg) vs. **[V3-DEPENDENT]** (needs the v3 rewrite specifically —
  apply the conditional-downgrade rule above for any issue not confirmed on a v3 branch).

---

## Core Features

### Upload images from local storage / server-side image hosting — POSSIBLE — [V3-DEPENDENT] — roadmap #73 + from issue #257
WaterMedia v3 ships a minimal built-in HTTP server (`org.watermedia.api.network.NetworkServer`,
token-authed `/upload` endpoint via `X-WaterMedia-Token` header, `network.maxUploadSizeMB` config,
default 8MB) that accepts raw file bytes and returns a short ID usable as a URI. I found no
equivalent confirmed in the old v2/VLC API (not independently indexed). WaterFrames still needs
its own client-side file-picker UI and item/data-model wiring either way. **For any issue not on
the v3 WIP/post-port line, treat as PARTIALLY POSSIBLE** ("possible once v3 ships") unless
WaterFrames builds its own upload channel independent of watermedia (plausible for the
single-player/same-machine case, which doesn't need a network upload primitive at all).
**Issue #257 maps here** (NOT to #296 — see the migration entry below; the two were briefly
conflated). #257's real ask (title "improvement") is server-side hosting: the reporter's images
"disappear at once depending on the connection," and they want to upload the image to the server by
link so the server hosts it instead of every client re-fetching the original source — exactly what
the `NetworkServer` upload channel provides. A commenter (TylorTwo) already correctly told them it's
coming with watermedia v3. Note: #257 also has a stray pasted comment containing #296's
littlepictureframes-migration text — that belongs to #296 and should be ignored when judging #257.

### Commercials — pre-defined sources every 3 min, configurable, owner-broadcastable, YouTube-only — POSSIBLE — [GENERATION-INDEPENDENT]
Pure WaterFrames-side scheduling logic: periodically swap the display's active source on a timer.
Every watermedia generation exposes some way to point a player at a new source and play it — this
doesn't depend on v3's specific `MediaAPI`/`MRL` class shapes, just on "can WaterFrames tell the
player to switch sources," which the mod already does today for normal URL changes on v2. YouTube
resolution itself (an ordinary video URL) already works on the current (v2) codebase.

### Multiple media sources (playlists), incl. YouTube playlist support — POSSIBLE — ALREADY PARTIALLY IMPLEMENTED — MIXED generation-dependency
Confirmed in the current (v2, 26.2) codebase: `DisplayData.java` already holds a
`LinkedList<URI> uris` + index alongside the single `uri` field, with `nextUri()`/`prevUri()`
cycling driven by the remote's channel buttons and a dedicated `PlayListScreen.java`
(add/remove/reorder), persisted as a newline-joined string. **The core playlist mechanism is
[GENERATION-INDEPENDENT]** — it's pure WaterFrames data/UI, already proven working today on v2, no
watermedia-generation dependency at all. It's just deliberately hidden: gated behind
`DisplaysConfig.useExperimentalPlaylistMode` (off by default, toggled via
`/waterframes experiments playlistMode <bool>`), and it's a flat list embedded per-display, not a
shareable/named `Playlist` object — no shuffle, no per-item duration metadata (stills hardcoded
10s each).
**What IS likely `[V3-DEPENDENT]`:** auto-expanding a single "YouTube playlist" URL into all its
video entries. On v3, `YtDlpPlatform` already distinguishes a bare playlist link from a
video-with-`&list=` link (`--no-playlist` flag) and flat-parses entries (`--flat-playlist`); I
could not confirm whether the old v2 API already shells out to yt-dlp the same way for this
specific case (plausible it does something cruder, or nothing). Treat "auto-expand a YouTube
playlist link" as conditional-on-v3 for a v2 target unless proven otherwise; treat "let me manually
add N URLs to a playlist" as already-working today regardless of generation.

### Rewrite recipes to be much harder — POSSIBLE — [GENERATION-INDEPENDENT]
Pure data/JSON recipe changes. No engine dependency whatsoever, no watermedia touch at all.

### Shutdown displays in a radius (PEM) — POSSIBLE — [GENERATION-INDEPENDENT]
Pure WaterFrames-side: scan a block radius and call the existing stop/pause on each display's
player instance. Every generation supports stopping/pausing playback — this is core functionality
in both the v2 VLC-backed player and v3's `MediaPlayer`.

### Override URLs of displays (HACK) — POSSIBLE — [GENERATION-INDEPENDENT]
Pure WaterFrames-side: re-point the URL/source a display block uses to a new value — the
fundamental "change what a display is showing" action every generation must support, since it's
literally what the mod already does for a normal edit.

### Interference — Creaking heart nearby, Pale biome momentary signal loss, Nether radioactive-biome interference — POSSIBLE — [GENERATION-INDEPENDENT]
Purely a WaterFrames-side simulated effect: temporarily blank the texture, drop frames, or swap to
a "static/no signal" placeholder based on biome/mob-proximity checks each tick. Never touches the
media pipeline at all, so it's identical to build on either generation.

---

## Item Features

### USB — item storing media sources (uploaded + online, playlist support) — POSSIBLE — NOT STARTED (confirmed) — MIXED generation-dependency
Confirmed: no USB/storage item exists anywhere in the current codebase (only item is
`RemoteControl.java`). The "stores online sources + playlist" half is **[GENERATION-INDEPENDENT]**
(same NBT/data-component storage as the already-working playlist prototype). The "stores an
uploaded local file" half is the same **[V3-DEPENDENT]** upload-primitive question as "Upload
images from local storage" above — apply the same conditional treatment for a non-v3 target.

### Batteries — FE storage, 3 variants (Disposable/Rechargeable/Creative) — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: zero Forge Energy integration anywhere in the repo (grep for energy/ForgeEnergy/
IEnergyStorage = no matches); the only "power" concept implemented today is redstone. Pure Forge
Energy capability API (a Minecraft/NeoForge system) — zero watermedia dependency of any generation.
**Precedent:** maintainer already explicitly rejected "wireless" battery charging in the #73
discussion — flag any future wireless-charging request as already-decided, not a fresh feasibility
question.

### Tablet — handheld display item for images/video, requires batteries, lectern-placeable — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: no tablet/handheld display item exists today. Rendering a media texture from a held
item is **not actually v3-gated** — the current (v2) rendering path
(`client\rendering\RendererWrapper.java`, wrapping a block entity's `ImageRenderer` as an
`AbstractTexture`) already proves texture-from-watermedia-handle rendering works fine on the
generation running today; a Tablet just needs the same underlying technique triggered from an
item/GUI context instead of a block entity. Genuinely new code either way, but not blocked on v3.

### Remote Control rework — requires batteries, per-display-specific, "focus" mode, validates dimension — POSSIBLE — ALREADY PARTIALLY IMPLEMENTED — [GENERATION-INDEPENDENT]
Confirmed: the current `RemoteControl.java` item already does per-display binding (crouch+use binds
to one display via a `RemoteData` component storing dimension+xyz; crouch+use again unbinds) and
already validates dimension + range before opening `RemoteControlScreen` — on the current v2
codebase, today. So "per-display-specific remotes, not cross-usable" and "validates display
dimension" are **already shipped behavior**. What's genuinely missing — the battery requirement
(blocked on the Batteries item above not existing yet) and the Create-like "focus" mode — is pure
WaterFrames/Minecraft-side work, not gated on watermedia generation at all.

### Smart Remote Control — universal, opens display screen remotely, syncs playlists, admin command variant — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: the current remote is single-bind only (one remote = one display, no multi-channel/
universal mode, no lectern placement, no playlist sync, no admin command variant) — fully
greenfield relative to today's code, but all the work is WaterFrames-side networking (Minecraft
packet layer) and data modeling. WaterMedia is not involved in remote-to-display sync at all,
regardless of generation.
**Community demand confirmed:** issue #245 independently requests "one remote = multiple displays"
— same ask as this roadmap item, from a user building a large venue (church) wanting one remote to
drive many displays at once.

---

## Block Features

### Inclined TV (15° angle variant) — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: current blocks are `FrameBlock`/`ProjectorBlock`/`TvBlock`/`BigTvBlock`/`TVBoxBlock`, all
built on a shared `DisplayBlock` base with a `DisplayCaps.java` capability-flag record (5 static
instances: renderBehind/projects/resizes/box-shape) — no tilt/incline support exists, only fixed
facing + attach-face combos. The existing capability-driven pattern (new block class + new
`DisplayCaps` instance) is exactly the kind of extension point this would use. Whatever texture
handle either watermedia generation exposes can be rendered at any angle — the quad's angle in the
world is entirely WaterFrames' own rendering code, unrelated to watermedia generation.

### Projector's projection restricted to nearest block (config, default on) — POSSIBLE — [GENERATION-INDEPENDENT]
Pure raytrace/collision logic in WaterFrames, no media API involvement at all.

### Modular redstone input — scriptable Play/Pause/Stop/Reset/Seek/FastForward/Rewind/Shutdown, per-display scripts — POSSIBLE — BASIC VERSION ALREADY IMPLEMENTED — [GENERATION-INDEPENDENT]
Confirmed: `DisplayBlock.java` already has working redstone input today, on v2 — `canConnectRedstone`
(facing-side only) + `neighborChanged` reads `hasNeighborSignal`, updates the `POWERED` blockstate,
and pauses/unpauses via `tile.setPause()`, gated by `useRedstone` (default on) and
`useMasterModeOnRedstone` (default off). That's binary pause/resume only, not the "modular,
scriptable, per-action" richness #73 asks for. Play/pause/stop/seek/fast-forward/rewind are all
core VLC functionality that has existed for decades, so the richer action set is very likely
already available on v2's `VideoPlayer` too, not just v3's `MediaPlayer` (unconfirmed exact method
shape on the old API, but low-risk). The remaining work is genuinely just WaterFrames-side: extend
the existing redstone hook from a single pause toggle into a signal-strength/pattern-to-action
mapping, plus a per-display script data structure.

### Multi-option redstone output via comparators — Media Time, Playlist position — POSSIBLE — BASIC VERSION ALREADY IMPLEMENTED — [GENERATION-INDEPENDENT]
Confirmed: `DisplayBlock.java` already implements `hasAnalogOutputSignal=true` with
`getAnalogOutputSignal` delegating to `DisplayTile.getAnalogOutput()`, computed from tick/tickMax
scaled to 1-15 (0 when inactive) — on the current v2 codebase, today. A comparator output already
exists and works, just for one fixed metric (playback progress). The "multi-option" ask is really
"let the player choose which metric drives the existing comparator hook" — a small, well-scoped
extension of an already-working, generation-independent feature.

### New block: Projector Squared — renders on all horizontal faces, resizable/rotatable/transparentizable, audio — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: only a single-face `ProjectorBlock` (horizontal-facing, one projection direction) exists
today — no all-faces variant. Bigger rendering-scope item than the other new blocks (multi-face
rendering is new territory vs. the existing single-quad-per-block model), but binding the same
texture handle to multiple quads per frame doesn't care which watermedia generation produced that
handle — no generation-specific blocker either way.

### New block: Old CCTV (#107) — modern TV reskin, 1:1 aspect ratio, 2 styles — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT]
Confirmed: no CCTV-style block exists. Pure reskin/model work reusing the same rendering mechanism
that already renders `TvBlock`/`BigTvBlock` today on v2. Side note: a `GOLDEN_PROJECTOR` block is
already registered in code but commented out in `DisplaysRegistry.java` (dormant, never shipped) —
not a #73 item, but worth knowing about in case a future issue references "the gold projector."

### New block: Hacking laptop — shutdown/override URLs in radius, admin command variant — POSSIBLE — [GENERATION-INDEPENDENT]
Combination of the PEM-shutdown and URL-override primitives above, packaged as an item/block —
both already established as generation-independent.

### New block: Radios — audio-only, no remote support, USB-only, status overlay — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT] (likely)
Confirmed: no audio-only block exists today (all current blocks are display/video-first). On v3,
`MediaPlayer`/`MediaAPI.createPlayer` explicitly support a null `GFXEngine` (audio-only playback) as
a first-class documented mode. Audio-only playback (no video output) is completely standard,
long-standing VLC functionality, so this is very likely equally available on v2's `VideoPlayer` —
just not directly confirmed against the old API's exact method shape.

### Increase Projector max view/projection distance — POSSIBLE, LARGELY ALREADY CONFIGURABLE — [GENERATION-INDEPENDENT] — from issue #245
Requester is building a large venue (church-scale room) and hit the "64-block" limit. **Confirmed
from source (`DisplaysConfig.java`): both relevant caps are already server-config values that go far
past 64 today** — `maxRenderDistance` (default 64, `defineInRange` **max 512**) controls how far the
display keeps rendering (`DisplayRenderer.java` does `closerThan(cameraPos, renderDistance)`), and
`maxProjectionDistance` (default 64d, **max 512**) controls how far a projector throws its surface.
So the church builder can already raise both to 512 in the server config right now — no code change
needed for anything up to 512. Code work is only warranted if someone needs *beyond* 512, or hits
Minecraft's own block-entity render-distance / frustum culling at long range (a solvable, well-
understood rendering concern — e.g. widening the tile's render bounding box / exempting the quad from
normal distance culling), not a hard wall. Practical answer to the reporter: bump the config first.

### Multi-display "link"/group sync — one display's source change propagates + synced playback across a linked group — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT] — from issue #245 (comment)
Distinct from Smart Remote Control above (that's remote-to-display; this is display-to-display).
Conceptually a generalization of a problem WaterFrames already has to solve today: keeping
multiple *clients* watching the same single display in sync. Extending that to a group of *display
block entities* sharing one logical "channel" (a group/channel ID field + packet propagation: the
"master" display's source/seek changes broadcast to every linked display, each independently opens
the same source and seeks to the same synchronized time) is the same shape of problem, just at the
block-entity level instead of the per-client level. Moderate WaterFrames-side networking/data-model
scope, no watermedia blocker on either generation — each linked display still runs its own decoder
instance under the hood.

### Join/merge adjacent Frame blocks into one combined display — POSSIBLE — NOT STARTED (confirmed) — [GENERATION-INDEPENDENT] — from issue #271
No watermedia blocker: sharing one decoded texture across several quads, each sampling a different
UV sub-rectangle of the same texture (a standard "video wall" tiling technique), is pure
WaterFrames rendering code — the same underlying idea already noted for Projector Squared above,
just applied to detected-adjacent Frame blocks instead of one block's multiple faces. Would need
adjacency/alignment detection (matching facing + contiguous grid) and a group-owner/data-linking
model, similar in spirit to other multiblock-detection patterns.
**Important existing-capability nuance, re-confirmed directly from source:** `DisplayCaps.java`
gives `FRAME` and `PROJECTOR` `resizes=true` (the 3rd record boolean; `TV`/`BIG_TV`/`TV_BOX` are
`false`), and the width/height fields (driven by `DisplayScreen`) are clamped only by
`DisplaysConfig.maxWidth`/`maxHeight` — **both default 40, `defineInRange` max 256**. So **a single
Frame can already be resized up to 256×256 blocks** via its own screen, no multi-block joining
needed. This very likely already solves the reporter's underlying goal ("one big display") today —
the practical first answer is "resize one frame" (raise `maxWidth`/`maxHeight` in config only if they
hit the 40 default). True multi-block *joining* (independently placed, seamlessly-tiled separate
frames sharing one video via per-frame UV sub-regions) is a distinct, still-feasible ask on top —
only worth the work if per-tile placement flexibility is specifically wanted over one big quad. True multi-block *joining* (physically separate, independently
placed/removable frames that look seamless together) is a distinct, still-feasible ask on top of
that — mainly useful if the reporter specifically wants per-tile placement flexibility rather than
one large single-block-entity quad — worth clarifying which one is actually wanted before scoping
new work.

---

## Screen Redesign

### Tabbed UI — Media (playlist/USB/file selector), Rendering (alpha/brightness/rotation/projection distance/image+audio position/volumes), Permissions (per-player edit auth), Aspects (cosmetics/textures) — POSSIBLE — MOST OF THIS ALREADY EXISTS, JUST NOT TABBED — [GENERATION-INDEPENDENT]
Confirmed: today's `DisplayScreen.java` — on the current v2 codebase — is a single non-tabbed
(CreativeCore `GuiLayer`) screen that **already exposes almost every property #73 lists**:
URL/playlist-button, width/height + aspect-quick-resize, anchor position + flip X/Y, rotation,
alpha/brightness, render distance, projection distance + audio offset (projector only),
mirror/both-sides + show-model/lit (frame only), shader-mode toggle, playback controls, volume +
min/max falloff distance, seek bar, cache-reload, WaterVision popout, Save. `PlayListScreen.java`
(Media tab content) and `RemoteControlScreen.java` already exist as separate purpose-built screens
too. This is one of the **least** v3-dependent items on the whole roadmap — it was found already
~90% built on the OLD (v2) API, so the "Rendering" and most of the "Media"/"Aspects" tabs are
fundamentally **a reorganization of existing, already-working fields into tabs**, not new
capability, and entirely independent of which watermedia generation is running.
The one genuinely new piece is **Permissions**: today's permission system (`DisplaysConfig.java`,
NeoForge Permissions API / LuckPerms nodes + a config-gate fallback, plus a separate URL
whitelist/blacklist) is server-wide/admin-configured, not a per-display, in-GUI, player-editable
ACL — there's no existing UI where a player standing at a display grants/revokes *another specific
player's* edit access to *that specific display* (today's per-tile "last editor" UUID is audit-only,
not an ACL). This would be new data-model work layered on top of the existing global
permission-node system — but it's pure Minecraft-side ACL logic, so it's generation-independent
too.

---

## QoL Features

### Custom advancements — crafting, playlists, USB via books, watching commercials, hacking/PEM'ing — POSSIBLE — [GENERATION-INDEPENDENT]
Pure Minecraft data-pack/advancement-trigger work, zero watermedia touch.

### Edit displays from inventory without placing — POSSIBLE — [GENERATION-INDEPENDENT]
Pure WaterFrames GUI-without-blockentity work — same texture-in-item-context reasoning as the
Tablet item above (the existing v2 `RendererWrapper`/`ImageRenderer` path already proves this is
achievable regardless of generation).

### Create mod compatibility (play media / open screens on assembled contraptions) — PARTIALLY POSSIBLE — [GENERATION-INDEPENDENT]
Depends entirely on Create's own contraption-rendering hooks/API surface, not on watermedia at all
— whichever generation WaterFrames is running has no bearing on whether Create's moving-block-entity
renderer can host our texture. Needs a dedicated look at Create's API once a concrete issue comes
in; not resolvable from WaterMedia's side alone either way.

### Native OS file picker for local media (button that opens Explorer/Finder instead of hand-typing a `water://local/` path) — POSSIBLE — [GENERATION-INDEPENDENT] — from issue #183 (cross-posted from watermedia#142)
Distinct from #73's "Upload images from local storage" (that's about network-shareable upload for
multiplayer; this is a same-machine convenience for a scheme that already works today). **Best route
confirmed: `org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog` — LWJGL is already bundled
with Minecraft (the tinyfd module + natives ship in the vanilla LWJGL distribution and are on the
classpath), so an OS-native open-file dialog is reachable client-side with ZERO new dependencies.**
Many mods (ReplayMod etc.) already call it. Preferable to `javax.swing.JFileChooser`/AWT `FileDialog`
because AWT and GLFW both fight over the main thread on macOS — TinyFileDialogs sidesteps that.
Confirmed via grep: no file-picker code (`tinyfd`/`JFileChooser`/`FileDialog`) exists in the repo
today, and the `water://local/` scheme parsing lives in watermedia's MRL layer, not WaterFrames — so
this is purely a new WaterFrames-side client button. Also likely fixes the reporter's "no spaces or
special characters" complaint as a side effect: constructing the URI from the picked path via
`File.toURI()` auto-escapes spaces/special characters correctly, instead of the hand-built string the
current manual-entry path uses. Pure client GUI work, zero watermedia dependency of any generation.
Already tagged "good first issue" in the tracker.

### Migration tool/command from LittlePictureFrames (~100 photos, manual re-entry unrealistic) — PARTIALLY POSSIBLE (realistic, with a caveat) — [GENERATION-INDEPENDENT] — from issue #296
Realistic in principle: NBT/world-data migration tools between similar block-entity mods are a
well-established Minecraft-modding pattern (scan for the other mod's block+data, translate to
WaterFrames' own block+data schema), and this has zero dependency on watermedia's API or
generation at all — it's pure world/NBT manipulation. Two real prerequisites/unknowns, not
watermedia-related: (1) needs LittlePictureFrames' actual data schema, which I have not
researched; (2) the reporter says the two mods "conflict with each other" — if that means they
can't be loaded simultaneously, a live in-game "migrate now" command won't work (both mods' block
classes would need to be loaded at once to read old + write new in a single pass), and the
realistic shape becomes a standalone/offline converter that reads the world's region/NBT files
directly instead of an in-game command. Worth surfacing this shape question before committing to
an implementation approach.

---

## Considered / Rejected in issue #73 (precedent — do not re-litigate without genuinely new info)

### Earbuds — NOT PROMISED
Maintainer: "maybe a separate mod instead." A scope decision, not a rejection on technical grounds.

### Sound Physics Remastered (SPR) compatibility — NOT POSSIBLE — [GENERATION-INDEPENDENT]
Maintainer-confirmed impossible: SPR exposes no contact/extension channel for another mod to hook
into, independent of anything WaterMedia could add on its own side. This was already blocked even
under the WATERMeDIA v3 OpenAL plan — the wall is SPR's own architecture, not our engine choice, so
it's equally impossible on v2 or v3. Check any future SPR-adjacent request against this before
treating it as new.

### Speaker block / spatial (3D positional) audio — PARTIALLY POSSIBLE (blocked on a prerequisite) — NEITHER GENERATION HAS IT
Confirmed directly from the v3 engine code: `SFXEngine`/`ALEngine`
(`org.watermedia.api.media.engines.{SFXEngine,ALEngine}.java`) currently expose only flat gain
(`volume()`) and pitch (`speed()`) per source — there is no `AL_POSITION`/`AL_VELOCITY`/listener-
orientation API anywhere in the engine today; each player gets one non-positional OpenAL source.
**This is not simply a "wait for v3" situation** — even the newest (v3, unreleased) engine doesn't
have spatial audio, so upgrading a user to v3 alone would not unlock this. Maintainer: needs "a
proper sound engine to process audio in java" (plus SPR-compat groundwork) — i.e. dedicated
**post-v3** work — before spatial audio specifically can be discussed. Not impossible, but
genuinely not buildable on either generation without that prerequisite landing first.

### Mute Minecraft background music when displays are nearby — POSSIBLE — [GENERATION-INDEPENDENT]
Entirely client-side Minecraft option/mixin territory; no watermedia dependency of any generation.

### Data-driven rendering — resourcepacks customizing picture position/rendering — POSSIBLE — [GENERATION-INDEPENDENT]
WaterFrames-side data-driven config over whatever texture handle exists; no generation-specific
blocker.

### Villagers — new village job/trades around WF stuff — POSSIBLE — [GENERATION-INDEPENDENT]
Pure Minecraft content.

### Enchantments — RC enchants for radius/battery duration — POSSIBLE — [GENERATION-INDEPENDENT]
Pure Minecraft enchantment API content.

### Overlays — small GUI on image renderer (RC-controllable): "no signal" text, volume bar, channel index, etc. — POSSIBLE — [GENERATION-INDEPENDENT]
Pure WaterFrames GUI/HUD rendering layered over whichever generation's texture handle is active.

---

## Version / porting & compat requests (precedent)

### Port to MC 1.21.11 — PLANNED (on roadmap) — from issue #276 — NOT_PLANNED_VERSION: no
1.21.11 IS in the maintainer's supported set (1.20.1, 1.21.1, 1.21.11, 26.1.3, 26.2.x) — ported
outward from the 1.21.1 base, not released yet. This flips the older near-identical #264 (closed as
not_planned): treat 1.21.11 as planned now, not rejected. Sub-note: the "and 1.21.8???" comment on
#276 is a different version — 1.21.5/1.21.8 are being abandoned, so 1.21.8 specifically is
NOT_PLANNED, but that doesn't change the issue's headline (1.21.11 = planned).

### watermedia v3 compatibility — YES, INTENDED & IN PROGRESS — from issue #285
Not a compatibility risk: watermedia and WaterFrames are the same dev, so v3 is a planned migration,
not a third-party break. Confirmed by the branch table above — the 1.20.1 default branch already runs
fully on watermedia v3 (vendored 3.0.0.18, WF 2.2.0-beta.6 WIP); other versions follow. A commenter
already answered #285 correctly with commit `18b6c92`. Just needs a short confirming reply.

## Threading / performance precedent (apply to future "reduce stutter / TPS impact" requests)

**[GENERATION-INDEPENDENT ARGUMENT, V3-SPECIFIC CITATIONS.]** Confirmed directly from the v3 engine
code: `FFMediaPlayer` already runs a dedicated lifecycle + demux + video-decode + audio-decode
thread set **per player** (decode pool sized to `ThreadTool.halfThreads()`), coordinated by a
lock/condition-based `MasterClock` with PTS-drift recalibration (`DRIFT_RECALIBRATION_THRESHOLD`
etc). Decode and A/V timing math are already fully off Minecraft's main thread on v3. The
underlying **argument almost certainly holds on v2/VLC too** — libVLC has always decoded and timed
playback on its own native/internal threads, decoupled from the calling Java thread, so "WaterFrames
is an orchestrator, not a decoder" is true regardless of generation — but the specific mechanism
names cited here (`ThreadTool.halfThreads()`, `MasterClock`, `tickTimeCorrection`) are v3
implementation details and may not exist verbatim on v2 (unconfirmed against old watermedia's
source). Either way, WaterFrames' own main-thread work is orchestration only — sampling the texture
handle on the render thread, redstone I/O, blockstate updates — and moving *that* off the main
thread would need custom tick timing plus atomic/volatile synchronization, for a game-mechanics
cost the maintainer has judged isn't worth the smoothness gained. The existing `tickTimeCorrection`
server config (v3; unconfirmed exact equivalent on v2) already compensates for low-TPS timing skew;
a user-reported ~2s lag before it kicks in after a TPS dip is a tuning gap in that existing feature,
not evidence the fundamental off-main-thread architecture is missing.

---

## WaterMedia API index (reference — how the v3 feasibility calls above were derived)

**This index describes watermedia v3 only** — the target architecture issue #73 is written
against, and what's confirmed running (as an unreleased WIP) on the 1.20.1 default branch. It is
**not** what any currently-released branch calls today: every actually-released branch (including
the currently checked-out `26.2`) runs old v2/VLC, whose integration points are
`org.watermedia.api.image.*` (`ImageAPI`/`ImageCache`/`ImageRenderer`), `org.watermedia.api.math.MathAPI`,
and `org.watermedia.api.player.videolan.VideoPlayer` (VLC/videolan4j-based), called from
`DisplayTile.java`, `client\display\Display.java`, `client\rendering\RendererWrapper.java`,
`DisplaysRegistry.java`, `WaterFramesCommand.java`, and `DisplayScreen.java`. I have not
independently indexed that old API's capabilities/limits (no old-watermedia source is checked out
anywhere visible to me) — every `[GENERATION-INDEPENDENT]` tag above is high-confidence inference
(from VLC's known feature set and from what's already confirmed working on v2 today), not a
line-by-line comparison against old watermedia's actual source. If a specific issue's feasibility
hinges on an old-API-specific limitation rather than a general engine/platform constraint, flag it
as unverified rather than asserting confidently either way.

- **Media backends:** FFmpeg (via bytedeco/javacpp) for video/audio, with optional hardware decode
  (CUDA, QSV, AMF, D3D11VA, D3D12VA, VideoToolbox, VAAPI, VDPAU, DRM, MediaCodec, Vulkan generic
  path). Hand-written pure-Java image codec stack: PNG/APNG, GIF, WebP, NETPBM (PNM/PBM/PGM/PPM/PAM),
  JPEG, SVG — plus an optional BC1/BC3/BC7 GPU-compressed texture cache layer for animated images.
- **Rendering pipeline:** `GFXEngine` abstraction over GL and Vulkan backends. Exposes a texture
  handle (`long` — GLuint or VkImageView) to the consumer mod; WaterMedia only manages decode +
  texture upload, **not** the actual in-world quad/face rendering — that responsibility belongs
  entirely to WaterFrames. Supports multi-plane uploads (YUV420/422/444[+10/12/16-bit], NV12/NV21,
  YUVA+alpha), a frame-texture preload mode for small animations, and direct compressed-texture
  upload. A texture handle can be sampled/bound to multiple quads per frame with no WaterMedia-side
  restriction (relevant to Projector Squared).
- **Threading model:** Each `MediaPlayer` (FFmpeg-backed) spawns its own lifecycle/demux/decode
  threads; nothing touches Minecraft's main thread except texture-handle consumption on render and
  whatever WaterFrames itself does for redstone/blockstate. `ServerMediaPlayer` is a headless,
  audio/video-free "clock" player (ticks every 50ms on one shared daemon thread) used purely for
  server-side time sync — literally the "time orchestrator" the maintainer described.
- **Audio:** `SFXEngine` abstraction, two backends: `ALEngine` (OpenAL/LWJGL — mono through 7.1,
  U8/S16/FLT/DBL, pitch + gain control) and `JSEngine` (javax.sound.sampled fallback — mono/stereo
  only, U8/S16 only, no pitch control, no source handle). Neither exposes 3D/spatial positioning.
- **URL/file loading:** `MRL` resolves a URI asynchronously via (1) a dispatch table of dedicated
  platform extractors — YouTube, Twitch, Kick, TikTok, Twitter/X, BiliBili, Bluesky, Streamable,
  Sendvid, Odysee, DTube, Imgur, Lightshot, MediaFire, Dropbox, Google Drive, PornHub, Medal, VidLii
  — plus a generic `YtDlpPlatform` fallback, or (2) direct HTTP with content-type/magic-byte
  sniffing and HLS/IPTV `.m3u`/`.m3u8` playlist parsing. `MatureContentException` +
  `allowMatureContent` config gate adult-content sources.
- **Local file upload:** `NetworkServer` — minimal built-in HTTP server, token-authed `/upload`,
  `network.maxUploadSizeMB` cap (default 8MB).
- **Relevant config knobs:** `media.tx.texturesBudget` (VRAM budget for preloaded animated
  textures), `media.txCodecCache` (BC7/3/1 compression cache toggle), `decoders.ffmpeg.hardwareAccel`,
  `decoders.maxImageSourceBytesMB`, `network.maxUploadSizeMB`, `network.token`.
- **MediaPlayer control surface directly usable by WaterFrames:** start/startPaused/pause/resume/
  stop/togglePlay/seek/seekQuick/skipTime/previousFrame/nextFrame/forward/rewind/speed(0-4x)/
  volume/mute/repeat/quality(with automatic closest-match)/lod(5 tiers, distance-based
  resolution)/maxSize cap.

---

## Still to investigate / open threads

- Old watermedia (v2/VLC, versions 2.0.70 through 2.1.34 depending on branch) API surface — not
  indexed (no source available to me). Only matters if a specific issue's feasibility hinges on an
  old-API-specific limitation rather than something already covered by a
  `[GENERATION-INDEPENDENT]` inference above.
- Create mod's contraption-rendering API surface — only needed once a concrete Create-compat issue
  arrives.

## DONE

- **Branch-to-watermedia-generation table** — complete (see confirmed table near the top of this
  document). Headline: every released branch is v2/VLC; the only v3/FFmpeg build anywhere is the
  unreleased 2.2.0-beta.6 WIP on the 1.20.1 default branch; `26.2` (current branch) is v2/VLC,
  mid-port to v3.
- **WaterFrames current-implementation survey** — complete, folded into every relevant entry above
  with "ALREADY (PARTIALLY) IMPLEMENTED" / "NOT STARTED (confirmed)" tags, and used to correct
  several v3-only assumptions (Tablet, Radios, redstone I/O, Tabbed UI, Remote Control rework all
  turned out to be generation-independent or already-partially-built once checked against the real
  code, not the v3-only assumptions I started with). Headline findings:
  - Blocks: `DisplayBlock` base + `FrameBlock`/`ProjectorBlock`/`TvBlock`/`BigTvBlock`/`TVBoxBlock`,
    driven by a `DisplayCaps` capability-flag record. No remote-control block, no inclined variant.
    A `GOLDEN_PROJECTOR` block is registered but commented out/dormant.
  - Items: only `RemoteControl.java` exists (single-display bind, dimension-validated). No USB,
    battery, tablet, or any Forge Energy integration anywhere in the repo.
  - Screens: three non-tabbed purpose-built `GuiLayer` screens (`DisplayScreen`, `PlayListScreen`,
    `RemoteControlScreen`) already cover nearly every property #73's tabbed redesign lists.
  - Redstone I/O: already fully wired (input pause/resume, comparator output from playback
    progress) — just not "modular/scriptable" or "multi-option" yet.
  - Permissions: already a dual system (NeoForge Permissions API/LuckPerms nodes + config-gate
    fallback) plus a URL whitelist/blacklist — but server-wide/admin-configured, not an in-GUI
    per-display per-player ACL.
  - Playlists: a rudimentary flat per-display version already exists, gated behind an experimental
    config flag, off by default.

---
_Last updated: 2026-07-12 (batch pass: #183 TinyFileDialogs route confirmed; #245 render/projection
distance confirmed already config-capped to 512; #257 correctly attributed to server-side upload
hosting, un-conflated from #296; #271 single-frame resize confirmed to 256×256 via DisplayCaps
FRAME resizes=true + maxWidth/maxHeight; #276 1.21.11 = planned; #285 v3 compat confirmed via
1.20.1 branch already on v3. Earlier: downgrade pass — branch table, [GENERATION-INDEPENDENT] vs
[V3-DEPENDENT] tags, corrected Tablet/Radios/redstone I/O/Tabbed UI/Remote Control rework)_
