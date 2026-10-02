package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * Typed view of a multi-phase plan tracker. Implementations of {@code Plan} are
 * served by the shared {@code PlanAppHost} (RFC 0005-ext1) — no per-plan
 * AppModule needed.
 *
 * <p>Per the
 * <a href="../../../../../../../../../../docs/doctrines/PlanContainerDoc.md">
 * Plans as Living Containers</a> doctrine, every Plan exposes three structural
 * pillars compiler-enforced by abstract methods:</p>
 *
 * <ol>
 *   <li>{@link #decisions()} — open questions to resolve.</li>
 *   <li>{@link #phases()} — phased actions to execute.</li>
 *   <li>{@link #acceptance()} — plan-level success criteria.</li>
 * </ol>
 *
 * <p>Skipping any pillar at the implementation level is a compile error. Empty
 * lists are valid — a plan with no open questions or no acceptance items just
 * returns an empty list — but the methods must be implemented.</p>
 *
 * <p>An optional fourth pillar — {@link #objectives()} — sits above the three
 * mandatory ones and describes the plan's goals at a glance. Defaulted to an
 * empty list; trackers that want a high-level "Objectives" section at the top
 * of the index view override it.</p>
 *
 * <p>Display data ({@link #name()}, optional {@link #summary()}, optional
 * {@link #subtitle()}, optional {@link #kicker()}) is intrinsic to the plan but
 * carries no presentation directives.</p>
 *
 * @since RFC 0005-ext1 (extended from the original tracker kit)
 */
public interface Plan extends StatelessFunctionalObject {

    // -------------------------------------------------------------------------
    // Identity-display (compiler-enforced)
    // -------------------------------------------------------------------------

    /** Human-readable plan name; used in catalogue tile listings and the page heading. */
    String name();

    // -------------------------------------------------------------------------
    // Three pillars (compiler-enforced — abstract, no defaults)
    // -------------------------------------------------------------------------

    /** Pillar 1 — open questions; may be empty. */
    List<Decision> decisions();

    /** Pillar 2 — phased actions; may be empty (rare; a plan with no phases yet). */
    List<Phase> phases();

    /** Pillar 3 — plan-level acceptance criteria; may be empty. */
    List<Acceptance> acceptance();

    // -------------------------------------------------------------------------
    // Optional 4th pillar — high-level objectives ("why we're doing this")
    // -------------------------------------------------------------------------

    /**
     * Optional list of high-level {@link Objective}s rendered at the top of the
     * index view. Empty by default — the renderer hides the section when the
     * list is empty. Distinct from {@link #acceptance()}: objectives describe
     * goals (un-checkboxed), acceptance describes ship-gates (pass/fail).
     */
    default List<Objective> objectives() { return List.of(); }

    // -------------------------------------------------------------------------
    // Optional display + cross-reference data
    // -------------------------------------------------------------------------

    /** Optional one-line summary used in catalogue tile listings. Default empty. */
    default String summary() { return ""; }

    /** Optional small-caps label above the title (e.g. "RFC 0001"). Default empty. */
    default String kicker() { return ""; }

    /** Optional lede paragraph beneath the title. Default empty. */
    default String subtitle() { return ""; }

    /**
     * Optional companion doc UUID (per RFC 0004) for the "Execution Plan" footer link.
     * Returns null when the plan has no companion doc.
     */
    default String executionDoc() { return null; }

    /** Optional secondary doc UUID for "Dossier" footer link. Null = no link. */
    default String dossierDoc() { return null; }

    // -------------------------------------------------------------------------
    // Derived (default implementations — no override needed)
    // -------------------------------------------------------------------------

    /** 0..100, computed across all phases' tasks. */
    default int totalProgress() {
        int total = 0;
        int done  = 0;
        for (Phase p : phases()) {
            for (Task t : p.tasks()) {
                total++;
                if (t.done()) done++;
            }
        }
        return total == 0 ? 0 : (int) ((long) done * 100 / total);
    }

    /** Number of decisions still in OPEN state. */
    default int openDecisions() {
        int n = 0;
        for (Decision d : decisions()) {
            if (d.status() == DecisionStatus.OPEN) n++;
        }
        return n;
    }

    /** Number of acceptance items already met. */
    default int acceptanceMet() {
        int n = 0;
        for (Acceptance a : acceptance()) {
            if (a.met()) n++;
        }
        return n;
    }

    /** Look up a phase by id; null if no match. */
    default Phase phaseById(String id) {
        for (var p : phases()) if (p.id().equals(id)) return p;
        return null;
    }
}
