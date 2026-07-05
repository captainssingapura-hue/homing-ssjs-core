/**
 * Plan Tracking — a <strong>subject</strong> domain topic (distinct from the
 * presentation surfaces in
 * {@link hue.captains.singapura.js.homing.realm.presentation}): a plan and its
 * phases are things that <em>exist</em>, independent of how they are shown.
 *
 * <h2>Functional structure</h2>
 * The topic decomposes into its functional aspects, flat beneath the root
 * (grounded in the framework's <em>Plans as Living Containers</em> model — a
 * plan exposes objectives, decisions, phases, acceptance, plus per-phase tasks /
 * dependencies / metrics and a derived progress):
 * <pre>
 * PlanTracking                (L0)
 * ├── Objectives              (L1)   the goals — the "why"
 * ├── Phases                  (L1)   the phased execution ("display all phases")
 * ├── OpenQuestions           (L1)   decisions in flight / resolved
 * ├── Acceptance              (L1)   the ship gates
 * ├── Progress                (L1)   completion across the plan (a magnitude)
 * ├── Tasks                   (L1)   the checklist of the work
 * ├── Dependencies            (L1)   ordering constraints (a relationship/edge concern)
 * ├── Metrics                 (L1)   quantified per-phase outcomes
 * └── Status                  (leaf) the canonical phase status (PhaseState) — deprioritised
 * </pre>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li><strong>Status is parked.</strong> {@link Status} + {@link PhaseState}
 *       remain (the {@code glance.phase-status.*} intents reference them), but
 *       status is deferred for now — functional structure first. It sits flat at
 *       L0 and may later relocate under {@code Phases}.</li>
 *   <li><strong>The aspects are two kinds.</strong> Some own a status
 *       ({@code Phases}, {@code OpenQuestions}, {@code Acceptance}, {@code Tasks});
 *       others are different shapes — {@code Progress} a magnitude,
 *       {@code Dependencies} typed edges, {@code Objectives} descriptive,
 *       {@code Metrics} measures. Don't force them into one mould.</li>
 *   <li><strong>The flat model defers a {@code Phase} entity.</strong>
 *       {@code Tasks} / {@code Dependencies} / {@code Metrics} are conceptually
 *       per-phase but kept flat here, per the "attach directly to L0" choice. A
 *       {@code Phase} entity that groups them can be introduced later if plan
 *       navigation needs it.</li>
 * </ul>
 *
 * <h2>Layering</h2>
 * The <em>abstraction</em> (the level ladder, {@code DomainNode}, {@code Desc})
 * is rebar's job; this package holds only concrete instances.
 */
package hue.captains.singapura.js.homing.realm.plan;
