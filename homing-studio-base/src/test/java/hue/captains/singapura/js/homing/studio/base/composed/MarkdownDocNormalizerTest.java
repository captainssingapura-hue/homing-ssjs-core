package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.MarkdownDoc;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkdownDocNormalizerTest {

    private static final UUID ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static String label(NormalizedNode n) {
        return n.dimensions().get(DisplayLabel.INSTANCE).displayText();
    }

    private static String bodyAt(DocTree tree, List<Integer> path) {
        var bundle = assertInstanceOf(ComposedLeaf.class, tree.providerAt(path).orElseThrow().content());
        return assertInstanceOf(MarkdownSegment.class, bundle.contents().get(0)).body();
    }

    @Test
    void noHeadingsStaysOneFlatNode() {
        var doc  = new MarkdownDoc(ID, "Notes", "just some prose\n\nmore prose");
        var tree = MarkdownDocNormalizer.INSTANCE.toDocTree(doc);

        NormalizedNode root = tree.structure();
        assertEquals(TreeLevel.L0.INSTANCE, root.level());
        assertTrue(root.children().isEmpty(), "no headings -> single flat node");
        assertEquals("just some prose\n\nmore prose", bodyAt(tree, List.of()));
    }

    @Test
    void headingsBecomeTheTocTree() {
        var doc = new MarkdownDoc(ID, "Ontology", """
                # Ontology
                lead-in prose

                ## Definition
                what it is

                ## Identity
                how it's named
                ### Sub
                deeper note
                """);
        var tree = MarkdownDocNormalizer.INSTANCE.toDocTree(doc);
        NormalizedNode root = tree.structure();

        // The leading "# Ontology" matched the title -> absorbed into root (no dupe).
        assertEquals("Ontology", label(root));
        assertEquals(2, root.children().size(), "two H2 sections under the root");
        assertEquals("lead-in prose", bodyAt(tree, List.of()));

        NormalizedNode definition = root.children().get(0);
        assertEquals("Definition", label(definition));
        assertEquals(TreeLevel.L1.INSTANCE, definition.level());
        assertEquals("what it is", bodyAt(tree, List.of(0)));

        NormalizedNode identity = root.children().get(1);
        assertEquals("Identity", label(identity));
        assertEquals(1, identity.children().size(), "the ### nests under its ##");
        assertEquals("Sub", label(identity.children().get(0)));
        assertEquals(TreeLevel.L2.INSTANCE, identity.children().get(0).level());
        assertEquals("deeper note", bodyAt(tree, List.of(1, 0)));
    }
}
