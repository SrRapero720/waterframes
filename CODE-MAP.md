# CODE-MAP: WaterFrames

Reference map of this repository for agents. Read it before every task and keep it current: when a task changes
behavior, API, layout or rules, update the matching section (never leave it describing the old code).
Mapped on 2026-11-01 against `3.0.0-alpha.1` (`ALPHA`) on branch `1.21.1-neo` (Minecraft 1.21.1, NeoForge 21.1.244,
WaterMedia 3.0.0, WaterUI 1.0.0, Java 21 toolchain, Gradle 9.6). Symbol names are exact; line numbers are omitted on purpose.
Legend: ⚠ gotcha or hard rule, ↪ reuse pointer ("to do X use Y").

## 0. Project declarations

- **Changelog: YES.** `CHANGELOG.md` at the root follows the user ruleset "CHANGELOG AND VERSIONING": one `# 📦` header
  per version, no blank lines between entries, entries ordered ⚙️ ✨ 🛠️ 🐛 🌐, non-technical wording. `getChangelogText()`
  in `build.gradle` cuts the file at the first empty line, so versions are separated by exactly one blank line.
  The changelog stays uncommitted on code changes and only lands in version bump commits together with the `modversion`
  and `versiontype` lines of `gradle.properties`.
- **Versioning:** `modversion=x.y.z-<type>.w` (`3.0.0-alpha.1`), `versiontype` one of `ALPHA`, `BETA`, `STABLE`.
- **Special rules of this project**
  - Two mods in one jar: `waterframes` (`me.srrapero720.waterframes`, side BOTH) and the client toolkit `waterui`
    (`me.srrapero720.waterui`, side CLIENT). ⚠ INVARIANT: `grep me.srrapero720.waterframes` under `waterui/**` stays
    empty; WaterFrames extends the toolkit from `client/ui/**` and registers its tags in `ScreenWidgets`.
  - `UI-SPEC.md` is the normative format of the `.ui` documents; the code homologates to it. Behaviour added by the
    implementation ahead of the spec is logged in `UI-OUTSPEC.md` (Spanish) until the owner folds it into the spec.
    ⚠ Never edit `UI-SPEC.md` without the owner's permission.
  - ⚠ No `@OnlyIn` anywhere: dist safety rests on `@EventBusSubscriber(value = Dist.CLIENT)` listeners and on client-only
    method bodies that have no server caller (`DisplayTile.openScreen`, `DisplaysRegistry.registerTexture`).
  - Saved worlds matter: the five block entity types (`waterframes:frame|projector|tv|big_tv|tv_box`) and the
    `DisplayData` NBT keys are compatibility surfaces even though the code is ALPHA.
  - WaterMedia jars are vendored in `libs/` (`local:` flatDir group) until 3.0.0 is published; its sources live at
    `A:\dev\java\watermedia` with their own `CODE-MAP.md` (read §7 and §8 for the player and engine APIs).
  - The server never decodes media: a `ServerMediaPlayer` clock per display is the time authority and the clients follow
    its snapshots (`MediaSyncPacket`). Every client originated packet is gated server-side by `DisplaysConfig.canControl`.
  - Run `gradle compileJava` after code changes, `gradle removeSemicolonSpace` before finishing, `gradle build` only
    before a commit or release. The game runs with `gradle runClient` (run dir `run/`).

## 1. Repository layout

| Path | Content |
|---|---|
| `src/main/java/me/srrapero720/waterframes/` | `WaterFrames` (entry), `DisplaysConfig`, `DisplaysRegistry`, `common/` (block, data, entity, network, commands, item, compat, util/geo), `client/` (display, rendering, sound, ui) |
| `src/main/java/me/srrapero720/waterui/` | The toolkit: `core`, `layout`, `format`, `theme`, `widget`, `screen` |
| `src/main/java/dev/ryanhcode/sable`, `org/valkyrienskies` | Compile-only stubs of third party APIs, excluded from the jar |
| `src/main/resources/assets/waterframes/` | `ui/**` (`.ui` documents, themes, icon table), `textures/screen_atlas.png`, blockstates, models, lang, sounds |
| `src/main/resources/assets/waterui/lang` | Toolkit strings (`waterui.gui.*`) |
| `src/main/resources/META-INF/` | `neoforge.mods.toml`, `accesstransformer.cfg` |
| `src/main/resources/data/waterframes/` | Recipes and loot tables |
| Root files | `CHANGELOG.md`, `UI-SPEC.md`, `UI-OUTSPEC.md`, `CODE-MAP.md`, `README.md`, `FEATURES.md`, temporary reports (`ISSUE-73-PLAN.md`, `MEDIA-REDISENO.md`, `OVERLAYS-INGAME.md`, `SANITIZACION.md`) |

## 2. Blocks, tiles and geometry (`common/block/**`, `common/util/geo/**`)

### `DisplayCaps` (enum, `common/block/data`)
- The five kinds `FRAME`, `PROJECTOR`, `TV`, `BIG_TV`, `TV_BOX`; one flag table drives the shared block, tile and renderer.
  Constructor order: renderBehind, projects, resizes, hideModel, solid, flipPlacement, growSize, facing.
  - `FRAME`: renderBehind, resizes, hideModel, `FACING` (six directions), growSize 0.001.
  - `PROJECTOR`: projects, resizes, hideModel, `HORIZONTAL_FACING`, growSize 0.999 (historical value, keep it).
  - `TV`, `BIG_TV`: flipPlacement, `HORIZONTAL_FACING`, growSize 0.001. `TV_BOX`: solid, flipPlacement.
- Public API: flag getters, `growSize()`, `facing()`, `permission()` (frame/projector/tv nodes), `projectionDefault()`
  (`FRAME_THICKNESS`, `DisplaysConfig.maxProjDis(8f)`, 0), `box(tile, direction, attachedFace)` (render box),
  `shape(state)` (hitbox), `invertedFace(tile)` and `growMax(tile, facing, inverted)` (TV hung on its back), static
  `basicBox(tile)` (frame and projector picture from `DisplayData.min/max` and the facing), `CODEC` (lowercase names).
- Every switch over the enum is exhaustive: a new kind fails to compile until every switch is filled.
- ⚠ `TV_BOX.shape` is a full cube while its `box` is a window on the front face. ⚠ `AlignedBox.scale(1.01f)` on the TV
  hitbox scales around the block origin.
- ↪ To add a kind: add the enum row, fill `permission()`, `projectionDefault()`, `box()` and `shape()`, register its
  block, item and tile type in `DisplaysRegistry`.

### `DisplayBlock` (final, `common/block`)
- One `BaseEntityBlock` for every kind; `caps` is the only per-kind data. `of(caps)` is the only factory: it writes the
  static `building` slot, builds, clears it. ⚠ `Block`'s constructor builds the state definition and default state
  before any field exists, so `createBlockStateDefinition` and `registerDefaultState` read `building`, never `this.caps`.
- State: `ATTACHED_FACE` (clicked face) on every kind, `VISIBLE` (property name `frame`) only when `hideModel`,
  `POWERED`, `WATERLOGGED`, plus the caps facing.
- Placement: frames take the clicked face; the rest take the horizontal direction (reversed while sneaking) and TV kinds
  reverse it again (`flipPlacement`). ⚠ `rotate`/`mirror` change only the facing, `ATTACHED_FACE` stays as placed.
- Rendering and collision: `getRenderShape` is INVISIBLE when `hideModel && !VISIBLE`; `solid` drives full-block
  collision, shade brightness 0.2 and piston NORMAL (DESTROY otherwise); `getLightEmission` reads `tile.getLightLevel()`.
- Interaction: a `RemoteControl` bound to this block toggles playback on the server after `canControl`, and sparkles on
  the client; anything else opens the screen through `DisplayNetwork.openScreen` after `canInteractBlock`.
- Redstone: `canConnectRedstone` on the facing side; `neighborChanged` stores `POWERED` and, server side, pauses on the
  rising edge (`data.redstonePaused = true`) and resumes on the falling edge only when that flag is set or master mode is
  on. Comparator output comes from `tile.getAnalogOutput()`.
- `newBlockEntity` uses `DisplaysRegistry.tileType(caps)`; the ticker calls `DisplayTile.tick()` on both sides.

### `DisplayTile` (`common/block/entity`)
- The one block entity. Server: owns the WaterMedia `ServerMediaPlayer` clock and ships its snapshots. Client: owns the
  `Display` follower. Fields `data`, `caps`, `mrl`, `display`.
- Server clock: `requestClock()` runs every tick, keyed on `url#source`; a changed key releases the old clock, opens a new
  one (`repeat(loop)`, `start()`) and adds the tile to `SESSIONS`; no url releases it. Hold: `paused || !active` pauses
  the clock, else resumes it. `ship()` sends the `MediaSyncPacket` on every `revision()` change and every
  `SYNC_HEARTBEAT_TICKS` (20). A non-looping clock that `ended()` advances the playlist and restarts.
  `report(duration, live)` accepts only the first report of a session and ignores durations under `MIN_REPORT_MS`.
  `restartClock()` keeps the instance (viewers compare revisions). `cleanClock()` releases and forgets.
- `holdSessions(hold)` (static, server thread through `IntegratedServer.execute`): the singleplayer pause menu freezes
  ticks but clocks run on wall time, so every running session is paused and shipped at once and remembered in `HELD`;
  on resume exactly those are resumed and shipped again (a display outside simulation distance never ticks).
  `releaseSessions(level)` runs on `LevelEvent.Unload`.
- Client: `requestDisplay()` rebuilds `mrl` when the url changes and drops the player when the url or the source index
  changes; it creates `Display` once `mrl.status().loaded()` and replays `pendingSync` into it. `sync(byte[])` stores the
  snapshot and forwards it to `follower()`; `loadAdditional` clears `pendingSync` when the media key changed (the
  server sends the data before the new clock speaks). `reload()` refetches the MRL and drops the player.
  `openScreen(remote)` is client-only with no server caller.
- Controls: `setActive`, `setMute`, `setPause`, `setStop`, `volumeUp/Down` (5 steps), `nextUrl`, `prevUrl`, `loop`,
  `seek(ms)`, `skip(delta)` (`SKIP_MS` = 5000), `position(h, v)`. ⚠ The `clientSide` flag is a literal, not a side check:
  true sends to the server, false applies on the server and pushes to the chunk.
- `tick()` also derives the light level (`lit && active && hasUrl`) and the comparator level (1..15 over the clock) and
  refreshes neighbours only when either changed.
- Persistence: `saveAdditional`/`loadAdditional` delegate to `DisplayData`; `getUpdateTag` is the full tag;
  `handleUpdateTag` is not overridden (a client receiving data must never `setDirty`).
- ↪ Server-side change: mutate `data`, call `setDirty()`; the clock reconciles on the next tick.

### `DisplayData` (`common/block/data`)
- All persisted and synced settings plus the playlist. NBT keys: `url`, `uri_list`, `uri_index`, `player_uuid`, `active`,
  `min_x/min_y/max_x/max_y`, `rotation` (resizes only), `render_distance`, `flip_x/flip_y`, `alpha`, `brightness`,
  `volume`, `volume_min_range`, `volume_max_range`, `paused`, `redstone_paused`, `muted`, `lit`, `loop`, `render_both`
  (renderBehind only), `projection_distance`, `audio_offset` (projects only), `data_v` (`V = 2`; v1 scaled floats, v0 the
  legacy `minx`/`maxx`/`flipX`/`render`/`visibleFrame`/`bothSides` keys). Screen patch keys: `width`, `height`,
  `pos_x`, `pos_y`, `visible`.
- Playlist: `Entry(url, source, owner)` record (`sameMedia` compares urls, `same` adds the source); `playlist`,
  `entryIndex` and the `url` field mirror the entry on air; `entry()`, `getUrl()`, `getSource()`, `setUrl` (clears the
  list), `nextUrl`/`prevUrl` wrap. `composePlaylist`/`decomposePlaylist` write one line per entry
  (`url`, `url<TAB>owner`, `url<TAB>owner<TAB>source`) and dedupe on `url#source`. `listTag(list, index)` feeds
  `DataListSyncPacket`.
- Server writes: `sync(tile, player, nbt)` applies only the keys present, every value through its config clamp (alpha
  and brightness 0..255, finite rotation), caps-gated keys ignored; `syncList` refuses lists over `MAX_PLAYLIST` entries
  or `MAX_PLAYLIST_BYTES`, checks `canSave` on every line, and never trusts owners from the wire (known entries keep
  their owner, new ones get the sender). Both answer a refusal with `reject` (red action-bar line + `setDirty()` so the
  sender snaps back to the authoritative data).
- `Patch` builder: setters write only what differs from the tile (floats within 1e-4), `empty()`, `nbt()`.
- Audio position: `audioOffset` 0 = BLOCK, `projectionDistance` = PROJECTION, half = CENTER; `getAudioPosition()` uses
  exact float equality. ⚠ Send the audio position together with a projection change.
- ⚠ `sync` writes `data.url` directly; with a playlist the entry wins, so the screen always sends the list first.

### Position enums (`common/block/data/types`)
- `AudioPosition {BLOCK, PROJECTION, CENTER}`, `PositionHorizontal {LEFT, RIGHT, CENTER}` (`left()`/`right()` cycle),
  `PositionVertical {TOP, BOTTOM, CENTER}` (`up()`/`down()` cycle). ⚠ Ordinals travel on the wire; `enumOf` clamps them.

### Geometry (`common/util/geo`)
- `Axis {X, Y, Z}` (`one()`/`two()` perpendicular pair, `vanilla`), `Facing` (vanilla order, `axis`, `positive`,
  `normal`, `opposite()`, `rotation(radians)` is a `Quaternionf` around the normal), `AlignedBox` (mutable float box,
  `min/max(axis)`, `edge(facing)`, `size`, `corner`, `scale`, `aabb(pos)`, `shape()` unclamped), `BoxCorner` (eight
  corners, `faces(Facing)`), `BoxFace` (`of(Facing)`, `texU`, `texV`, four `corners` counter-clockwise).
- ↪ Draw one face: `BoxFace.of(facing)`, iterate `corners` through `box.corner(...)`, UVs from `texU`/`texV`.

## 3. Entry, config, registry, network, commands, items

### `WaterFrames`
- `@Mod(ID)` entry: `ID`, `NAME`, `LOGGER`, `asResource(path)`, `isInstalled(modId)`, `isValidUrl(url)` (existing local
  file or `://`), `getDistance(tile | level + pos, position)` (Sable, then Valkyrien Skies, then plain), `deltaFrames()`.
  ↪ Every reach check goes through `getDistance`.

### `DisplaysConfig`
- Server keys (section.key default, getter): `rendering.maxWidth` 40 `maxWidth()`, `maxHeight` 40, `maxRenderDistance`
  64 `maxRenDis()`, `maxProjectionDistance` 64 `maxProjDis()`, `keepRendering` true `keepsRendering()`;
  `multimedia.maxVolumeDistance` 64 `maxVolDis()`, `maxVolume` 100 (10..100, WaterMedia clamps gain at 100) `maxVol()`,
  `masterVolume` false, `vsEurekaCompat` true, `sableCompat` true; `block_behavior.lightOnPlay` true,
  `forceLightOnPlay` false, `redstone.enable` true `useRedstone()`, `redstone.masterMode` false
  `useMasterModeRedstone()`; `remote_control.distance` 32 `maxRcDis()`; `permissions.usePermissionsAPI` false,
  `usableInAdventureMode` false, `usableInSurvivalMode` true, `usableForAnyone` true, `allowSaving` true,
  `usableRemote` true, `usableRemoteBinding` true; `permissions.whitelist.enable` true, `blacklistMode` false, `urls`
  (34 hosts, `HOSTS_PATTERN`). The one-argument overloads (`maxVol(int)`, `maxWidth(float)`, ...) clamp a value and
  refuse NaN, infinities and negatives.
- Client keys: `overrideConfig.enable`, `overrideConfig.keepRendering`, `overrideConfig.shaderMode` (`shaderMode()`,
  `shaderMode(boolean)` saves), `soundEngineIntegration` true (`soundIntegration()`).
- Permission model: `isOwner` (hard-coded maintainer names, kept on purpose), `isAdmin` (owner, op 4, or the singleplayer
  owner on both sides), `canSave(player, url)`, `canInteractBlock(player, block)`, `canInteractRemote`, `canBindRemote`,
  and `canControl(ServerPlayer, tile)`: same level, within `max(maxRcDis, reach + 3)`, then `canInteractBlock` or a
  bound remote in either hand with `canInteractRemote`. ⚠ With the permissions API on, every `can*` answers
  optimistically on the client (the handler only exists on the server). Nodes: `waterframes.displays.save|interact|
  interact.frame|interact.projector|interact.tv`, `remote.interact`, `remote.bind`, `whitelist.bypass`.
- Whitelist: `isWhiteListed(url)` (off → true, `water://` → true, no host → false, host suffix match honours
  `blacklistMode`), `addOnWhitelist` (validates against `HOSTS_PATTERN`, false otherwise), `removeOnWhitelist`,
  `toggleWhitelist`.

### `DisplaysRegistry`
- Blocks (`frame`, `projector`, `tv`, `big_tv`, `tv_box` = `DisplayBlock.of(caps)`), items (`remote` + one `BlockItem`
  each), five tile types (`TILE_*`, all `DisplayTile`; `tileType(caps)` maps a kind to its holder; kept per kind so saved
  worlds keep their displays), the `display` sound event, the `remote` data component (`CodecManager` codecs), the
  creative tab, the permission nodes, commands, `LevelEvent.Unload` → `DisplayTile.releaseSessions`.
- `ClientEvents` (Dist.CLIENT): `ScreenWidgets.register()` at client setup, `DisplayRenderer` for the five tile types.
  `registerTexture`/`unregisterTexture` wrap the `TextureManager` (client callers only).

### `DisplayNetwork` and packets (`common/network`)
- `VERSION = "6"`: bump on any payload or codec change. Payload ids `waterframes:<name>` via `type(name)`.
- Server authoritative. `onControl` (bidirectional): clientbound runs `apply` then `applyClient`; serverbound runs
  `allowed` (`DisplaysConfig.canControl`, one WARN per server life) then `apply`, `setChanged` and a chunk broadcast.
  `onData` (client → server) runs `allowed` then `applyServer`. `onMediaSync` (server → client) feeds `tile.sync`.
  `onMediaReport` (client → server) requires the sender to track the chunk. `onOpenScreen` opens the GUI client side.
  ⚠ `tile(pos, context)` never loads a chunk (`level.isLoaded` first): a forged position cannot make the server generate
  terrain.
- Senders: `openScreen(player, pos, remote)`, `sendChunk(level, pos, payload)`, `sendServer(payload)`,
  `sendClient(controlPacket, tile)` (server origin: apply, mark changed, broadcast).
- `ControlPacket` (`pos`, `apply(tile)`, `applyClient(tile)`): `ActivePacket`, `LoopPacket` (also `clock.repeat`),
  `MutePacket` (client `setMuteMode`), `NextPacket`/`PreviousPacket` (playlist step + `restartClock`), `PausePacket`
  (`paused`, `rewind` = stop; master redstone forces paused; a resume clears `redstonePaused`), `PositionPacket`
  (ordinals, -1 keeps the axis), `SeekPacket` (clock `seek`/`skipTime`, client `seekQuick` for instant feedback),
  `VolumePacket`, `VolumeRangePacket` (min clamped to 0..max).
- `DataPacket` (`applyServer(tile, player)`): `DataSyncPacket` → `DisplayData.sync`, `DataListSyncPacket` →
  `DisplayData.syncList`. Others: `MediaSyncPacket(pos, byte[])` (`PlayerState` bytes, never read by the mod),
  `MediaReportPacket(pos, duration, live)`, `OpenScreenPacket(pos, remote)`.

### `WaterFramesCommand`
- `/waterframes` needs `hasPermissions` (non-players, op 3 or owner name). `edit <pos> url|position|size|rotation|
  alpha|brightness|renderDistance|projectionDistance|volumeDistance|volume|pause|time` (every value clamped by the
  config like a screen edit; `size <w>` alone keeps the height; `url` replaces the playlist and restarts the clock),
  `audit author|in_range <chunks>`, `give [targets]`, `whitelist add|remove|toggle`. Client: `reload_all` →
  `DisplayList.reloadAll()`.

### `RemoteControl`, `RemoteData`, `CodecManager` (`common/item`)
- Sneak-use on a display binds (`canBindRemote`), use opens the remote screen within `maxRcDis` in the same dimension
  (`canInteractRemote`; the range is checked before the chunk is touched), sneak-use in the air unbinds, a dead binding
  unbinds itself. Static `bound(stack, level, pos)`. `RemoteData(dimension, x, y, z)` with `RemoteData(level, pos)`,
  `pos()`, `matches(level, pos)`; persistent and stream codecs in `CodecManager`.

### Compat (`common/compat`)
- `SableCompat` / `VSCompat` (distance through sub-levels and ships), `WVCompat` (WaterVision screen hand-off from the
  Media tab). The stub sources under `dev/ryanhcode` and `org/valkyrienskies` are compile-only and excluded from the jar.

### Build and metadata
- `build.gradle`: moddev plugin, `local:` flatDir for `libs/watermedia-3.0.0.jar` (tracked) and
  `libs/watermedia_binaries-3.0.0.jar` (copied by hand, ignored), WaterVision `compileOnly` from cursemaven,
  `processResources` expands every gradle property into `neoforge.mods.toml` and `pack.mcmeta`, `publishMods`
  (CurseForge `834607`, Modrinth `eBzFuVTM`, `dryRun = false`), `getChangelogText()`, `removeSemicolonSpace`.
- `accesstransformer.cfg` opens `SoundEngine`/`ChannelAccess`/`ChannelHandle` internals for `DisplaySound`,
  `Minecraft.gameThread`, `BlockEntity.level`, `Entity.level`, `Block.registerDefaultState`, `Blocks.never`, the
  `RenderStateShard` constants. ⚠ A Minecraft update must re-check these first.
- `neoforge.mods.toml`: `waterframes` BOTH, `waterui` CLIENT (`displayTest=IGNORE_ALL_VERSION`); `watermedia`
  required BOTH, ordering AFTER.

## 4. Client display, render and sound (`client/display`, `client/rendering`, `client/sound`)

### `Display`
- `AbstractTexture` registered once per block position (`display_at_x_y_z`); owns the follower player created with
  `PlayerAPI.player(mrl, sourceIndex, () -> PlayerAPI.glEngine(gameThread, mc), PlayerAPI::alEngine)` (engines built in
  the suppliers, owned by the player). It never calls `start` or `seek`: time, state, loop and speed arrive as session
  snapshots and WaterMedia's follower engine mirrors them (`seekQuick` past the 1 s tolerance).
- The engine is `PlayerAPI.alEngine(true)` (spatial, positional mono sinks) and the player gets `audioLanguage` from the
  game language (`es_mx` → `es-MX`); WaterMedia still prefers the original track when nothing matches.
- `tick()`: reports duration/live once (`MediaReportPacket`), then audio. Sound integration on (default): the player
  stays `mute(true)` and a stereo pair is registered: the primary sink switched to `Mix.LEFT`, a second sink added with
  `Mix.RIGHT`, one `DisplaySound` each positioned by `soundPos(side)` at that edge of the picture (mirrored for
  projectors, centred for vertical frames; mono media plays the same signal on both). A refused side drops the pair and
  retries every 20 ticks; without sinks the single source plays centred. Needs MASTER and RECORDS above zero.
  Integration off: direct `mute`/`volume` with `rangedVol` on the primary only (muted players get no gain updates).
- Queries: `player()`, `sourceIndex()`, `tile()`, `width()/height()`, `texture()` (0 and negatives → -1),
  `textureId()`, `time()`, `duration()`, `canTick()`, `canRender()`, status predicates, `status()`, `isReleased()`.
  `release()` is idempotent and catches a dying backend. Gain helpers `rangedVol`, `soundVol(ranged)`, `falloffVol`
  (full until min, linear to zero at max + 1, must match `DisplaySound.refresh`).

### `DisplayList`
- Plain client list: `add`, `remove`, `reloadAll()` (every tile `reload()`), `release()` on `LevelEvent.Unload`, and
  `onClientPause(ClientPauseChangeEvent.Post)` → `IntegratedServer.execute(() -> DisplayTile.holdSessions(paused))`
  (only fires in true singleplayer). Walks iterate a copy because releasing removes.

### `DisplayRenderer`
- `BlockEntityRenderer<DisplayTile>`: culls by distance from the projected anchor (block centre + normal *
  `projectionDistance`), `shouldRenderOffScreen` above 16 blocks, `getRenderBoundingBox` from `getRenderBox()`. Draws
  one quad (both faces for `renderBothSides`, back face for projectors) rotated by `-data.rotation` around the facing
  normal, grown by `caps.growSize()` on the `growMax` side, full bright, colour = brightness, alpha = alpha. Render
  type: `entityTranslucentCull` under `shaderMode`, else the memoized `BLOCK_TRANSLUCENT_CULL_CUSTOM_TEXTURE`.
  ⚠ `getLoadingBox` waits for a loading/buffering texture the mod does not ship yet.

### `DisplaySound` and `SourceWrapper`
- `DisplaySound` (`AbstractTickableSoundInstance`, event `waterframes:display`, RECORDS): `create(display, tile, source)`
  plays through the vanilla pipeline, then swaps the pooled channel for a `SourceWrapper` over WaterMedia's AL source,
  removes the handle from `channelAccess.channels` and completes an empty stream so `PlayStreamingSourceEvent` reaches
  audio mods. `refresh()` per tick: emulated mode (no `AL_SOFT_source_spatialize`, Sable or VS compat) keeps the source
  head-locked with `soundVol(true)`; otherwise world position at the block plus `audioOffset`, `soundVol(false)` and
  `linearRange(min, max + 1)`; pitch = player speed. `drop()` stops the sound for both sides.
- `SourceWrapper extends Channel`: `linearRange` (absolute position, `AL_LINEAR_DISTANCE_CLAMPED`, spatialize on),
  `headLocked` (relative, no attenuation); `stop()` means "drop", every ownership call is a no-op so Minecraft never
  starts, feeds or deletes the source. ⚠ Do not "simplify" this shape: `SFXEngine` is sealed and `Library.releaseChannel`
  throws on foreign channels.

## 5. Client UI (`client/ui/**`, `assets/waterframes/ui/**`)

### Conventions
- Documents load via `WaterFrames.asResource("<path>")` from `assets/waterframes/ui/`; ids use `/` (`display/preview`).
  A screen resolves a widget with `this.get(asResource(id))` plus an `instanceof` cast and tolerates null (a pack dropped
  the id). Vars: `registerVar(name, value | supplier)` in the constructor or `@UIVar` methods for hot or composed
  values. Events: `registerUIEvent(name, Runnable | args -> ...)` (`args[1]` is the first payload after the element id),
  `@UIEvent` methods, or public exact-`String` methods resolved by name (`SearchScreen.pick`, `UploadScreen.browse`).
  Element events: `registerEvent("render/resize_x", el -> ...)`.
- Atlas `textures/screen_atlas.png` is 256 px, addressed in 16 px chunks: `gui(x, y[, w, h])`. `constants/icons.ui`
  declares every icon name; `client/ui/Icons.java` duplicates only the chunks Java picks at runtime (volume ladder,
  status badges, signal bars, `ADD`, `SEARCH`, anchor grid). ⚠ Both tables were rebuilt on 2026-11-01 against the
  redrawn atlas; column 7 is empty art-wise (`icon_saturation`, `icon_overlay_alpha`, `icon_video_quality`,
  `icon_audio_quality` draw nothing until art lands). Unnamed art: (3,7) "18+", (12,15) fox, (13,15) swatch.

### `DisplayScreen` (`display_preview`, 391x227)
- Loads `display_preview.ui` (imports `tabs/sources|render|media|redstone`, `constants/icons`). Opened by
  `DisplayTile.openScreen(false)`.
- Live vars: `loop`, `paused`, `muted`, `reload_ready`, `watervision_ready`, `media_source`, `transport_ready`
  (`tile.follower() != null`), `now_title`/`now_byline` (the on-air `SourceRow`, see `onAir()`), `status_icon`/
  `status_tooltip` (`SourceRow.statusIcon/describe`), `submit_enabled`/`submit_icon`, `delete_enabled`,
  `resize_x_tooltip`/`resize_y_tooltip`, `shader_mode`, `pos_x`/`pos_y`, every knob as a supplier over `tile.data`.
  `@UIVar`: `time`, `duration`, `loop_tooltip`, `playback_tooltip`, `mute_tooltip`, `submit_tooltip`, `audio_pos_tooltip`.
  Baked: `resizes`, `projects`, `render_behind`, `hide_model`, `watervision`, `max_*`, `emblem_item`, `emblem_name`.
- Events: `toggleLoop`, `togglePlayback`, `toggleMute`, `stop`, `rewind`/`fastForward` (`tile.skip(±SKIP_MS)`), `reload`,
  `openWatervision`, `mute(args)`, `shaderMode(args)`, `submit`, `upload`, `deleteSource`, `select(args)`,
  `remove(args)`, `moveEntry(args)`, `seek(args)`, `skip(args)`, `commit` (`@UIEvent`, flushes), `openUri(id, key)`
  (`@UIEvent`, http/https behind `ConfirmLinkScreen`), element events `render/resize_x|y`.
- Resolved in `build()`: `display/tabs`, `display/preview` (`UIMediaPlayer`, re-wrapped to `tile.follower()` every tick),
  `render/width|height` (`Stepper`), `render/position` (`AnchorPicker`), the render and media sliders, the render
  switches, `media/audio_offset`, `sources/url` (`UrlField`), `sources/list` (`ParentList`).
- Working playlist: `playlist` (copy of the tile's), `active` (pick that goes on air), `selectedEntry`, `models`
  (`Entry -> SourceRow`, stable per key, pruned by `refresh()`), counter tokens (`"r<n>"`, baked as the row `key`),
  `refresh()` (submit + prune + faces), `expand()` (a media alone on source 0 grows its sub-sources, never mid-drag),
  `play(target)` (writes `tile.data` optimistically, sends the list, flushes), `adopt()` (the tile wins: when the working
  list still differs from the tile a second after the last send, and no drag is running, it is replaced and `active`
  cleared), `flush()` (list packet when changed, then the `Patch`; `active(true)` only when the url actually changed, so
  a knob turn on a switched off display leaves it off), `listSent` (tick stamp of the last list send), `loose` (a
  `SourceRow` for an on-air url without a playlist entry, kept out of `models`).
- ⚠ `closed()` flushes once more and `tick()` closes the screen when the tile is removed (`flush` returns early then).
- ↪ Templated list pattern: `ParentList` + `RowBinder` with baked tokens from `baked(key)` and a stable `model(key)`.

### `SourceRow`, `UrlField`, `AnchorPicker`, `ScreenWidgets`
- `SourceRow(tile, entry)`: one model per playlist entry, resolves its MRL once, memoizes title/byline/duration/
  thumbnail on the `Source`/`Metadata` references (`@UIVar title|byline|duration_text|thumbnail`), `sourceCount()`,
  `missing()`; static `statusIcon`/`describe` feed the transport badge (keep both switches in sync).
- `UrlField extends InputText` (tag `UrlField`): `url()`, `valid()` (absolute URI, then the MRL is FETCHING or loaded;
  cached per tick), tooltip explains empty or not-whitelisted.
- `AnchorPicker`: 3x3 anchor grid painted from `POS_BASE`/`POS_ICON`; props `x`/`y` bake position names, events
  `onValueChange`/`onValueCommit` carry `x().name(), y().name()`.
- `ScreenWidgets.register()` registers both tags in `UIRegistry` at client setup (`UrlField` inherits `InputText`).

### `RemoteScreen` (`remote`, 90x210), `SearchScreen` (`dialogs/search`), `UploadScreen` (`dialogs/upload`)
- Remote: vars `in_range`, `move_ready`, `channel_ready`, `mute_icon`, `@UIVar signal_icon`; events `power`, `mute`,
  `arrowUp|Down|Left|Right|Center`, `reload`, `play`, `pause`, `stop`, `volumeUp|Down`, `channelUp|Down`, `rewind`,
  `fastForward`. Closes when the tile is removed.
- Search: `search/query`, `search/notice`, `search/results` (`ParentList` of `dialogs/search_result`); `search` event,
  `pick(id, key)` by name; `PlatformAPI.search` drained on tick. Upload: `upload/path|progress|browse|confirm`, browse
  through `TinyFileDialogs` on a daemon thread, `NetworkAPI.upload`, result handed to `DisplayScreen.append`.

### Documents
- `display_preview.ui`: the live screen. Left: `ParentTab display/tabs` full height. Right: 120 px column with a 68 px
  `Frame` (`MediaPlayer display/preview` + status `Icon` overlay), two now-playing `Text`s, `Spacer`, `SeekBar`, a 4x2
  `Grid` keypad (transport over loop/mute/blank/reload), plus `ItemIcon display/emblem` outside the right edge.
- `display.ui`: the classic single-column variant with a bottom transport band; kept on purpose, loaded by nothing.
- `remote.ui`, `tabs/sources.ui` (url row, `ParentList sources/list` of `widgets/source_entry`, footer),
  `tabs/render.ui` (size steppers with ratio buttons, sliders, anchor picker, switches), `tabs/media.ui` (thumbnail,
  volume sliders, mute, audio position, WaterVision, disabled placeholders), `tabs/redstone.ui` (static),
  `dialogs/search.ui`, `dialogs/search_result.ui`, `dialogs/upload.ui`, `widgets/source_entry.ui` (`dragThumbId=grip`,
  `onRightClick=openUri(key)`), `constants/icons.ui`, `themes/waterui.ui.json` (default), `themes/minecraft.ui.json`.
- `UI-OUTSPEC.md` records what the player pane added beyond `UI-SPEC.md` (badge overlay, `ToggleButton` iconOn
  semantics, host contract, proposals such as `textAlign` and audio track selection).

## 6. WaterUI toolkit (`me.srrapero720.waterui`)

### Invariants
- No code reference to `waterframes` (comments only). Hosts use `UIRegistry`, `UIHost`, `@UIVar`, `@UIEvent`; host code
  never calls `UIBuilder` directly, the screen inflates on `init()` and every re-init.
- ⚠ Package cycles exist (`core.DebugOverlay` → `widget`/`layout`, `layout.ParentList` → `format`, `format` → `layout`/
  `screen`/`widget`/`theme`).

### `core`
- `Element` (abstract, `draw` is the only abstract hook): sizing `FILL`/`CONTAIN`/px per axis, `weight`, measure hooks
  `prefContent*`/`minContent*`/`maxContent*` with a flag-gated memo, `dirty()` (walks up to `queueReflow`), insets
  `border`/`padding`/`margin` (only CONTAIN adds them), `anchor` (free space) vs `alignMask` (flow cross axis),
  `visible` (leaves layout) vs `hidden` (keeps room), `enabled`, `scale`, `elevation`, `theme()` (own or inherited,
  orphan → `Theme.ERROR`), `face(Drawable)`/`outline(Drawable)` pins, `tooltip(list | supplier)` read on hover, pointer
  listeners (`onClick` … `onFileDrop`), routed entries `press/release/move/wheel/focusChange` (final) over widget hooks
  `mouseDown/mouseUp/mouseMove/scroll/keyDown/charTyped/focusable`, `thumb(DragThumb)`, `tick()`, `dispose()`,
  `ownsResources()`. ⚠ `press()` still reaches `mouseDown` when disabled: widgets self-gate. Disabled overlay paints only
  on a non-NONE face.
- `AbstractParent`: `children`, `add/remove/clear` (no dispose), `reorder`, `get(ResourceLocation)` (direct children
  first), `pick`, `shown()`, static `placeFree`. ⚠ `add()` does not detach from a previous parent.
- `Anchor` bitmask (`START/END/CENTER_H/STRETCH_H/TOP/BOTTOM/CENTER_V/STRETCH_V`; spec LEFT/RIGHT map to START/END),
  `Spacing(top, right, bottom, left)` (`{h, v}` is horizontal first), `Rect`, `PointerEvents` (lazy listener bag,
  double click 250 ms, long press 500 ms), `DragThumb` (grab/slide/drop/tap, 4 px threshold, call `reset()` after a
  rebuild), `Cursors` (ARROW/GRAB/GRABBING, `claim` per frame, `frame()` sweep), `DebugOverlay` (F3+W inspector).

### `layout`
- `ParentLinear` ("Parent"): `orientation`, `spacing` (2), `alignChildren`, `justify`, `scrollX/Y` (children laid out at
  preferred size, scissored viewport, themed scrollbar, wheel 10 px per step), `focus(child)`. Leftover shared by weight
  among FILL children, oversize shrinks the largest. ⚠ An emptied scroll list keeps its stale range until the next layout.
- `FrameLayout` ("Frame"): every child anchored in the same rectangle, preferred size = largest child.
- `Grid`: `columns` (min 1), `rows` (grows), `spacing`, `justify`; cell width = (box - gaps) / columns, row height = the
  tallest cell, children anchored inside cells, `Blank` skips a cell. No spans.
- `Justify {START, CENTER, END, BETWEEN}`: `offset(leftover)`, `gap(budget, step, gaps)`.
- `ParentList` (not a container): `entry(path)` template compiled once on the first `submit` (`UILoader.document` +
  `UIContract.skeleton`; a broken template latches one red row), `binder(RowBinder)` (`baked(key)`, `model(key)`),
  `eventHost`, `groupBy`, `reorder(MOVE | SWAP)`, `dragOut`, `onListMove/onListSwap/onListDelete`. Key diff: new keys
  inflate (`UIBuilder.inflateRow`), dropped keys dispose, survivors move. `rows()`/`rows(RowStore)` hand the host-owned
  store across re-inits. `row(key)`, `row(index)`, `order()`, `selected(key)`, `playing(key)`, `dragging()`, `swap`,
  `delete`. `get(ResourceLocation)` returns null by design; template ids resolve through `ListRow.element(String)`.
  Integrated drag from `dragThumbId` with live gap relocation, edge auto-scroll and block moves. Rows outside the
  viewport plus 32 px are not ticked. `ListRow` paints dragging → accent, playing → selection, else field.
  ⚠ A row that fails to build leaks its inflated disposables until the store is disposed; `submit` is O(n²) on large lists.

### `format`
- Pipeline: `UILoader` (reload listener; clears atlases, themes and `DOCUMENTS`, parses `ui/**/*.ui`, loads
  `ui/themes/*.ui.json`, rebuilds an open `WaterScreen`) → `UILexer.tokenize` → `UIParser.parse` (strict order theme,
  imports, decls, root, tree; values are the sealed `UIValue` tree `Num/Hex/Str/Id/Tuple/Call/Flags`, first-defined
  wins) → `UIContract.build(doc, host)` or `skeleton(doc)` + `of(skeleton, source, host)` (importer wins, same name with
  another type is an error, cycle guard, `validateJava`) → `UIBuilder.inflate(ui, root, host, vars)` (theme via
  `UIThemes.resolve`, `applyRoot`, `outsideAnchor` → `screen.outside`, depth-first `build`, `buildImport`,
  `buildTabs`, `applyProps` pass 1 props and live bindings, pass 2 events, `error()` appends a red `Text` and logs
  file:line).
- `UIContract.resolve(name)`: memo → alias error → undeclared error → LIVE error → host map (`checkType`) → inline
  default (`loadAtlas`, atlas calls). Typed getters `size`, `intValue` … `component`, `icon`, `icons`, `item`, `spacing`,
  `color`, `eventArg`, `docPath`, `interpolate` (`{name}` for baked vars only).
- `UIValues`: pure decoders (`color`, `number`, `spacing`, `anchorMask`, `outsideAnchor`, `alignMask`, `orientation`,
  `justify`, `format`). `DataContext`: one per screen (dialogs own theirs); direct suppliers `boolOr/numberOr/
  stringOr/iconOr`, ticked `enabled/visible/component`, `tooltip(element, name)`, `require(name, types...)`.
  ⚠ A live `component` is a literal, `translatable:` is ignored (lags spec §4).
- `UIEvents.resolve(host, name, baked, file, line)` → `Handler`: registered store handler → `@UIEvent` → exact-arity
  all-`String` → narrowest wider → varargs; missing handler logs ERROR and no-ops. `UIBindings` scans every mod file for
  `@UIVar`/`@UIEvent` and resolves reflectively (superclasses included).
- `UIAtlases.load("[ns:]path", defaultNs)`: `textures/<path>` as `.png` then `.webp`, side from `NativeImage`
  (non-square or not divisible by 16 → 256 with a WARN), `Atlas.icon(cx, cy, w, h)`; no bounds check on chunk indices.
- `UIThemeLoader`/`UIThemes`: Gson themes, a Drawable is the first recognised key `color`/`icon`/`sprite`; missing
  slots default, a malformed drawable fails the theme to `Theme.ERROR`; undeclared theme → silent `WATERUI`.
- `UIRegistry`: `register(tag, factory, container[, parentTag])`, `property`, `event` (per tag or generic), `binding`;
  lookups walk the parent chain then the generic map; `RESERVED` generic properties (`id`, `width`, … `scrollY`) are
  duplicated by hand from `UIBuilder.applyGeneric`, keep both in sync. Core tags: `Parent`, `Grid`, `Frame`,
  `ParentList`, `ParentTab`, `ComboButton`, `Text`, `Button`, `ToggleButton`, `StateButton`, `TabButton`, `Icon`,
  `ItemIcon`, `PlayerHead`, `Switch`, `Slider`, `Stepper`, `ProgressBar`, `SeekBar`, `InputText`, `Thumbnail`,
  `MediaPlayer`, `Blank`, `Spacer`.
- `UIHost`: `uiStore()` plus default sugar `registerVar(key, boolean|long|double|Object|Supplier)`,
  `registerUIEvent(key, Handler|Runnable)`, `registerEvent(id, Consumer<Element>)`. `Store.baked()` snapshots the
  dynamics overlaid by constants on every inflate. ⚠ `baked()` swallows supplier throwables.

### `widget`
- Live cadence: draw-time `Button.source`, `ToggleButton.value`, `IconView.source`, `Switch.source`, `ProgressBar`/
  `SeekBar` suppliers; tick-time `Thumbnail.source(Supplier)` and the `DataContext` evaluators; push-only `Text`,
  `InputText`, `Slider`, `Stepper`, `ItemIcon`, `PlayerHead`, `UIMediaPlayer.source`.
- `Button` (`repeat(delay, interval)` re-fires while physically held; icon buttons are 20 px), `ToggleButton` (`iconOn`
  shows when `value` is true, the click flips nothing: the host flips its own state), `StateButton` (cycles `states`,
  `onChange(index)`), `Tab`/`ParentTab` (18 px strip over a `FrameLayout` body, `active(int)` silent, `indexOf(id)`,
  `onChange` click-only; `onActivate` never fires for the initial tab), `ComboButton` (fused `Button` segments, spacing
  forced to 0), `Text` (`text`, `color`, `textScale`, `ellipsize`, `lines`, wrap cache; equal text returns early),
  `Switch` (`value`, `source`, `onToggle`; with a source the knob follows the source only), `Slider` (`range`,
  `value`, `stepped`, `icon`, `onChange` per move, `onCommit` on release; right click opens an `InputNumber` editor),
  `Stepper` (`InputNumber` + 9 px `+`/`-` column; `onChange`/`onCommit` also fire for code-driven writes), `InputText`
  (vanilla `EditBox`; `onChange`, `onSubmit`, `onCommit` on blur or Enter when changed since focus), `InputNumber`,
  `ValueFormat` (`RAW`, `PERCENT`, `RAW_PERCENT`, `ANGLE`, `BLOCKS`, `BLOCKS_DECIMAL`, `DURATION`; statics `decimal`,
  `timestamp`), `ProgressBar`/`SeekBar` (suppliers read every draw; `onSeekStart/onSeek/onSeekEnd/onScroll`; press needs
  `max > 0`), `IconView` (tag `Icon`), `ItemIcon` (`flipX` one-way), `PlayerHead` (live `PlayerInfo`), `Spacer`, `Blank`.
- `Thumbnail`: owns a WaterMedia player and GL engine, no SFX, http/https only, `repeat(true)`, upload capped to the box
  at gui scale, `onLoad` (first texture frame, inside `draw`) and `onError` once. `UIMediaPlayer`: wrap (never capped,
  never released) or own (exclusive GFX + SFX, audible). Both draw letterboxed through `MediaTexture` (package-private
  registration of the player's GL id under `WaterUI.id(prefix + id)`, swap-aware, frees only its own). Class-load probe
  keeps every WaterMedia type inside the nested `Media` so the toolkit loads without it.

### `theme` and `screen`
- `Theme` record: palette drawables (`text`, `textDisabled`, `accent`, `accentBorder`, `disabledOverlay`, `field`,
  `fieldHover`, `selection`, `danger`, `dangerBorder`), roles `panel`/`clickable`/`nested`/`bar` (`ElementTheme`),
  sub-themes `toggle` (`SwitchTheme`), `slider`, `progress`, `scroll`; presets `WATERUI` and `ERROR` (⚠ the spec's
  `MINECRAFT` preset exists only as `themes/minecraft.ui.json`). `Face {NONE, BAR, NESTED, CLICKABLE, PANEL}`.
  Drawables: `Color`, `Icon` (`drawSquared`, `drawExact`), `Sprite` (nine-slice), `ThemeRole` (live resolve).
  ⚠ `Palette.argb` truncates alpha (`0.5f` → 127). ⚠ `panel` must be non-null (selected tab and `EDGE` frame read it).
- `WaterScreen extends Screen implements UIHost`: `(id, width, height)`; `init()` (also on resize) clears, inflates,
  calls `build()`, re-inflates open dialogs, queues a reflow. Dialog stack (`openDialog`, `closeDialog`, `topDialog`),
  dimming under the top dialog, elevation per layer, pointer capture with other buttons swallowed, stale captures
  released on tick against GLFW's physical button state, `onFilesDrop`, `get(ResourceLocation)` (root then outside),
  hooks `closed()`, `afterReflow()`, `afterRender`, `pickExtra`; `removed()` disposes everything and resets cursors.
  ⚠ `clear()` does not dispose code-added media widgets; only builder-registered disposables are freed on re-init.
- `Panel extends ParentLinear` (PANEL face, `edgeFrame()`, `disposable`, `disposeInflated`, `filesDropped`), `Dialog
  extends Panel implements UIHost` (own `DataContext`, `init()`, `closed()`, `closeable()`, `close()`), `ScreenStack`
  (layer host).

## 7. Audio: sinks and tracks
- WaterMedia 3.0.0 (local build of 2026-11-01, `libs/watermedia-3.0.0.jar`) gives every spatial `ALEngine` sinks:
  `player.sinks()` (primary first), `player.addSink(ALEngine.Mix)` (`MONO`, `LEFT`, `RIGHT`), `Sink.source()/mix(Mix)/
  spatialAudio()/release()`. Every sink queues the same buffers and plays in step; the host adopts the source id but never
  plays, queues or deletes it (`SourceWrapper` fits). Surround is folded to stereo by FFmpeg.
- WaterFrames uses two sinks per display (left and right picture edges). ↪ A Speaker block is one more
  `addSink(mix)` registered as its own `DisplaySound` at the speaker's position and `sink.release()` when it goes.
- Audio tracks: `player.audioTracks()`, `audioTrack()`, `audioTrack(AudioTrack)` (hot switch, viewer-local) and
  `audioLanguage(bcp47)`; priority is explicit pick > language preference > original/default > first. The Media tab's
  `media/audio_track` button cycles the tracks (`nextAudioTrack`, tooltip `audio_track_tooltip`).
- ⚠ Not yet validated in game by WaterMedia itself (their PENDING H16); WaterFrames only checked that a stereo video
  registers without OpenAL errors.
