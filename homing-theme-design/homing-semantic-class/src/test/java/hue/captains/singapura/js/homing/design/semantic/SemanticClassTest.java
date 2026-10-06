package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Kind;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Trees;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The semantic class: a target leaf × a node of the taxonomy, a value nobody declares. The forest
 * is their product; a class falls back on its own target along its component's chain, a part
 * through its base and never its owner; its token is the node's and the target's.
 */
class SemanticClassTest {

    record Control() implements Kind<Root> {
        static final Control INSTANCE = new Control();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Text() implements Kind<Root> {
        static final Text INSTANCE = new Text();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record PlainButton() implements Component<Control> {
        static final PlainButton INSTANCE = new PlainButton();
        @Override public Control parent() { return Control.INSTANCE; }
    }

    record Caption() implements Component<Text> {
        static final Caption INSTANCE = new Caption();
        @Override public Text parent() { return Text.INSTANCE; }
    }

    record Dialog() implements Component<Root> {
        static final Dialog INSTANCE = new Dialog();
        @Override public Root parent() { return Root.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Ok.INSTANCE, Title.INSTANCE); }

        record Ok() implements Role<PlainButton> {
            static final Ok INSTANCE = new Ok();
            @Override public PlainButton base() { return PlainButton.INSTANCE; }
        }

        record Title() implements Role<Caption> {
            static final Title INSTANCE = new Title();
            @Override public Caption base() { return Caption.INSTANCE; }
        }
    }

    private static final Taxonomy TAXONOMY = new ReadTaxonomy().read(List.of(Dialog.INSTANCE));
    private static final ComponentNode TITLE = TAXONOMY.partsOf(Dialog.INSTANCE).get(1);

    @Test
    void theForestIsTheProduct_ofEveryTargetLeafAndEveryNode() {
        var forest = new DeriveSemanticClasses().derive(TAXONOMY);
        assertEquals(Trees.targetLeaves().size() * TAXONOMY.nodes().size(), forest.size());
        assertEquals(31, Trees.targetLeaves().size(), "the target leaves today");
        assertEquals(List.of("root", "control", "text", "dialog", "plain-button", "caption", "dialog-ok", "dialog-title"),
                TAXONOMY.nodes().stream().map(ComponentNode::token).toList());
    }

    @Test
    void aClassIsAValue_itsTokenTheNodesAndTheTargets() {
        var title = new SemanticClass<>(Target.Color.Ink.INSTANCE, TITLE);
        assertEquals("dialog-title-color-ink", title.token());
        assertEquals(new SemanticClass<>(Target.Color.Ink.INSTANCE, Caption.INSTANCE), new SemanticClass<>(Target.Color.Ink.INSTANCE, Caption.INSTANCE));
        assertEquals("root-shape-corner", new SemanticClass<>(Target.Shape.Corner.INSTANCE, Root.INSTANCE).token());
    }

    @Test
    void aClassFallsBackOnItsOwnTarget_aPartThroughItsBase() {
        var chain = new SemanticClass<>(Target.Color.Ink.INSTANCE, TITLE).fallback(TAXONOMY);
        assertEquals(List.of("dialog-title-color-ink", "caption-color-ink", "text-color-ink", "root-color-ink"),
                chain.stream().map(SemanticClass::token).toList());
        assertTrue(chain.stream().noneMatch(c -> c.component().equals(Dialog.INSTANCE)), "never through the dialog that owns the title");
        assertTrue(chain.stream().allMatch(c -> c.target().equals(Target.Color.Ink.INSTANCE)), "on its own target throughout");
    }
}
