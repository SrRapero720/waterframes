# WaterUI Format Specification

> Status: draft v1.0 — full redesign, supersedes draft v0.8.
> The core toolkit lives in `me.srrapero720.waterui` (core, layout, format, theme,
> widget, screen framework, media); mod-specific screens and widgets stay in
> `me.srrapero720.waterframes.client.ui`. **The spec is the source of truth; where
> the code lags, it homologates to the spec** — the spec does not track
> implementation status. Sections marked **[FUTURE]** are explicitly deferred
> layers, not open questions.

---

## 1. Overview & principles

WaterUI describes screens for Minecraft mods through two file kinds:

| Kind   | Extension  | Content                                                    | Format    |
|--------|------------|------------------------------------------------------------|-----------|
| Layout | `.ui`      | Header statements (theme, imports, variables), root preferences, element tree | Block DSL |
| Theme  | `.ui.json` | A `Theme`: global colors, per-role styling, per-control sub-themes | JSON |

Principles the whole format hangs from:

- **A UI is a UI.** There are no file classes: no dialog files, no tab files. The
  same document can load as a screen, insert as a tab, or open as a dialog. A
  property that only makes sense in one context is consumed there and silently
  ignored everywhere else (§6.2).
- **The variable contract is the public interface.** `var` / `final var`
  declarations state what a document needs; the host provides it (§5). Everything
  else is presentation.
- **Context interpretation.** Root preferences bind when the document is loaded
  first and are recommendations otherwise (§6.1). The same rule shapes every
  contextual feature.
- **The screen always opens.** Format errors surface as a red error element plus a
  log line — never a crash (§13.4).

All sizes are **GUI pixels** (Minecraft's scaled GUI space).

---

## 2. Files, discovery & theme assignment

```
/assets
  /<modid>
    /ui
      /themes
        waterui.ui.json
      display.ui
      remote.ui
      /tabs
        sources.ui
      /dialogs
        search.ui
      /constants
        icons.ui
```

- Layouts live anywhere under `ui/` (except `ui/themes/`); subfolders are free
  organization. A document is keyed by its **path relative to `ui/` minus the
  `.ui` extension**: `ui/dialogs/search.ui` → `<modid>:dialogs/search`. Imports
  reference that path (§7).
- Themes live in `ui/themes/` and are keyed by bare stem:
  `themes/waterui.ui.json` → `<modid>:waterui`.

**Theme assignment**, in resolution order:
1. Declared: `theme "<ref>"` (§6).
2. Auto-paired: a theme whose stem matches the document's own stem.
3. The toolkit default (`WATERUI`).

`Theme.ERROR` is reserved for a theme that was declared or paired but is missing
or failed to parse — never for the "no theme at all" case.

---

## 3. Lexical structure

### 3.1 Comments
Line: `//` to end of line. · Block: `/* ... */`.

### 3.2 Whitespace
Insignificant outside a quoted string; newlines carry no meaning. Structural
symbols: `{ } [ ] ( ) , = |`.

### 3.3 Tokens

| Token      | Charset / form                    | Examples                        |
|------------|-----------------------------------|---------------------------------|
| identifier | `[A-Za-z_][A-Za-z0-9_]*`          | `Parent`, `width`, `player_time` |
| number     | `-?[0-9]+(\.[0-9]+)?`             | `2`, `-10`, `0.0625`            |
| keyword    | uppercase identifier              | `FILL`, `CENTER`, `PERCENT`     |
| hex color  | `#` + 6 or 8 hex digits (ARGB)    | `#000000`, `#AAFFFFFF`          |
| string     | `"` … `"` with `\` escape         | `"translatable:key"`            |

### 3.4 Strings and escaping
A quoted string is a first-class token, recognized before any separator split.
Inside `"..."` every structural symbol is literal content; `\` escapes the next
character (`\"`, `\\`); the string ends at the first unescaped `"`.

### 3.5 Bare keys
A key inside a `[...]` bag with no `=` and no value is **ignored**: no effect, no
error. It is tolerated noise, not a flag.

---

## 4. Value grammar

```
value  ::= scalar | tuple | call | flags
scalar ::= number | color | string | identifier
tuple  ::= '{' value (',' value)* '}'
call   ::= identifier '(' value (',' value)* ')'
flags  ::= keyword ('|' keyword)+
```

- Inside a bag, `,` separates **assignments** and `=` separates key from value. A
  value with several parts wraps them in a `{...}` tuple or a `(...)` call, whose
  own commas never leak into the bag.
- **Colors** are ARGB, matching Minecraft's packed int: `#RRGGBB` (alpha defaults
  to `FF`) or `#AARRGGBB`.
- **Identifiers** in value position resolve, in order: a keyword the property
  expects (enum value, flag, sizing sentinel) → a declared variable (§5). An
  identifier that is neither is a compile error.
- **Component strings** (`component`, `tooltip`, `tabTooltip`): a
  `translatable:` prefix selects i18n, anything else is a literal. The prefix is
  resolved by the component factory, not the parser.
- **First-defined wins** governs mutually exclusive alternatives (a button's
  `icon` vs `component`, a theme Drawable's `color`/`icon`/`sprite`): the first
  key in source order is kept, later rivals are skipped without error.

---

## 5. Variables — the contract

A document declares the data it needs; the host supplies it. The set of
**used** declarations, aggregated across the import chain, is the document's
public interface.

### 5.1 Declaration

```
var [<type>] <name>                      // LIVE: the host provides a supplier
final var [<type>] <name>                // BAKED: one value fixed at build time
final var [<type>] <name> = <value>      // BAKED with an inline default
```

- `var` — a **live** value. The host provides a supplier resolved through
  `@UIVar("<name>")`; the widget re-reads it at runtime (§5.5).
- `final var` — a **baked** value. Resolved once at build from the host's
  variable map; an inline `= <value>` is the default when the host map does not
  carry the name — **the host value outranks the inline default**.
- Only `final var` may carry an inline value; a live value comes from a provider
  by definition.

**Types** are optional. When omitted, the type is inferred from the property
sites that use the variable; when a variable has no inferring site (e.g. it is
only passed as a handler argument), an explicit type is required. Recognized
types: `boolean`, `int`, `long`, `float`, `double`, `String`, `Icon`,
`ItemStack`, `Atlas` (§5.7). Tooltip sites infer a component list; that type is
inference-only.

### 5.2 Usage & the contract

- A referenced identifier that is not declared anywhere in the document or its
  imports is a **compile error**.
- A declaration that is never used demands nothing from the host.
- The contract of a loaded document is the **union** of the used declarations of
  the whole import chain.
- A missing or type-mismatched provider is a **load error**, surfaced per §13.4
  with the variable name and the declaring file.

### 5.3 Shadowing across imports

When the same name is declared by an importer and an import:
- **Same type** — the importing side wins (root outranks everything); the
  imported declaration is ignored. No error, no merge ceremony.
- **Different types** — load error. The format does not guess or convert.
- The mode (`final` vs live) follows the winning declaration.

### 5.4 References

A declared variable is referenced as a **bare identifier** anywhere a value is
expected: a whole property (`value=loop`), a tuple member
(`states={icon_a, icon_b}`), a call argument, or a handler argument
(`onClick=skip(skip_step)`). Inside a **string literal**, `{name}` interpolates
a declared variable's value as text (`"Now playing: {title}"`). Only **baked**
values interpolate: an unknown name — or a live `var`, which has no build-time
value — is preserved literally, graceful passthrough, no error. Braces only
carry this meaning inside strings, so they never collide with tuples.

### 5.5 Live modes

| Mode       | Properties                                   | Cadence                        |
|------------|----------------------------------------------|--------------------------------|
| **direct** | `value` (boolean), `time`/`duration`/`value`/`max` (numbers), `icon`, `tooltip` | read at draw, never cached |
| **ticked** | `enabled`, `visible`, `component` (Text) | evaluated at the 20Hz screen tick, cached between ticks |

Direct suppliers keep a seekbar's clock honest, and `tooltip` is read for the
single hovered element only; ticked evaluators may allocate or reflow, so they
ride the tick.

### 5.6 Host resolution

- Live: a dynamic supplier registered on a `UIHost` (`registerVar(key,
  supplier)`), or a `@UIVar("<name>")` parameterless method on the host,
  discovered through the mod annotation scan; return type must match §5.1.
- Baked: the inflatable registry (`registerVar(key, value)` constants) fed to
  the contract by the screen's internal inflate, or a variable map handed to
  `UIBuilder` by machinery (templated rows, §10.6).
- Handlers: a named handler (`registerUIEvent`), `@UIEvent("<name>")` or
  by-name reflection, plus element events bound by id (`registerEvent`,
  §12.2).

**The host store.** A host implementing `UIHost` carries three name-keyed
registries, filled in one-line `protected final` calls **in its constructor**
and living as long as the instance:

- **Dynamic** — `registerVar(key, supplier)`: a live read resolved for `var`
  bindings before the `@UIVar` walk; shadowing an annotated method logs a
  warning. The supplier's type is erased, so each binding site probes it once
  at bind and logs the mismatch there; at runtime a mismatch falls back to the
  safe default **silently**. Suppliers read boxed — keep hot numeric vars
  annotated — and must capture the host or its domain, never tree elements:
  the tree rebuilds under them on every re-init.
- **Inflatable** — `registerVar(key, value)` (boolean/long/double/Object
  overloads): a constant handed to the contract as the baked host map on
  every inflate; the declared-type check of §5.1 validates it. For a name
  with **no** constant, a `final var` takes a fresh **snapshot of the dynamic
  supplier** under the same key instead — register mutable state as a
  supplier and every re-inflate re-reads it; constants are for values that
  never change while the screen lives.
- **Events** — `registerUIEvent(key, handler)` named handlers and
  `registerEvent(id, callback)` element events (§12.2). Named handlers are
  looked up at fire time, so a registration always outranks a stale capture.

A bind-time registration landing after the first inflate logs a warning and
binds nothing until a re-init. Annotations stay the home for logic worth a
named method; the store is for one-liners and composed gates.

**Framework-driven inflate.** The screen and the dialog take their document
id in the **constructor**; the framework inflates it on open and on every
re-init — feeding the baked view to the contract and wiring the element-event
registry once the tree is built — and only then calls `build()`/`init()`,
which are pure **dressing hooks**: resolve widgets, attach couplings, regain
state. Host code never inflates and never calls `UIBuilder` directly.

There is exactly **one host per layer**: the object the screen or dialog
inflates against (a templated row's model is the host of that row's context,
§10.6). Both variables and events resolve on it alone.

### 5.7 Atlases & icons — `loadAtlas()`

```
final var gui = loadAtlas("screen_atlas")      // ONE LINE PER ATLAS
final var icon_reload = gui(1, 11)             // EVERY ICON AFTER IT IS COORDINATES ONLY
final var icon_wide   = gui(3, 13, 2, 1)       // OPTIONAL 2x1 CHUNK SPAN
```

- `loadAtlas("<[ns:]path>")` produces an **atlas handle** (type `Atlas`), a
  value like any other `final var`. The path carries **no `textures/` prefix
  and no extension**: `"screen_atlas"` expands to `textures/screen_atlas.<ext>`
  and resolves through the vanilla resource manager, so the texture format is
  whatever the runtime can decode (a texture pack shipping webp over png just
  works). Namespace omitted → the document's own.
- **Calling the handle** — `<handle>(chunkX, chunkY [, chunksWide, chunksTall])`
  — yields an `Icon`. The atlas is square, its side divisible by 16, addressed
  in **16-px chunks**; `chunkX`/`chunkY` are chunk indices, the span defaults
  to `1,1`. The side is read from the texture itself, so the valid index range
  follows automatically (256 → 0–15, 512 → 0–31).

Atlas handles and icons shadow and aggregate exactly like any other variable
(§5.3) — a root document can repaint an imported icon by redeclaring its name.

### 5.8 Constants documents

A document containing only header statements — imports and declarations, no
tree — exists to be imported: a shared icon table, shared bounds, a shared
contract. Convention: `constants/…`. Importing one needs no alias (§7).

---

## 6. Document structure

A `.ui` contains, in **strict order** — an out-of-order statement is a compile
error:

1. **`theme`** — at most once; absent falls to auto-pair or the default (§2).
2. **Imports** — zero or more (§7). They precede the declarations so the
   document's own `var`s can override what its imports declare (§5.3).
3. **Variable declarations** — zero or more (§5).
4. An optional **`root[...]`** — at most once.
5. **Top-level elements** — the tree.

### 6.1 The root

The root is **preferential, not mandatory**. The hosting screen pre-generates
the root panel; `root[...]` states how this document would like it configured.

- **Loaded first (as a screen or dialog):** the values bind — they become the
  panel's properties. Top-level elements flow into it.
- **Imported:** the imported document's root is **the container the importer
  generated for it** — a tab's body, an alias placement — and its `root[...]`
  values configure that container under one precedence rule: **the importer
  outranks the imported**. Any property the placing side already set (the alias
  bag, §7.2) silences the imported one; everything else is obeyed. An
  auto-generated container (a tab body) sets nothing of its own, so there the
  imported root props apply whole — no wrapper element needed.

`frame=FULL|EDGE` is root-only chrome: `FULL` (default) keeps the thick panel
border, `EDGE` trades it for a single-pixel edge in the panel colour. The trade
only exists when there is something to trade: on a theme whose panel border is
already a single pixel, `EDGE` changes nothing. Any other value is a compile
error.

Because the screen stack is free 2D space, the root places itself with
`anchor` (§9.3).

### 6.2 Contextual root properties

Properties consumed only when the document is used a certain way, ignored
otherwise — the mechanism behind "a UI is a UI":

| Property       | Consumed when              | Meaning                                 |
|----------------|----------------------------|-----------------------------------------|
| `tabIcon`      | inserted as a tab (§10.3)  | The strip button's icon face            |
| `tabTooltip`   | inserted as a tab          | The strip button's tooltip              |
| `onActivate`   | inserted as a tab          | Fired when the tab is shown (§12.6)     |
| `onDeactivate` | inserted as a tab          | Fired when the tab is hidden            |
| `dragThumbId`  | stamped as a row (§10.6)   | The row element carrying the drag handle |

Used any other way — screen, dialog, import, tab — each is inert.

---

## 7. Imports

```
import "<path>"                     // LOCAL DOCUMENT; IMPLICIT NAME FROM THE PATH
import "<path>" as <alias>          // SAME, WITH AN EXPLICIT NAME
import "<ns>:<path>" as <alias>     // ANOTHER MOD'S DOCUMENT; ALIAS REQUIRED
import "<fqcn>" [as <Tag>]          // JAVA ELEMENT CLASS; DOTS MARK THE CLASS REF
```

The reference form decides the kind: a `:` marks another mod's document, dots
mark a Java class, anything else is a **local document path** — no extension,
`.ui` is implicit. Dots are therefore reserved: a local document path never
carries them.

### 7.1 File imports & the implicit variable

An import **is a `final var`**: `import "dialogs/upload"` declares an implicit
`final var dialogs_upload` holding the imported document, its name derived from
the path with separators turned into underscores. `as <alias>` merely renames
it. A cross-mod import assumes nothing about a foreign path layout, so it must
always name itself with `as`.

Because the alias is a variable, it lives in the one variable namespace and
follows §5.3: a same-name declaration of a different type is a load error; two
imports deriving the same name are disambiguated with `as`.

The variable has two uses:
- **Placement** — `sources[width=FILL]` inserts the imported document's root
  content where the tag sits; the placing bag shapes the container.
- **Reference** — `tabs={sources, media}` on `ParentTab` (§10.3).

An import is resolved and parsed **before** the importing document, and
inherits the loading context: the effective theme and the variable environment
(declares aggregate per §5.2–5.3). **Recursive imports are rejected**: the
import ancestry is scanned and a repeat fails compilation.

### 7.2 What an import contributes

Its root content, its declarations, and its root preferences applied to the
generated container under the importer-outranks rule (§6.1): on an alias
placement, the alias bag's keys win over the imported root's same keys. Its
contextual properties activate only in the matching context (§6.2).

### 7.3 Java element imports

A dotted reference imports a Java class: it must be an `Element` subclass
registered in the element registry; the tag defaults to the class simple name,
`as` overrides it. The import only binds the tag — properties and events come
from the registry entry. An unloadable, unregistered or non-`Element` class is
a compile error.

The registry is open: WaterUI core registers the built-in catalog at bootstrap;
a mod registers its own elements at client setup through the public `UIRegistry`
API.

### 7.4 Lazy document paths

Not every document reference is an import. A `ParentList`'s `entry` (§10.6) names a
document by the **same path form** — a bare `<path>` in the referring document's
own namespace, `<ns>:<path>` across mods — but resolves it **lazily**: the target
is parsed only when the list binds, and it contributes **nothing** to the
referring document's contract. An import aggregates the referenced document's
`var`s into the public interface (§5.2); a lazy path deliberately does not,
because a row template's per-row `var`s are supplied per row, not by the screen
host.

---

## 8. Elements — generic properties

Sizes describe the **outer box**: border and padding never grow an element,
they inset its content. Only `CONTAIN` adds them on top of what the content
measures, so wrapped content always fits.

### 8.1 Sizing — per axis

| Value      | Meaning                                        |
|------------|------------------------------------------------|
| `FILL`     | Take the parent's leftover space on that axis. |
| `CONTAIN`  | Wrap whatever the content measures (default).  |
| `<number>` | Exact outer size in GUI pixels.                |

Minimums are never declared — they are computed from content. An explicit size
outranks the intrinsic minimum and maximum.

### 8.2 Property table

| Property        | Type / values                | Notes |
|-----------------|------------------------------|-------|
| `id`            | identifier or string         | Element identity. Namespace is implicit (the document's modid); an explicit `other:name` is allowed and logs a **warning** when it differs from the document's namespace (the resource-pack override escape). Passed to every handler as the first argument (§12.1). |
| `width` / `height` | `FILL` \| `CONTAIN` \| px |  |
| `weight`        | float (default `1`)          | Share of leftover among `FILL` siblings. |
| `margin`        | number or tuple              | Arity §8.3. |
| `padding`       | number or tuple              | Arity §8.3. |
| `border`        | px                           | **Width only.** Colour comes from `outline`. |
| `align`         | side \| `CENTER` \| `STRETCH`| Cross-flow placement (§9.2). Containers: default for children. |
| `justify`       | `START`\|`CENTER`\|`END`\|`BETWEEN` | Containers only: flow-direction distribution (§9.4). |
| `anchor`        | flags                        | Free-space attachment (§9.3). |
| `outsideAnchor` | flags                        | Lifts the element outside the root (§9.5). |
| `shadow`        | bool                         | Drop shadow under icons/text. |
| `enabled`       | bool                         | Disabled controls get the theme's `disabledOverlay`. |
| `visible`       | bool                         | Invisible → leaves layout; room goes to siblings. |
| `hidden`        | bool                         | Hidden → skips paint/input, keeps its room. |
| `elevation`     | int                          | Z-lift over siblings. |
| `scale`         | float                        | Uniform scale factor. |
| `tooltip`       | String or variable           | Component resolution §4; a live variable ticks (§5.5). |
| `face`          | `#hex` or role name          | Surface override (§8.4). |
| `outline`       | `#hex` or role name          | Frame override (§8.4). |
| `scrollX` / `scrollY` | bool                   | Containers: scroll that axis in place (§10.1). |
| `on<Event>`     | handler                      | Event binding (§12). |

Generic property names are **reserved**: a widget cannot register a tag
property that shadows one.

### 8.3 Margin / padding arity

| Form           | Mapping                      | Note                                  |
|----------------|------------------------------|---------------------------------------|
| `4` or `{4}`   | all four edges               |                                       |
| `{4, 2}`       | **(horizontal, vertical)**   | **not CSS order**                     |
| `{4, 2, 4, 2}` | (top, right, bottom, left)   | CSS clockwise                         |

Any other tuple size is invalid.

### 8.4 Surface overrides — `face` / `outline`

Each takes a **`#hex`** literal (a pinned flat colour) or a bare **role name**
resolved against the **live theme** each frame, so it follows a theme swap.
Valid role names: the global drawables `field`, `fieldHover`, `accent`,
`accentBorder`, `selection`, `danger`, `dangerBorder`, `disabledOverlay`, and
the role faces `panel`, `clickable`, `nested`, `bar`. An unknown role name is a
compile error.

Geometry overrides (`border`, `padding`, `margin`, `shadow`) and surface
overrides address disjoint fields; both beat the inherited theme role, so there
is no precedence conflict. When a theme role has border `0` (sprites carrying a
baked frame), pinning an `outline` needs `border` pinned beside it or the frame
has no room to show.

---

## 9. Placement — flow, align, anchor, justify

Four concepts, one term each, mutually exclusive contexts.

### 9.1 Flow

A linear container stacks its children along its `orientation` — that direction
is the **flow**. Along the flow nobody chooses a position: each child sits after
the previous one plus `spacing`. The only knobs are leftover sharing (`FILL` +
`weight`, with over-full containers shrinking the largest children until they
fit) and `justify` (§9.4).

### 9.2 Alignment — `align`

Placement **across the flow**. Two scopes, same word, same values:

- `align` on the container — the default for all children.
- `align` on a child — its personal override.

Values are the sides valid for that orientation plus `CENTER` and `STRETCH`:

| Orientation  | Valid sides                        |
|--------------|------------------------------------|
| `HORIZONTAL` | `TOP`, `CENTER`, `BOTTOM`, `STRETCH` |
| `VERTICAL`   | `LEFT`, `CENTER`, `RIGHT`, `STRETCH` |

A flow-direction side (`align=LEFT` in a row) is a **compile error**. `FILL` on
the cross side implies `STRETCH`; `align=STRETCH` is the explicit form.

### 9.3 Anchor — `anchor`

The attachment point in **free 2D space**: the screen stack (the root, dialog
panels) and grid cells (§10.2). Flags: `TOP`, `BOTTOM`, `LEFT`, `RIGHT`,
`CENTER`, `STRETCH`, combined with `|`.

- Each side flag claims its axis (`TOP`/`BOTTOM` vertical, `LEFT`/`RIGHT`
  horizontal).
- `CENTER` and `STRETCH` fill every **unclaimed** axis; alone they claim both.
- Contradictory sides (`TOP|BOTTOM`) are a compile error.
- The anchored corner grows inward, so an anchored element never spills out of
  its space; `margin` insets from the edges.

`anchor` on a child of a flow container is a compile error (use `align`);
`align` on a child in free space is a compile error (use `anchor`). One job,
one name, no guessing.

### 9.4 Justify — `justify`

Distribution **along the flow** of a container's children and leftover:

| Value     | Meaning                                        |
|-----------|------------------------------------------------|
| `START`   | Children packed at the flow start (default).   |
| `CENTER`  | Packed group centered.                         |
| `END`     | Packed at the flow end.                        |
| `BETWEEN` | Even gaps between children, none at the edges. |

Valid on `Parent` (its flow) and `Grid` (its rows, vertically). `justify`
replaces the flexible-spacer idiom for distributing sections. It acts on the
**leftover** after sizing: a `CONTAIN`-sized container wraps its content and
has none by definition, so `justify` is only meaningful with `FILL` or an
explicit size on the flow axis.

### 9.5 Outside placement — `outsideAnchor`

An element carrying `outsideAnchor` leaves the root flow entirely and attaches
to an **edge** of the root panel's outer rectangle. Ornaments live on the
rectangle's edges, never on its diagonals — the corners are dead space no
design wants.

- The **first** flag names the edge (`TOP`, `BOTTOM`, `LEFT` or `RIGHT`): the
  element sits fully outside that edge, growing away from the panel.
- The **second** flag, optional, aligns the element **along** the edge and is
  bounded by it: `RIGHT|BOTTOM` sets the element's bottom edge flush with the
  panel's bottom edge — the edge acts as the bounding of where the element can
  sit, alignment is flush, never centered on the corner. Omitted, the missing
  axis defaults to `CENTER`: `outsideAnchor=RIGHT` floats at the right edge,
  vertically centered.
- The second flag must belong to the perpendicular axis. `CENTER`/`STRETCH` as
  the edge, a same-axis second flag, or more than two flags are compile errors.
- `margin` offsets: the edge-facing side pushes away from the panel, the
  along-edge sides nudge within the bound.
- **This is the one place flag order matters** — `RIGHT|BOTTOM` (right edge,
  bottom-aligned) and `BOTTOM|RIGHT` (bottom edge, right-aligned) are different
  placements.

This is how screen ornaments (an item emblem, a status badge) are authored in
the document instead of code.

- Value grammar is §9.3 without `STRETCH`.
- `FILL` sizes are invalid on an outside element: exact or `CONTAIN` only.
- Only meaningful on the loaded document's own top level; on an import it is
  ignored (§7.2).

---

## 10. Container catalog

### 10.1 Row / column — `Parent`

- `orientation` = `HORIZONTAL` (default) | `VERTICAL`.
- `spacing` (default `2`) · `align` (§9.2) · `justify` (§9.4).
- `scrollX` / `scrollY` make the container itself scrollable on that axis —
  there is no wrapper element. Content on a scrolling axis is laid out at its
  full preferred size and the visible slice is clipped; the scrollbar (themed
  by the `scroll` sub-theme, §14.4) appears only on overflow. The wheel drives
  the vertical axis when `scrollY` is on, and the horizontal one when it is the
  only scrolling axis. A child set to `FILL` on a scrolling axis is a compile
  error — the leftover there is unbounded.

### 10.2 Grid — `Grid`

A cell lattice. Children fill cells **left-to-right, top-to-bottom**.

- `columns` (required) · `rows` (optional — grows from the child count) ·
  `spacing` · `justify` (§9.4, vertical distribution of rows).
- Cell width is the grid width divided by `columns`; a row's height is its
  tallest cell.
- Inside a cell there is no flow — it is free space — so the child places
  itself with **`anchor`** (§9.3).
- A bare `Blank` is the idiomatic empty cell: it consumes its slot and does
  nothing else (§11) — the `continue` of a container.
- **[FUTURE]** spans and positional cell addressing.

### 10.3 Tabbed container — `ParentTab`

A strip of tab buttons over a body showing one tab at a time.

- `tabs={alias, alias, ...}` — **import aliases** (§7.1), one tab each, in
  order. Referencing a document that was not imported is a compile error.
- Each tab's strip button takes its face from the tab document's `tabIcon` and
  its tooltip from `tabTooltip` (§6.2); the rest of that document's root
  preferences shape its **body container** (§6.1) — the body is auto-generated
  and bare, so they apply whole. Declare the body's layout there
  (`orientation`, `spacing`, `align`, `width=FILL, height=FILL`...), no wrapper
  element needed.
- Every tab is inflated up front; ids resolve across all tabs, active or not.
- The first tab shows by default; left-click switches. Switching is entirely
  self-contained — `onTabChange` is an **optional** notification hook firing
  with the new index; the hidden/shown tab's `onDeactivate`/`onActivate` fire
  in that order (§12.6). The initially shown tab fires no `onActivate` on open.
- A host restores selection after a rebuild code-side (`active(int)`).

---

### 10.4 Fused buttons — `ComboButton`

A container that fuses two or more buttons into what reads as a **single
control**: one connected chrome — a single clickable face and outline drawn
over the union — with no margins or borders between segments. Only the hover
boundary separates them: each child keeps its own hover shade, tooltip,
`component`/`icon` and events.

- `orientation` = `HORIZONTAL` | `VERTICAL` — the stacking direction of the
  segments.
- Children must be button tags (`Button`, `ToggleButton`, `StateButton`);
  anything else is a validation error.
- Each segment sizes from its own properties; the fused chrome follows the
  union of their boxes.

The remote's volume and channel rockers (up over down) are the canonical use.

### 10.5 Overlay — `Frame`

Stacks every child in the **same rectangle**, one on top of the next in source
order. There is no flow: the frame is free 2D space, so each child places itself
with `anchor` (§9.3), never `align`. It is how a badge sits over a picture, a
label centers over a bar, or a spinner covers a panel while it loads.

- No properties of its own; the generics (§8) and the children's `anchor` do all
  the work. A child sized `FILL` on an axis, or anchored `STRETCH` there, spans
  the whole frame on that axis; otherwise it takes its measured size, anchored.
- The frame's own `CONTAIN` size wraps the largest child on each axis.

### 10.6 Templated list — `ParentList`

A vertical list whose rows are stamped from one shared `.ui` **template**, one per
data key. It is the declarative form of the dynamic mount point (§13.2): a list
authored as a document instead of hand-built in code.

- `entry="<path>"` names the row template as a **lazy document path** (§7.4), not
  an import — the template's per-row `var`s never enter the screen's contract.
- The list is **not a container**: inline children are a validation error. Rows
  come only from the template.
- `spacing` and the generics (§8) shape the list; its flow is always vertical.

**Per-row contract.** The template's import chain is walked **once** when the list
binds, into a reusable skeleton. Each row then gets its own contract over that
skeleton — its **baked** values supplied per row — and its own binding context
whose host is the **row model**: the model's `@UIVar` methods feed the row's live
`var`s (§5.5). **Events resolve against the screen-level host** (§12), not the
model — a row's `onClick=select(key)` calls the screen, carrying the row's key as
a baked argument (§12.1).

**Key diff.** The host submits an ordered list of keys. A key already shown reuses
its live row, a new key inflates one, a dropped key is disposed; the rows are then
**moved** into the submitted order, never rebuilt — so selection, scroll and
embedded media players survive a reorder or a resize.

**Row cache.** The live rows are an opaque store the **host owns**, so a screen
re-inflate (which builds a fresh list) re-adopts the surviving rows with their
contexts and players intact. The list is **never a panel disposable**; it releases
its rows only as a safety net when the whole tree is disposed.

**Surface.** A row paints its state per frame, code-side (§13.3): the dragged row
shows the accent, the row on air the selection face, a selected row the accent
border.

**Id encapsulation.** Template ids repeat across rows, so a screen-level
`get(id)` never descends into a `ParentList` and resolves them to nothing. Entry
elements resolve through the list only: `row(index)`/`row(key)` gives the row,
`row.element("<id>")` the element inside it (bare paths take the template's
namespace). The root's `dragThumbId` resolves the same row-scoped way.

**Integrated drag.** Declaring `dragThumbId=<id>` on the template root hands that
element to the list as the drag handle: the list owns the whole session — the
floating copy at the cursor, the live gap relocation, edge auto-scroll on the
tick, and the physical-release belts. A short tap on the handle fires the
handle's **own declared `onClick`** (the tap-to-select idiom); a real drag never
does. A `dragThumbId` resolving to nothing logs ERROR and the rows build
without a drag handle. Hosts gate list-rebuilding work on `dragging()`.

- `groupBy="<baked key>"` — contiguous rows whose baked value under that key
  match travel as **one block** (a media and its expanded sources). Omitted,
  every row is its own block.
- `reorder=MOVE|SWAP` — drop semantics: `MOVE` (default) displaces the other
  rows, relocating the gap live; `SWAP` exchanges the dragged block with the
  block under the cursor at drop.
- `dragOut=true` — dropping past the list's sides (20px beyond the content box)
  deletes the dragged block. Off by default.

**List events.** The drag is visual until the drop notifies, in row-index space,
and the host mirrors its data (`order()` gives the keys in visual order):

| Event          | Payload            | Fired by                                    |
|----------------|--------------------|---------------------------------------------|
| `onListMove`   | `origin, target`   | a MOVE drop that left the block elsewhere   |
| `onListSwap`   | `a, b`             | a SWAP drop, or `swap(a, b)` from code      |
| `onListDelete` | `index`            | a drag-out drop, or `delete(index)` from code — the block's rows dispose |

**Errors.** A broken template degrades to **one** red row inside the list plus one
log line, and never retries (§13.4); a single row that fails to build is dropped
on its own, the rest stand.

## 11. Widget catalog

Registered core tags, their own properties and typed events. Sizing, surface
and pointer generics (§8) apply everywhere. Stateful widgets are **fully
authorable from the document**: initial value, live source and events — code
setters exist for all of them but are never required.

| Tag | Properties | Typed events (payload) |
|---|---|---|
| `Text` | `component` (baked or live), `color` (#hex), `fontScale`, `ellipsize` (bool), `lines` (int) | — |
| `Button` | `component` \| `icon` (first-defined wins), `repeat` (`{delay, interval}` ticks — re-fires `onClick` while the left press is held, after `delay` then every `interval`; a single number sets both; `delay >= 0`, `interval > 0` or load error; re-fires are silent and keep firing with the cursor off the button; a disabled button stops without resuming; `Button` tag only — `ToggleButton`/`StateButton`/`TabButton` do not inherit it) | `onClick` (—) |
| `ToggleButton` | `iconOn`, `iconOff` (either alone is fine), `value` (live boolean) | `onClick` (—) |
| `StateButton` | `states={icon, ...}`, `state` (initial index; clamps to `states`, author `states` first) | `onClick` (new index) |
| `Icon` | `icon` (static or live) | — |
| `ItemIcon` | `item` (`ItemStack` variable), `flipX` (bool, mirrors horizontally) | — |
| `PlayerHead` | `uuid` (`String` variable) | — |
| `Switch` | `component`, `value` (initial bool or live boolean), `textScale` | `onToggle` (`true`/`false`) |
| `Slider` | `min`, `max`, `value`, `stepped` (bool), `icon`, `format`, `formatKey` (baked translation key: the readout always shows that translation), `textScale` | `onValueChange` (value), `onValueCommit` (final value) |
| `Stepper` | `min`, `max`, `step`, `value` (author bounds before `value`) | `onValueChange` (value), `onValueCommit` (value) |
| `ProgressBar` | `value`, `max` (live numbers), `format`, `textScale` | — |
| `Seekbar` | `time`, `duration` (live numbers), `format` (default `DURATION`), `textScale` | `onSeekStart` (start ms), `onSeek` (ms), `onSeekEnd` (final ms), `onScroll` (amount) |
| `InputText` | `value` (initial text — baked only; live text would fight the user's typing), `suggestion` (component string, §4), `maxLength` | `onTextChange` (text), `onSubmit` (text), `onValueCommit` (text) |
| `Thumbnail` | `source` (`String` URL/URI, baked or live), `fallback` (icon) | `onLoad` (—, once), `onError` (error text, once) |
| `Frame` | — (overlay container, §10.5: children stack in free space, anchor-placed) | — |
| `Spacer` | — | — |
| `Blank` | — | — |

`Spacer` is real flexible space (give it `FILL` to flex); `Blank` is a pure
skip marker — it occupies its slot, paints nothing and measures nothing. Grids
use `Blank` for empty cells (§10.2).

WaterFrames extension tags (registered by the host mod):

| Tag | Properties | Typed events |
|---|---|---|
| `UrlField` | inherits `InputText` | inherits `InputText` |
| `AnchorPicker` | `x`, `y` (position names, baked) | `onValueChange` (X, Y names), `onValueCommit` (X, Y names) |

`format` values (`ValueFormat`): `RAW`, `PERCENT`, `RAW_PERCENT`, `ANGLE`,
`BLOCKS`, `BLOCKS_DECIMAL`, `DURATION`.

**Container vs leaf** is validation, not syntax: the registry declares whether
a tag accepts children; a leaf opening `{}` is a validation error.

**Not part of the format, by design:** the numeric editor inside
`Slider`/`Stepper` is a private implementation detail, and drag-thumb gestures
(`thumb(DragThumb)`) are wired in code by the owner (§13.3).

---

## 12. Events

A UI element does nothing until an event binds it to host code. Two families:
**generic pointer events** any element may declare (§12.4), and **typed
events** carrying a widget's value (§11); a typed event with the same name
outranks the generic one on that tag.

### 12.1 Handler spec & baked arguments

- `on<Event>=<handler>(...)` — the parentheses are **mandatory**: a handler is
  visibly a method, never confusable with a variable. Empty for no arguments
  (`onClick=reload()`) or carrying baked arguments (`onClick=skip(-10)`,
  `onClick=jump(skip_step)`). A bare name without parentheses is a compile
  error.
- Arguments are literals or variable references (§5.4), **all passed as
  strings**, baked at build time.
- **Every handler receives the element's `id` (fully namespaced) as the first
  argument**, then the baked arguments, then the firing widget's runtime
  payload. An element with no `id` bakes an empty string there.
- A templated row (§10.6) bakes its **key** as a handler argument
  (`onClick=select(key)`), so the screen host learns which row fired from the
  baked value rather than from the row model.
- Number payloads drop the `.0` on whole values.

### 12.2 Resolution & handler shapes

Resolution order on the host: a named handler registered in the host's store
(`registerUIEvent`, §5.6) → a `@UIEvent("name")`-annotated method → the
exact-arity all-`String` method → the narrowest wider all-`String` arity → a
trailing `String...` varargs match. A registered handler receives the element
id, the baked arguments and the runtime payload as one array, like an
all-varargs method; the `Runnable` form drops them all. A handler declaring
fewer parameters than carried drops the tail; a missing handler logs ERROR and
the event is a no-op — the screen still opens.

**Element events.** `registerEvent(id, callback)` binds a callback straight to
an element's generic click (§12.4) with no document declaration and no name
resolution: left-only, `enabled`-gated, consuming, receiving the element
itself. The id is a bare path (namespaced by the inflated document) or an
explicit `ns:path`. The internal inflate (§5.6) wires these after the tree
builds, so they re-attach on every re-init; an id resolving to nothing logs
ERROR and the screen still opens.

`close` is provided by the dialog layer: `onClick=close()` inside any document
opened as a dialog dismisses it (§13.1).

### 12.3 Pointer model

- **Click means press.** `onClick` fires the moment the left button lands;
  there is no release-inside gate.
- **Dispatch.** A press is offered depth-first to the deepest hovered element;
  an element that neither consumes it as a widget nor listens lets it fall
  through to its siblings' hit-test order. Consumption stops **siblings**, not
  ancestors: a container's own listeners are evaluated for every press landing
  in its bounds, regardless of what a descendant consumed.
- **Pointer capture.** The press that starts a capture owns the pointer: every
  drag and the owning button's release route to the consuming element. While
  the capture lives, presses and releases of other buttons are swallowed — a
  gesture can never be stolen or ended by a second button. A capture whose
  button is no longer physically down (its release was lost, e.g. to focus
  loss) is ended on the next tick by delivering that release at the current
  cursor position; the owning button pressing again ends it the same way
  before the fresh press proceeds.
- **Timing.** Double click: second press within 250ms. Long press: held 500ms.

### 12.4 Generic pointer events

| Event | Fires | Payload |
|---|---|---|
| `onClick` | every left press | — |
| `onRightClick` | every right press | — |
| `onDoubleClick` | second left press inside the window | — |
| `onLongPress` | once, when a held press crosses the threshold | — |
| `onPressStart` | left press lands | — |
| `onPressEnd` | left release ending a press, wherever the cursor is | — |
| `onDrag` | every move while left-pressed | local `x`, `y` |
| `onScroll` | wheel the widget itself left unconsumed | signed amount |
| `onEnter` / `onExit` | cursor enters / leaves the bounds | — |
| `onHover` | both transitions through one handler | `true` / `false` |
| `onFocus` / `onBlur` | keyboard focus gained / lost | — |
| `onFileDrop` | an OS file drop lands on this element | one string per path |

`onFileDrop` routes to the deepest listening element under the last known
cursor; with no listener the drop falls back to the panel itself.

### 12.5 Typed events

Listed per tag in §11. `onValueCommit` marks the end of an interaction — that is
what screens save on: a slider drag ships exactly one save, at release. Widgets
whose every change is final (switches, state buttons) need no `onValueCommit`;
their change event is the commit.

### 12.6 Tab lifecycle

`onActivate` / `onDeactivate` in a tab document's root preferences (§6.2)
resolve against the host with the tab's document id as the element id and fire
when the tab is shown or hidden — outgoing `onDeactivate` before incoming
`onActivate`.

---

## 13. Screens, dialogs & the host boundary

### 13.1 The screen stack

The outermost host is a `WaterScreen` (a vanilla `Screen`), a free-space stack
holding, by anchor: the **root panel** (§6.1), the root document's
**outside elements** (§9.5), and **dialogs**.

A **dialog is an opening mode, not a file kind**: any document opened over the
current screen by code (`WaterScreen.openDialog`) or from an event handler.
Several dialogs stack; the topmost owns the input and dims everything under it.
Each dialog gets its own binding context — its live bindings never leak into
the root's. The dialog layer provides `close` (§12.2) and an escape gate the
host can hold (`closeable`). Dialog chrome — title band, dismiss cross, action
band — is authored in the document like any other content.

### 13.2 Mount points & dynamic content

A container with an `id` and no children is a **mount point**: the document
owns its place, size and chrome; the host populates and manages the rows in
code (`host.get(id)`). Dynamic rows — playlist entries, search results — are
either **code widgets** the host builds and manages, or a **`ParentList`**
(§10.6) that stamps them from a row template while the host supplies the keys and
models. Either way their interactivity — selection, drag-reorder, embedded
players — is driven from the host.

**The document positions, the host dresses**: resolving an element by `id`
after inflate and finishing its configuration in Java is a supported idiom, not
a workaround — a button whose icon depends on runtime state, a field whose
validity gates a neighbour.

### 13.3 Code-only surface

Deliberately outside the format: per-subtree theme swaps
(`element.theme(Theme)`), custom drag-thumb gestures (`DragThumb`) beyond the
list's integrated drag (§10.6), and any tick-driven gating beyond the ticked
bindables (§5.5).

### 13.4 Errors

Compile and load errors (syntax, unknown tags or properties, contract
violations, import cycles) surface as a red error element inside the root plus
one ERROR log line carrying file and position — the screen always opens.
Recoverable authoring slips (missing handler, unknown event) log ERROR and
no-op. Theme slips (missing keys) warn once and default.

---

## 14. Theme files (`.ui.json`)

Encodes a `Theme`: global colors, one styling block per visual role, one
sub-theme per rich control. In-code presets: `WATERUI` (the default),
`MINECRAFT`.

### 14.1 Drawable

Many theme values are **Drawables**; the key name is the discriminator, and
they are mutually exclusive per slot (first-defined wins, §4):

- `"color"` — ARGB hex string.
- `"icon"` — `"<atlas>, <chunkX>, <chunkY>[, w, h]"` using the §5.7 atlas
  reference form (no `textures/` prefix, no extension).
- `"sprite"` — nine-slice vanilla GUI sprite path, e.g. `"widget/button"`.

### 14.2 Global palette

`text`, `textDisabled` (plain colors) · `accent`, `accentBorder` ·
`disabledOverlay` · `field`, `fieldHover` · `selection` · `danger`,
`dangerBorder` (Drawables).

### 14.3 Roles

A widget's visual role is intrinsic to its type; the theme supplies one block
per role: `panel`, `clickable`, `nested`, `bar` — each mirroring
`ElementTheme`: `border` (px), `padding`/`margin` (number or array, §8.3
arity), `shadow` (bool), `outline`/`face`/`hover` (Drawables).

The `panel` face must be non-null: the selected tab and the thin `EDGE` frame
derive their colour from it.

### 14.4 Control sub-themes

| Field      | Members |
|------------|---------|
| `toggle`   | `trackWidth`, `trackHeight`, `border`, `gap`, `slideMs`, `thumbWidth`, `thumbHeight`, `thumbMargin`, `outline`, `off`, `on`, `thumb`, `thumbHover` |
| `slider`   | `height`, `knobWidth`, `track`, `knob`, `knobHover` |
| `progress` | `height`, `fill` |
| `scroll`   | `width`, `track`, `thumb` |

### 14.5 Defaulting

Absent keys default silently **by design** — omission is how a lean theme is
authored. Only two cases warn, once each: a missing slot the runtime
dereferences unconditionally (it becomes transparent), and a missing whole role
block or sub-theme (it paints nothing / falls to the error preset's component).
An **unknown** key warns too — that is the typo catcher. A document with no
declared or paired theme takes `WATERUI` silently (§2) and never warns.

---

## 15. Future layers

Deferred by decision, not open questions: Grid spans and positional cell
addressing (§10.2).
