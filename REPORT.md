# WaterFrames × WaterMedia v3 — Análisis de optimización y adaptación

> **Alcance.** Rama `1.20.1` (Forge 1.20.1, WF `2.2.0-beta.6`), que es la única que corre sobre
> **WaterMedia v3** (vendorizada en `libs/`). El objetivo del documento es dictaminar qué se puede
> **optimizar, simplificar, adaptar o eliminar** ahora que la v3 reescribió la librería y movió
> responsabilidades (unas se volvieron automáticas, otras DIY). Se contemplan `FEATURES.md` e
> `ISSUES.md`.
>
> **Cómo se verificó.** Se leyeron los 3 archivos que tocan WaterMedia (`Display`, `DisplayTile`,
> `WidgetURLTextField`), el build y los mixins; y se mapeó la API pública de **v2 (2.1.37,
> `A:\dev\java\watermedia`)** y **v3 (3.0.0.21, `A:\dev\java\wmt\watermedia`)** contra el código.
> Las afirmaciones sobre firmas de la v3 están comprobadas sobre el fuente `3.0.0.21` (el mismo que
> el jar de `libs/`).

---

## 0. Dictamen (TL;DR)

1. **La rama no compila tal cual.** La migración de WaterMedia **3.0.0.18 → 3.0.0.21 quedó a
   medias**: `Display.java` usa una API de `GLEngine.Builder` (9 setters de punteros GL) que
   **ya no existe** en la 3.0.0.21, y `build.gradle` importa jars que **no están** en `libs/`.
   Esto es lo primero y bloqueante (Bloque A).
2. **La buena noticia:** el rewrite de la v3 permite **borrar** una parte grande del "pegamento"
   con forma de v2. Toda la integración cabe en 3 archivos y la v3 unificó imagen+vídeo en un solo
   `MediaPlayer`, se encarga sola del ciclo de vida de texturas, la conversión YUV→RGBA, el
   guardado/restaurado de estado GL y la sincronización A/V. Hay ~7 restos vestigiales y ~3 features
   muertas que hoy no hacen nada (Bloques B y C).
3. **Oportunidad de adaptación:** varias piezas DIY del mod (máquina de *seek*/drift manual, reloj
   de servidor por tick, corrección de lag) hoy **reimplementan** —o directamente sustituyen mal—
   primitivas que la v3 ya trae hechas (`MasterClock`, `ServerMediaPlayer`, `MRL.subscribe`). Bloque D.
4. Varios bugs abiertos de `ISSUES.md` viven exactamente en los archivos que hay que refactorizar;
   conviene arreglarlos en la misma pasada (Bloque E).

Prioridad sugerida al final (§7).

---

## 1. Cómo cambió la integración v2 → v3 (contexto)

| | **v2 (VLC, 2.1.x)** | **v3 (FFmpeg, 3.0.0.x)** |
|---|---|---|
| Modelo | **Dos caminos**: `ImageCache`/`ImageRenderer` (imagen/GIF) **+** `VideoPlayer` (vídeo), multiplexados por el mod con `switch(Mode)` | **Un solo** `MediaPlayer` vía `MediaAPI.createPlayer(mrl, …)` (elige `TxMediaPlayer` para imagen o `FFMediaPlayer` para vídeo/audio) |
| Textura GL | La librería crea/sube/animа; el mod pide un `int` | La librería sigue exponiendo un handle (`long`), **pero además** ahora gestiona sola subida asíncrona (PBO ring), conversión YUV→RGBA, borrado diferido y **guardado/restaurado de todo el estado GL** para no romper Minecraft/Sodium/Iris |
| Wiring GL | La librería enlazaba LWJGL a pelo | **DIY mínimo**: solo le pasas el *render thread* + un `Executor` que despacha en él. **Sin punteros de función** |
| URL | `java.net.URI` + `NetworkAPI.patch` interno | `MRL` (string-aware, async, cacheado global, con `Status`, `subscribe`, `videoSource()/imageSource()`) |
| Sync A/V | El mod comparaba `getTime()` y hacía `seekTo` | `MasterClock` (drift PTS, recalibración a 50 ms, invalidación de seeks por *serial*) **dentro** de `FFMediaPlayer` |
| Sync servidor | El mod incrementaba el tick a mano | Existe `ServerMediaPlayer` (reloj headless, `syncTime`/`syncDuration`, ticker 50 ms, repeat/ended) |
| Superficie de control | `getX/setX/isX` + `getStateName()` string | verbos terse (`pause(b)`, `paused()`, `seek`/`seekQuick`, `volume(i)`, `status()→enum`) |

La colección de imports de WaterMedia hoy es **solo 3 archivos**:
`Display.java`, `DisplayTile.java`, `WidgetURLTextField.java`. Es una superficie chica y manejable.

---

## 2. 🔴 Bloque A — La migración a 3.0.0.21 está incompleta (bloqueante)

### A1. 🔴 `Display.GL_BUILDER` usa una API que ya no existe → no compila
`Display.java:31-40` construye el motor GL así:

```java
private static final GLEngine.Builder GL_BUILDER = new GLEngine.Builder(Minecraft.getInstance().gameThread, Minecraft.getInstance())
        .setGenTexture(GlStateManager::_genTexture)
        .setBindTexture((target, tex) -> GlStateManager._bindTexture(tex))
        .setTexParameter(GlStateManager::_texParameter)
        .setPixelStore(GlStateManager::_pixelStore)
        .setBindBuffer(GlStateManager::_glBindBuffer)
        .setBindVertexArray(GlStateManager::_glBindVertexArray)
        .setBindFrameBuffer(GlStateManager::_glBindFramebuffer)
        .setActiveTexture(GlStateManager::_activeTexture)
        .setDelTexture(GlStateManager::_deleteTexture);
```

En **WaterMedia 3.0.0.21** (el jar de `libs/`), `GLEngine.Builder` es **solo esto**
(`GLEngine.java:1563-1575`):

```java
public static class Builder {
    public Builder(final Thread renderThread, final Executor renderThreadEx) { … }
    public GLEngine build() { … }
}
```

Un `grep` de los 9 setters sobre todo el fuente 3.0.0.21 devuelve **0 coincidencias**: esos métodos
fueron **eliminados**. La v3 pasó de "inyecta tus punteros GL" (beta.18, DIY) a enlazar LWJGL
directamente y gestionar el estado GL por dentro. Es literalmente el caso "la v3 volvió DIY algo y
después lo simplificó" que motivó este análisis.

**Fix (simplifica *y* corrige):** borrar los 9 setters. Los argumentos del constructor ya son los
correctos (`gameThread` está AT-eado a público, y `Minecraft` implementa `Executor`):

```java
private static final GLEngine.Builder GL_BUILDER =
        new GLEngine.Builder(Minecraft.getInstance().gameThread, Minecraft.getInstance());
```

> Nada más hay que tocar del pipeline GL: la v3 ahora **guarda y restaura sola** todos los bindings
> de textura/unidad/sampler/PBO/VAO/FBO/programa/viewport/blend alrededor de cada subida, así que la
> razón por la que se inyectaba `GlStateManager` (mantener coherente el estado de Minecraft) ya está
> resuelta dentro del motor.

### A2. 🔴 `build.gradle` referencia jars ausentes / versiones cruzadas
`build.gradle` importa:
```
implementation files('libs/watermedia_youtube_extension-3.0.0-beta.5.jar')   // NO está en libs/
implementation files('libs/watermedia-3.0.0.18.jar')                          // libs/ tiene la .21
implementation 'com.github.WaterMediaTeam:binaries:3.0.0-rc.2'                 // libs/ tiene binaries rc.4
```
`libs/` en disco contiene únicamente `watermedia-3.0.0.21.jar` y `watermedia_binaries-3.0.0-rc.4.jar`.
Aunque se arregle A1, **Gradle no resuelve** `libs/watermedia-3.0.0.18.jar`. **Fix:** alinear
`build.gradle` con los artefactos realmente vendorizados (o volver a colocar los jars que se esperan).
Decidir además si `binaries` viene de Maven o de `libs/` (hoy está duplicado y descuadrado).

### A3. 🟡 `gradle.properties` con versión v2 colgada
`watermediaversion=2.1.34` (valor de la era v2) junto a `watermediarange=[3.0.0.17,3.1)`. El rango es
el que usa `mods.toml` (correcto); `watermediaversion` no se usa y despista. Reconciliar a la versión
v3 real (3.0.0.21).

---

## 3. 🟠 Bloque B — Código vestigial con forma de v2 (eliminar/simplificar)

### B1. 🟠 `Display.Mode {VIDEO, PICTURE, AUDIO}` — 0 referencias
`Display.java:434-436`. Era el discriminador del `switch(displayMode)` de la v2, que ya no existe.
**Borrar el enum.**

### B2. 🟠 Accesores de fuente muertos — 0 llamadores
`getSource()`, `getSourceCount()`, `isVideo()`, `isImage()` (`Display.java:122-145`) no los llama
nadie. Sostenían la lógica de modo v2. **Eliminar** (o cablear a propósito si se van a usar en UI).

### B3. 🟠 `openPlayer()` selecciona la fuente a mano de forma redundante
`Display.java:68-92` calcula `currentSource` con fallback `videoSource()/imageSource()`… y luego llama
`MediaAPI.createPlayer(tile.mrl, sourceIndex, …)` con `sourceIndex` (siempre 0). Pero `createPlayer`
**ya** resuelve la fuente y elige el subtipo de player por dentro (`MediaAPI.java:49-50`:
`final MRL.Source source = mrl.source(sourceIndex);` → `TxMediaPlayer` si es imagen, `FFMediaPlayer`
si no). La preselección solo alimenta los accesores muertos de B2. **Simplificar:** decidir el índice
deseado y pasárselo a `createPlayer`; quitar el bloque manual de `currentSource` (o conservarlo solo si
de verdad se usa `type()` para el HUD). De paso desaparecen las advertencias "No sources"/"No valid
source" duplicadas con la validación interna de la librería.

### B4. 🟡 `noEngine`/`isNoEngine()` arrastra semántica `notVideo` inexistente
`Display.java:52,334-336`. En v2, la bandera significaba "VLC roto → cae al camino de imagen". En v3
**no hay fallback a imagen**: sin motor no se renderiza nada. Hoy `isNoEngine()` solo duplica
`mediaPlayer == null`. **Colapsar** a esa comprobación y renombrar/eliminar la bandera.

### B5. 🟡 `TextureWrapper` quedó como cascarón
`TextureWrapper.java` es un `AbstractTexture` no-op que solo guarda un `int`. Su razón de ser en v2
(el inner `Renderer` que adaptaba `ImageRenderer`) ya no está. Sigue siendo necesario **algún** puente
handle-GL → `ResourceLocation`, pero conviene revisar si el mapa `TEXTURES` +
`DisplaysRegistry.registerTexture/unregisterTexture` (`Display.java:159-173,390-405`) se puede
simplificar ahora que hay exactamente un `long texture()` por player.

### B6. 🟠 `/waterframes reload_all` es un stub que miente
`WaterFramesCommand.watermedia$reloadAll` (`:519-523`) **no recarga nada**: solo imprime "success".
En v2 llamaba a `ImageAPI.reloadCache()`. **Fix:** cablearlo a `mrl.reload()` por display cercano (la
v3 expone `MRL.reload()`), o quitar el comando. Hoy da falsa sensación de éxito.

### B7. 🟠 Animación de carga: muerta y con regresión
- El dibujado de "loading" está comentado con `// TODO` (`DisplayRenderer.java:82-85`).
- La registración del asset (`registerOtherStuff` + `ImageAPI.loadingGif`) se borró en la migración.
- Pero `WaterFrames.LOADING_ANIMATION` (`WaterFrames.java:34`) **sigue** usándose para el indicador de
  *buffering* (`DisplayRenderer.java:95`) → apunta a una textura **no registrada** → se ve la textura
  faltante (rosa/negra).

**Fix:** registrar un asset propio de carga (la v3 ya no trae `loadingGif`) y reactivar el dibujado,
o quitar el `LOADING_ANIMATION` y el bloque de *buffering*. Hoy está a medio quitar.

---

## 4. 🟠 Bloque C — Features muertas / desconectadas (config que no hace nada)

### C1. 🟠 `useMultimedia()` no se consulta en el camino v3
La opción de servidor "Enables VLC/FFMPEG usage" y su override cliente `clientUseMultimedia` tienen
getter (`DisplaysConfig.java:346`) que **no llama nadie**. `openPlayer()` crea el player siempre. El
toggle no apaga nada. **Fix:** o se respeta en `openPlayer()`/`requestDisplay()` (no crear player si
está desactivado), o se elimina la config (y su comentario "VLC/FFMPEG", ver F6).

### C2. 🟠 Corrección de lag por tick: campos leídos pero nunca escritos
`DisplayTile.lagTickTime`/`lagTickCompensate` (`:37-38`) se **leen** en `tick()` (`:241-251`) pero
**no se asignan** en ningún sitio → siempre 0 → **todo el bloque nunca se ejecuta**. El
`MinecraftServerMixin` de la v2 que alimentaba `setLagTickTime` **se borró** en la migración y dejó
el consumidor huérfano. `useLagTickCorrection()` (`DisplaysConfig.java:335`) tampoco lo llama nadie.
Es una feature **muerta**.

**Fix — dos caminos, el 2º es el recomendado:**
- (a) Quitar los campos + config y no prometer la corrección.
- (b) **Adoptar `ServerMediaPlayer.syncTime(...)`** de la v3, que "continúa avanzando si está PLAYING"
  — es exactamente una corrección de tiempo autoritativa del servidor, mantenida por la librería
  (ver D2). Esto reemplaza la feature muerta por una que sí existe.

### C3. 🟠 El mixin `@Shadow pause` es frágil (causa del crash #292) y reemplazable
`MinecraftMixin` (`:17-22`) hace `@Shadow` de `Minecraft.pause` solo para llamar a
`DisplayList.onClientPause(pause)`. Su función real es válida (pausar los players cuando el juego se
pausa, porque los ticks se congelan y `Display.tick()` no correría). **Pero:**
- Es la causa de **#292** (`No refMap loaded` → el `@Shadow` no mapea a SRG en producción).
- El propio autor **ya dejó cableado el reemplazo nativo** en un comentario:
  `DisplayList.onClientPause(/*ClientPauseChangeEvent.Post event*/…)` (`DisplayList.java:104`).

**Fix:** sustituir el mixin por el evento Forge `ClientPauseChangeEvent` (verificar que existe en
Forge 47.4.20; se añadió en la línea 1.20.x). Se **elimina el mixin, el riesgo de refmap y #292** de
un plumazo, y desaparece toda la maquinaria `MixinPlugin`/refmap si no queda otro mixin.

---

## 5. 🟡 Bloque D — Adaptar a primitivas que la v3 ya trae (bajar DIY a la librería)

### D1. 🟡 Máquina de *seek*/drift manual vs. `MasterClock`
`Display.java` mantiene `QUICK_SEEK_THRESHOLD`, `SYNC_SEEK_COOLDOWN`, `syncSeek()`, `lastTargetTime`,
`lastSyncSeekTime` (`:44-45,222-239,285-314`) para elegir `seek()` vs `seekQuick()` según el drift.
`FFMediaPlayer` **ya** lleva un `MasterClock` con drift PTS, recalibración a 50 ms e invalidación de
seeks por *serial*.
> **Matiz honesto:** `MasterClock` sincroniza **audio↔vídeo dentro de un player**, no la **red**
> (que todos los clientes coincidan con el tick del servidor de Minecraft), que es lo que WF necesita.
> No se puede borrar todo. Pero sí se puede **adelgazar** la heurística propia (los umbrales/cooldown)
> y confiar en `seek/seekQuick` + el reloj para el pulido fino. Cambio de tamaño medio; evaluar.

### D2. 🟡 Reloj de servidor manual vs. `ServerMediaPlayer`
Hoy el servidor incrementa `data.tick` a mano en `DisplayTile.tick()` y rebota `TimePacket`s. La v3
trae `ServerMediaPlayer` (headless, ticker compartido de 50 ms, `syncTime`/`syncDuration`, repeat/ended
y corrección de tiempo integrada) pensado **exactamente** para sync autoritativa multi-cliente. Es la
pieza que el mantenedor describía como "orquestador de tiempo". Adoptarlo unificaría el tick del
servidor **y** resolvería C2. Cambio arquitectónico mayor → marcarlo como estratégico, no urgente.

### D3. 🟡 `WidgetURLTextField.isUrlValid()` crea un MRL por frame de GUI
`:69-77` llama `MediaAPI.getMRL(url)` en cada `getBorder()/getTooltip()` (o sea, cada frame de la
pantalla). `getMRL` está cacheado global (no duplica), pero igual parsea `String→URI` y hace lookup en
cada render, y este camino de validación por-frame está implicado en los crashes de tipeo de URL
(**#283/#239**). **Fix:** cachear el `MRL` (o el último string validado) y/o usar `MRL.subscribe`;
endurecer contra URI nula/parcial. Barato y quita un pie de la trampa de #283.

### D4. 🟢 `requestDisplay()` hace *polling* de `mrl.status().loaded()`
`DisplayTile.java:62-96` sondea el estado del MRL cada tick con varios *early-returns* enredados. La v3
ofrece `MRL.subscribe(Consumer<MRL>)` (one-shot al quedar listo). Migrar de *poll* a *callback*
simplifica el método. Opcional (el polling funciona), pero deja el flujo más limpio.

### D5. 🟢 `WVCompat` re-parsea `new URI(url)`
`WVCompat.java:19` construye una `URI` a mano mientras el resto del mod ya es string/MRL. Puede
reutilizar el MRL ya resuelto del tile. Consistencia menor.

---

## 6. 🟠 Bloque E — Bugs de `ISSUES.md` que viven en el "glue" (arreglar en la misma pasada)

Estos ya están diagnosticados en `ISSUES.md`; los ubico en los archivos que este informe propone
refactorizar, para no tocarlos dos veces.

| Issue | Dónde | Arreglo |
|---|---|---|
| **#290/#294** (crash Create contraption) | `DisplayTile.handleUpdateTag()` → `setDirty()` → `blockEntityChanged`+`sendBlockUpdated` (`:329-332,344-349`) | En `handleUpdateTag` solo cargar datos + pedir update de modelo; **nunca** `blockEntityChanged/sendBlockUpdated`. Guardar `setDirty()` contra `Level` no estándar (VirtualRenderWorld de Create). |
| **#279** (stutter de audio al alejarse de un frame *muted*) | `Display.tick()` recalcula `rangedVol`+`setVolume` cada tick **sin mirar mute** (`:257-260`) | Saltar el camino de volumen cuando `muted` o master-mode (o poner 0 una vez). |
| **#297** (el vídeo rebobina al entrar un jugador al render distance) | `Display.syncDuration()` envía `tile.data.tick` (`:198-202`) | Enviar `tick = -1` centinela; el servidor ignora `tick == -1` (fix del reporter). `syncDuration` solo debe reportar `tickMax`. |
| **#295** (el proyector desaparece de frente a ~9 bloques) | `DisplayRenderer.shouldRenderOffScreen`/`shouldRender` (`:38-46`) + `getRenderBoundingBox` fino | Ensanchar el bounding box para cubrir el quad proyectado / devolver `true` para displays que proyectan; dejar de truncar `(int) projectionDistance`. |
| **#239** (`usePermissionsAPI=true` crashea en cliente) | `DisplaysConfig.canSave()` (`:422-440`) llamado desde GUI cliente (`WidgetURLTextField:49`, `DisplayScreen`) → `PermissionAPI.getOfflinePermission` NPE | No llamar a la API de permisos server-only desde GUI cliente: asumir-permitido en cliente y dejar que el `canSave` server-side rechace; o null-check del handler activo en `getPermBoolean`. |
| **#292** (no arranca en Forge 1.20.1) | refmap del mixin | Ver **C3**: eliminar el mixin vía `ClientPauseChangeEvent` mata el problema de raíz. |

---

## 7. 🟢 Bloque F — Otras observaciones (calidad/robustez, baja prioridad)

- **F1 — Backdoor de admin por nombre.** `DisplaysConfig.isOwner` (`:497-501`) y
  `WaterFramesCommand.hasPermissions` (`:604-617`) conceden admin a `"SrRapero720"`/`"SrRaapero720"`
  en **cualquier** servidor (además con typo `SrRaapero720`). Considerar quitarlo o limitarlo a dev
  mode; es un riesgo de seguridad para servidores ajenos.
- **F2 — Typo de coordenada Maven.** `group=me.srrrapero720` (3 erres) vs paquete `me.srrapero720`
  (2 erres).
- **F3 — Bug de copy-paste.** `DisplayData.load` (`:258`):
  `this.brightness = … : this.alpha;` → el brillo cae a `alpha` en vez de a `brightness`.
- **F4 — `DisplayList` (array disperso a mano).** OK por rendimiento, pero `remove(int i)` usa
  `i > displays.length` (debería `>=`) y nunca compacta `position`. Riesgo bajo (el camino usado es
  `remove(Display)`), pero es una estructura DIY que quizá ya no valga la pena mantener.
- **F5 — Claves NBT ad-hoc.** `DisplayData.build(DisplayScreen…)` usa strings sueltos
  (`"width"`,`"height"`,`"pos_x"`) en paralelo a las constantes `URL/ROTATION/…`; y
  `build(PlayListScreen, tile)` ignora `tile`. Unificar claves.
- **F6 — Doc-rot VLC.** Comentarios "values over 100 uses VLC überVolume" y "Enables VLC/FFMPEG"
  siguen citando VLC (backend v2); el backend ahora es FFmpeg.

---

## 8. Priorización sugerida

| # | Acción | Bloque | Impacto | Esfuerzo |
|---|---|---|---|---|
| 1 | Simplificar `GL_BUILDER` a `new Builder(gameThread, mc)` | A1 | 🔴 Desbloquea el build + simplifica | XS |
| 2 | Alinear `build.gradle`/`gradle.properties` con los jars de `libs/` | A2,A3 | 🔴 Desbloquea el build | S |
| 3 | Borrar `Mode`, accesores muertos, simplificar `openPlayer`/`noEngine` | B1–B4 | 🟠 Menos superficie, más claro | S |
| 4 | Arreglar loading/buffering (B7) y `reload_all` (B6) | B6,B7 | 🟠 Quita regresión visible + comando mentiroso | S |
| 5 | Resolver features muertas: `useMultimedia` (C1), lag-tick (C2) | C1,C2 | 🟠 Config que no engaña | S–M |
| 6 | Cambiar mixin `pause` por `ClientPauseChangeEvent` → cierra #292 | C3 | 🟠 Arranque estable | S |
| 7 | Bugs del glue #290/#279/#297/#295/#239 en la misma pasada | E | 🟠 Cierra issues reales | M |
| 8 | Cachear MRL en el widget de URL (endurecer #283/#239) | D3 | 🟡 Menos crashes de tipeo | S |
| 9 | (Estratégico) adoptar `ServerMediaPlayer` como reloj autoritativo | D2,C2 | 🟡 Unifica sync, mata deuda | L |
| 10 | (Estratégico) adelgazar la máquina de drift apoyándose en `MasterClock` | D1 | 🟡 Menos código de sync frágil | M–L |

**Recomendación de arranque:** hacer 1–2 (que vuelva a compilar), luego 3–6 en una sola rama de
limpieza (son borrados y ajustes de bajo riesgo que reducen el "glue" a su mínimo v3-nativo), y
después evaluar 9–10 como trabajo arquitectónico aparte.

---

## Apéndice — Superficie de integración WaterMedia (hoy)

Solo 3 archivos importan `org.watermedia.*`:

- **`client/display/Display.java`** — `MRL`, `MediaAPI`, `MediaPlayer`, `GLEngine`, `ALEngine`,
  `MathUtil`. (Creación del player, tick de sync, textura, volumen ranged, ciclo de vida.)
- **`common/block/entity/DisplayTile.java`** — `MRL`, `MediaAPI`, `MathUtil`. (Crea el MRL desde la
  URL, decide cuándo instanciar `Display`, tick de servidor/cliente.)
- **`common/screens/widgets/WidgetURLTextField.java`** — `MRL`, `MediaAPI`. (Validación de URL en GUI.)

Primitivas v3 relevantes ya disponibles y **no** aprovechadas hoy: `MediaAPI.createPlayer` (elige
fuente+backend+engines), `MRL.subscribe`/`videoSource()`/`imageSource()`/`reload()`,
`ServerMediaPlayer.syncTime/syncDuration`, `MasterClock` (interno a `FFMediaPlayer`),
`ALEngine.buildDefault()`, `NetworkServer` (para el *upload* de #257 de `FEATURES.md`).
