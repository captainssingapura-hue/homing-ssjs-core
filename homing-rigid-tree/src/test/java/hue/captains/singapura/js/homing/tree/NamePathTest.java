package hue.captains.singapura.js.homing.tree;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamePathTest {

    private static NodeName n(String s) { return new NodeName(s); }

    // ── The root ───────────────────────────────────────────────────────────

    @Test
    void rootIsEmptyAndWiresToTheEmptyString() {
        assertTrue(NamePath.ROOT.isEmpty());
        assertEquals(0, NamePath.ROOT.depth());
        assertEquals("", NamePath.ROOT.wire());
    }

    @Test
    void nullAndBlankWireFormsAreTheRoot() {
        assertEquals(NamePath.ROOT, NamePath.parse(null));
        assertEquals(NamePath.ROOT, NamePath.parse(""));
        assertEquals(NamePath.ROOT, NamePath.parse("   "));
        assertEquals(NamePath.ROOT, NamePath.parse("/"));
    }

    // ── Round trip ─────────────────────────────────────────────────────────

    @Test
    void parseAndWireRoundTrip() {
        NamePath p = NamePath.parse("animals/turtle");
        assertEquals(List.of(n("animals"), n("turtle")), p.segments());
        assertEquals("animals/turtle", p.wire());
        assertEquals(2, p.depth());
    }

    @Test
    void leadingAndTrailingSeparatorsAreTolerated() {
        assertEquals(NamePath.of(n("animals"), n("turtle")), NamePath.parse("/animals/turtle/"));
    }

    // ── All-or-nothing parsing: the property this type exists for ──────────

    /**
     * The bug this type is designed out of existence: a lenient parser SKIPS the
     * bad segment and resolves {@code animals/../turtle} as {@code animals/turtle}
     * — a real node, served with a 200. Refusing the whole path is the only safe
     * answer, because a partial parse of a path is still a well-formed path.
     */
    @Test
    void oneIllegalSegmentRefusesTheWholePath() {
        assertNull(NamePath.parse("animals/../turtle"));
        assertNull(NamePath.parse("animals/./turtle"));
        assertNull(NamePath.parse("animals/has space/turtle"));
        assertNull(NamePath.parse("animals/" + "x".repeat(NodeName.MAX_CHARS + 1) + "/turtle"));
    }

    @Test
    void aDoubledSeparatorIsRefusedRatherThanCollapsed() {
        assertNull(NamePath.parse("animals//turtle"));
    }

    @Test
    void aTrailingIllegalSegmentDoesNotSilentlyTruncate() {
        // The other half of the same failure: truncating leaves a VALID ancestor.
        assertNull(NamePath.parse("animals/turtle/.."));
    }

    // ── Construction ───────────────────────────────────────────────────────

    @Test
    void thenAppendsOneSegment() {
        NamePath p = NamePath.ROOT.then(n("animals")).then(n("turtle"));
        assertEquals("animals/turtle", p.wire());
    }

    @Test
    void segmentsAreDefensivelyCopiedAndNullsRefused() {
        var mutable = new java.util.ArrayList<>(List.of(n("a")));
        NamePath p = new NamePath(mutable);
        mutable.add(n("b"));
        assertEquals(1, p.depth());

        var withNull = new java.util.ArrayList<NodeName>();
        withNull.add(null);
        assertThrows(IllegalArgumentException.class, () -> new NamePath(withNull));
    }

    // ── Composition: position composes, identity does not ──────────────────

    @Test
    void underRerootsBeneathAPrefix() {
        NamePath within = NamePath.of(n("turtle"));
        NamePath host   = NamePath.of(n("demo"), n("animals"));
        assertEquals("demo/animals/turtle", within.under(host).wire());
    }

    @Test
    void underIsIdentityAtTheRootOnBothSides() {
        NamePath p = NamePath.of(n("turtle"));
        assertSame(p, p.under(NamePath.ROOT));
        assertEquals(p, NamePath.ROOT.under(p));
    }

    @Test
    void underIsAssociativeSoNestedGraftsAgree() {
        NamePath leaf = NamePath.of(n("turtle"));
        NamePath mid  = NamePath.of(n("animals"));
        NamePath top  = NamePath.of(n("cat"), n("demo"));

        assertEquals(leaf.under(mid).under(top), leaf.under(mid.under(top)));
    }

    // ── Value semantics — what the resolver union depends on ───────────────

    @Test
    void equalPathsAreEqualAndHashAlike() {
        NamePath a = NamePath.parse("animals/turtle");
        NamePath b = NamePath.of(n("animals"), n("turtle"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
