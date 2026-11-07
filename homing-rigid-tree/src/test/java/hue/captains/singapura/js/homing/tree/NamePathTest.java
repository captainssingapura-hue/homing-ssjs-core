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
        assertEquals(NamePath.ROOT, NamePathReader.INSTANCE.parse(null));
        assertEquals(NamePath.ROOT, NamePathReader.INSTANCE.parse(""));
        assertEquals(NamePath.ROOT, NamePathReader.INSTANCE.parse("   "));
        assertEquals(NamePath.ROOT, NamePathReader.INSTANCE.parse("/"));
    }

    // ── Round trip ─────────────────────────────────────────────────────────

    @Test
    void parseAndWireRoundTrip() {
        NamePath p = NamePathReader.INSTANCE.parse("animals/turtle");
        assertEquals(List.of(n("animals"), n("turtle")), p.segments());
        assertEquals("animals/turtle", p.wire());
        assertEquals(2, p.depth());
    }

    @Test
    void leadingAndTrailingSeparatorsAreTolerated() {
        assertEquals(new NamePath(n("animals"), n("turtle")), NamePathReader.INSTANCE.parse("/animals/turtle/"));
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
        assertNull(NamePathReader.INSTANCE.parse("animals/../turtle"));
        assertNull(NamePathReader.INSTANCE.parse("animals/./turtle"));
        assertNull(NamePathReader.INSTANCE.parse("animals/has space/turtle"));
        assertNull(NamePathReader.INSTANCE.parse("animals/" + "x".repeat(NodeName.MAX_CHARS + 1) + "/turtle"));
    }

    @Test
    void aDoubledSeparatorIsRefusedRatherThanCollapsed() {
        assertNull(NamePathReader.INSTANCE.parse("animals//turtle"));
    }

    @Test
    void aTrailingIllegalSegmentDoesNotSilentlyTruncate() {
        // The other half of the same failure: truncating leaves a VALID ancestor.
        assertNull(NamePathReader.INSTANCE.parse("animals/turtle/.."));
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
        NamePath within = new NamePath(n("turtle"));
        NamePath host   = new NamePath(n("demo"), n("animals"));
        assertEquals("demo/animals/turtle", within.under(host).wire());
    }

    @Test
    void underIsIdentityAtTheRootOnBothSides() {
        NamePath p = new NamePath(n("turtle"));
        assertSame(p, p.under(NamePath.ROOT));
        assertEquals(p, NamePath.ROOT.under(p));
    }

    @Test
    void underIsAssociativeSoNestedGraftsAgree() {
        NamePath leaf = new NamePath(n("turtle"));
        NamePath mid  = new NamePath(n("animals"));
        NamePath top  = new NamePath(n("cat"), n("demo"));

        assertEquals(leaf.under(mid).under(top), leaf.under(mid.under(top)));
    }

    // ── Value semantics — what the resolver union depends on ───────────────

    @Test
    void equalPathsAreEqualAndHashAlike() {
        NamePath a = NamePathReader.INSTANCE.parse("animals/turtle");
        NamePath b = new NamePath(n("animals"), n("turtle"));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
