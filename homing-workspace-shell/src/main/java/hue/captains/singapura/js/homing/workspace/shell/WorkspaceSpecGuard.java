package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * RFC 0060 — the checks a {@link WorkspaceSpec}'s <b>declarations</b> must pass
 * before it may be registered.
 *
 * <h2>Why these run at registration</h2>
 *
 * <p>Every failure below is one the running system cannot report. A spec's seed
 * is consumed in the browser, once, by a workspace with no saved state — so a
 * mistake surfaces as a pane that came up empty for one person on their first
 * visit, months after the line was written, with nothing in the log that names
 * the spec. Registration happens at boot in the JVM that owns the declaration:
 * the wrong thing fails immediately and says whose it is.</p>
 *
 * <p>They are all <b>intra-spec</b> — a spec is checked against itself, never
 * against the registry — so they hold for a spec nobody registers, and a test can
 * assert them without touching global state. Cross-spec identity (duplicate
 * {@code kind}) stays in {@link WorkspaceSpecRegistry}, which is the only thing
 * that can see it.</p>
 *
 * <h2>What is checked</h2>
 *
 * <ol>
 *   <li><b>Widget simpleNames are unique.</b> {@code simpleName} is the wire key
 *       — {@code WidgetEntriesJson} emits it and the shell builds
 *       {@code byName[e.simpleName]} from it. Two declared classes sharing one
 *       means the later silently wins and the earlier is unmountable.</li>
 *   <li><b>Arrangement placements are declared.</b> Compared by {@code Class},
 *       not name: a placement of a same-named class from another package would
 *       pass a name check and mount the other widget.</li>
 *   <li><b>Nothing is seeded twice.</b> The seeded instance id is the widget kind,
 *       and the model's spawn is idempotent on that id, so a widget placed in two
 *       panes appears in the first and <b>vanishes without a warning</b> from the
 *       second. This is the one failure here with no diagnostic at all today.</li>
 *   <li><b>The seed fits the budget</b> (D12). {@code maxTabs()} is a shared pool
 *       across every pane; a seed that exceeds it opens a workspace already over
 *       its limit, with the "+" affordance disabled everywhere and no way back
 *       but closing tabs.</li>
 * </ol>
 *
 * @since RFC 0060
 */
public final class WorkspaceSpecGuard implements StatelessFunctionalObject {

    private WorkspaceSpecGuard() {}

    /**
     * Validate one spec's declarations against each other.
     *
     * @throws IllegalStateException on the first violation, naming the spec class
     *                               and what would have gone wrong at boot
     */
    public static void check(WorkspaceSpec spec) {
        String who = spec.getClass().getName();

        Map<String, Class<?>> declared = declaredByName(spec, who);
        Arrangement arrangement = spec.arrangement();

        checkPlacementsAreDeclared(arrangement, declared, who);
        checkNothingSeededTwice(arrangement, who);
        checkSeedFitsBudget(spec, arrangement, who);
    }

    /** (1) simpleName is the wire key, so it has to identify exactly one class. */
    private static Map<String, Class<?>> declaredByName(WorkspaceSpec spec, String who) {
        var byName = new LinkedHashMap<String, Class<?>>();
        for (WidgetEntry e : spec.widgetEntries()) {
            Class<? extends WorkspaceWidget<?, ?>> cls = e.widgetClass();
            Class<?> clash = byName.put(cls.getSimpleName(), cls);
            if (clash != null && clash != cls) {
                throw new IllegalStateException(
                        who + ".widgetEntries() declares two widgets named "
                      + cls.getSimpleName() + " — " + clash.getName() + " and " + cls.getName()
                      + ". The simpleName is the wire key the picker and the arrangement "
                      + "mount by, so only one of them would ever be reachable.");
            }
        }
        return byName;
    }

    /** (2) A placed class the spec never declared cannot be mounted. */
    private static void checkPlacementsAreDeclared(Arrangement arrangement,
                                                   Map<String, Class<?>> declared,
                                                   String who) {
        arrangement.widgets().forEach((pane, placed) -> {
            for (Class<? extends WorkspaceWidget<?, ?>> cls : placed) {
                if (declared.get(cls.getSimpleName()) != cls) {
                    throw new IllegalStateException(
                            who + ".arrangement() places " + cls.getName() + " in " + pane
                          + ", which " + who + ".widgetEntries() does not declare"
                          + declaredHint(declared));
                }
            }
        });
    }


    /**
     * (3) The seeded id is derived from the kind alone, and the model's spawn is
     * idempotent on it — so the second occurrence is dropped in silence.
     */
    private static void checkNothingSeededTwice(Arrangement arrangement, String who) {
        var seen = new LinkedHashMap<String, String>();   // simpleName -> where it came from
        arrangement.widgets().forEach((pane, placed) -> {
            for (Class<? extends WorkspaceWidget<?, ?>> cls : placed) {
                seededOnce(seen, cls.getSimpleName(), "pane " + pane, who);
            }
        });
    }

    private static void seededOnce(Map<String, String> seen,
                                   String kind, String where, String who) {
        String first = seen.putIfAbsent(kind, where);
        if (first != null) {
            throw new IllegalStateException(
                    who + " seeds " + kind + " twice — in " + first + " and in " + where
                  + ". A seeded widget's instance id is its kind, so the second placement "
                  + "would be dropped silently and that pane would open empty. Seed it once, "
                  + "or give the workspace two widget kinds.");
        }
    }

    /** (4) D12 — the seed has to fit in the budget it opens under. */
    private static void checkSeedFitsBudget(WorkspaceSpec spec,
                                            Arrangement arrangement,
                                            String who) {
        int budget = spec.maxTabs();
        if (budget < 1) {
            throw new IllegalStateException(
                    who + ".maxTabs() is " + budget
                  + " — a workspace that may hold no tabs has nothing to be.");
        }
        int seeded = arrangement.totalWidgets();
        if (seeded > budget) {
            throw new IllegalStateException(
                    who + " seeds " + seeded + " widget(s) from arrangement "
                  + arrangement.name() + " but maxTabs() is " + budget
                  + " — the workspace would open already over its budget, with the "
                  + "picker disabled on every pane.");
        }
    }

    /** The declared names, so a typo is one glance from being obvious. */
    private static String declaredHint(Map<String, Class<?>> declared) {
        List<String> names = new ArrayList<>(new LinkedHashSet<>(declared.keySet()));
        return names.isEmpty() ? "." : " — declared widgets are " + names + ".";
    }
}
