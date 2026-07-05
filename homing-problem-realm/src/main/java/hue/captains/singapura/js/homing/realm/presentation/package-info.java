/**
 * Homing's Problem Realm for <em>site presentation</em> — the typed,
 * single-parent taxonomy of the problems the studio's presentation solves,
 * modelled on rebar's {@code problem-realm-core} ladder ({@code L0_Domain} …
 * {@code LeafDomain}).
 *
 * <h2>The tree (from the S0 audit)</h2>
 * <pre>
 * SitePresentation                         (L0 — Root)
 * ├── SiteChrome                           (L1)   chrome &amp; navigation
 * ├── CatalogueOrganization                (L1)   presenting a collection
 * ├── DocumentPresentation                 (L1)   presenting one document
 * └── ProgressTracking                     (L1)   presenting a plan/tracker
 * </pre>
 *
 * <h2>Presentation surfaces only — subject concepts live elsewhere</h2>
 * This realm holds only <em>presentation surfaces</em>. A former
 * {@code PresentationCommons} node once held "shared concepts" (Status / Kind /
 * Completion) here — that was a mistake: those are <em>subject</em> concepts,
 * not presentation, and its {@code Status} was a borrowed generic vocabulary
 * (Facet-native), not a domain. They now live in their own subject domains
 * (e.g. {@link hue.captains.singapura.js.homing.realm.plan}, where
 * {@code PlanPhasesManagement → Status} is the genuinely domain-bound status),
 * added family-by-family as their intents are built.
 *
 * <h2>Layering</h2>
 * The <em>abstraction</em> (the level ladder, {@code DomainNode}, {@code Desc})
 * is rebar's job; this package holds only concrete instances and never
 * redefines the abstraction. The four surfaces are modelled as genuine problem
 * sub-domains from the framework's self-perspective (a downstream app would see
 * them as Solution Facets; Homing itself solves them).
 *
 * <h2>Deferred</h2>
 * The per-surface L2 concerns (tile, prose, meta-header, step, …) are added
 * family-by-family as their expression-intent families are built (journey
 * S1/S3), not eagerly.
 */
package hue.captains.singapura.js.homing.realm.presentation;
