# Downstream Studio Pattern — Bootstrap a Studio with `homing-studio-base`

How to spin up a new studio project (homepage + apps + typed markdown docs +
theme picker) on top of `homing-studio-base` in **one file** plus per-app pairs.

This guide is written for coding agents. Each section gives a concrete
template you can paste in, a one-line verification command, and the failure
mode if the template is wrong. Skim the templates; copy the matching ones
for your project; ignore the rest.

---

## What you get for free

Depending on `homing-studio-base` and calling `StudioBootstrap.start(...)`
gives you:

| Endpoint | Behaviour |
|---|---|
| `/`                       | redirects to the home **catalogue** (configured via `StudioBrand`) |
| `/app?app=<name>`         | renders any registered `AppModule`, kebab-cased simple name |
| `/app?app=catalogue&id=…` | shared `CatalogueAppHost` — serves any registered `Catalogue` by class FQN (RFC 0005) |
| `/app?app=plan&id=…`      | shared `PlanAppHost` — serves any registered `Plan` tracker by class FQN (RFC 0005-ext1) |
| `/catalogue?id=…`         | JSON payload for one catalogue (entries, breadcrumbs, brand) |
| `/plan?id=…`              | JSON payload for one plan (objectives, decisions, phases, acceptance) |
| `/module`                 | served ES module per Java module (auto-imports + nav + params) |
| `/css-content`            | typed `CssGroupImpl`-backed stylesheets, theme-keyed |
| `/css-content?class=…GlobalColorPalette` | the global palette for the active theme (RFC 0066 — the prior; `/theme-vars` is gone) |
| `/theme-globals`          | global CSS rules (light/dark `@media` overrides) |
| `/doc`                    | classpath markdown / html / txt / json / svg by `?path=` |

Plus: a fixed-position **theme picker** in every page (top-right), the
**Default / Forest / Sunset** themes, light/dark adaptation per theme, an
**internal default** for `?theme=` so the URL stays clean, and a
**typed-container model** where pages that browse collections (RFCs, doctrines,
plan trackers) are stateless `Catalogue` records served by a single shared
`CatalogueAppHost`, and progress trackers are stateless `Plan` records served
by a single shared `PlanAppHost`.

---

## When this pattern fits

Reach for this whenever you want a Java-backed reference site, plan tracker,
or design tool that:

- has a small fixed set of "apps" (homepage + a handful of pages)
- serves typed reference docs from the classpath
- benefits from typed Java-side declarations (so renames are compile errors)
- doesn't need user accounts, persistence, or server-side state

If the project is a public-facing product UI, generic CRUD app, or anything
needing auth / DB persistence, this is the wrong stack.

---

## Project layout

A studio is a Maven multi-module with three pieces:

```
my-studio/
├── pom.xml                          ← parent: dependency management only
├── my-docs/                         ← markdown + typed Doc records
│   ├── pom.xml
│   └── src/main/
│       ├── java/<pkg>/MyDocs.java
│       └── resources/docs/**.md
└── studio-main/                     ← server entry + AppModules + JS views
    ├── pom.xml
    └── src/main/
        ├── java/<pkg>/MyStudioServer.java
        ├── java/<pkg>/MyHome.java   ← AppModule per page
        └── resources/homing/js/<pkg>/MyHome.js
```

**Why split docs vs. studio-main?** The docs module is a reusable data
artifact — a different studio (or a CLI) can depend on it without pulling in
the Vert.x server. If you don't anticipate that reuse, a single module is
fine.

### Parent `pom.xml`

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example.studio</groupId>
    <artifactId>my-studio</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <homing.version>0.0.1</homing.version>
    </properties>

    <modules>
        <module>my-docs</module>
        <module>studio-main</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.github.captainssingapura-hue.homing.js</groupId>
                <artifactId>homing-studio-base</artifactId>
                <version>${homing.version}</version>
            </dependency>
            <dependency>
                <groupId>io.github.captainssingapura-hue.homing.js</groupId>
                <artifactId>homing-libs</artifactId>
                <version>${homing.version}</version>
            </dependency>
            <dependency>
                <groupId>${project.groupId}</groupId>
                <artifactId>my-docs</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### `my-docs/pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>io.github.captainssingapura-hue.homing.js</groupId>
        <artifactId>homing-studio-base</artifactId>
    </dependency>
</dependencies>
```

### `studio-main/pom.xml`

```xml
<dependencies>
    <dependency>
        <groupId>${project.groupId}</groupId>
        <artifactId>my-docs</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.captainssingapura-hue.homing.js</groupId>
        <artifactId>homing-studio-base</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.captainssingapura-hue.homing.js</groupId>
        <artifactId>homing-libs</artifactId>
    </dependency>
</dependencies>
```

---

## The server entry — one file

`studio-main/src/main/java/<pkg>/MyStudioServer.java`:

```java
package com.example.studio;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.StudioBootstrap;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.app.StudioBrand;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost;

import java.util.List;

public class MyStudioServer {
    public static void main(String[] args) {

        // 1. AppModules — top-level pages. Almost every studio uses these four
        //    shared hosts; add your own custom AppModules for app-level pages
        //    (calculators, editors, anything that isn't a Catalogue or a Plan).
        List<AppModule<?>> apps = List.of(
                CatalogueAppHost.INSTANCE,   // /app?app=catalogue&id=<fqn>
                PlanAppHost.INSTANCE,        // /app?app=plan&id=<fqn>
                DocReader.INSTANCE,          // /app?app=doc-reader&doc=<uuid>
                MyDocBrowser.INSTANCE        // your DocBrowser, or use the shared one
        );

        // 2. Catalogues — typed structural containers (RFC 0005). Stateless
        //    records implementing Catalogue. The first one (or any one) can be
        //    your home; downstream picks via StudioBrand.
        List<Catalogue> catalogues = List.of(
                MyHomeCatalogue.INSTANCE,
                MyDoctrinesCatalogue.INSTANCE,
                MyJourneysCatalogue.INSTANCE
        );

        // 3. Plans — typed multi-phase trackers (RFC 0005-ext1). Optional;
        //    pass List.of() if you don't have any.
        List<Plan> plans = List.of(
                /* MyMigrationPlanData.INSTANCE, … */
        );

        // 4. Brand — per-installation label + which catalogue is "home".
        StudioBrand brand = new StudioBrand("My Studio", MyHomeCatalogue.class);

        StudioBootstrap.start(8081, apps, catalogues, plans, brand);
    }
}
```

That's the whole server. **`/` redirects to the home catalogue** — the one
named in `StudioBrand`.

### `StudioBootstrap.start(...)` overloads

`StudioBootstrap` ships several overloads for legacy and constrained cases.
**The signature shown above** —
`start(port, apps, catalogues, plans, brand)` — is the preferred one for
new code. The other overloads exist for back-compat:

| When you have | Call |
|---|---|
| Catalogues + plans + brand (typical) | `start(port, apps, catalogues, plans, brand)` |
| Catalogues + brand, no plans yet      | `start(port, apps, catalogues, List.of(), brand)` |
| AppModules only (legacy / minimal)    | `start(port, apps)` — no `/`-redirect, no catalogue listings |
| Custom theme registry / extra routes  | longer overload — see the **Custom themes** section |

**Verify:** `curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8081/`
should print `200` (redirects to your home catalogue), and the body should be
a tiny `<meta http-equiv="refresh">` page pointing at
`/app?app=catalogue&id=<MyHomeCatalogue-FQN>`.

---

## Anatomy of a `Catalogue` (RFC 0005)

A catalogue is a **stateless record** — pure structural data. The shared
`CatalogueAppHost` renders it. No per-catalogue `AppModule`, no JS, no CSS;
identity = the implementing Java class, display data = `name()` + `summary()`,
content = the typed `entries()` list.

```java
public record MyHomeCatalogue() implements Catalogue {

    public static final MyHomeCatalogue INSTANCE = new MyHomeCatalogue();

    @Override public String name()    { return "My Studio"; }
    @Override public String summary() { return "A workspace for project artifacts."; }

    @Override public List<Entry> entries() {
        return List.of(
                Entry.of(MyDoctrinesCatalogue.INSTANCE),     // sub-catalogue
                Entry.of(MyJourneysCatalogue.INSTANCE),
                Entry.of(MyDocs.Readme.INSTANCE),            // a Doc
                Entry.of(MyMigrationPlanData.INSTANCE),      // a Plan
                // An AppModule entry — wrap in a typed Navigable that binds the
                // app to its Params (use AppModule._None.INSTANCE for paramless)
                // and supplies the tile's display name + summary.
                Entry.of(new Navigable<>(
                        MyDocBrowser.INSTANCE,
                        AppModule._None.INSTANCE,
                        "Documents",
                        "Browse all docs."))
        );
    }
}
```

`Entry` is sealed: `OfDoc | OfCatalogue | OfApp | OfPlan`. Pick the
right `Entry.of(...)` for each item; the shared renderer handles the rest.

For `OfApp`, the wrapping `Navigable<P, M>` carries the typed binding
(AppModule + its Params record) plus the tile's user-facing name and
summary. The compiler enforces that `P` matches the AppModule's declared
params type, so a "wrong params for app" mistake fails at the construction
site — no silent broken URLs.

For full details (validations, identity rules, render contract): see
**`CatalogueKitDoc`** in the studio's Building Blocks catalogue.

---

## Anatomy of a `Plan` tracker (RFC 0005-ext1)

A plan is a **two-file tracker** — `XxxSteps.java` (source-of-truth data:
phases, decisions, objectives, acceptance) + `XxxPlanData.java` (an adapter
implementing `Plan`). The shared `PlanAppHost` renders it. No per-tracker
`AppModule`, no JS, no CSS.

```java
public final class MyMigrationPlanData implements Plan {
    public static final MyMigrationPlanData INSTANCE = new MyMigrationPlanData();

    @Override public String kicker() { return "MIGRATION"; }
    @Override public String name()   { return "Service Mesh Rollout"; }

    @Override public List<Decision>   decisions()  { /* compiler-enforced pillar 1 */ }
    @Override public List<Phase>      phases()     { /* compiler-enforced pillar 2 */ }
    @Override public List<Acceptance> acceptance() { /* compiler-enforced pillar 3 */ }
    @Override public List<Objective>  objectives() { /* optional 4th pillar */ }
}
```

For the full recipe (decision/phase shapes, registry validations,
URL contract, conformance): see **`PlanKitDoc`** in the studio's Building
Blocks catalogue.

---

## Anatomy of a custom `AppModule`

> **When to reach for this**: app-level pages with custom logic — a calculator,
> a graph editor, a search box, anything that isn't *just* "list these docs"
> (use `Catalogue`) or "track these phases" (use `Plan`). For the common case
> of browsing collections of docs/plans/sub-catalogues, prefer `Catalogue` —
> see above.

Every custom page is one Java class + one JS file. The Java side declares typed
imports (CSS, nav targets, libs); the framework auto-emits the corresponding
`import { … } from "/module?class=…"` lines into the served JS, so the JS
can reference those bindings as plain consts.

### `MyHome.java`

```java
public record MyHome() implements AppModule<MyHome> {

    record appMain() implements AppModule._AppMain<MyHome> {}
    public record link() implements AppLink<MyHome> {}
    public static final MyHome INSTANCE = new MyHome();

    @Override public String title() { return "My studio"; }

    @Override
    public ImportsFor<MyHome> imports() {
        return ImportsFor.<MyHome>builder()
                // Nav targets — every app you'll link to needs its `link()` here.
                .add(new ModuleImports<>(List.of(new MyHome.link()),       MyHome.INSTANCE))
                .add(new ModuleImports<>(List.of(new MyDocBrowser.link()), MyDocBrowser.INSTANCE))
                // Studio chrome — the CSS classes your JS references.
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_header(),
                        new StudioStyles.st_brand(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_title(),
                        new StudioStyles.st_subtitle(),
                        new StudioStyles.st_section()
                        // …list every st_* you actually use. Unlisted classes
                        // are still type-checked but won't be exported into
                        // your JS module — referencing them errors out.
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<MyHome> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }
}
```

### `MyHome.js`

`studio-main/src/main/resources/homing/js/<pkg>/MyHome.js` — path mirrors
the Java package exactly. Plain ES6: the framework prepends every `import`
itself (typed CSS classes, nav, params, doc handles, libs), so the file
contains only the `appMain` function body and any helpers it needs:

```js
function appMain(rootElement) {

    function cn() {
        var parts = [];
        for (var i = 0; i < arguments.length; i++) {
            if (arguments[i]) parts.push(css.className(arguments[i]));
        }
        return parts.join(" ");
    }

    rootElement.innerHTML = ''
        + '<div class="' + cn(st_root) + '">'
        + '  <div class="' + cn(st_header) + '">'
        + '    <a class="' + cn(st_brand) + '" ' + href.toAttr(nav.MyHome()) + '>My studio</a>'
        + '  </div>'
        + '  <div class="' + cn(st_main) + '">'
        + '    <h1 class="' + cn(st_title) + '">Hello</h1>'
        + '    <p class="' + cn(st_subtitle) + '">'
        + '      <a ' + href.toAttr(nav.MyDocBrowser()) + '>Docs</a>'
        + '    </p>'
        + '  </div>'
        + '</div>';
}
```

**Auto-injected globals available in every `.js`:**
- `nav.<AppName>(params?)` — typed URL builder per app you imported (RFC 0001)
- `params.<name>` — typed query params from your `Params` record (see "Typed query params")
- `css` — `CssClassManager` instance (`css.className`, `addClass`, `toggleClass`, etc.)
- `href` — `HrefManager` (`href.toAttr(url)` produces a properly-escaped `href="…"` attribute)
- Each imported CSS class as a const (`st_root`, `st_header`, …)
- Each imported Doc record / Lib symbol as a const

**Verify:** `curl -s "http://localhost:8081/app?app=my-home" | grep title`
returns the rendered page; `curl -s "http://localhost:8081/module?class=com.example.studio.MyHome" | head -5`
shows the auto-prepended `import` block + your `appMain` function + `export {appMain}`.

---

## Typed query params

For pages that take URL parameters (e.g. a doc reader takes `?path=…`),
declare a `Params` record:

```java
public record MyDocReader() implements AppModule<MyDocReader> {
    public record Params(String path) {}
    @Override public Class<?> paramsType() { return Params.class; }
    // … rest as MyHome
}
```

The framework emits a frozen `params` const into the served JS:
```js
// auto-generated
const params = Object.freeze({ path: (sp.get("path") || "") });
```

Build links to this app from other apps with `nav.MyDocReader({path: "x"})`.

---

## Typed Docs (markdown reference)

Drop `.md` files under `my-docs/src/main/resources/docs/` then declare them
as records inside a `DocGroup`:

`my-docs/src/main/java/<pkg>/MyDocs.java`:

```java
public record MyDocs() implements DocGroup<MyDocs> {

    public static final MyDocs INSTANCE = new MyDocs();

    public record Readme() implements MarkdownDoc<MyDocs> {
        @Override public String path()     { return "docs/README.md"; }
        @Override public String title()    { return "README"; }
        @Override public String category() { return "OVERVIEW"; }
    }
    public record GettingStarted() implements MarkdownDoc<MyDocs> {
        @Override public String path()     { return "docs/getting-started.md"; }
        @Override public String title()    { return "Getting started"; }
        @Override public String category() { return "GUIDE"; }
    }
    // …one record per .md file

    @Override
    public List<Doc<MyDocs>> docs() {
        return List.of(new Readme(), new GettingStarted());
    }
}
```

Doc kinds beyond markdown — `HtmlDoc`, `PlainTextDoc`, `JsonDoc`, `SvgDoc`,
or `Doc<D>` directly with custom `contentType()` + `fileExtension()`.

A doc browser `AppModule` imports these records and projects them through
the auto-injected `docs` manager:

```javascript
// in MyDocBrowser.js — every Doc record imported by name is a const here.
var allDocs = [Readme, GettingStarted, /* … */];
allDocs.forEach(function(d) {
    console.log(docs.title(d), docs.summary(d), docs.category(d), docs.url(d));
    // docs.fetch(d) → Promise<string> body via /doc?path=…
});
```

A reader fetches the body and renders it; for markdown, depend on
`MarkedJs` from `homing-libs`:

```java
.add(new ModuleImports<>(List.of(new MarkedJs.marked()), MarkedJs.INSTANCE))
```

```js
// in the reader's .js
fetch("/doc?path=" + encodeURIComponent(params.path))
    .then(function(r) { return r.text(); })
    .then(function(md) { bodyEl.innerHTML = marked.parse(md); });
```

**Verify:** `curl "http://localhost:8081/doc?path=docs/README.md"` returns
the markdown body; the browser's served JS module has the line
`const Readme = _docs.doc("docs/README.md", …)` near the top.

---

## Custom themes (optional)

`StudioThemeRegistry.INSTANCE` ships Default / Forest / Sunset. To add your
own theme alongside them, declare a `Theme` record + a `GlobalColorPalette.Provision<TH>` (RFC 0066) +
`ThemeGlobals<TH>` mirroring `HomingDefault.java`, then compose a registry
that includes both yours and the studio's:

```java
public record MyThemeRegistry() implements ThemeRegistry {
    public static final MyThemeRegistry INSTANCE = new MyThemeRegistry();

    @Override public List<Theme>             themes()    {
        return List.concat(StudioThemeRegistry.INSTANCE.themes(), List.of(MyTheme.INSTANCE));
    }
    @Override public List<PaletteProvision<?, ?>> palettes() { /* same pattern */ }
    @Override public List<ThemeGlobals<?>>   globals()   { /* same pattern */ }
}
```

Pass it to `StudioBootstrap.start(port, apps, MyThemeRegistry.INSTANCE, MyTheme.INSTANCE, …)`.
The picker auto-renders your theme as an extra `<option>`.

For the variable vocabulary (`COLOR_SURFACE`, `COLOR_TEXT_PRIMARY`,
`SPACE_*`, `RADIUS_*`), see `StudioVars.java`.

---

## Conformance baseline

CI guardrails for downstream studios. Each is a small abstract test base; you
extend it with a 4-line subclass that lists the modules to scan. Recommended
set:

| Test base | What it pins | Source |
|---|---|---|
| `CssConformanceTest`            | No raw `.classList` / `.className` ops in DomModule JS | `homing-conformance` |
| `HrefConformanceTest`           | No raw `href=` / `window.location` / `.href` ops | `homing-conformance` |
| `CdnFreeConformanceTest`        | No `<script src="//cdn.…">` or external network deps | `homing-conformance` |
| `DoctrineConformanceTest`       | Pure-component views + owned references | `homing-conformance` |
| `DocConformanceTest`            | Every `#ref:<name>` anchor in markdown maps to a declared `Reference` | `homing-conformance` |
| `CssGroupImplConsistencyTest`   | Every `CssGroup` has matching `CssGroupImpl` for each theme | `homing-conformance` |
| `ManagerInjectionConformanceTest` | No `var/let/const <name>` redeclaration of framework-auto-injected identifiers (`href`, `css`, …) — catches the `Identifier 'href' has already been declared` browser-side SyntaxError | `homing-conformance` |
| `StudioCatalogueConstructsTest` pattern | Your `CatalogueRegistry` constructs cleanly (strict tree, doc reachability, no cycles) | bespoke ~30 LoC, see `homing-studio` |
| `StudioPlanConstructsTest` pattern      | Your `PlanRegistry` constructs cleanly (phase/decision id uniqueness, doc reachability, dependency targets resolve) | bespoke ~30 LoC, see `homing-studio` |

A single test class can wire all 7 abstract bases plus the two construct
tests. The 4-line shape:

```java
class MyManagerInjectionConformanceTest extends ManagerInjectionConformanceTest {
    @Override protected List<EsModule<?>> esModules() {
        return List.of(MyHome.INSTANCE, MyDocBrowser.INSTANCE, /* … */);
    }
}
```

The conformance walks the import graph transitively from each root, so
listing top-level AppModules is enough — renderers and shared element
modules are scanned automatically.

---

## Cookbook of pitfalls

| Symptom | Likely cause |
|---|---|
| `404 No app registered with this simple name` | Forgot to add the app to the `List` passed to `StudioBootstrap.start` |
| `404 No catalogue registered with this id` | Forgot to add the `Catalogue` to the catalogues list, or the FQN in the URL is stale |
| `Catalogue X references Doc Y which is not in the DocRegistry` | A `Doc` is referenced by an `Entry.of(doc)` but its `DocGroup` isn't reachable through any registered AppModule or Catalogue |
| `Identifier 'href' has already been declared` (browser console) | Manually declared `var href = HrefManagerInstance;` in a module that also imports an `AppLink` — the framework auto-injects `href` for AppLink importers. Remove the manual declaration. Pinned by `ManagerInjectionConformanceTest` |
| `nav.SomeApp` is undefined in the JS | Forgot `.add(new ModuleImports<>(List.of(new SomeApp.link()), SomeApp.INSTANCE))` in the importing app |
| CSS class works but is unstyled | Forgot to list the `st_*` class in the `StudioStyles` import block; or the class is misspelt — Java is case-sensitive |
| Doc browser missing some docs | Forgot to add the new `Doc` record to `MyDocs.docs()` list |
| `/doc?path=…` returns 404 | Path must start `docs/…` and end in a registered extension; no `..`, no leading slash |
| Theme picker doesn't appear | `themeRegistry.themes().size() < 2` — picker hides itself when there's only one option |
| All pages render unstyled (white background) | `StudioStyles` not imported, or the bundle endpoints failed (check `/css-content?class=…GlobalColorPalette&theme=default` returns CSS, not 404) |

---

## Reference layout — verified end-to-end

The `notation-studio` project at `Q:/repos/music/notation-studio` is a
working reference. Three modules, ~500 LoC of Java + JS, 31 typed Docs.
Every pattern above is in use there.

---

## Build + run

```bash
mvn install               # at parent
java -cp "studio-main/target/classes;<every dep jar>" com.example.studio.MyStudioServer
```

Or for a fat-jar / `exec:java` setup, add the standard
`maven-shade-plugin` / `exec-maven-plugin` configuration to `studio-main/pom.xml`.

After launch:

```bash
curl -s -o /dev/null -w 'root=%{http_code}\n'    http://localhost:8081/
curl -s -o /dev/null -w 'home=%{http_code}\n'    "http://localhost:8081/app?app=my-home"
curl -s -o /dev/null -w 'css=%{http_code}\n'     "http://localhost:8081/css-content?class=hue.captains.singapura.js.homing.studio.base.css.StudioStyles"
curl -s -o /dev/null -w 'theme=%{http_code}\n'   "http://localhost:8081/css-content?class=hue.captains.singapura.js.homing.studio.base.theme.GlobalColorPalette&theme=default"
```

Any non-200 → check the cookbook above.
