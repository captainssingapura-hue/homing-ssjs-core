# 0.0.110 — Typed Content Vocabulary

| Field | Value |
|---|---|
| **Version** | 0.0.110 *(binary — sixth release)* |
| **Released** | 2026-05-18 |
| **Predecessor** | [0.0.101](#ref:rel-0-0-101) |
| **Highlight** | One doctrine + four RFCs ship as a single arc. Prose Docs are no longer markdown-with-HTML-escapes — they're `ComposedDoc`s built from a sealed 5-variant `Segment` ADT (Text / Markdown / Svg / Table / Image). `TextSegment` parses a strict additive `.mdad+` grammar discovered by auditing the framework's actual prose. The case study **Why We Ditched HTML** is itself a `ComposedDoc` — the page argues the doctrine by being written in it. Purely additive; no downstream migration required. |

---

## Summary

0.0.110 is one focused arc — the typed content vocabulary the framework had been trending toward since RFC 0015 Phase 3 (SvgDoc as a first-class Doc kind) lands in full.

**[Typed Content Vocabulary](#ref:doc-tcv)** — the doctrine: *you don't really need HTML, just SVGs*. Pick a small set of typed content kinds; ban the HTML escape hatch; let each visual kind own its viewer and its conformance gate. Markdown for words, SVG for diagrams, table-as-JSON for tabular data, images by reference. That's it. Every property tech docs need from HTML — themability, semantic structure, accessibility, citability, reusability — falls out of the typed schema instead of being annotated onto it.

**[RFC 0017 — Themable Content](#ref:rfc-17)** is the first beam: typed CSS tokens (`var(--color-*)` everywhere, `currentColor` for SVG) so visual primitives inherit the active theme without per-instance styling. **[RFC 0018 — Slim Markdown](#ref:rfc-18)** spec'd the `.mdad+` grammar; the actual grammar was *discovered from audit data*, not designed — the implementation found that paragraphs + inline emphasis + lists + blockquotes + typed references cover every existing prose segment with five small grammar additions over plain text. **[RFC 0019 — ComposedDoc](#ref:rfc-19)** replaces prose-with-inline-HTML with an ordered sequence of typed `Segment`s — `MarkdownSegment` (the legacy escape-hatch carrier, kept around), `TextSegment` (the strict-grammar option), `SvgSegment` / `TableSegment` / `ImageSegment` (typed visual proxies). **[RFC 0020 — Visual Asset Docs](#ref:rfc-20)** formalises the three visual Doc kinds as first-class registered citable artefacts, each with its own viewer.

**The self-proof.** A case study, [Why We Ditched HTML](#ref:cs-why-html), is authored entirely as a `ComposedDoc` — `TextSegment` prose + a custom `SvgDoc` of the segment-ADT diagram + a `TableDoc` auditing every HTML feature against its typed replacement. The page argues the doctrine by being written in it. A Building Block doc — the [`.mdad+` Kit](#ref:mdad-kit) — likewise uses three SVG diagrams and a grammar table, all typed, all themed.

Two new conformance tests close a previously-implicit class of 404 ([Defect 0005](#ref:def-5)): the framework needs Plans registered in both `Studio.plans()` and the catalogue tree; same shape for AppModules via `ContentViewer.app()` and `Fixtures.harnessApps()`. Forgetting either side produced a tile that 404'd on click. `PlanRegistrationConformanceTest` and `ContentViewerConformanceTest` make both gates build-time failures with actionable messages. The root-cause registration-model fix is sketched in the defect with four options; choice deferred until next dual-registered kind forces it. The taxonomy gap that surfaced from the conformance family growing to nine tests is its own filed issue ([Defect 0006](#ref:def-6)).

---

## What shipped

### Framework primitives (homing-studio-base)

#### Typed segment ADT (RFC 0019)

- **`Segment` sealed interface** — five permits: `MarkdownSegment`, `TextSegment`, `SvgSegment`, `TableSegment`, `ImageSegment`. Pure ADT — no common base beyond the marker; exhaustive switch dispatch in the serialiser and (transitively) the JS renderer.
- **`ComposedDoc` record** — `Doc` of kind `"composed"`; carries `List<Segment>`; `contents()` emits a JSON envelope with the segments + a server-derived TOC; deterministic-UUID helper for code-defined docs.
- **`MarkdownSegment`** — kept as the lazy escape hatch (parses any CommonMark client-side via marked.js). Recommended for legacy migrations; new code prefers `TextSegment`.

#### Strict `.mdad+` text vocabulary (RFC 0018 / Phase 4)

- **`TextSegment`** — new permit; eager constructor-time parse so unparseable bodies never load.
- **`text/Block` sealed ADT** — four variants: `Para`, `Bullets`, `Numbered`, `Quote`.
- **`text/Inline` sealed ADT** — five variants: `Text`, `Bold`, `Italic`, `Code`, `Ref`. Bold/italic refuse to nest (no `***` ambiguity); empty inline code rejected with a "promote to CodeSegment" hint.
- **`text/TextParser`** — ~180-line four-pass strict parser; throws `ParseException(line, column, message)` on any malformed input. The grammar simply doesn't recognise `<`, `#`, or fenced code as tokens — angle-bracket markup comes through as literal text; HTML escape hatches are inexpressible.
- **22-case `TextParserTest`** pinning the grammar against positive + negative scenarios — the discipline lives in test cases that prove what's accepted and what's refused.

#### Visual asset Docs (RFC 0020)

- **`SvgDoc<G>`** + **`SvgViewer`** + **`SvgContentViewer`** — kind `"svg"`; classpath-shipped vector via typed `SvgRef`. Themable via `currentColor` and `var(--color-*)` tokens (RFC 0017). UUID derived from resource path.
- **`TableDoc`** + **`TableData`** (with `Cell` / `Badge` / `Align`) + CSV ingestion factory + **`TableViewer`** + **`TableViewerRenderer`** (Java + JS) + **`TableContentViewer`** — kind `"table"`; slim feature set per RFC 0020 §2.2 (colspan/rowspan/badges/align; no formulas, no interactivity). JSON envelope on the wire; themed cell CSS via 10 new `st_table*` / `st_td_*` classes.
- **`ImageDoc`** + **`ImageViewer`** + **`ImageViewerRenderer`** (Java + JS) + **`ImageContentViewer`** — kind `"image"`; classpath raster (PNG/JPG/WebP); required `alt`; optional caption + intrinsic dimensions. Raw tier per RFC 0017 — no theming attempted on the raster, themed chrome around it (3 new `st_image_*` classes). Bytes shipped inline as a base64 data URL in a small JSON envelope; no new server endpoint required.

#### ComposedViewer

- **`ComposedViewer`** + **`ComposedViewerRenderer`** (Java + JS) — extends `DocViewer` (V11 chrome composition); fetches the JSON envelope, renders a 2-column layout (TOC sidebar + body), dispatches per segment kind. The `TextSegment` renderer is a pure data walk over the parsed AST (~90 lines of JS, no markdown library imported). Visual segment renderers delegate to `TableViewerRenderer.renderTable` and `ImageViewerRenderer.renderImage` so the same code path serves standalone viewers and inline segment-rendering.

#### Data-authored ContentTree (RFC 0016)

- **`ContentTree`** + **`TreeNode`** sealed sum + **`TreeBranch`** + **`TreeLeaf`** records — data-authored hierarchical content surface, sibling to the typed code-authored `Catalogue` family.
- **`TreeAppHost`** + **`TreeGetAction`** + **`TreeRegistry`** + **`ContentRef`** — auto-wired by `Bootstrap` when `Fixtures.trees()` is non-empty. URL contract: `/app?app=tree&id=<slug>` for root, `/app?app=tree&id=<slug>&path=<segment>/<segment>` for branches.
- **`DocRegistry.harvestFromTrees(...)`** — picks up the Doc wrapped by every `TreeLeaf` so tree-only Docs land in the registry without a parallel registration path.

#### Wiring

- **`Fixtures.contentViewers()`** default extended with `SvgContentViewer`, `ComposedContentViewer`, `TableContentViewer`, `ImageContentViewer` — every new kind gets dispatched out of the box.
- **`DefaultFixtures.harnessApps()`** extended with `SvgViewer`, `ComposedViewer`, `TableViewer`, `ImageViewer` — so the app resolver knows the names referenced by the content viewers.
- **`DocRegistry.harvestSyntheticFromLeaves(...)`** extended for `ComposedDoc`, `TableDoc`, `ImageDoc` — catalogue leaves wrapping these kinds reach `DocRegistry` automatically.
- **`CatalogueHostRenderer.js`** dispatch extended for `"composed"`, `"table"`, `"image"`, `"svg"` tile kinds.
- **`DocViewer`** abstract base — V11 axiom in the type system: every Doc viewer that extends `DocViewer<P, M>` automatically inherits framework chrome (Header + brand + breadcrumb + theme picker). Concrete viewers supply only the kind-specific body JS.

### Conformance (homing-conformance)

- **`ContentViewerConformanceTest`** — for each `Fixtures.contentViewers()` entry, asserts the bound `app()` instance is present (by class) in the studio's app closure. Catches "registered the viewer but forgot the AppModule" — the failure mode that produced "No app registered with this simple name: composed-viewer" before the test landed.
- **`PlanRegistrationConformanceTest`** — for each Plan wrapped as an `Entry.OfDoc(PlanDoc(plan))` catalogue leaf, asserts the plan is also in some `Studio.plans()`. Catches "tile appears but `/plan?id=…` 404s with Plan not registered" — Defect 0005's exemplar bug.
- Both abstract bases mirror the existing `DocConformanceTest` / `DoctrineConformanceTest` shape; downstream studios provide inputs via a one-method override and run as `@TestFactory` dynamic tests with precise per-failure messages.

### Studio (homing-studio)

#### Doctrine + RFCs

- **[Typed Content Vocabulary doctrine](#ref:doc-tcv)** — *you don't really need HTML, just SVGs*. Names the principle, the five typed kinds the framework provides, and the four properties (themability, scannability, reusability, citability) that fall out of refusing the HTML escape hatch.
- **[RFC 0017 — Themable Content](#ref:rfc-17)** — three theming tiers (Themed / Tokenised / Raw); the `currentColor` + `var(--color-*)` discipline; per-kind tier assignment.
- **[RFC 0018 — Slim Markdown (.mdad+)](#ref:rfc-18)** — the strict additive grammar with explicit exclusions; spec'd from the audit, not designed top-down.
- **[RFC 0019 — ComposedDoc](#ref:rfc-19)** — the sealed Segment ADT; the no-cross-segment-references constraint; the canonical-vs-proxy distinction for visual segments.
- **[RFC 0020 — Visual Asset Docs](#ref:rfc-20)** — `SvgDoc` + `TableDoc` + `ImageDoc` as first-class Doc subtypes; slim discipline for tables; Raw tier for images.

#### Defects

- **[Defect 0005 — Two-Source Registration Drift](#ref:def-5)** — Open. The root issue Plan + AppModule both manifest. Documented with reproduction for both, four resolution-sketch options with tradeoffs, decision deferred until the next dual-registered kind forces it. Backstopped by the two new conformance tests.
- **[Defect 0006 — ConformanceTest Family Lacks a Taxonomy](#ref:def-6)** — Open. With nine conformance tests in the family, there's no map showing which invariants are covered or which gaps remain. Proposes a five-family taxonomy (registration consistency / wire-surface integrity / discipline enforcement / shape conformance / pattern adherence) and a Building Block doc as the canonical resolution.

#### Plan tracker

- **`TypedContentVocabularyPlanData`** — covers the full arc. Six phases tracked; this release ships Phases 1–5 (ComposedDoc PoC → Visual Asset Docs → Segment ADT → `.mdad+` grammar → self-proof case study). Phase 6 (retrofit existing prose Docs) deferred to on-touch migration per the user's call.

#### Content authored in the new vocabulary

- **[Case study — Why We Ditched HTML](#ref:cs-why-html)** — 10-segment `ComposedDoc`: text prose + the segment-ADT diagram (custom `SvgDoc` via `WhyWeDitchedHtmlSvgs`) + the comparison `TableDoc` auditing every HTML feature. Self-proof: the doctrine is true if the page renders.
- **[Building Block — `.mdad+` Kit](#ref:mdad-kit)** — 12-segment `ComposedDoc` documenting the typed text vocabulary: 3 custom SVG diagrams (AST shape, parse pipeline, render flow) via `MdadKitSvgs` + the T0..T4 grammar `TableDoc` + prose between. The kit documenting the kit, built on the kit.
- **`ContentDoctrinesCatalogue`** — new L3 sub-catalogue under `DoctrineCatalogue` housing content-discipline doctrines.

### Skills (homing-skills)

- **NEW `create-homing-content-tree` skill** — full walkthrough for the RFC 0016 ContentTree authoring path (records, Fixtures wiring, optional SVG group). Includes the Catalogue-vs-ContentTree decision matrix + 6-row pitfalls table + sibling-skill pointers.
- **Updated `create-homing-studio`** — new "Doc kinds available" section enumerating all 9 typed Doc subtypes with picker guidance; ComposedDoc + TextSegment marked as the recommended prose path; Plans guidance expanded with the dual-registration warning (Defect 0005); conformance baseline gains `ContentViewerConformanceTest` + `PlanRegistrationConformanceTest`; pointer to the new content-tree skill.
- **Updated `create-homing-component`** — decision tree gains a *Step 0 — typed content vocabulary* branch routing data-shaped requests to typed Doc kinds before the component-build paths.
- **Updated `homing-skills-bootstrap`** — orientation map gains the content-tree skill + the previously-missing `migrate-from-0-0-100` row.

### Demo (homing-demo)

- **`AnimalsTree`** — RFC 0016 demo: two branches (Animals / Halloween), six SVG leaves.
- **`DemoFixtures`** — overrides `Fixtures.trees()` to register `AnimalsTree`.
- **`ComposedDemoDoc`** — 10-segment `ComposedDoc` interleaving every Phase-4 segment kind on one page; pinned by record-value equality to the shared turtle `SvgDoc` from `AnimalsTree`.
- **`TableDemoDoc`** — standalone `TableDoc` (phase-status roll-up with badges + alignment).
- **`ImageDemoDoc`** — standalone `ImageDoc` referencing a 240×120 placeholder PNG generated via `System.Drawing`.
- **`DemoContentViewerConformanceTest`** + **`DemoPlanRegistrationConformanceTest`** — subclasses of the two new conformance bases; mirror the multi-studio demo umbrella; gate the framework's CI.

---

## Numbers

| Module | Tests passing | Net file count |
|---|---|---|
| `homing-core` | 150 | unchanged |
| `homing-server` | 13 | unchanged |
| `homing-libs` | (no tests) | unchanged |
| `homing-studio-base` | 49 | +20 (composed/, table/, image/, tree/, DocViewer abstract base, SvgDoc + SvgViewer + SvgContentViewer, ComposedViewerRenderer, TableViewerRenderer, ImageViewerRenderer); +1 JS file (ComposedViewerRenderer.js); ~13 new CSS classes in StudioStyles |
| `homing-conformance` | 26 | +2 (ContentViewerConformanceTest, PlanRegistrationConformanceTest) |
| `homing-studio` | 279 | +1 doctrine, +4 RFCs, +2 defects, +1 case study + supporting SvgGroup + 1 SVG resource, +1 Building Block (MdadKit) + supporting SvgGroup + 3 SVG resources, +1 release doc, +1 new L3 catalogue (ContentDoctrinesCatalogue), +1 plan tracker (TypedContentVocabularyPlanData) |
| `homing-skills` | (no tests) | +1 new skill (create-homing-content-tree) + matching Java Doc record, +3 updated skills |
| `homing-demo` | 115 | +5 new demos (AnimalsTree, DemoFixtures, ComposedDemoDoc, TableDemoDoc, ImageDemoDoc) + 1 binary resource (demo.png), +2 conformance subclasses |

**632 tests passing across the reactor.** No failing tests. No skipped tests. Conformance suites green across the board, including the two new ones (both verified by temporarily reverting their fixes and watching them fail with actionable messages).

---

## Compatibility

**No breaking changes for downstream studios.** Every new typed Doc kind, segment variant, viewer, and conformance test is purely additive:

- Existing `ClasspathMarkdownDoc` Docs keep working unchanged — `MarkdownSegment` remains a valid `Segment` permit and the framework continues to dispatch markdown through the original `DocReader` for the `"doc"` kind.
- Existing AppModules and ContentViewers continue to work — the two new conformance tests are *additional* gates that only fire if you've already drifted into a registration-half-done state.
- Existing custom `Fixtures<S>` implementations continue to compile — the new defaults in `Fixtures.contentViewers()` and `DefaultFixtures.harnessApps()` are picked up automatically unless your custom Fixtures returns its own non-empty lists (in which case you opt in to the new kinds by adding them).

**Migration policy for existing prose Docs.** *On-touch only.* The release deliberately does not retrofit the ~74 existing `ClasspathMarkdownDoc` Docs to `ComposedDoc`; per the survey in the prior session, ~84% of them contain fenced code blocks that the typed segment family doesn't yet cover (a future `CodeSegment` kind is unblocked but unscheduled). When a code-heavy doc is touched for unrelated reasons, three options exist: build `CodeSegment` inline, fall back to `MarkdownSegment` inside a `ComposedDoc`, or stay on `ClasspathMarkdownDoc`. All three are defensible.

**Doctrinal change going forward.** The Typed Content Vocabulary doctrine binds new prose pages — `TextSegment` over `MarkdownSegment` is the recommended path, with `MarkdownSegment` remaining valid as the lazy escape hatch. The case study and the `.mdad+` Kit doc are the canonical examples; the `create-homing-studio` skill's "Doc kinds available" section is the agent-facing reference.

**Browser support floor** unchanged from 0.0.101.

**Maven artifact version** stays at `1.0-SNAPSHOT`. Release identity is the git tag + this Doc.

---

## What's next

The work paths visible from 0.0.110's vantage:

- **`CodeSegment` kind** — the missing visual-asset Doc kind the audit surfaced. Same shape as `TableDoc` / `ImageDoc`; one Java doc record + one viewer + one content viewer + one Segment permit. Build when the first code-heavy doc is touched.
- **Root-cause fix for Defect 0005** — four options sketched in the defect; pick one when the next dual-registered kind appears or when the conformance backstop misses one.
- **Conformance Kit Building Block doc** — the Defect 0006 resolution; a `ComposedDoc` listing every conformance test with its taxonomy slot and origin link. Would close the family's documentation debt and set the convention for future test additions.
- **References rendering in `ComposedViewer`** — the `DocReader` page emits a `References` section from `/doc-refs` for typed `[label](#ref:name)` resolution; `ComposedViewer` currently doesn't. Wiring it through would let `TextSegment` body refs render as live cross-doc links.
- **AccessibilityAnnotation** stays *refused* — the case study's audit reclassified ARIA from DEFERRED to REFUSED, and the doctrine's stance is that the typed schema IS the accessibility surface (extended via new typed fields, never via parallel annotation). Surfaces if a real access need exposes a missing typed field.

The typed content vocabulary is in. The next releases can be smaller again.
