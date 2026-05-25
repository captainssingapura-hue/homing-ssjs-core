package hue.captains.singapura.js.homing.studio.base.conformance;

import hue.captains.singapura.js.homing.studio.base.Studio;
import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.tracker.Plan;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanDoc;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Closes a known class of registration-drift bug (see {@code Defect0005Doc}
 * — Two-Source Registration Drift).
 *
 * <p>A {@link Plan} reaches its viewer through two parallel paths:</p>
 * <ul>
 *   <li>{@link Studio#plans()} — feeds {@code PlanRegistry}, which
 *       backs {@code /plan?id=&lt;fqn&gt;} (the data endpoint
 *       {@code PlanGetAction} consults).</li>
 *   <li>{@code Entry.of(catalogue, plan)} → wraps the plan in a
 *       {@link PlanDoc} and registers it as a catalogue leaf — drives
 *       the tile listing and the breadcrumb chain.</li>
 * </ul>
 *
 * <p>Forgetting either side is a 404. Symptom of forgetting
 * {@code Studio.plans()}: {@code /plan?id=…} returns 404 with
 * <i>"Plan not registered"</i>, but the tile appears on the catalogue
 * page (because the catalogue side was registered). The framework's
 * type system can't catch this — the two registries don't share a
 * type and authors can author either side in isolation.</p>
 *
 * <p>This test asserts the invariant: <b>every Plan wrapped in a
 * catalogue's {@code Entry.OfDoc(PlanDoc(plan))} leaf must also appear
 * (by class) in some {@link Studio#plans()}.</b> The reverse direction
 * — plans listed by a studio but never surfaced as a catalogue leaf —
 * isn't checked here; that's a navigation choice (a plan could
 * intentionally be reachable only by URL), not a 404 hazard.</p>
 *
 * <p>Per the abstract-base pattern shared with {@link DocConformanceTest}
 * and {@link ContentViewerConformanceTest}, downstream subclasses
 * provide the inputs via {@link #studios()}.</p>
 *
 * @since Defect 0005 (relocated in b.2j cleanup — see
 *        {@link DocConformanceTest} for the layering rationale)
 */
public abstract class PlanRegistrationConformanceTest {

    /** Studios under test — each contributes both its {@code plans()} and
     *  its catalogue tree (via {@code home()} and its sub-catalogues). */
    protected abstract List<? extends Studio<?>> studios();

    @TestFactory
    public Stream<DynamicTest> planRegistrationConformance() {
        var studios = studios();

        // (a) Catalogue side — walk every studio's catalogue tree, collect
        // every Plan wrapped in a PlanDoc leaf. Tracks the catalogue path
        // for the error message.
        Map<Class<? extends Plan>, String> leafPlans = new LinkedHashMap<>();
        for (var studio : studios) {
            walkCatalogueLeavesForPlans(studio.home(), studio.getClass().getSimpleName(),
                    leafPlans);
        }

        // (b) Studio side — union every studio's plans() by class.
        Set<Class<? extends Plan>> registeredPlanClasses = new HashSet<>();
        for (var studio : studios) {
            for (Plan p : studio.plans()) {
                registeredPlanClasses.add(p.getClass());
            }
        }

        var tests = Stream.<DynamicTest>builder();

        for (var entry : leafPlans.entrySet()) {
            Class<? extends Plan> planClass = entry.getKey();
            String wherePath = entry.getValue();
            tests.add(DynamicTest.dynamicTest(
                    "plan registered in Studio.plans(): " + planClass.getSimpleName(),
                    () -> assertTrue(registeredPlanClasses.contains(planClass),
                            "Plan " + planClass.getName() + " is wrapped as a PlanDoc catalogue "
                                    + "leaf under " + wherePath + " but is missing from every "
                                    + "registered Studio.plans() list. The catalogue tile will "
                                    + "appear, but /plan?id=" + planClass.getName() + " will "
                                    + "return 404 \"Plan not registered\". Add "
                                    + planClass.getSimpleName() + ".INSTANCE to the owning "
                                    + "studio's plans() return list.")));
        }

        return tests.build();
    }

    /** Recursively walk a catalogue's leaves and sub-catalogues; collect every
     *  PlanDoc-wrapped Plan with the catalogue path that contained it. */
    @SuppressWarnings("unchecked")
    private static void walkCatalogueLeavesForPlans(
            Catalogue<?> cat,
            String pathSoFar,
            Map<Class<? extends Plan>, String> out) {
        String here = pathSoFar + " › " + cat.getClass().getSimpleName();
        for (var leaf : cat.leaves()) {
            if (leaf instanceof Entry.OfDoc<?, ?>(var d)
                    && d instanceof PlanDoc pd) {
                out.putIfAbsent((Class<? extends Plan>) pd.plan().getClass(), here);
            }
        }
        // RFC 0005-ext2: sub-catalogues flow through subCatalogues(), not
        // through Entry leaves.
        for (Catalogue<?> sub : cat.subCatalogues()) {
            walkCatalogueLeavesForPlans(sub, here, out);
        }
    }
}
