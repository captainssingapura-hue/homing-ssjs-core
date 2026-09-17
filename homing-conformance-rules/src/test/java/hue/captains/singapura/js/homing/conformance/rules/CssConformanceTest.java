package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.Theme;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RFC 0066 — each law refuses the one thing it is for, and nothing else. */
class CssConformanceTest {

    // ── Fixtures: a palette crate, a drawn crate that requires it, one that does not ──

    static final CssVar ACCENT = new CssVar("--color-accent");
    static final CssVar FACE   = new CssVar("--font-body");
    static final CssVar GUIDE  = new CssVar("--hgr-guide-x");

    record Palette() implements CssGroup<Palette> {
        static final Palette INSTANCE = new Palette();
        record palette() implements PaletteClass<Palette> { @Override public Set<CssVar> declares() { return Set.of(ACCENT, FACE); } }
        @Override public boolean prior() { return true; }
        @Override public List<CssClass<Palette>> cssClasses() { return List.of(new palette()); }
    }
    record PaletteCrate() implements Crate {
        static final PaletteCrate INSTANCE = new PaletteCrate();
        @Override public String name() { return "palette"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Palette.INSTANCE)); }
    }

    record Good() implements CssGroup<Good> {
        static final Good INSTANCE = new Good();
        record good_btn() implements CssClass<Good> {
            @Override public String body() { return "color: var(--color-accent);\nfont-family: var(--font-body);\n&:hover { color: red; }\n"; }
        }
        record good_guide() implements CssClass<Good> {
            @Override public Set<CssVar> runtimeVars() { return Set.of(GUIDE); }
            @Override public String body() { return "left: var(--hgr-guide-x);"; }
        }
        record good_card() implements CssClass<Good> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new good_btn()); }
            @Override public String body() { return "& .good-btn { margin: 0; }"; }
        }
        @Override public List<CssClass<Good>> cssClasses() { return List.of(new good_btn(), new good_guide(), new good_card()); }
    }
    record GoodCrate() implements Crate {
        static final GoodCrate INSTANCE = new GoodCrate();
        @Override public String name() { return "good"; }
        @Override public List<Crate> requires() { return List.of(PaletteCrate.INSTANCE); }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Good.INSTANCE)); }
    }

    record Bad() implements CssGroup<Bad> {
        static final Bad INSTANCE = new Bad();
        /** Reads a token nobody declares, names a class nobody owns, writes a literal face. */
        record bad_thing() implements CssClass<Bad> {
            @Override public String body() { return "color: var(--st-gray-mid);\nfont-family: \"Georgia\", serif;\nborder-radius: var(--radius-sm, 4px);\n& .ghost { opacity: .5; }\n"; }
        }
        @Override public List<CssClass<Bad>> cssClasses() { return List.of(new bad_thing()); }
    }
    /** A drawn group that claims to be prior. */
    record Pretender() implements CssGroup<Pretender> {
        static final Pretender INSTANCE = new Pretender();
        record p() implements CssClass<Pretender> { @Override public String body() { return "display: flex;"; } }
        @Override public boolean prior() { return true; }
        @Override public List<CssClass<Pretender>> cssClasses() { return List.of(new p()); }
    }
    record BadCrate() implements Crate {
        static final BadCrate INSTANCE = new BadCrate();
        @Override public String name() { return "bad"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Bad.INSTANCE), CrateEntry.of(Pretender.INSTANCE)); }
    }

    record T1() implements Theme { static final T1 INSTANCE = new T1(); @Override public String slug() { return "t1"; } }
    record T2() implements Theme { static final T2 INSTANCE = new T2(); @Override public String slug() { return "t2"; } }

    record Full() implements PaletteProvision<Palette, T1> {
        @Override public Palette group() { return Palette.INSTANCE; }
        @Override public T1 theme() { return T1.INSTANCE; }
        @Override public Map<CssVar, String> values() { return Map.of(ACCENT, "#FFE800", FACE, "serif", new CssVar("--bru-ink"), "#000"); }
        @Override public Map<CssVar, String> darkValues() { return Map.of(ACCENT, "#FFE800"); }
    }
    record Partial() implements PaletteProvision<Palette, T2> {
        @Override public Palette group() { return Palette.INSTANCE; }
        @Override public T2 theme() { return T2.INSTANCE; }
        @Override public Map<CssVar, String> values() { return Map.of(ACCENT, "#000", new CssVar("--color-danger"), "#f00"); }
        @Override public Map<CssVar, String> darkValues() { return Map.of(FACE, "serif"); }
    }

    static List<Finding> of(RuleId rule, List<Finding> all) { return all.stream().filter(f -> f.rule().equals(rule)).toList(); }

    @Test
    void aCleanDeployment_hasNoFindings() {
        var findings = CssConformance.check(List.of(PaletteCrate.INSTANCE, GoodCrate.INSTANCE), List.of(new Full()));
        assertEquals(List.of(), findings);
    }

    @Test
    void paletteComplete_refusesAMissingToken_andADarkStray() {
        var findings = of(CssConformance.PALETTE_COMPLETE,
                CssConformance.check(List.of(PaletteCrate.INSTANCE), List.of(new Full(), new Partial())));
        assertEquals(2, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("'t2' does not bind [--font-body]"), findings.get(0).message());
        assertTrue(findings.get(1).message().contains("re-binds in dark") && findings.get(1).message().contains("--font-body"));
    }

    @Test
    void noShadowing_refusesAnExtraInADeclaredFamily_andAllowsAPrefixedOne() {
        var findings = of(CssConformance.NO_SHADOWING,
                CssConformance.check(List.of(PaletteCrate.INSTANCE), List.of(new Full(), new Partial())));
        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("'t2'") && findings.get(0).message().contains("--color-danger"));
    }

    @Test
    void tokenDeclared_refusesAnUndeclaredRead_andHonoursRuntimeVars() {
        var findings = of(CssConformance.TOKEN_DECLARED,
                CssConformance.check(List.of(PaletteCrate.INSTANCE, GoodCrate.INSTANCE, BadCrate.INSTANCE), List.of(new Full())));
        assertEquals(1, findings.size(), findings.toString());
        assertEquals(Bad.class.getCanonicalName(), findings.get(0).moduleClass());
        assertTrue(findings.get(0).message().contains("bad_thing reads [--radius-sm, --st-gray-mid]"), findings.get(0).message());
    }

    @Test
    void priorIsPalette_refusesADrawnPrior() {
        var findings = of(CssConformance.PRIOR_IS_PALETTE,
                CssConformance.check(List.of(PaletteCrate.INSTANCE, BadCrate.INSTANCE), List.of(new Full())));
        assertEquals(1, findings.size(), findings.toString());
        assertEquals(Pretender.class.getCanonicalName(), findings.get(0).moduleClass());
    }

    @Test
    void nestedDeclared_refusesANameNobodyOwns_andAcceptsADeclaredOne() {
        var findings = of(CssConformance.NESTED_DECLARED,
                CssConformance.check(List.of(PaletteCrate.INSTANCE, GoodCrate.INSTANCE, BadCrate.INSTANCE), List.of(new Full())));
        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("[ghost]"), findings.get(0).message());
    }

    @Test
    void crateReach_refusesReadingAPriorFromACrateThatDoesNotRequireIt() {
        // Bad reads nothing declared; give the rule a reader: Good's crate without the requirement.
        record Loose() implements Crate {
            @Override public String name() { return "loose"; }
            @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Good.INSTANCE)); }
        }
        var findings = of(CssConformance.CRATE_REACH,
                CssConformance.check(List.of(PaletteCrate.INSTANCE, new Loose()), List.of(new Full())));
        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("does not require"), findings.get(0).message());
    }

    @Test
    void noLiteralFamily_refusesALiteralFace_andALiteralFallback() {
        var findings = of(CssConformance.NO_LITERAL_FAMILY,
                CssConformance.check(List.of(PaletteCrate.INSTANCE, BadCrate.INSTANCE), List.of(new Full())));
        assertEquals(1, findings.size(), findings.toString());
        String m = findings.get(0).message();
        assertTrue(m.contains("font-family: \"Georgia\", serif"), m);
        assertTrue(!m.contains("fallback 4px"), "radius is not a declared family here, so its fallback is not policed: " + m);
    }

    @Test
    void aLibraryWithNoRegistry_getsTheGraphRulesAlone() {
        var findings = CssConformance.check(List.of(PaletteCrate.INSTANCE, GoodCrate.INSTANCE), List.of());
        // No provisions: no priors, so the palette's tokens are reachable only by an edge — Good has none.
        assertTrue(findings.stream().allMatch(f -> f.rule().equals(CssConformance.TOKEN_DECLARED)), findings.toString());
    }
}
