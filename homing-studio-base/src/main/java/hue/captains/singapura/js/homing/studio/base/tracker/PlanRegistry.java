package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Boot-time registry of {@link Plan}s. Constructed once at studio startup; immutable
 * afterwards.
 *
 * <p>Per RFC 0005-ext1 §2.5, the constructor performs structural validations that
 * the Java type system can't express — fail-fast at boot, no runtime surprises:</p>
 *
 * <ol>
 *   <li><b>Class uniqueness</b> — each Plan class registered at most once.</li>
 *   <li><b>Name non-blank</b> — every Plan has a non-empty {@code name()}.</li>
 *   <li><b>Phase ID uniqueness within a plan</b> — duplicate phase IDs throw.</li>
 *   <li><b>Decision ID uniqueness within a plan</b> — duplicate decision IDs throw.</li>
 *   <li><b>Phase status non-null</b> per phase.</li>
 *   <li><b>Decision status non-null</b> per decision.</li>
 *   <li><b>Phase dependency targets exist</b> — {@code Dependency.phaseId()} references
 *       a real phase ID in the same plan.</li>
 *   <li><b>executionDoc / dossierDoc resolve</b> in the supplied {@link DocRegistry}
 *       when non-null and parseable as UUID.</li>
 * </ol>
 *
 * @since RFC 0005-ext1
 */
public final class PlanRegistry {

    private final Map<Class<? extends Plan>, Plan> byClass;

    /**
     * Build a registry from an explicit list of plans. Performs all §2.5 validations
     * at construction.
     *
     * @throws IllegalStateException on any validation failure
     */
    public PlanRegistry(Collection<? extends Plan> plans, DocRegistry docRegistry) {
        Objects.requireNonNull(plans,       "plans");
        Objects.requireNonNull(docRegistry, "docRegistry");

        var byClass = new LinkedHashMap<Class<? extends Plan>, Plan>();
        for (Plan p : plans) {
            requireValid(p, docRegistry);
            Class<? extends Plan> cls = p.getClass();
            Plan prev = byClass.put(cls, p);
            if (prev != null && prev != p) {
                throw new IllegalStateException(
                        "Plan class registered twice with different instances: " + cls.getName());
            }
        }
        this.byClass = Map.copyOf(byClass);
    }

    private static void requireValid(Plan p, DocRegistry docRegistry) {
        Objects.requireNonNull(p, "plan must not be null");
        if (p.name() == null || p.name().isBlank()) {
            throw new IllegalStateException(
                    "Plan " + p.getClass().getName() + " has null/blank name()");
        }
        if (p.decisions() == null) {
            throw new IllegalStateException(
                    "Plan " + p.getClass().getName() + " has null decisions()");
        }
        if (p.phases() == null) {
            throw new IllegalStateException(
                    "Plan " + p.getClass().getName() + " has null phases()");
        }
        if (p.acceptance() == null) {
            throw new IllegalStateException(
                    "Plan " + p.getClass().getName() + " has null acceptance()");
        }

        // Phase ID uniqueness + status non-null + dep targets exist.
        Set<String> phaseIds = new HashSet<>();
        for (Phase phase : p.phases()) {
            if (phase == null) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has a null Phase");
            }
            if (phase.id() == null || phase.id().isBlank()) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has a Phase with null/blank id");
            }
            if (!phaseIds.add(phase.id())) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has duplicate phase id: " + phase.id());
            }
            if (phase.status() == null) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " phase " + phase.id() + " has null status");
            }
        }
        // Second pass for dep validation (need full id set first).
        for (Phase phase : p.phases()) {
            if (phase.dependsOn() == null) continue;
            for (Dependency dep : phase.dependsOn()) {
                if (dep == null) continue;
                if (!phaseIds.contains(dep.phaseId())) {
                    throw new IllegalStateException(
                            "Plan " + p.getClass().getName() + " phase " + phase.id()
                          + " depends on unknown phase id: " + dep.phaseId());
                }
            }
        }

        // Decision ID uniqueness + status non-null.
        Set<String> decisionIds = new HashSet<>();
        for (Decision d : p.decisions()) {
            if (d == null) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has a null Decision");
            }
            if (d.id() == null || d.id().isBlank()) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has a Decision with null/blank id");
            }
            if (!decisionIds.add(d.id())) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " has duplicate decision id: " + d.id());
            }
            if (d.status() == null) {
                throw new IllegalStateException(
                        "Plan " + p.getClass().getName() + " decision " + d.id() + " has null status");
            }
        }

        // Optional doc references resolve in DocRegistry (when non-null + parseable as UUID).
        validateDocRef(p, p.executionDoc(), docRegistry, "executionDoc");
        validateDocRef(p, p.dossierDoc(),   docRegistry, "dossierDoc");
    }

    private static void validateDocRef(Plan p, String maybeUuid, DocRegistry docs, String which) {
        if (maybeUuid == null || maybeUuid.isBlank()) return;
        UUID id;
        try {
            id = UUID.fromString(maybeUuid);
        } catch (IllegalArgumentException e) {
            // Backward compat — pre-RFC-0004 trackers may have set executionDoc to a
            // path string. Skip validation; the renderer will fail at click time.
            return;
        }
        if (docs.resolve(id) == null) {
            throw new IllegalStateException(
                    "Plan " + p.getClass().getName() + " " + which + " UUID " + id
                  + " is not in the DocRegistry");
        }
    }

    /**
     * Defect 0005 fix — harvest every {@link Plan} reachable as a
     * {@code Entry.OfDoc(PlanDoc(plan))} catalogue leaf, across the supplied
     * catalogue closure. {@link Bootstrap} unions this with each studio's
     * {@code Studio.plans()} when constructing the registry, so a plan
     * registered only via {@code Entry.of(catalogue, plan)} is reachable at
     * {@code /plan?id=&lt;fqn&gt;} automatically. Catalogue-leaf is the
     * canonical registration site; {@code Studio.plans()} remains as an
     * escape hatch for URL-only plans that aren't surfaced as tiles.
     *
     * <p>The caller is expected to pass the full closure (per
     * {@link hue.captains.singapura.js.homing.studio.base.Studio#catalogues()},
     * which BFS-walks {@code subCatalogues()} from the home catalogue).
     * Sub-catalogues that this method receives directly are only walked one
     * level deep — recursion happens at the caller's closure-building step.</p>
     *
     * @since Defect 0005 resolution
     */
    public static List<Plan> harvestFromLeaves(Collection<? extends Catalogue<?>> catalogues) {
        var out = new ArrayList<Plan>();
        for (var c : catalogues) {
            for (var e : c.leaves()) {
                if (e instanceof Entry.OfDoc<?, ?> ofDoc && ofDoc.doc() instanceof PlanDoc pd) {
                    out.add(pd.plan());
                }
            }
        }
        return out;
    }

    /** Resolve a Plan by its implementing class, or null if not registered. */
    public Plan resolve(Class<? extends Plan> cls) {
        return byClass.get(cls);
    }

    /** All registered plans in registration order. */
    public Collection<Plan> all() {
        return Collections.unmodifiableCollection(byClass.values());
    }

    /** Number of registered plans. */
    public int size() {
        return byClass.size();
    }
}
