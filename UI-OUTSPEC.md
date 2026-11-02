# UI-OUTSPEC: pendientes de integrar en UI-SPEC.md

Comportamientos, usos de propiedades y convenciones de host introducidos con el rediseño "player pane" de
`ui/display_preview.ui` que `UI-SPEC.md` no describe hoy. Nada aquí cambia el toolkit; todo funciona con los
widgets actuales salvo lo marcado como **Propuesta**.

## 1. Documento `display_preview.ui`

- **Columna izquierda sin banda de transporte.** Solo `ParentTab[id="display/tabs"]`, ocupa todo el alto.
- **Player pane (columna derecha, 120 px)**, de arriba a abajo: preview 16:9 (`Frame` 120x68 con
  `face=field`), texto "now playing" (título 2 líneas a 0.6, byline a 0.5), `Spacer` FILL, `SeekBar` y un
  teclado `Grid[columns=4, rows=2]` con `anchor=CENTER` por tecla: transporte arriba (rewind, play/pause, stop,
  fast forward) y opciones abajo (loop, mute, `Blank`, reload), de modo que ambas filas comparten columnas.
- **Badge de estado como overlay.** `Icon[id="display/status"]` vive dentro del `Frame` de la preview con
  `anchor=TOP|RIGHT, margin=2` (12x12), en vez de ir en la banda de transporte.
- **`MediaPlayer` como hijo de `Frame` con `anchor=STRETCH`.** Válido hoy (los hijos de `Frame` son espacio
  libre y `anchor` es genérico), pero §11 solo lista `MediaPlayer` sin propiedades propias; conviene un ejemplo en §10.5.
- **Emblema de vuelta.** `ItemIcon[id="display/emblem", outsideAnchor=RIGHT|BOTTOM]`, igual que `display.ui`.
- **Ids eliminados:** `display/play`, `display/pause`, `display/transport`. **Ids nuevos:** `display/playback`
  (ya existía en `display.ui`), `display/mute`, `display/emblem`.
- **Gating:** la fila de transporte (incluido el toggle de playback) usa `enabled=transport_ready`; loop y mute
  quedan sin gate porque son estado del bloque y no necesitan reproductor; reload sigue con `reload_ready`.

## 2. Semántica de `ToggleButton` (aclaración)

- `iconOn` se pinta cuando `value` es `true` e `iconOff` cuando es `false` (`ToggleButton.icon()`). El "on" es
  el valor booleano, no el estado semántico: `value=paused, iconOn=icon_play` muestra play mientras está
  pausado, y `value=muted, iconOn=icon_volume_0` muestra el altavoz tachado mientras está silenciado.
  §11 no lo dice explícitamente.

## 3. Contrato de host de `DisplayScreen`

| Nombre | Tipo | Fuente | Nota |
|---|---|---|---|
| `now_title` | `var String` (registrado) | `SourceRow.title()` del contenido en aire | `""` sin URL |
| `now_byline` | `var String` (registrado) | `SourceRow.byline()` del contenido en aire | `""` sin URL |
| `mute_tooltip` | `@UIVar` lista de componentes | `waterframes.gui.mute.state` + `waterframes.common.<bool>` | rojo si silenciado, verde si no |
| `toggleMute()` | evento registrado | `tile.setMute(true, !tile.data.muted)` | |
| `muted` | ya existía | reutilizado por `display/mute` | compartido con `tabs/media` (mismo nombre sin tipo, gana el importador, §5.3) |

- **Eliminados del host:** eventos `play` y `pause` (ningún documento cargado por `DisplayScreen` los usa;
  `RemoteScreen` mantiene los suyos).
- **Modelo "en aire" (convención nueva):** la entrada de la playlist que coincide con URL y source del tile
  aporta su `SourceRow` estable (el mismo de `models`, vía `computeIfAbsent`). Si no hay entrada que coincida,
  se construye un `SourceRow` suelto para esa URL/source, cacheado en un campo y **fuera de `models`** (que
  `refresh()` poda); se reconstruye solo cuando cambia la URL o el source.
- **Lang nuevas:** `waterframes.gui.mute.state` (`en_us`: "Mute: %s", `es_mx`: "Silenciar: %s").

## 4. Comportamientos observados no especificados

- Un `Text` con `component` vivo vacío conserva el alto de una línea; no colapsa. Con nada en aire el pane
  deja dos huecos de texto vacíos encima del `Spacer` (inocuo porque el `Spacer` absorbe la diferencia).

## 5. Propuestas (no implementadas, el toolkit actual no lo permite)

- **Alineación horizontal del texto en `Text`** (`textAlign=LEFT|CENTER|RIGHT`): el título y la byline solo
  pueden ir alineados a la izquierda dentro de su caja `FILL`; centrados bajo la preview leerían mejor.
- **Colapso automático de `Text` vacío** (o `visible` derivado de un `String` vacío): hoy requeriría un
  `var boolean` extra en el host por cada texto.
- **Estéreo:** no toca la UI, pero cambia qué se oye: cada display registra dos sonidos (izquierda y derecha en los
  bordes de la imagen). La pestaña Media sigue mostrando un solo volumen y un solo mute, que aplican al par.

## 6. Widget nuevo: `Dropdown` (implementado)

Campo cerrado que muestra la opción elegida y, al hacer clic, abre una lista de texto por encima de todas las capas.
Entra en el catálogo §11 así:

| Tag | Propiedades | Eventos tipados (payload) |
|---|---|---|
| `Dropdown` | `options` (tupla de strings/números fijos, o `List` vivo), `enum` (nombre de clase enum, baked), `selected` (índice inicial o `int`/`long` vivo), `placeholder` (component), `textScale` | `onSelect` (índice elegido) |

- **Fuentes de opciones:** en `.ui` una tupla fija (`options={"A", "B"}`), una clase enum por nombre
  (`enum="org.watermedia.api.util.MediaQuality"`, se cargan sin inicializar y se rechaza una clase que no sea enum), un
  `List` vivo del host (`options=audio_tracks`, cualquier `List`: los elementos se muestran con `toString()`, los
  `Component` con su texto) o nada (campo solo de estilo hasta que el código le dé datos con `options(List|Object...|Class)`
  o `source(Supplier)`).
- **Cadencia:** la lista viva se lee **una vez por tick** (no por frame, porque el host suele construirla); un cambio
  re-mide el campo para que quepa la opción más larga. El índice vivo (`selected`) se lee por frame como `Switch.source`.
  Con `selected` vivo el host manda: elegir una opción dispara `onSelect` y el campo muestra lo que el host devuelva.
- **Selección:** `selected()` se recorta a la lista actual; `-1` o lista vacía pintan el `placeholder` en el color
  deshabilitado. Un campo con lista vacía no abre nada.
- **Lista abierta:** se despliega bajo el campo (o encima si no cabe en la ventana), 8 filas máximo con rueda para
  desplazar y chevrones de aviso; la fila bajo el cursor usa `fieldHover`, la seleccionada `selection`. Clic en una fila
  elige y cierra; clic fuera cierra y **se consume** (no llega al elemento de abajo); `Esc` cierra la lista antes que
  la pantalla. Cambiar de pestaña, `visible=false`, `enabled=false`, abrir un diálogo o `dispose()` también la cierran.
- **Tooltips y hover:** mientras hay una lista abierta ningún otro elemento muestra tooltip ni hover (la lista posee el
  cursor, como un diálogo).
- **Límite:** la lista se pinta en coordenadas de pantalla a partir de `bounds`, así que un `Dropdown` bajo un ancestro con
  `scale != 1` colocaría mal su lista (misma limitación que el scissor).

### 6.1 Capa popup en el toolkit (código)

- `core/Popup`: interfaz (`paintPopup`, `pressPopup`, `scrollPopup`, `closedPopup`) para capas transitorias.
- `Element.openPopup(Popup)`/`closePopup(Popup)` burbujean por el árbol como `floatDragged`; `Panel` las entrega a
  `WaterScreen.popup(Popup)`/`closePopup(Popup)`. La pantalla guarda **un** popup: abrir otro cierra el anterior,
  `closePopup(p)` solo cierra si `p` es el abierto.
- Orden de pintado: raíz → outside → diálogos → **popup** (profundidad `LAYER_DEPTH * (diálogos + 1)`) → `afterRender`
  → elemento arrastrado. Mientras hay popup, raíz, outside y diálogos reciben el cursor falso (`-10000`).
- Enrutado: `mouseClicked` consulta primero al popup (`false` → cerrar y consumir), `mouseScrolled` le ofrece la rueda,
  `keyPressed` cierra con `Esc`; `init()`, `openDialog()` y `removed()` lo cierran.
- `DataContext.listOr(name)`: proveedor vivo de `List<?>` (registrado o `@UIVar` que devuelva `List`); el antiguo
  resolutor de tooltips pasa a llamarse `componentsOr`.

### 6.2 Pestaña Media

- `Button video_quality` (placeholder) y el botón cíclico `media/audio_track` se sustituyen por dos `Dropdown` con
  `width=FILL`, `placeholder` y `tooltip` en la etiqueta del control:
  - `media/video_quality`: `options=video_qualities` (calidades que ofrece la fuente en el aire, de menor a mayor, como
    `"720p"`; `UNKNOWN` se muestra como `waterframes.gui.media.video_quality.auto`), `selected=video_quality` (la calidad
    más cercana a la del reproductor), `enabled=video_qualities_ready` (más de una), `onSelect=selectVideoQuality()`
    → `MediaPlayer.quality(q)` del seguidor local.
  - `media/audio_track`: `options=audio_tracks` (nombre de la pista, si no su idioma, si no `#n`), `selected=audio_track`
    (índice de `audioTrack()`), `enabled=audio_tracks_ready`, `onSelect=selectAudioTrack()` → `audioTrack(track)`.
- Ambas elecciones son por espectador (nunca estado de sesión). Variables eliminadas: `audio_track_tooltip`, evento
  `nextAudioTrack`; claves lang eliminadas: `waterframes.gui.media.audio_track.{none,current,switch}`; nuevas:
  `waterframes.gui.media.video_quality.auto` (y `video_quality` en `es_mx`).
