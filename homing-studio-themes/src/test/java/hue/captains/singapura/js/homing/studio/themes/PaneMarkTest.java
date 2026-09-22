package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.Box.Container;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target.Shape;
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
 * Where the keys are, on a pane. A dock draws no frame of its own — what
 * holds it does, or nothing does — so its rule says one thing only: the mark
 * at {@code HELD}, {@code CANDIDATE} and {@code LENT}. A float is its own
 * frame and keeps the container's rule under the same mark.
 *
 * <p>Pinned because the mark was silent for a while: the pane wore the mark's
 * colour without its geometry, so every design had an answer and nothing was
 * ever drawn.</p>
 */
class PaneMarkTest {

    private static final DesignClass<?> DOCK  = of(Container.Pane.class, Shape.Rule.class);
    private static final DesignClass<?> FLOAT = of(Container.Pane.Floating.class, Shape.Rule.class);

    private static final List<Design> BASES = List.of(
            HomingEditorial.INSTANCE, HomingNeoBrutalism.INSTANCE, HomingNeoFuturism.INSTANCE, HomingNeumorphism.INSTANCE,
            HomingGlassmorphism.INSTANCE, HomingRetroFuturism.INSTANCE, HomingSketchy.INSTANCE);

    private static Impl.Bindings rule(Design d, DesignClass<?> pair) {
        var r = Deployment.of(Set.of(pair), d).resolve();
        assertEquals(List.of(), r.findings(), () -> d.slug() + " on " + pair + ": " + r.findings());
        var impl = r.impls().get(pair);
        assertTrue(impl instanceof Impl.Bindings, () -> d.slug() + " answers " + pair + " with " + impl);
        return (Impl.Bindings) impl;
    }

    private static Map<String, String> at(Impl.Bindings b, State state) { return b.values().get(Mode.LIGHT).get(state); }

    /** Every theme: a dock has no rule at rest, and a mark at each of the three states the keys have. */
    @Test
    void aDockDrawsNoFrame_andMarksWhereTheKeysAre() {
        for (Theme t : StudioThemeRegistry.INSTANCE.themes()) {
            Design d = (Design) t;
            var b = rule(d, DOCK);
            assertEquals("0", at(b, State.REST).get("border-width"), d.slug() + ": a dock draws a frame of its own");
            assertEquals("none", at(b, State.REST).get("border-style"), d.slug() + ": a dock draws a frame of its own");
            for (State s : List.of(State.HELD, State.CANDIDATE, State.LENT)) {
                assertNotNull(at(b, s), d.slug() + " draws nothing for " + s);
                assertNotNull(at(b, s).get("outline-width"), d.slug() + " gives " + s + " no geometry: the mark cannot draw");
                assertNotNull(at(b, s).get("outline-style"), d.slug() + " gives " + s + " no style");
            }
        }
    }

    /** A float keeps the frame it had — the container's rule, in each design's own weight — and marks the same way. */
    @Test
    void aFloatKeepsItsFrame_andMarksTheSameWay() {
        for (Design d : BASES) {
            var f = rule(d, FLOAT);
            var c = rule(d, of(Container.class, Shape.Rule.class));
            assertEquals(at(c, State.REST).get("border-width"), at(f, State.REST).get("border-width"), d.slug() + ": a float lost the container's rule");
            assertEquals(at(c, State.REST).get("border-style"), at(f, State.REST).get("border-style"), d.slug() + ": a float lost the container's rule");
            assertEquals(at(c, State.HELD).get("outline-width"), at(f, State.HELD).get("outline-width"), d.slug() + ": a float marks differently from a region");
        }
    }

    /** The marks are each design's own: a region's weight, in its own material. */
    @Test
    void theMarkIsEachDesignsOwn() {
        assertEquals("2px", at(rule(HomingEditorial.INSTANCE, DOCK), State.HELD).get("outline-width"));
        assertEquals("6px", at(rule(HomingNeoBrutalism.INSTANCE, DOCK), State.HELD).get("outline-width"), "a region shouts");
        assertEquals("-4px", at(rule(HomingNeumorphism.INSTANCE, DOCK), State.HELD).get("outline-offset"), "pressed in, not stood off");
        assertEquals("dashed", at(rule(HomingSketchy.INSTANCE, DOCK), State.CANDIDATE).get("outline-style"), "the marker's dashed maybe");
    }
}
