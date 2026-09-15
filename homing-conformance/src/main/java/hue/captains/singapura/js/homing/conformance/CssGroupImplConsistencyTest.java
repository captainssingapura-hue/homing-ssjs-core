package hue.captains.singapura.js.homing.conformance;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupImpl;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import hue.captains.singapura.js.homing.core.DomModule;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * RFC 0002 §3.4 — build-time gate over a deployment's
 * {@link CssGroupImpl} registry.
 *
 * <p>Subclasses provide the registry and the active DomModules. The base
 * generates dynamic tests that assert:</p>
 * <ol>
 *   <li>every registered impl returns non-null {@code group()} and {@code theme()};</li>
 *   <li>no two impls share the same {@code (group class, theme.slug)} pair;</li>
 *   <li>every {@link CssGroup} reachable transitively from an active DomModule's
 *       {@code cssGroups()} that has at least one declared CssClass has at least
 *       one impl in the registry under the deployment's default theme slug.</li>
 * </ol>
 *
 * <p>Empty CssGroups (no declared CssClasses, e.g. base/alice-style aggregator
 * groups that exist only to chain imports) need not have an impl — the action
 * has nothing to render for them and never resolves them by class.</p>
 */
public abstract class CssGroupImplConsistencyTest {

    /** All impls shipped by the deployment. Typically your registry's {@code ALL} list. */
    protected abstract List<CssGroupImpl<?, ?>> impls();

    /** All DomModules whose CssGroups must be reachable to a registered impl. */
    protected abstract List<DomModule<?>> domModules();

    /**
     * The default theme slug the deployment serves when a request omits {@code ?theme=}.
     * Used to pick which impl satisfies the per-group reachability check.
     */
    protected abstract String defaultThemeSlug();

    @TestFactory
    Stream<DynamicTest> consistencyChecks() {
        var tests = new ArrayList<DynamicTest>();
        tests.add(DynamicTest.dynamicTest("every impl has non-null group() and theme()",
                this::assertImplsNonNull));
        tests.add(DynamicTest.dynamicTest("no duplicate (group, theme.slug) in registry",
                this::assertNoDuplicates));
        tests.add(DynamicTest.dynamicTest(
                "every reachable non-empty CssGroup has an impl under theme '" + defaultThemeSlug() + "'",
                this::assertEveryActiveGroupHasDefaultImpl));
        return tests.stream();
    }

    // ---- assertions -----------------------------------------------------

    private void assertImplsNonNull() {
        var violations = new ArrayList<String>();
        for (var impl : impls()) {
            if (impl.group() == null) {
                violations.add(impl.getClass().getName() + ".group() is null");
            }
            if (impl.theme() == null) {
                violations.add(impl.getClass().getName() + ".theme() is null");
            }
        }
        if (!violations.isEmpty()) {
            throw new AssertionError("Registry impls must have non-null group() and theme():\n  "
                    + String.join("\n  ", violations));
        }
    }

    private void assertNoDuplicates() {
        var seen = new HashMap<String, String>(); // key → first impl class name
        var violations = new ArrayList<String>();
        for (var impl : impls()) {
            String key = impl.group().getClass().getName() + " / " + impl.theme().slug();
            String prior = seen.put(key, impl.getClass().getName());
            if (prior != null) {
                violations.add("duplicate (group, theme.slug) for " + key
                        + " — first seen in " + prior
                        + ", again in " + impl.getClass().getName());
            }
        }
        if (!violations.isEmpty()) {
            throw new AssertionError("Registry has duplicate (group, theme.slug) pairs:\n  "
                    + String.join("\n  ", violations));
        }
    }

    private void assertEveryActiveGroupHasDefaultImpl() {
        Set<Class<?>> reachable = collectReachableGroupClasses();

        // Build set of (group class) that have an impl under defaultThemeSlug
        Set<Class<?>> implementedAtDefault = new HashSet<>();
        for (var impl : impls()) {
            if (defaultThemeSlug().equals(impl.theme().slug())) {
                implementedAtDefault.add(impl.group().getClass());
            }
        }

        // For each reachable group: skip empty, otherwise demand an impl
        // OR every CssClass providing an inline body() (RFC 0002-ext1 Phase 10).
        var missing = new ArrayList<String>();
        Map<Class<?>, CssGroup<?>> byClass = collectReachableGroupInstances();
        for (Class<?> groupClass : reachable) {
            CssGroup<?> g = byClass.get(groupClass);
            if (g == null) continue; // shouldn't happen
            if (g.cssClasses().isEmpty()) continue; // empty groups need no impl
            if (implementedAtDefault.contains(groupClass)) continue;
            // No registered impl — accept the group iff every class has an inline body().
            boolean allInline = g.cssClasses().stream().allMatch(c -> c.body() != null);
            if (!allInline) {
                missing.add(groupClass.getName());
            }
        }

        if (!missing.isEmpty()) {
            throw new AssertionError(
                    "The following CssGroups are reachable from active DomModules but have no impl "
                    + "under default theme '" + defaultThemeSlug() + "':\n  "
                    + String.join("\n  ", missing));
        }
    }

    // ---- traversal ------------------------------------------------------

    private Set<Class<?>> collectReachableGroupClasses() {
        Set<Class<?>> out = new LinkedHashSet<>();
        for (var m : domModules()) {
            for (var g : m.cssGroups()) {
                walk(g, out);
            }
        }
        return out;
    }

    private Map<Class<?>, CssGroup<?>> collectReachableGroupInstances() {
        Map<Class<?>, CssGroup<?>> out = new HashMap<>();
        for (var m : domModules()) {
            for (var g : m.cssGroups()) {
                walkInstances(g, out);
            }
        }
        return out;
    }

    private void walk(CssGroup<?> g, Set<Class<?>> seen) {
        if (!seen.add(g.getClass())) return;
        for (var imp : CssImportsFor.dependenciesOf(g)) {
            walk(imp, seen);
        }
    }

    private void walkInstances(CssGroup<?> g, Map<Class<?>, CssGroup<?>> seen) {
        if (seen.putIfAbsent(g.getClass(), g) != null) return;
        for (var imp : CssImportsFor.dependenciesOf(g)) {
            walkInstances(imp, seen);
        }
    }
}
