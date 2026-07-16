package hue.captains.singapura.js.homing.studio.base.composed.graph;

import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2;
import hue.captains.singapura.js.homing.studio.base.composed.ParagraphSegment;
import hue.captains.singapura.js.homing.studio.base.composed.RigidNodeContent;
import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NodeKey;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RigidDocV2 adapter, parent-pointer form: {@link RigidNodeNormalizer} assembles a
 * flat list of parent-linked {@link RigidNode}s — each wrapping a source {@code T}
 * (here a {@code String} body) — into a {@link DocTreeV2}, resolving content with a
 * single {@code Function<T, RigidNodeContent>}. Cycles / multi-parent / wrong-level
 * are unrepresentable, so the surviving checks are one-root, all-reachable, and
 * unique sibling names.
 */
class RigidNodeNormalizerTest {

    /** The one content provider: an empty body is a structural node, else a paragraph. */
    private static final Function<String, RigidNodeContent> CONTENT =
            body -> body.isEmpty() ? RigidNodeContent.of() : RigidNodeContent.of(ParagraphSegment.of(body));

    private static RigidNode<String> root(String name, String title) {
        return RigidNode.root("", new NodeName(name), new Title(title));   // structural root
    }

    private static RigidNode<String> child(RigidNode<String> p, String name, String title, String body) {
        return p.child(body, new NodeName(name), new Title(title));
    }

    private static DocTreeV2 build(List<RigidNode<String>> nodes) {
        return RigidNodeNormalizer.INSTANCE.toDocTree(nodes, CONTENT);
    }

    private static String dim(NormalizedNode n, hue.captains.singapura.js.homing.tree.DimensionKey k) {
        var v = n.dimensions().get(k);
        return v == null ? null : v.displayText();
    }

    // ── the happy path ────────────────────────────────────────────────────────

    @Test
    void buildsStructureAndKeysContentByNamePath() {
        var r = root("root", "The Guide");
        var animals = child(r, "animals", "Animals", "Warm and cuddly.");
        var turtle = child(animals, "turtle", "Turtle", "Slow and steady.");
        var plants = child(r, "plants", "Plants", "Green and quiet.");

        DocTreeV2 tree = build(List.of(r, animals, turtle, plants));

        assertEquals(java.util.Set.of("animals", "animals/turtle", "plants"),
                tree.providers().keySet());
        assertTrue(tree.providerAt("animals/turtle").isPresent());
        assertFalse(tree.providerAt("").isPresent());          // root is structural

        NormalizedNode structRoot = tree.structure();
        assertEquals("L0", structRoot.level().tag());
        assertEquals("The Guide", dim(structRoot, DisplayLabel.INSTANCE));
        assertEquals("root", dim(structRoot, NodeKey.INSTANCE));
        assertEquals(2, structRoot.children().size());

        NormalizedNode a = structRoot.children().get(0);
        assertEquals("L1", a.level().tag());
        assertEquals("animals", dim(a, NodeKey.INSTANCE));
        assertEquals("L2", a.children().get(0).level().tag());
    }

    @Test
    void ordersChildrenByExplicitOrderThenName() {
        var r = root("root", "Root");
        // Declared out of order; "b" has explicit order 0 so it leads, then the
        // rest fall back to alphabetical: a, c.
        var c = r.child("", new NodeName("c"), new Title("C"), 5);
        var a = r.child("", new NodeName("a"), new Title("A"));
        var b = r.child("", new NodeName("b"), new Title("B"), 0);

        NormalizedNode structRoot = build(List.of(r, c, a, b)).structure();
        var names = structRoot.children().stream().map(n -> dim(n, NodeKey.INSTANCE)).toList();
        assertEquals(List.of("b", "c", "a"), names);   // order 0, order 5, then unordered "a"
    }

    @Test
    void derivesTitleFromNodeNameWhenOmitted() {
        var r = RigidNode.root("", new NodeName("root"));            // title defaults
        var a = r.child("body", new NodeName("dancing-animals"));    // title defaults
        NormalizedNode structRoot = build(List.of(r, a)).structure();

        assertEquals("Root", dim(structRoot, DisplayLabel.INSTANCE));
        NormalizedNode kid = structRoot.children().get(0);
        assertEquals("Dancing Animals", dim(kid, DisplayLabel.INSTANCE));   // humanized
        assertEquals("dancing-animals", dim(kid, NodeKey.INSTANCE));        // name unchanged
    }

    // ── construction-time invariant ───────────────────────────────────────────

    @Test
    void constructorPinsLevelToParentPlusOne() {
        var r = root("root", "Root");
        // A mis-stamped level is rejected at construction, not later.
        var ex = assertThrows(IllegalArgumentException.class, () ->
                new RigidNode<>(r, 5, new NodeName("x"), new Title("X"), "", OptionalInt.empty()));
        assertTrue(ex.getMessage().contains("parent.level + 1"), ex.getMessage());
        // A non-zero root is likewise rejected.
        assertThrows(IllegalArgumentException.class, () ->
                new RigidNode<>(null, 3, new NodeName("y"), new Title("Y"), "", OptionalInt.empty()));
    }

    // ── the rejected states ───────────────────────────────────────────────────

    private static MalformedTreeException rejected(List<RigidNode<String>> nodes) {
        return assertThrows(MalformedTreeException.class, () -> build(nodes));
    }

    @Test
    void rejectsMultipleRoots() {
        assertTrue(rejected(List.of(root("root", "Root"), root("root2", "Root2")))
                .getMessage().contains("multiple roots"));
    }

    @Test
    void rejectsNoRootWhenTheRootIsOmitted() {
        var r = root("root", "Root");
        var a = child(r, "a", "A", "body");
        // Only the child is provided — its parent (the real root) is absent, so no
        // node in the set has a null parent.
        assertTrue(rejected(List.of(a)).getMessage().contains("no root"));
    }

    @Test
    void rejectsUnreachableWhenAParentIsOutsideTheSet() {
        var r1 = root("root", "Root");
        var a = child(r1, "a", "A", "b1");
        var r2 = root("root2", "Root2");          // a second tree, its root NOT provided
        var b = child(r2, "b", "B", "b2");
        // Provided: r1, a, b. r1 is the sole root; b's parent (r2) is outside the
        // set, so b is unreachable from r1.
        assertTrue(rejected(List.of(r1, a, b)).getMessage().contains("unreachable"));
    }

    @Test
    void rejectsNodeListedTwice() {
        var r = root("root", "Root");
        var a = child(r, "a", "A", "body");
        assertTrue(rejected(List.of(r, a, a)).getMessage().contains("listed twice"));
    }

    @Test
    void rejectsDuplicateSiblingName() {
        var r = root("root", "Root");
        var x = child(r, "same", "First", "b1");
        var y = child(r, "same", "Second", "b2");   // same NodeName under the same parent
        assertTrue(rejected(List.of(r, x, y)).getMessage().contains("duplicate sibling name"));
    }
}
