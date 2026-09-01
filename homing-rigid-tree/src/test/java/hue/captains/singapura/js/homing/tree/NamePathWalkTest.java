package hue.captains.singapura.js.homing.tree;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamePathWalkTest {

    private record Id(String name) implements NodeIdentity {}

    private static NodeName n(String s) { return new NodeName(s); }

    private static NormalizedNode node(TreeLevel level, String seg, NormalizedNode... kids) {
        return new NormalizedNode(level, n(seg), new Id(seg), Map.of(), List.of(kids));
    }

    /** studio -> meta -> ontology -> doc-ontology (a leaf), and studio -> rfcs. */
    private static NormalizedNode studio() {
        return node(TreeLevel.L0.INSTANCE, "studio",
                node(TreeLevel.L1.INSTANCE, "meta",
                        node(TreeLevel.L2.INSTANCE, "ontology",
                                node(TreeLevel.L3.INSTANCE, "doc-ontology"))),
                node(TreeLevel.L1.INSTANCE, "rfcs"));
    }

    // ── Found ──────────────────────────────────────────────────────────────

    @Test
    void anEmptyPathNamesTheRootItself() {
        var found = assertInstanceOf(NamePathWalk.Found.class,
                NamePathWalk.from(studio(), NamePath.ROOT));
        assertEquals(new Id("studio"), found.identity());
        assertEquals(NamePath.ROOT, found.path());
    }

    @Test
    void aPathAdvancesOneLevelPerSegment() {
        var found = assertInstanceOf(NamePathWalk.Found.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/ontology")));
        assertEquals(new Id("ontology"), found.identity());
        assertEquals("meta/ontology", found.path().wire());
    }

    @Test
    void itWalksAllTheWayToALeaf() {
        var found = assertInstanceOf(NamePathWalk.Found.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/ontology/doc-ontology")));
        assertEquals(new Id("doc-ontology"), found.identity());
    }

    /**
     * The root's own segment is not part of any name-path — so a path repeating
     * it is a miss, not a courtesy. This is what keeps a catalogue vertex's
     * name-path equal to its URL after the route prefix.
     */
    @Test
    void theRootsOwnSegmentIsNotPartOfThePath() {
        var missing = assertInstanceOf(NamePathWalk.Missing.class,
                NamePathWalk.from(studio(), NamePath.parse("studio/meta")));
        assertEquals(n("studio"), missing.wanted());
        assertEquals(0, missing.depth());
    }

    // ── NoSuchChild — carries what IS there ────────────────────────────────

    @Test
    void noSuchChildReportsTheSegmentsThatDoExist() {
        var miss = assertInstanceOf(NamePathWalk.NoSuchChild.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/nope/deeper")));

        assertEquals("meta", miss.matched().wire(), "the partial path that did resolve");
        assertEquals(n("nope"), miss.wanted());
        assertEquals(1, miss.depth());
        // The evidence a reason tag could never have carried: what to suggest.
        assertEquals(List.of(n("ontology")), miss.available());
    }

    @Test
    void theMissIsAtTheFIRSTLevelThatFails() {
        // Both 'nope' and 'alsoNope' are absent; the walk stops at the first.
        var miss = assertInstanceOf(NamePathWalk.NoSuchChild.class,
                NamePathWalk.from(studio(), NamePath.parse("nope/alsoNope")));
        assertEquals(NamePath.ROOT, miss.matched());
        assertEquals(n("nope"), miss.wanted());
        assertTrue(miss.available().containsAll(List.of(n("meta"), n("rfcs"))));
    }

    @Test
    void availableIsAnImmutableCopy() {
        var miss = assertInstanceOf(NamePathWalk.NoSuchChild.class,
                NamePathWalk.from(studio(), NamePath.parse("absent")));
        var available = miss.available();
        assertEquals(2, available.size());
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> available.add(n("x")));
    }

    // ── PastALeaf — carries the leaf, so a redirect is possible ────────────

    /**
     * A real address with something appended is usually a stale deep link, not a
     * typo. The leaf travels with the failure, so a caller can answer with the
     * leaf rather than only refusing — which the enum this replaced promised in
     * its javadoc and could not support.
     */
    @Test
    void pastALeafCarriesTheLeafItselfAndTheUnconsumedTail() {
        var miss = assertInstanceOf(NamePathWalk.PastALeaf.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/ontology/doc-ontology/extra/more")));

        assertEquals("meta/ontology/doc-ontology", miss.matched().wire(),
                "where a caller would redirect");
        assertEquals(new Id("doc-ontology"), miss.leaf(),
                "the leaf's identity, so its content can still be resolved");
        assertEquals(n("extra"), miss.wanted());
        assertEquals("extra/more", miss.remaining().wire());
    }

    @Test
    void aBranchMissingAChildIsNotPastALeaf() {
        assertInstanceOf(NamePathWalk.NoSuchChild.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/absent")));
    }

    /** Both failures group, so a caller may handle them uniformly. */
    @Test
    void bothFailuresAreMissingAndCarryHowFarTheyGot() {
        for (String path : List.of("meta/absent", "meta/ontology/doc-ontology/extra")) {
            var missing = assertInstanceOf(NamePathWalk.Missing.class,
                    NamePathWalk.from(studio(), NamePath.parse(path)), path);
            assertEquals("meta", missing.matched().wire().split("/")[0], path);
        }
    }

    // ── The two halves compose ─────────────────────────────────────────────

    /**
     * Path to node to identity to content, end to end — and the seam holds: the
     * walk never names a payload, the resolver never names a path.
     */
    @Test
    void theWalkHandsAnIdentityStraightToTheResolver() {
        NodeResolver resolver = NodeResolver.forKind(Id.class, id -> "content for " + id.name());

        var found = assertInstanceOf(NamePathWalk.Found.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/ontology")));

        assertEquals(Optional.of("content for ontology"), resolver.resolve(found.identity()));
    }

    /** ...and a past-a-leaf miss can still resolve the leaf it overshot. */
    @Test
    void aPastALeafMissCanStillResolveTheLeafContent() {
        NodeResolver resolver = NodeResolver.forKind(Id.class, id -> "content for " + id.name());

        var miss = assertInstanceOf(NamePathWalk.PastALeaf.class,
                NamePathWalk.from(studio(), NamePath.parse("meta/ontology/doc-ontology/extra")));

        assertEquals(Optional.of("content for doc-ontology"), resolver.resolve(miss.leaf()));
    }

    @Test
    void aGraftedSubtreeIsWalkedLikeAnyOther() {
        // The graft happened at build time, so the walk steps through it with no
        // unwrapping and no knowledge that it was ever a separate tree.
        NormalizedNode source = node(TreeLevel.L0.INSTANCE, "animals",
                node(TreeLevel.L1.INSTANCE, "turtle"));
        NormalizedNode host = node(TreeLevel.L0.INSTANCE, "demo",
                RigidTrees.graftUnder(source, TreeLevel.L0.INSTANCE));

        var found = assertInstanceOf(NamePathWalk.Found.class,
                NamePathWalk.from(host, NamePath.parse("animals/turtle")));
        assertEquals(new Id("turtle"), found.identity());
    }
}
