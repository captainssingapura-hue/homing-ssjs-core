package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.Box.Container;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target.Shape;
import hue.captains.singapura.js.homing.design.Target.Size;
import hue.captains.singapura.js.homing.core.Theme;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The panel, in seven materials. A panel is the workspace's unit of work —
 * a head, a body, and whatever holds them — so the word is answered here
 * before a component wears it: every design says the air of its head, the
 * air of its body, and the line between them, and what it says is its own.
 * A panel standing alone takes the container's edge, the keys mark included.
 */
class PanelDesignTest {

    /** What a panel asks of a design. */
    private static final Set<DesignClass<?>> PANEL = Set.of(
            of(Container.Panel.class, Size.Gap.class),
            of(Container.Panel.Head.class, Size.Inset.class),
            of(Container.Panel.Head.class, Size.Gap.class),
            of(Container.Panel.Head.class, Shape.Rule.class),
            of(Container.Panel.Body.class, Size.Inset.class),
            of(Container.Panel.Body.class, Size.Gap.class));

    private static Map<String, String> rest(Design d, DesignClass<?> pair) {
        var r = Deployment.of(Set.of(pair), d).resolve();
        assertEquals(List.of(), r.findings(), () -> d.slug() + " on " + pair + ": " + r.findings());
        var impl = r.impls().get(pair);
        assertTrue(impl instanceof Impl.Bindings, () -> d.slug() + " answers " + pair + " with " + impl);
        return ((Impl.Bindings) impl).values().get(Mode.LIGHT).get(State.REST);
    }

    /** The line under the head, as {@code width/style}. */
    private static String line(Design d) {
        var v = rest(d, of(Container.Panel.Head.class, Shape.Rule.class));
        return v.get("border-width") + "/" + v.get("border-style");
    }

    /** The air a design gives a part, as {@code block/inline}. */
    private static String air(Design d, DesignClass<?> pair) {
        var v = rest(d, pair);
        return v.get("padding-block") + "/" + v.get("padding-inline");
    }

    /** Every design, every theme over it: the panel's pairs resolve, and none is left to a parent that cannot answer. */
    @Test
    void everyDesignAnswersThePanel_validly() {
        for (Theme t : StudioThemeRegistry.INSTANCE.themes()) {
            Design d = (Design) t;
            var r = Deployment.of(PANEL, d).resolve();
            assertEquals(List.of(), r.findings(), () -> d.slug() + ": " + r.findings());
            assertEquals(PANEL.size(), r.impls().size(), d.slug() + " resolved fewer of the panel's pairs than it was asked");
        }
    }

    /** The line under the head is where each design says what a panel is. */
    @Test
    void theLineUnderTheHead_isEachDesignsOwn() {
        assertEquals("0 0 1px 0/solid", line(HomingEditorial.INSTANCE), "a running head, ruled off");
        assertEquals("0 0 3px 0/solid", line(HomingNeoBrutalism.INSTANCE), "a bar, not a line");
        assertEquals("0/none", line(HomingNeumorphism.INSTANCE), "no line: the air is the division");
        assertEquals("0 0 3px 0/double", line(HomingSketchy.INSTANCE), "gone over twice");
        assertEquals("0 0 1px 0/solid", line(HomingGlassmorphism.INSTANCE), "a hairline of light");
        assertEquals("0 0 1px 0/solid", line(HomingRetroFuturism.INSTANCE), "the console's rule, lit by the palette");
        assertEquals("0 0 1px 0/solid", line(HomingNeoFuturism.INSTANCE), "a hairline, held close");
    }

    /** A panel is a work surface: its body has less air than a card, and each design says how much. */
    @Test
    void theAirInside_isDenserThanACards_andEachDesignsOwn() {
        assertEquals("14px/16px", air(HomingEditorial.INSTANCE, of(Container.Panel.Body.class, Size.Inset.class)));
        assertEquals("8px/14px", air(HomingEditorial.INSTANCE, of(Container.Panel.Head.class, Size.Inset.class)));
        assertEquals("16px/20px", air(HomingNeumorphism.INSTANCE, of(Container.Panel.Body.class, Size.Inset.class)), "relief needs room");
        assertEquals("10px/14px", air(HomingNeoFuturism.INSTANCE, of(Container.Panel.Body.class, Size.Inset.class)), "no more air than the work needs");
        for (Design d : List.of(HomingEditorial.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE,
                                HomingNeumorphism.INSTANCE, HomingGlassmorphism.INSTANCE, HomingRetroFuturism.INSTANCE, HomingSketchy.INSTANCE)) {
            int body = px(air(d, of(Container.Panel.Body.class, Size.Inset.class)));
            int card = px(air(d, of(Container.Card.class, Size.Inset.class)));
            assertTrue(body < card, d.slug() + ": a panel's body (" + body + ") is not denser than its card (" + card + ")");
        }
    }

    /** Where a design draws no line, it leaves air instead: the head and the body read as two plates. */
    @Test
    void theDesignThatDrawsNoLine_partsThemWithAir() {
        assertEquals("10px", rest(HomingNeumorphism.INSTANCE, of(Container.Panel.class, Size.Gap.class)).get(Impl.Bindings.SOLE));
        assertEquals("0px", rest(HomingEditorial.INSTANCE, of(Container.Panel.class, Size.Gap.class)).get(Impl.Bindings.SOLE), "the line does the parting - and a length, since a bare 0 in a calc is a number, not a gap");
    }

    /**
     * A panel that stands alone: its corner and its edge are the container's,
     * by lineage — and the container's rule carries the keys mark, so a panel
     * that holds the keys says so without a word of its own.
     */
    @Test
    void aPanelStandingAlone_wearsTheContainersEdge_andTheKeysMark() {
        var pair = of(Container.Panel.class, Shape.Rule.class);
        for (Design d : List.of(HomingEditorial.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingSketchy.INSTANCE)) {
            var r = Deployment.of(Set.of(pair, of(Container.Panel.class, Shape.Corner.class)), d).resolve();
            assertEquals(List.of(), r.findings(), () -> d.slug() + ": " + r.findings());
            var impl = (Impl.Bindings) r.impls().get(pair);
            assertNotNull(impl.values().get(Mode.LIGHT).get(State.HELD), d.slug() + " draws no mark on a panel that holds the keys");
            assertNotNull(impl.values().get(Mode.LIGHT).get(State.CANDIDATE), d.slug() + " draws no mark on a panel the walk rests on");
        }
    }

    private static int px(String blockOverInline) {
        String block = blockOverInline.substring(0, blockOverInline.indexOf('/'));
        return Integer.parseInt(block.replace("px", "").trim());
    }
}
