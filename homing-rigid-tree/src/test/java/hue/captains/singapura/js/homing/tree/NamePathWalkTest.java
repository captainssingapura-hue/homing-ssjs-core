package hue.captains.singapura.js.homing.tree;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
        var walk = NamePathWalk.from(studio(), NamePath.ROOT);
        var found = assertInstanceOf(NamePathWalk.Found.class, walk);
        assertEquals(new Id("studio"), found.identity());
        assertEquals(NamePath.ROOT, found.path());
    }

    @Test
    void aPathAdvancesOneLevelPerSegment() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/ontology"));
        var found = assertInstanceOf(NamePathWalk.Found.class, walk);
        assertEquals(new Id("ontology"), found.identity());
        assertEquals("meta/ontology", found.path().wire());
    }

    @Test
    void itWalksAllTheWayToALeaf() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/ontology/doc-ontology"));
        var found = assertInstanceOf(NamePathWalk.Found.class, walk);
        assertEquals(new Id("doc-ontology"), found.identity());
    }

    /**
     * The root's own segment is not part of any name-path — so a path that
     * repeats it is a miss, not a courtesy. This is what keeps a catalogue
     * vertex's name-path equal to its URL after the route prefix.
     */
    @Test
    void theRootsOwnSegmentIsNotPartOfThePath() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("studio/meta"));
        var missing = assertInstanceOf(NamePathWalk.Missing.class, walk);
        assertEquals(n("studio"), missing.segment());
        assertEquals(0, missing.depth());
    }

    // ── Missing: the partial path is the useful half ────────────────────────

    @Test
    void aMissReportsHowFarItGotAndWhatFailed() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/nope/deeper"));
        var missing = assertInstanceOf(NamePathWalk.Missing.class, walk);

        assertEquals("meta", missing.matched().wire(), "the partial path that did resolve");
        assertEquals(n("nope"), missing.segment(),     "the segment that matched nothing");
        assertEquals(1, missing.depth());
        assertEquals(NamePathWalk.Reason.NO_SUCH_CHILD, missing.reason());
    }

    @Test
    void theMissIsAtTheFIRSTLevelThatFails() {
        // Both 'nope' and 'alsoNope' are absent; the walk stops at the first.
        var walk = NamePathWalk.from(studio(), NamePath.parse("nope/alsoNope"));
        var missing = assertInstanceOf(NamePathWalk.Missing.class, walk);
        assertEquals(NamePath.ROOT, missing.matched());
        assertEquals(n("nope"), missing.segment());
    }

    /**
     * Running past a leaf is distinguishable from a branch lacking a child —
     * different answers, and the difference costs one emptiness test.
     */
    @Test
    void runningPastALeafSaysSoRatherThanJustNoSuchChild() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/ontology/doc-ontology/extra"));
        var missing = assertInstanceOf(NamePathWalk.Missing.class, walk);

        assertEquals("meta/ontology/doc-ontology", missing.matched().wire());
        assertEquals(n("extra"), missing.segment());
        assertEquals(NamePathWalk.Reason.PAST_A_LEAF, missing.reason());
    }

    @Test
    void aBranchMissingAChildIsNotPastALeaf() {
        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/absent"));
        var missing = assertInstanceOf(NamePathWalk.Missing.class, walk);
        assertEquals(NamePathWalk.Reason.NO_SUCH_CHILD, missing.reason());
    }

    // ── The two halves compose ─────────────────────────────────────────────

    /**
     * Path to node to identity to content, end to end — and the seam holds: the
     * walk never names a payload, the resolver never names a path.
     */
    @Test
    void theWalkHandsAnIdentityStraightToTheResolver() {
        NodeResolver resolver = NodeResolver.forKind(Id.class,
                id -> "content for " + id.name());

        var walk = NamePathWalk.from(studio(), NamePath.parse("meta/ontology"));
        var found = assertInstanceOf(NamePathWalk.Found.class, walk);

        assertEquals(Optional.of("content for ontology"), resolver.resolve(found.identity()));
    }

    @Test
    void aGraftedSubtreeIsWalkedLikeAnyOther() {
        // The graft happened at build time, so the walk steps through it with no
        // unwrapping and no knowledge that it was ever a separate tree.
        NormalizedNode source = node(TreeLevel.L0.INSTANCE, "animals",
                node(TreeLevel.L1.INSTANCE, "turtle"));
        NormalizedNode host = node(TreeLevel.L0.INSTANCE, "demo",
                RigidTrees.graftUnder(source, TreeLevel.L0.INSTANCE));

        var walk = NamePathWalk.from(host, NamePath.parse("animals/turtle"));
        var found = assertInstanceOf(NamePathWalk.Found.class, walk);
        assertEquals(new Id("turtle"), found.identity());
    }
}
