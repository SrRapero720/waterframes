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
- **Idioma de la pista de audio:** el pane no puede elegir la pista de audio (vídeos de YouTube doblados suenan
  en otro idioma). Depende de que WaterMedia exponga `audioTracks()`/`audioTrack(int)`; ya pedido a esa sesión.
  Cuando llegue, la pestaña Media debería estrenar el botón "Audio quality" (hoy deshabilitado) como selector.
