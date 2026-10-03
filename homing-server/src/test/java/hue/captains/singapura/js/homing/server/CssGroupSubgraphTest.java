package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The dependency subgraph a served CSS group module carries to the client: the
 * group and everything it transitively depends on, derived on the server from
 * its classes' dependsOn(), in dependency order - and a group that does not vary
 * with the theme says so.
 */
class CssGroupSubgraphTest {

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
    void theSubgraphIsTheClosure_dependenciesFirst() {
        String js = CssGroupContentProvider.subgraphJs(Root.INSTANCE);
        assertEquals("{ \"" + fq(Dep.class) + "\": { deps: [] }, \"" + fq(Root.class) + "\": { deps: [\"" + fq(Dep.class) + "\"] } }", js);
    }

    @Test
    void aGroupThatDoesNotVary_saysSo() {
        String js = CssGroupContentProvider.subgraphJs(Root.INSTANCE, g -> g != Dep.INSTANCE);
        assertEquals("{ \"" + fq(Dep.class) + "\": { deps: [], varies: false }, \"" + fq(Root.class) + "\": { deps: [\"" + fq(Dep.class) + "\"] } }", js);
    }

    @Test
    void theGeneratedModule_loadsWithItsSubgraph() {
        var provider = new CssGroupContentProvider<>(Root.INSTANCE, "default", new QueryParamResolver("/module"));
        String load = provider.content().get(1);
        assertTrue(load.startsWith("await _css.loadCss(\"" + fq(Root.class) + "\", \"default\", { \"" + fq(Dep.class) + "\": { deps: [] }"), load);
    }
}
