package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Wearable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a served CSS module hands the client (RFC 0064, D22, D25). A provided group carries its
 * dependency subgraph - the group and everything it transitively depends on, derived on the
 * server from its classes' dependsOn(), in dependency order - to the CSS manager, which loads it
 * once, with no theme; the derived sheets its classes wear go to the theme's owner. A derived
 * sheet's own module goes to the theme's owner alone.
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

    /** A derived sheet: a design's words, say. */
    record Words() implements DerivedCss<Words> {
        static final Words INSTANCE = new Words();
    }

    /** A derived sheet a module imports so that it loads: a look, say. */
    record Look() implements DerivedCss<Look> {
        static final Look INSTANCE = new Look();
        record look_sheet() implements Exportable._Constant<Look> {}
        @Override public ExportsOf<Look> exports() { return new ExportsOf<>(this, List.of(new look_sheet())); }
    }

    record Worn(String cssName) implements Wearable {
        @Override public DerivedCss<?> sheet() { return Words.INSTANCE; }
    }

    /** A provided group whose class wears a word, and depends on one that does not. */
    record Wearer() implements CssGroup<Wearer> {
        static final Wearer INSTANCE = new Wearer();
        record wearer_thing() implements CssClass<Wearer> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Dep.dep_thing()); }
            @Override public List<? extends Wearable> wears() { return List.of(new Worn("raised-color-surface")); }
        }
        @Override public List<CssClass<Wearer>> cssClasses() { return List.of(new wearer_thing()); }
    }

    private static String fq(Class<?> c) { return c.getCanonicalName(); }

    @Test
    void theSubgraphIsTheClosure_dependenciesFirst() {
        String js = CssGroupContentProvider.subgraphJs(Root.INSTANCE);
        assertEquals("{ \"" + fq(Dep.class) + "\": { deps: [] }, \"" + fq(Root.class) + "\": { deps: [\"" + fq(Dep.class) + "\"] } }", js);
    }

    @Test
    void theGeneratedModule_loadsItsSubgraphOnce_withNoTheme() {
        var provider = new CssGroupContentProvider<>(Root.INSTANCE, "default", new QueryParamResolver("/module"));
        List<String> lines = provider.content();
        assertTrue(lines.get(1).startsWith("await _css.loadCss(\"" + fq(Root.class) + "\", { \"" + fq(Dep.class) + "\": { deps: [] }"), lines.get(1));
        assertFalse(String.join("\n", lines).contains("_themes"), "a group that wears nothing asks the theme for nothing");
    }

    @Test
    void aWornDerivedSheet_isNoDependency_butGoesToTheThemesOwner() {
        assertEquals("{ \"" + fq(Dep.class) + "\": { deps: [] }, \"" + fq(Wearer.class) + "\": { deps: [\"" + fq(Dep.class) + "\"] } }",
                CssGroupContentProvider.subgraphJs(Wearer.INSTANCE), "the words are not in the manager's graph");
        List<String> lines = new CssGroupContentProvider<>(Wearer.INSTANCE, "default", new QueryParamResolver("/module")).content();
        assertTrue(lines.get(1).startsWith("import { ThemeSheetsInstance as _themes } from "), lines.get(1));
        assertTrue(lines.contains("await _themes.load([\"" + fq(Words.class) + "\"], \"default\");"), String.join("\n", lines));
        List<String> none = new CssGroupContentProvider<>(Wearer.INSTANCE, null, new QueryParamResolver("/module")).content();
        assertTrue(none.contains("await _themes.load([\"" + fq(Words.class) + "\"], null);"), "no default theme: the steward's answer alone");
    }

    @Test
    void aDerivedSheetsModule_goesToTheThemesOwnerAlone_andBindsWhatItExports() {
        List<String> lines = new DerivedCssContentProvider<>(Look.INSTANCE, "default", new QueryParamResolver("/module")).content();
        assertEquals(3, lines.size(), String.join("\n", lines));
        assertTrue(lines.get(0).startsWith("import { ThemeSheetsInstance as _themes } from "), lines.get(0));
        assertEquals("await _themes.load([\"" + fq(Look.class) + "\"], \"default\");", lines.get(1));
        assertEquals("const look_sheet = \"" + fq(Look.class) + "\";", lines.get(2));
        assertFalse(String.join("\n", lines).contains("_css"), "never the CSS manager's");
    }
}
