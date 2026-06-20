---
name: tree-into-tree-view
description: Use this skill when the user wants to render an arbitrary tree-shaped data structure (a filesystem, a JSON tree, an org chart, a dependency graph spanning tree, any labeled hierarchy) as Homing's interactive tree view — foldable, keyboard-navigable, selection-emitting — with ZERO bespoke JavaScript. Triggers — "show this tree in the tree view", "adapt/normalize a tree", "render my hierarchy", "tree navigation for X", "write a TreeNormalizer". The whole job is one small recursive `TreeNormalizer<SPEC>`; the renderer, JSON writer, and widget are reused unchanged. Skip if the user wants columns / a data grid / a pivot / per-node content bodies — the tree view is navigation-only by design; that richness belongs in a complementing workspace widget, not the renderer.
---

# Transform an arbitrary tree into the Homing tree view

**Written for: Homing 0.5.1** (the rigid-tree substrate — RFC 0040 `NormalizedNode` / `TreeNormalizer` / `TreeNodeJsonWriter`, RFC 0039/0040 renderer + leveled Open). Verify against your framework version if older.

The rigid tree substrate (RFC 0040) has one headline promise: **any conforming tree renders through the single `TreeRenderer` with zero bespoke JS.** Adapting your tree is therefore *one function* — a `TreeNormalizer<SPEC>` that maps your native node shape onto the canonical `NormalizedNode`. Everything downstream (serializer, renderer, fold/keyboard/selection) is off-the-shelf.

> **Design boundary — read first.** The tree view carries **structure + navigation only**. It draws a label per node and emits selection/open events. It is *not* a data grid. Multi-column, pivots, per-node content bodies, drill-downs, unbounded depth → **compose a complementing workspace widget** wired to the navigation Party (RFC 0028), never extend `TreeRenderer`/`NormalizedNode`. If you're tempted to add a second visible column to a tree node, you've left this skill.

## The one thing you write

A `TreeNormalizer<SPEC>` (a `@FunctionalInterface`: `NormalizedNode normalize(SPEC)`). It is **context-free** — it always builds a standalone tree rooted at `L0` and knows nothing of where the result attaches.

```java
import hue.captains.singapura.js.homing.tree.*;
import hue.captains.singapura.js.homing.tree.dims.NameValue;
import java.util.*;

// Your arbitrary tree — anything with a label + children.
record FileNode(String name, List<FileNode> kids) {}

public final class FileTreeNormalizer implements TreeNormalizer<FileNode> {
    public static final FileTreeNormalizer INSTANCE = new FileTreeNormalizer();
    private FileTreeNormalizer() {}

    @Override public NormalizedNode normalize(FileNode root) {
        return at(root, TreeLevel.L0.INSTANCE);
    }

    private NormalizedNode at(FileNode n, TreeLevel level) {
        // DisplayLabel is the ONE dimension the renderer draws. (Add Kind/Summary
        // only if a sibling widget needs them off the selection event — see below.)
        Map<DimensionKey, DimensionValue> dims = new LinkedHashMap<>();
        dims.put(DisplayLabel.INSTANCE, new NameValue(n.name()));

        // Children sit one level below. At the L18 cap there is no room —
        // a node that deep simply renders without its descendants.
        List<NormalizedNode> kids = new ArrayList<>();
        level.below().ifPresent(below -> n.kids().forEach(k -> kids.add(at(k, below))));

        return new NormalizedNode(level, dims, kids);
    }
}
```

That's the whole adaptation — ~15 lines.

## The canonical shape

| Type | Module / package | Role |
|---|---|---|
| `TreeNormalizer<SPEC>` | `homing-rigid-tree` · `…homing.tree` | The seam you implement: `normalize(SPEC) → NormalizedNode`. |
| `NormalizedNode(level, dimensions, children)` | `homing-rigid-tree` · `…homing.tree` | The one canonical node. Immutable. Identity is **positional** — no id field. |
| `TreeLevel` (`L0…L18`) | `homing-rigid-tree` · `…homing.tree` | Sealed level ladder. `level.below()` → `Optional` (empty at the `L18` cap); root is `TreeLevel.L0.INSTANCE`. Depth lives here alone — never as a dimension. |
| `DisplayLabel` / `Kind` / `Summary` / `Category` (+ `dims.NameValue`) | `homing-rigid-tree` · `…homing.tree` | The universal dimension keys + a string value. The renderer **draws `DisplayLabel`**; carries `Kind`/`Summary` in selection events. |
| `TreeNodeJsonWriter` | `homing-tree-views` · `…homing.tree` | `new TreeNodeJsonWriter().write(TreeNode<?>) → String` JSON. Reused as-is (it's a plain class, no `INSTANCE`). |
| `RigidTrees.graftUnder` | `homing-rigid-tree` · `…homing.tree` | Pure recursive level shift — re-homes a standalone sub-tree under a host. Use only when your tree *embeds* sub-trees from another normalizer. |

## Wire it up (no JS)

**1. Serialize.** One line:
```java
String json = new TreeNodeJsonWriter().write(FileTreeNormalizer.INSTANCE.normalize(root));
```

**2. Serve** the JSON from a `GetAction` (model it on `CatalogueTreeGetAction` in homing-studio-workspace — it returns the writer's output for `/catalogue-tree`).

**3. Render** with the existing substrate `TreeRenderer` (homing-core-js) — you write no renderer:
```js
var renderer = new TreeRenderer({
    branch: branch, container: host, data: treeJson, expandDepth: 2,
    onSelect:   function (sel) { /* sel = {path, level, kind, label, summary, hasChildren} */ },
    onActivate: function (sel) { /* Enter / double-click — the intentional "open" */ }
});
renderer.handleKeydown(ev);   // arrow nav (host gates WHEN keys flow)
```

To drop it into the workspace, copy `TreeWidget` (homing-studio-workspace): a `WorkspaceWidget` that fetches your endpoint and `new TreeRenderer(...)`s it. Its only import is `TreeRendererModule`.

## Selection is positional — `path`, not id

`NormalizedNode` carries no id. The renderer hands every selection a structural child-index `path` (`[l0, l1, …]`). Address the selected node by that path — e.g. a leveled fetch `?l0=..&l1=..` resolved server-side by walking the same tree (`ForestPathResolver` is the precedent). Do not invent node ids.

## Richness composes OUTWARD (not into the tree)

The tree navigates; a **sibling widget renders**. The pattern: your tree widget publishes `NodeSelected` / `NodeOpened` to the workspace navigation Party (RFC 0028); a complementing widget joins the same Party and reacts.

- **Cheap select** (arrow / single click) → `onSelect` → `NodeSelected` → a summary/preview pane.
- **Intentional open** (Enter / double-click) → `onActivate` → `NodeOpened` → an expensive content pane.

`DocContentWidget` (homing-studio-workspace) is the worked example: the tree navigates, the Document pane renders the opened item in place. Build *your* drill-down / table / detail view as another such sibling — never as a column on the tree.

## Hard constraints (by design, not gaps)

- **L18 depth cap.** `level.below()` is empty past 18; deeper descendants silently drop. A pathologically deep tree (a full filesystem) truncates. Fine for catalogues/docs/most hierarchies.
- **One visible dimension.** Only `DisplayLabel` is drawn. `Kind`/`Summary` ride the selection event for sibling widgets; anything richer is a sibling widget's job.
- **Structure only.** Per-node *content* (a body, a document) is the separate `DocTree` `{structure, content}` provider seam — not a tree concern.

## Verify

1. **Unit:** assert `normalize(root)` returns the expected `level`/`DisplayLabel`/`children` shape, and that `new TreeNodeJsonWriter().write(...)` produces non-empty JSON. A depth-3 fixture is enough. (`TreeNodeJsonWriterTest` / `CatalogueNormalizerTest` are the precedents.)
2. **Render:** point a `TreeWidget`-style widget (or `/catalogue-tree`-style endpoint + `TreeRenderer`) at it; confirm fold (caret + ArrowRight/Left), arrow nav, and that `onSelect` fires with the right `path`.

## What to never do

- **Write a bespoke tree renderer in JS.** If you're authoring `.js` to draw rows/carets, stop — `TreeRenderer` already does it for any `NormalizedNode`.
- **Add a second visible column / badge / inline content to a node.** That breaks the zero-JS universality. Compose a sibling widget on the navigation Party.
- **Put depth in a dimension** (`LevelDepth` etc.). Depth lives in `level()` alone; a graft touches one field per node.
- **Stamp ids on nodes.** Identity is the positional `path`.
- **Make `normalize` context-aware** (peeking at where it'll attach). Keep it standalone-`L0`; use `RigidTrees.graftUnder` for composition.

## Reference reading

- `CatalogueNormalizer` (homing-studio-base · `…studio.base.tree`) — the catalogue's normalizer; the full precedent (branch + leaf + portal graft).
- `ComposedDocNormalizer` (homing-studio-base · `…studio.base.composed`) — a doc normalized to structure.
- `TreeRendererModule.js` (homing-core-js) — the one renderer; reads only universal dimensions.
- `TreeWidget` + `DocContentWidget` (homing-studio-workspace) — the navigate-here / render-there composition.
