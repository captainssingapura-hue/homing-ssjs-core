# RFC 0021 — Codebase Separation

| Field | Value |
|---|---|
| **Status** | Proposed |
| **Author** | Howard, with Homing |
| **Filed** | 2026-05-18 |
| **Target release** | 0.0.111 — the first framework-only release post-extraction |
| **Builds on** | [RFC 0011 — Cross-tree Composition](#ref:rfc-11), [RFC 0012 — Typed Studio Composition](#ref:rfc-12) |
| **Operational sibling** | `SEPARATION_PLAN.md` (lives at the legacy repo root through Phase 3; archived after execution completes) |
| **Scope** | Separate the monorepo into four repos by audience — framework / public docs+demos / private maintainer surface, with a fifth (components) deferred. Top-down migration order; public/private firewall ships first. |

---

## 1. Motivation

The framework, its design history, its demos, and the maintainer's in-flight working notes have all lived in one Maven reactor since the project began. That was right while the framework's typed shape was still settling — every change touched every layer and the dogfood loop wanted them close. By 0.0.110, the shape has settled: [RFC 0012](#ref:rfc-12) typed *what a studio is*; [RFC 0019 / 0020](#ref:rfc-19) closed the typed content vocabulary; conformance tests cover the registration-drift class ([Defect 0005](#ref:def-5)). The framework can now stand on its own.

Three audiences are visibly mixed today:

- **Framework consumers** — anyone who depends on `homing-ssjs-core` to build a studio. Wants the framework jars without dragging in 79 documentation pages.
- **Framework readers** — anyone evaluating Homing. Wants the RFCs, doctrines, case studies, and runnable demos. Doesn't want the framework's internals next to the published narrative.
- **Framework maintainer (one person)** — wants a working surface for in-flight ideas, draft RFCs, defects under investigation. Wants the privacy of working notes without leaking pre-publication thinking.

The current monorepo serves all three by accident; each one would be better served by a focused product. This RFC formalises the separation as a typed plan with locked decisions, a sequenced migration, and an explicit set of deferred non-goals.

---

## 2. Final shape

Four repos at the end of execution, with a fifth deferred:

```
homing-ssjs-core                      (public)
    The framework. Java↔JS bridge + action infra + bundled libs
    + studio framework + conformance bases + skills bundle.

        ▲
        │ Maven dependency at released versions only
        │
homing-doc-plus-demo                  (public)
    The framework's public face. Public design history (RFCs /
    doctrines / closed defects / case studies / releases /
    building-block kits) + runnable demos + the canonical
    "public studio" that doubles as both the published doc site
    and a complete worked example.

homing-private-studio                 (private)
    The maintainer's working surface. In-flight plan trackers,
    draft RFCs, defects under investigation, scratch notes.

[deferred]
homing-components                     (public)
    Eventually — building blocks packaged independently from
    studio-base. Extract only on evidence of an external consumer
    who wants one without the rest.
```

**One-way dependency.** Nothing in `homing-ssjs-core` depends on the others. The framework releases on its own schedule; downstream upgrades on theirs.

**Private-to-public firewall.** `homing-private-studio` is allowed to depend on `homing-ssjs-core` and `homing-doc-plus-demo`, but only at *released-tag* versions, never SNAPSHOT or branch builds. The dependency graph is one-way; in-flight private work cannot leak into a public build.

---

## 3. Decisions (locked)

The plan is the contract; these are its load-bearing choices.

1. **Components stay together (one repo, eventually).** Today they live inside `homing-studio-base` as packages (`composed/`, `table/`, `image/`, `tree/`, `tracker/`, …). Extract one consolidated `homing-components` repo only when there is evidence of an external consumer needing one component independently of the rest. Fine-grained per-kind extraction (8+ repos) is rejected on operational-cost grounds.

2. **Public design docs sit with the demos.** The framework's public design history (RFCs / doctrines / closed defects / case studies / releases) and the runnable demos live in **one** repo, `homing-doc-plus-demo`. The codebase IS the documentation; visitors read the same artefacts they could clone and run.

3. **Private docs live in a dedicated private repo.** In-flight plan trackers, draft RFCs, defects pre-publication, and exploratory notes ship to `homing-private-studio`. The public/private split is a repo boundary, not a tag or a folder convention.

4. **Migration order is top-down.** `homing-doc-plus-demo` and `homing-private-studio` extract *first*, peeling public + private content off the existing repo. The framework code (`homing-core`, `homing-server`, `homing-libs`, `homing-studio-base`, `homing-conformance`, `homing-skills`) stays in the legacy repo through Phase 1 + Phase 2, untouched. Bottom-up extraction was rejected: the highest-value separation (public/private firewall) deserves to ship first.

5. **Cross-repo CI is deferred.** No automated downstream-integration build job at Phase 1. Manual coordination is the policy for the maintainer-of-one phase. Automate when the manual cost is felt, not before.

6. **The `homing-ssjs-core` rename is a Phase 3 mechanical operation.** Maven `artifactId` rename + git remote rename are cheap to do once, expensive to do twice. They happen *after* the public/private extraction lands cleanly, as a standalone follow-up.

7. **Skills lives with the framework (initially).** The `homing-skills` Maven module ships as part of `homing-ssjs-core`. The skills' content is framework-API-coupled; release lockstep is the right coupling. Revisit after Phase 1 once there's evidence of how the skills bundle is being used externally.

8. **Public/private content policy is sketched, refinable.** Working defaults: closed defects + resolved RFCs + completed plans + case studies + releases → public; open defects → public (the framework's design issues are part of its history); draft RFCs + in-flight plans + scratch notes → private. Policy lives as a doctrine doc inside `homing-doc-plus-demo` once Phase 1 completes; specific items can override the default during pre-flight classification.

---

## 4. Invariants

1. **Reversibility.** Every extraction is undoable by `git remote add legacy && git merge`. Repo separation is a logical layout, not a one-way commitment. If multi-repo operation proves painful, merging back is mechanical.

2. **No SNAPSHOT cross-repo deps.** Public repos resolve only released tags from each other. The private repo resolves only released tags from public repos. SNAPSHOT resolution is configured to fail at build time, not warn.

3. **The framework's compile-time integrity is preserved.** Until components extract (deferred), `homing-ssjs-core` is one Maven reactor; a breaking change to homing-core fails the homing-studio-base build immediately as it does today.

4. **The dogfood loop survives.** After Phase 1, `homing-doc-plus-demo` is the framework's first external consumer running on the same machine. Every framework release is exercised against it before tagging.

5. **History preservation.** Each extraction uses `git filter-repo --path` to retain the moved files' commit history in the new repo. The legacy repo retains its full history minus what extracted; the new repos start with the filtered subset.

---

## 5. Phased execution

| Phase | Scope | Status |
|---|---|---|
| **0** | Tag 0.0.110 — the pre-separation snapshot every extraction can roll back to. | Pending (release prepared in 0.0.110; tag operation is the user's call). |
| **1** | Extract `homing-doc-plus-demo`. Pre-flight classification → `git filter-repo` → restructure demos into multi-module portfolio → wire Maven deps to released `homing-ssjs-core` → delete extracted content from legacy → tag legacy as `0.0.111`. | Pending. |
| **2** | Extract `homing-private-studio`. Same pattern; private content peels off (or repo starts empty and accumulates private work going forward). | Pending — can run in same session as Phase 1 or as a follow-up. |
| **3** | Maven `artifactId` rename: `homing.js` → `homing-ssjs-core`. Git remote rename if hosting moves. Update dependent repos' `<parent>` and coordinate accordingly. | Pending. |
| **4** | Components extraction. *Only* on evidence of external need. | Deferred — no scheduled trigger. |

Phase 1's operational detail (pre-flight classification, `git filter-repo` invocations, Maven re-wiring, verification gates) lives in the operational sibling `SEPARATION_PLAN.md` at the legacy repo root, kept in sync until Phase 3 completes.

---

## 6. Cost — Weighed Complexity

Per the [Weighed Complexity doctrine](#ref:doc-wc), the proposal is judged across five dimensions:

| Dimension | Cost | Comment |
|---|---|---|
| **Cognitive density** | Low per repo, increased across repos | Each repo becomes simpler to read. Coordinating across repos is the new cognitive cost. |
| **Blast radius** | Moderate during Phase 1; low after | One coordinated extraction touches many files. Once stable, blast radius narrows per repo. |
| **Reversibility** | High | `git remote add legacy && git merge` undoes the split. |
| **Authoring tax** | Slightly higher per change | Multi-repo PRs replace single-PR atomic changes for some workflows. Most workflows are within-repo. |
| **Failure mode** | Visible | Cross-repo coordination failures surface as build failures (downstream can't resolve). |

Per the doctrine: the typing improvement is the win. The operational tax is real but bounded; the public/private firewall is structurally hard to achieve any other way; deferred extractions (components, fine-grained per-kind) hold the line on over-engineering.

---

## 7. Compatibility

**No breaking change for current downstream studios.** Any external user depending on `homing-studio-base` or `homing-core` Maven artefacts continues to work at the pre-rename coordinates through Phase 2. The Phase 3 rename ships with a [migration skill](#ref:rel-0-0-101) (precedent set by the 0.0.100 → 0.0.101 migration) describing the mechanical `find-and-replace` from `homing.js` to `homing-ssjs-core`.

**No breaking change to the framework's API.** Phases 1–3 are entirely about packaging; no Java records change, no Maven coordinate semantics shift, no protocol breaks. The artefact rename in Phase 3 is the only thing downstream code sees.

**Browser / runtime support** unchanged.

---

## 8. Non-goals

What this RFC *deliberately does not do*:

- Fine-grained per-component extraction (per Decision §3 D1).
- Cross-repo CI automation (per Decision §3 D5).
- Splitting `homing-skills` from the framework (per Decision §3 D7).
- Republishing under a new Maven group ID. Group ID stays `io.github.captainssingapura-hue.homing.js` until/unless there's a reason to change.
- Reorganising the framework's own package structure. Packages stay where they are.

These are all available as future RFCs if and when evidence warrants.

---

## 9. Open questions blocking Phase 1

These are surfaced in detail in the operational sibling `SEPARATION_PLAN.md` §7, transferred here for visibility:

1. **Repo hosting.** GitHub under `captainssingapura-hue` for the public repos? Same provider for `homing-private-studio` (private repo) or different?
2. **Maven publishing strategy.** Does `homing-doc-plus-demo` publish to Maven Central, a private repo, or stay clone-and-run only?
3. **Release numbering.** Does `homing-doc-plus-demo` start its own 0.0.11 / 0.0.100 binary numbering, or continue the framework's count? Separate is likely cleaner — they serve different products.
4. **Brand identity for the public studio.** The current `homing-studio` brand is "Homing · studio". After extraction it's plausibly "Homing · documentation" or just "Homing". Worth a small product-identity moment.

---

## See also

- [RFC 0012 — Typed Studio Composition](#ref:rfc-12) — enabled this separation by lifting "what a studio is" into the type system. RFC 0021 actualizes the next step in that arc: separation by audience now that the typed shape supports it.
- [RFC 0011 — Cross-tree Composition](#ref:rfc-11) — established that studios can be composed at deployment time via Umbrella. RFC 0021 lets the composing studios live in different repos.
- [Functional Objects doctrine](#ref:doc-fo) — the typed-record discipline that survives the split unchanged. Behaviour stays on records regardless of which repo the record lives in.
- [Weighed Complexity doctrine](#ref:doc-wc) — the lens used to weigh the operational cost of multi-repo against the structural win of the public/private firewall.
- [Defect 0005 — Two-Source Registration Drift](#ref:def-5) — separately complicates multi-repo coordination; called out so the resolution-sketch can factor in repo boundaries when a real fix is picked.
- [Release 0.0.110 — Typed Content Vocabulary](#ref:rel-0-0-110) — the immediate predecessor and the natural seam for the separation work. The framework reaches a coherent shape there; this RFC ships the next.
