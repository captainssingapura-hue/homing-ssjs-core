---
name: style-homing-widget
description: Use this skill when a downstream app/studio needs to provide its OWN CSS for a custom widget or component — semantic, theme-aware, type-safe classes (not inline styles, not raw className). Triggers — "add CSS for my widget", "style a custom widget", "downstream/custom CSS", "define a CssGroup", "my widget needs its own classes", "theme-aware styles for X". The whole job is one `CssGroup` record of `CssClass` bodies + an import line in the widget; the sheet is served and the `css` binding injected automatically. Skip if the user wants to change the framework's own look (that's a theme — see create-homing-theme) or is editing framework `StudioStyles` directly.
---

# Style a Homing widget (downstream CSS)

**Written for: Homing 0.5.1** (behavior per RFC 0002-ext1 Phase 10/11 — inline-body `CssGroup`s render with **no** per-theme `CssGroupImpl`; theme cascade comes from `/theme-vars` + `/theme-globals`). Verify against your framework version if older.

Downstream code provides CSS the **type-safe** way: a `CssGroup` of `CssClass` records, applied via `css.addClass(el, my_class)`. **Never** inline `style.cssText`, raw `el.className`, or `classList` — those bypass semantic theming and fail the conformance scans. If you're hand-writing a `.css` file or a `<style>` tag, you've left this skill.

## The whole job — two steps

### 1. Declare a `CssGroup`

Each `CssClass` is a record; its snake_case simple name maps 1:1 to a kebab CSS class (`mw_panel` → `.mw-panel`). Put theme tokens (`var(--color-*)`) in `body()` so it themes for free.

```java
package com.example.widget;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

public record MyWidgetStyles() implements CssGroup<MyWidgetStyles> {

    public static final MyWidgetStyles INSTANCE = new MyWidgetStyles();

    public record mw_panel() implements CssClass<MyWidgetStyles> {
        @Override public String body() { return """
            padding: 16px;
            box-sizing: border-box;
            background: var(--color-surface);
            color: var(--color-text-primary);
            border: 1px solid var(--color-border);
            border-radius: 8px;
            """; }
    }

    public record mw_title() implements CssClass<MyWidgetStyles> {
        @Override public String body() { return """
            font: 600 18px system-ui, sans-serif;
            color: var(--color-text-primary);
            """; }
    }

    // A muted note — reuse theme tokens, never hardcode greys.
    public record mw_note() implements CssClass<MyWidgetStyles> {
        @Override public String body() { return """
            color: var(--color-text-muted);
            font-style: italic;
            """; }
    }

    @Override public CssImportsFor<MyWidgetStyles> cssImports() { return CssImportsFor.none(this); }

    @Override public List<CssClass<MyWidgetStyles>> cssClasses() {
        return List.of(new mw_panel(), new mw_title(), new mw_note());
    }
}
```

### 2. Import the classes in your widget, then `css.addClass`

In the widget's `bodyImports()` (a `DocWidget` / `WorkspaceWidget` / any `DomModule`), import the classes you use. That **one line does three things**: injects the `css` binding (via `ManagerInjector`), pulls the group into the module graph so its sheet is generated + served, and gives you the JS handles.

```java
@Override
protected List<ModuleImports<? extends Importable>> bodyImports() {
    return List.of(
            new ModuleImports<>(List.of(
                    new MyWidgetStyles.mw_panel(),
                    new MyWidgetStyles.mw_title(),
                    new MyWidgetStyles.mw_note()
            ), MyWidgetStyles.INSTANCE)
            // … your other imports …
    );
}
```

In the body JS, apply them — the `css` binding and the `mw_*` handles are in scope automatically:

```js
var panel = branch.createElement('panel', 'div');
css.addClass(panel, mw_panel);
var title = branch.createElement('title', 'div');
css.addClass(title, mw_title);
```

Done. **No `<link>`, no `Bootstrap` wiring, no registry entry, no per-theme impl.** The framework auto-generates the loader (`CssGroupContentProvider`) and serves the rule text (`/css-content?class=<canonical>&theme=<slug>`) purely because the group is now in the served module graph.

## Theming is free

Because bodies reference `var(--color-*)`, the **active theme's** `/theme-vars` supplies the values — your widget restyles across Default / Forest / Sunset / Bauhaus / … with zero extra work. Always reach for a token, never a literal colour:

| Want | Use |
|---|---|
| Page/card background | `var(--color-surface)` (`--color-surface-raised` for a lifted layer) |
| Primary text | `var(--color-text-primary)` |
| Muted/secondary text | `var(--color-text-muted)` |
| Borders | `var(--color-border)` |
| Accent | `var(--color-accent)` |

(See `StudioStyles` for the full token vocabulary in use.)

## States & variants

- **Pseudo-class:** override `pseudoState()` to render `.mw-row:hover { … }`:
  ```java
  public record mw_row_hover() implements CssClass<MyWidgetStyles> {
      @Override public String pseudoState() { return ":hover"; }
      @Override public String body() { return "background: var(--color-surface-raised);"; }
  }
  ```
- **Auto-generated hover/focus/active:** override `variants()` to return e.g. `Set.of("hover","focus")`; the framework synthesizes the extra rules and exposes `mw_row.hover` / `mw_row.focus` handles. For a utility base that wants all three, extend `UtilityCssClass`.

## When you need a per-theme `CssGroupImpl` (rare)

Only if a class needs *genuinely different rules/structure per theme* (not just different colours) — e.g. a theme that flips a layout or swaps a texture. Then ship one `CssGroupImpl` per served theme (the body of each `CssClass` is sourced by reflection from the impl's matching method) and register it in the deployment's impls list. `UtilImpl` is the worked example. The `CssGroupImplConsistencyTest` **exempts** groups whose classes all have non-null `body()`, so the inline-body path above never needs this.

## Verify

1. **Build** — your reactor compiles the `CssGroup`; the widget importing it pulls it into the graph.
2. **Conformance** — run the `CssConformanceTest` / raw-CSS scan (e.g. `homing-conformance`): it passes only if you used `css.addClass` + typed classes and no inline/raw CSS ops.
3. **Browser** — open the widget; confirm `document.querySelector('.mw-panel')` exists and that switching themes (header picker) recolours it (proves the `var()` tokens resolve through `/theme-vars`).

## What to never do

- **Inline `style.cssText` / `el.style.*`** for anything semantic — bypasses theming; fails conformance. (One-off pure-geometry values like a computed pixel width are the only grudging exception.)
- **Raw `el.className = …` / `classList.add(…)`** — use `css.addClass(el, handle)`; the typed handle is the contract.
- **Hardcode colours** (`#888`, `rgba(...)` for theme surfaces) — use `var(--color-*)`. Literals are fine only for genuinely theme-invariant values (e.g. a fixed shadow alpha).
- **Re-`import`/redeclare `css`** with `var/let/const css` — it's framework-injected; shadowing it breaks the binding (caught by `ManagerInjectionConformanceTest`).
- **Hand-author a `.css` file or `<style>` block** — the rule text comes from `CssClass.body()`, served by the framework.

## Reference reading

- `StudioStyles` (homing-studio-base · `…studio.base.css`) — the canonical inline-body `CssGroup`; the token vocabulary in practice.
- `CssContentGetAction` (homing-server) — how a group's sheet is rendered (and the `impl == null` inline-body path).
- `UtilImpl` + `CssGroupImplRegistry` (homing-studio-base · `…studio.base.theme`) — the per-theme impl pattern, if you ever need it.
- `DocContentWidget` (homing-studio-workspace) — a widget that imports typed classes and styles entirely via `css.addClass` (no inline styles).
