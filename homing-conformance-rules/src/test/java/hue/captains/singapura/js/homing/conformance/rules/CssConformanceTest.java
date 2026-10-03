package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RFC 0066 — each law refuses the one thing it is for, and nothing else. */
class CssConformanceTest {

    // ── Fixtures: a base crate, a drawn crate that requires it, one that does not ──

    static final CssVar GUIDE = new CssVar("--hgr-guide-x");

    record Base() implements CssGroup<Base> {
        static final Base INSTANCE = new Base();
        record base_btn() implements CssClass<Base> { @Override public String body() { return "margin: 0;"; } }
        @Override public List<CssClass<Base>> cssClasses() { return List.of(new base_btn()); }
    }
    record BaseCrate() implements Crate {
        static final BaseCrate INSTANCE = new BaseCrate();
        @Override public String name() { return "base"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Base.INSTANCE)); }
    }

    record Good() implements CssGroup<Good> {
        static final Good INSTANCE = new Good();
        record good_btn() implements CssClass<Good> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Base.base_btn()); }
            @Override public String body() { return "color: red;\n&:hover { color: blue; }\n& .base-btn { margin: 0; }\n"; }
        }
        record good_guide() implements CssClass<Good> {
            @Override public Set<CssVar> runtimeVars() { return Set.of(GUIDE); }
            @Override public String body() { return "left: var(--hgr-guide-x);"; }
        }
        @Override public List<CssClass<Good>> cssClasses() { return List.of(new good_btn(), new good_guide()); }
    }
    record GoodCrate() implements Crate {
        static final GoodCrate INSTANCE = new GoodCrate();
        @Override public String name() { return "good"; }
        @Override public List<Crate> requires() { return List.of(BaseCrate.INSTANCE); }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Good.INSTANCE)); }
    }

    record Bad() implements CssGroup<Bad> {
        static final Bad INSTANCE = new Bad();
        /** Reads a variable nobody sets, and names a class nobody owns. */
        record bad_thing() implements CssClass<Bad> {
            @Override public String body() { return "color: var(--st-gray-mid);\nborder-radius: var(--radius-sm, 4px);\n& .ghost { opacity: .5; }\n"; }
        }
        @Override public List<CssClass<Bad>> cssClasses() { return List.of(new bad_thing()); }
    }
    record BadCrate() implements Crate {
        static final BadCrate INSTANCE = new BadCrate();
        @Override public String name() { return "bad"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Bad.INSTANCE)); }
    }

    static List<Finding> of(RuleId rule, List<Finding> all) { return all.stream().filter(f -> f.rule().equals(rule)).toList(); }

    @Test
    void aCleanDeployment_hasNoFindings() {
        assertEquals(List.of(), CssConformance.check(List.of(BaseCrate.INSTANCE, GoodCrate.INSTANCE)));
    }

    @Test
    void tokenDeclared_refusesAnUndeclaredRead_andHonoursRuntimeVars() {
        var findings = of(CssConformance.TOKEN_DECLARED,
                CssConformance.check(List.of(BaseCrate.INSTANCE, GoodCrate.INSTANCE, BadCrate.INSTANCE)));
        assertEquals(1, findings.size(), findings.toString());
        assertEquals(Bad.class.getCanonicalName(), findings.get(0).moduleClass());
        assertTrue(findings.get(0).message().contains("bad_thing reads [--radius-sm, --st-gray-mid]"), findings.get(0).message());
    }

    @Test
    void nestedDeclared_refusesANameNobodyOwns_andAcceptsADeclaredOne() {
        var findings = of(CssConformance.NESTED_DECLARED,
                CssConformance.check(List.of(BaseCrate.INSTANCE, GoodCrate.INSTANCE, BadCrate.INSTANCE)));
        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("[ghost]"), findings.get(0).message());
    }

    @Test
    void crateReach_refusesDependingOnAGroupOfACrateNotRequired() {
        record Loose() implements Crate {
            @Override public String name() { return "loose"; }
            @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Good.INSTANCE)); }
        }
        var findings = of(CssConformance.CRATE_REACH, CssConformance.check(List.of(BaseCrate.INSTANCE, new Loose())));
        assertEquals(1, findings.size(), findings.toString());
        assertTrue(findings.get(0).message().contains("does not require"), findings.get(0).message());
    }
}
