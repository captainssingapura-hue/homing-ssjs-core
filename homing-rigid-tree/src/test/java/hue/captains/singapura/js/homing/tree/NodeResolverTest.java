package hue.captains.singapura.js.homing.tree;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeResolverTest {

    // The substrate ships the interfaces and never an implementation — a
    // consumer's identity kinds are exactly what it cannot name. Tests stand in
    // for two subtrees bringing unrelated kinds to the same forest.
    private record CatId(String name)  implements NodeIdentity {}
    private record DocId(String uuid)  implements NodeIdentity {}

    private static NodeResolver<String> cats(Map<String, String> index) {
        return NodeResolver.forKind(CatId.class, id -> index.get(id.name()));
    }

    private static NodeResolver<String> docs(Map<String, String> index) {
        return NodeResolver.forKind(DocId.class, id -> index.get(id.uuid()));
    }

    // ── The union ──────────────────────────────────────────────────────────

    @Test
    void aUnionAsksEachPartAndTakesTheFirstAnswer() {
        NodeResolver<String> u = NodeResolver.union(List.of(
                cats(Map.of("meta", "the meta catalogue")),
                docs(Map.of("u-1", "a document"))));

        assertEquals(Optional.of("the meta catalogue"), u.resolve(new CatId("meta")));
        assertEquals(Optional.of("a document"),         u.resolve(new DocId("u-1")));
    }

    @Test
    void anIdentityNobodyOwnsResolvesToEmpty() {
        NodeResolver<String> u = NodeResolver.union(List.of(cats(Map.of("meta", "x"))));
        assertEquals(Optional.empty(), u.resolve(new CatId("absent")));
        assertEquals(Optional.empty(), u.resolve(new DocId("u-9")));
    }

    @Test
    void aResolverDeclinesIdentityKindsItDoesNotOwn() {
        // forKind is the boundary: the lambda sees its own type, and a foreign
        // kind is a miss rather than a cast failure.
        NodeResolver<String> onlyCats = cats(Map.of("meta", "x"));
        assertEquals(Optional.empty(), onlyCats.resolve(new DocId("u-1")));
    }

    @Test
    void emptyUnionAndNoneOwnNothing() {
        assertEquals(Optional.empty(), NodeResolver.<String>union(List.of()).resolve(new CatId("meta")));
        assertEquals(Optional.empty(), NodeResolver.<String>none().resolve(new CatId("meta")));
    }

    @Test
    void noneIsTheIdentityElement() {
        NodeResolver<String> c = cats(Map.of("meta", "x"));
        assertEquals(c.resolve(new CatId("meta")), c.or(NodeResolver.<String>none()).resolve(new CatId("meta")));
        assertEquals(c.resolve(new CatId("meta")), NodeResolver.<String>none().or(c).resolve(new CatId("meta")));
    }

    // ── Nesting: a union IS a resolver, so grafts compose ───────────────────

    @Test
    void unionsNestSoAGraftOfAGraftNeedsNoSpecialCase() {
        NodeResolver<String> inner = NodeResolver.union(List.of(
                docs(Map.of("u-1", "inner doc"))));
        NodeResolver<String> outer = NodeResolver.union(List.of(
                cats(Map.of("meta", "outer catalogue")),
                inner));

        assertEquals(Optional.of("inner doc"),       outer.resolve(new DocId("u-1")));
        assertEquals(Optional.of("outer catalogue"), outer.resolve(new CatId("meta")));
    }

    @Test
    void nestingIsAssociative() {
        NodeResolver<String> a = cats(Map.of("meta", "A"));
        NodeResolver<String> b = docs(Map.of("u-1", "B"));
        NodeResolver<String> c = docs(Map.of("u-2", "C"));

        NodeResolver left  = NodeResolver.union(List.of(NodeResolver.union(List.of(a, b)), c));
        NodeResolver<String> right = NodeResolver.union(List.of(a, NodeResolver.union(List.of(b, c))));

        for (NodeIdentity id : List.of(new CatId("meta"), new DocId("u-1"), new DocId("u-2"))) {
            assertEquals(left.resolve(id), right.resolve(id), "associativity at " + id);
        }
    }

    // ── The property the whole design rests on ─────────────────────────────

    /**
     * First-match is order-INDEPENDENT exactly when the domains are disjoint,
     * which is what {@link NodeIdentity} already requires. So over a conforming
     * forest the union is commutative, and merge order cannot change an answer.
     */
    @Test
    void theUnionIsCommutativeWhenDomainsAreDisjoint() {
        NodeResolver<String> a = cats(Map.of("meta", "A"));
        NodeResolver<String> b = docs(Map.of("u-1", "B"));

        NodeResolver<String> ab = NodeResolver.union(List.of(a, b));
        NodeResolver<String> ba = NodeResolver.union(List.of(b, a));

        for (NodeIdentity id : List.of(new CatId("meta"), new DocId("u-1"), new CatId("nope"))) {
            assertEquals(ab.resolve(id), ba.resolve(id), "order must not matter at " + id);
        }
    }

    /**
     * ...and when they are NOT disjoint, order decides — which is why the
     * disjointness contract is load-bearing rather than tidy. The union does not
     * try to reconcile this; a collision is a Law 1 violation, reported where the
     * law is checked ({@link NodeIdentities#duplicatesIn}).
     */
    @Test
    void overlappingDomainsMakeTheAnswerDependOnOrder() {
        NodeResolver<String> a = cats(Map.of("meta", "from A"));
        NodeResolver<String> b = cats(Map.of("meta", "from B"));

        assertEquals(Optional.of("from A"), NodeResolver.union(List.of(a, b)).resolve(new CatId("meta")));
        assertEquals(Optional.of("from B"), NodeResolver.union(List.of(b, a)).resolve(new CatId("meta")));
    }

    @Test
    void aUnionOfOneIsThatResolversAnswer() {
        NodeResolver<String> a = cats(Map.of("meta", "A"));
        assertEquals(a.resolve(new CatId("meta")),
                     NodeResolver.union(List.of(a)).resolve(new CatId("meta")));
    }

    // ── Identity uniqueness over a real tree shape ─────────────────────────

    private static NormalizedNode node(TreeLevel level, String seg, NodeIdentity id,
                                       NormalizedNode... kids) {
        return new NormalizedNode(level, new NodeName(seg), id, Map.of(), List.of(kids));
    }

    @Test
    void aConformingTreeCarriesDistinctIdentities() {
        NormalizedNode root = node(TreeLevel.L0.INSTANCE, "root", new CatId("root"),
                node(TreeLevel.L1.INSTANCE, "a", new CatId("a"),
                        node(TreeLevel.L2.INSTANCE, "doc", new DocId("u-1"))),
                node(TreeLevel.L1.INSTANCE, "b", new CatId("b")));

        assertTrue(NodeIdentities.disjoint(root));
        assertEquals(Map.of(), NodeIdentities.duplicatesIn(root));
        assertEquals(4, NodeIdentities.allIn(root).size());
    }

    /**
     * The same source mounted twice: two vertices, one identity. Under the design
     * that is not a duplicate to merge but "a navigable at two positions" — the
     * violation the union's precondition exists to surface.
     */
    @Test
    void oneIdentityAtTwoVerticesIsReportedWithItsCount() {
        NodeIdentity shared = new DocId("u-1");
        NormalizedNode root = node(TreeLevel.L0.INSTANCE, "root", new CatId("root"),
                node(TreeLevel.L1.INSTANCE, "a", new CatId("a"),
                        node(TreeLevel.L2.INSTANCE, "doc", shared)),
                node(TreeLevel.L1.INSTANCE, "b", new CatId("b"),
                        node(TreeLevel.L2.INSTANCE, "doc", shared)));

        assertFalse(NodeIdentities.disjoint(root));
        assertEquals(Map.of(shared, 2), NodeIdentities.duplicatesIn(root));
    }

    @Test
    void identityRidesAGraftSoADuplicateMountCollides() {
        // A subtree normalized standalone, then grafted under a host that already
        // contains it. Because the shift leaves identity alone, the two copies
        // collide — which is exactly what makes the mount detectable at all.
        NormalizedNode source = node(TreeLevel.L0.INSTANCE, "src", new CatId("src"),
                node(TreeLevel.L1.INSTANCE, "doc", new DocId("u-1")));

        NormalizedNode host = node(TreeLevel.L0.INSTANCE, "host", new CatId("host"),
                RigidTrees.graftUnder(source, TreeLevel.L0.INSTANCE),
                RigidTrees.graftUnder(source, TreeLevel.L0.INSTANCE));

        var dupes = NodeIdentities.duplicatesIn(host);
        assertEquals(2, dupes.size(), "both the source root and its doc collide: " + dupes);
        assertEquals(2, dupes.get(new CatId("src")));
        assertEquals(2, dupes.get(new DocId("u-1")));
    }

    @Test
    void graftingDoesNotDisturbTheResolver() {
        // The resolver is keyed on identity, and a graft changes only levels — so
        // the same resolver answers for a grafted vertex without rebasing.
        NodeResolver<String> r = docs(Map.of("u-1", "the doc"));
        NormalizedNode source = node(TreeLevel.L0.INSTANCE, "src", new CatId("src"),
                node(TreeLevel.L1.INSTANCE, "doc", new DocId("u-1")));
        NormalizedNode grafted = RigidTrees.graftUnder(source, TreeLevel.L3.INSTANCE);

        NodeIdentity beforeId = source.children().get(0).identity();
        NodeIdentity afterId  = grafted.children().get(0).identity();

        assertSame(beforeId.getClass(), afterId.getClass());
        assertEquals(beforeId, afterId);
        assertEquals(r.resolve(beforeId), r.resolve(afterId));
        assertEquals(Optional.of("the doc"), r.resolve(afterId));
    }
}
