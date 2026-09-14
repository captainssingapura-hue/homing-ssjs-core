package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0058 — a {@link WorkspaceGroup} is a tree, and it validates itself at
 * construction (laws 1–3); the registry refuses a kind in two groups (law 4,
 * first half). Every failure names the group and the thing that is wrong.
 */
class WorkspaceGroupTest {

    @AfterEach
    void reset() {
        WorkspaceGroupRegistry.INSTANCE.resetForTesting();
        WorkspaceSpecRegistry.INSTANCE.resetForTesting();
    }

    // ------------------------------------------------------------- the tree

    @Test
    void sectionsKeepFirstAppearanceOrderAndCarryServedSlugs() {
        var trader = reg(spec("trader",   "Trader Desk",     "Trading"));
        var sales  = reg(spec("sales",    "Sales Desk",      "Trading"));
        var ipv    = reg(spec("ipv",      "IPV",             "Product Control & IPV"));
        var risk   = reg(spec("risk",     "Risk",            "Risk"));
        var group  = WorkspaceGroup.of("fx-desk", "FX Options Desk", "", List.of(trader, ipv, sales, risk));

        var sections = group.sections();
        assertEquals(List.of("Trading", "Product Control & IPV", "Risk"),
                sections.stream().map(WorkspaceGroup.Section::name).toList());
        assertEquals(List.of("trading", "product-control-ipv", "risk"),
                sections.stream().map(s -> s.slug().value()).toList());
        assertEquals(List.of("trader", "sales"), sections.get(0).kinds().stream().map(WorkspaceSpec::kind).toList());

        assertEquals("trader", group.defaultKind(), "the first spec is the default when none is named");
        assertEquals("ws/product-control-ipv/ipv", group.anchorFor("ipv").orElseThrow());
        assertTrue(group.anchorFor("nope").isEmpty());
        assertEquals("Trading", group.sectionOf("sales").orElseThrow().name());
    }

    @Test
    void aNamedDefaultIsHonoured() {
        var a = reg(spec("a", "A", "S")); var b = reg(spec("b", "B", "S"));
        assertEquals("b", new WorkspaceGroup("g", "G", null, "b", List.of(a, b)).defaultKind());
    }

    // ------------------------------------------------------------- the laws

    @Test
    void law1_everyKindIsRegistered() {
        var a = reg(spec("a", "A", "S"));
        var stray = spec("stray", "Stray", "S");       // never registered
        var e = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("g", "G", "", List.of(a, stray)));
        assertTrue(e.getMessage().contains("'stray'") && e.getMessage().contains("not registered"), e.getMessage());
    }

    @Test
    void law1_sectionSlugsAreUniqueAmongTheGroupsSections() {
        var a = reg(spec("a", "A", "Product Control"));
        var b = reg(spec("b", "B", "Product-Control"));   // a different heading, the same slug
        var e = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("g", "G", "", List.of(a, b)));
        assertTrue(e.getMessage().contains("'Product Control'") && e.getMessage().contains("'Product-Control'")
                && e.getMessage().contains("'product-control'"), e.getMessage());
    }

    @Test
    void law2_theDefaultKindIsAMember() {
        var a = reg(spec("a", "A", "S"));
        var e = assertThrows(IllegalArgumentException.class,
                () -> new WorkspaceGroup("g", "G", "", "b", List.of(a)));
        assertTrue(e.getMessage().contains("default kind 'b'") && e.getMessage().contains("members: a"), e.getMessage());
    }

    @Test
    void law3_aKindsSegmentIsItsId() {
        // The registry accepts any non-blank kind; a group needs it to be a segment.
        var odd = reg(spec("has space", "Odd", "S"));
        var e = assertThrows(IllegalArgumentException.class,
                () -> WorkspaceGroup.of("g", "G", "", List.of(odd)));
        assertTrue(e.getMessage().contains("'has space'") && e.getMessage().contains("anchor segment"), e.getMessage());
        // And the group's own id is a segment too.
        var a = reg(spec("a", "A", "S"));
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("bad id", "G", "", List.of(a)));
    }

    @Test
    void aGroupHoldsAtLeastOneKindAndNoKindTwice() {
        assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("g", "G", "", List.of()));
        var a = reg(spec("a", "A", "S"));
        var e = assertThrows(IllegalArgumentException.class, () -> WorkspaceGroup.of("g", "G", "", List.of(a, a)));
        assertTrue(e.getMessage().contains("listed twice"), e.getMessage());
    }

    @Test
    void law4_aKindBelongsToExactlyOneGroup() {
        var a = reg(spec("a", "A", "S")); var b = reg(spec("b", "B", "S"));
        var g1 = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("g1", "G1", "", List.of(a)));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("g2", "G2", "", List.of(a, b))));
        assertTrue(e.getMessage().contains("'a'") && e.getMessage().contains("'g1'") && e.getMessage().contains("'g2'"),
                e.getMessage());
        // The registry answers kind → group, and re-registering the same value is idempotent.
        assertSame(g1, WorkspaceGroupRegistry.INSTANCE.groupOf("a").orElseThrow());
        assertSame(g1, WorkspaceGroupRegistry.INSTANCE.register(g1));
        assertTrue(WorkspaceGroupRegistry.INSTANCE.groupOf("b").isEmpty());
        // A different group under an id already taken is refused.
        var e2 = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("g1", "Other", "", List.of(b))));
        assertTrue(e2.getMessage().contains("Duplicate WorkspaceGroup id 'g1'"), e2.getMessage());
    }

    // ------------------------------------------------------------ the wire

    @Test
    void theWireShapeCarriesSectionsInOrderWithSlugs() {
        var trader = reg(spec("trader", "Trader Desk", "Trading"));
        var ipv    = reg(spec("ipv", "IPV", "Product Control & IPV"));
        var group  = WorkspaceGroup.of("fx-desk", "FX Desk", "Docs and desks", List.of(trader, ipv));
        String json = WorkspaceGroupJson.one(group);
        assertTrue(json.startsWith("{\"id\":\"fx-desk\",\"title\":\"FX Desk\",\"summary\":\"Docs and desks\",\"defaultKind\":\"trader\",\"kinds\":["), json);
        assertTrue(json.contains("{\"kind\":\"trader\",\"title\":\"Trader Desk\",\"section\":\"Trading\",\"sectionSlug\":\"trading\"}"), json);
        assertTrue(json.contains("{\"kind\":\"ipv\",\"title\":\"IPV\",\"section\":\"Product Control & IPV\",\"sectionSlug\":\"product-control-ipv\"}"), json);
        assertTrue(WorkspaceGroupJson.allAsObject(List.of(group)).startsWith("{\"fx-desk\":{"));
    }

    // ------------------------------------------------------------ fixtures

    static WorkspaceSpec reg(WorkspaceSpec s) { return WorkspaceSpecRegistry.INSTANCE.register(s); }

    /** A minimal spec: one widget, the default single pane, a section. */
    static WorkspaceSpec spec(String kind, String title, String section) {
        return new WorkspaceSpec() {
            @Override public String kind()    { return kind; }
            @Override public String title()   { return title; }
            @Override public String section() { return section; }
            @Override public List<WidgetEntry> widgetEntries() {
                return List.of(WidgetEntry.of(OneWidget.class, WidgetLabel.of("One")));
            }
        };
    }

    abstract static class OneWidget extends WorkspaceWidget<WorkspaceWidget._None, OneWidget> {}
}
