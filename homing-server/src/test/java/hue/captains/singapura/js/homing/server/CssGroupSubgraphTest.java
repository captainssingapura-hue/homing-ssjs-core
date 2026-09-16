package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteClass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0066 — the dependency subgraph a served CSS group module carries to the
 * client, with the deployment's priors written in. The palette is a node the
 * server puts into EVERY subgraph, as a prior with no dependencies, and every
 * non-prior node lists it — the edge each class has by definition, held once
 * on the server rather than declared per class.
 */
class CssGroupSubgraphTest {

    record Palette() implements CssGroup<Palette> {
        static final Palette INSTANCE = new Palette();
        record palette() implements PaletteClass<Palette> {
            @Override public Set<CssVar> declares() { return Set.of(new CssVar("--x")); }
        }
        @Override public boolean prior() { return true; }
        @Override public List<CssClass<Palette>> cssClasses() { return List.of(new palette()); }
    }

    /** A prior in its own right — the studio's base styles today. */
    record Base() implements CssGroup<Base> {
        static final Base INSTANCE = new Base();
        record base_thing() implements CssClass<Base> {}
        @Override public boolean prior() { return true; }
        @Override public List<CssClass<Base>> cssClasses() { return List.of(new base_thing()); }
    }

    record Dep() implements CssGroup<Dep> {
        static final Dep INSTANCE = new Dep();
        record dep_thing() implements CssClass<Dep> {}
        @Override public List<CssClass<Dep>> cssClasses() { return List.of(new dep_thing()); }
    }

    record Root() implements CssGroup<Root> {
        static final Root INSTANCE = new Root();
        record root_thing() implements CssClass<Root> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Dep.dep_thing()); }
        }
        @Override public List<CssClass<Root>> cssClasses() { return List.of(new root_thing()); }
    }

    private static String fq(Class<?> c) { return c.getCanonicalName(); }

    @Test
    void withoutPriors_theSubgraphIsTheClosureAlone() {
        String js = CssGroupContentProvider.subgraphJs(Root.INSTANCE, List.of());
        assertEquals("{ \"" + fq(Dep.class) + "\": { deps: [] }, \"" + fq(Root.class) + "\": { deps: [\"" + fq(Dep.class) + "\"] } }", js);
    }

    @Test
    void thePalette_comesFirstAsAPrior_andEveryNonPriorNodeListsIt() {
        String js = CssGroupContentProvider.subgraphJs(Root.INSTANCE, List.of(Palette.INSTANCE));
        assertEquals("{ \"" + fq(Palette.class) + "\": { deps: [], prior: true }, "
                   + "\"" + fq(Dep.class) + "\": { deps: [\"" + fq(Palette.class) + "\"] }, "
                   + "\"" + fq(Root.class) + "\": { deps: [\"" + fq(Palette.class) + "\", \"" + fq(Dep.class) + "\"] } }", js);
    }

    @Test
    void aPriorInTheClosure_gainsNoDependencies() {
        // A prior declares none (the client refuses one that does); the palette
        // is loaded in the same wave, so nothing is lost.
        String js = CssGroupContentProvider.subgraphJs(Base.INSTANCE, List.of(Palette.INSTANCE));
        assertEquals("{ \"" + fq(Palette.class) + "\": { deps: [], prior: true }, "
                   + "\"" + fq(Base.class) + "\": { deps: [], prior: true } }", js);
    }

    @Test
    void thePaletteServedAsItself_isOneNode() {
        String js = CssGroupContentProvider.subgraphJs(Palette.INSTANCE, List.of(Palette.INSTANCE));
        assertEquals("{ \"" + fq(Palette.class) + "\": { deps: [], prior: true } }", js);
        assertFalse(js.contains("deps: [\"" + fq(Palette.class)), "a palette never depends on itself");
    }

    @Test
    void theGeneratedModule_loadsWithThePaletteInItsSubgraph() {
        var provider = new CssGroupContentProvider<>(Root.INSTANCE, "default",
                new QueryParamResolver("/module"), List.of(Palette.INSTANCE));
        String load = provider.content().get(1);
        assertTrue(load.startsWith("await _css.loadCss(\"" + fq(Root.class) + "\", \"default\", { \"" + fq(Palette.class) + "\": { deps: [], prior: true }"), load);
    }
}
