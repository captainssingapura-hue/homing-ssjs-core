package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A concrete leaf's drawing of a target in a style, its redirects followed: what it comes to - a body for each state it
 * answers, its colours as the style writes them (a palettable one its palette's variable), or an effect, or nothing -
 * and the concrete leaf that said it. A redirect lands on a concrete leaf of the same leaf, which draws the target; one
 * that goes astray - to another leaf, to one that draws nothing of it, round in a circle - is refused. A colour moves
 * along a scale only on a leaf that declares the COLOUR axis.
 */
record ResolveDrawing() implements StatelessFunctionalObject {

    static final ResolveDrawing INSTANCE = new ResolveDrawing();

    /**
     * What a drawing comes to: a body for each state it answers - none for a drawing of nothing - or an effect, whole;
     * and who said it.
     *
     * @param bodies the body for each state answered, written in the style
     * @param effect the animation effect, where it draws one; null otherwise
     * @param by     the concrete leaf that said it
     */
    record Resolved(Map<State, String> bodies, AnimationEffect effect, ConcreteComponent<?, ?, ?> by) {
        Resolved { bodies = Collections.unmodifiableMap(bodies.isEmpty() ? Map.of() : new EnumMap<>(bodies)); }
    }

    Resolved resolve(Taxonomy taxonomy, ConcreteComponent<?, ?, ?> concrete, Target target, InStyle style) {
        VirtualComponent<?> leaf = concrete.virtual();
        var path = new ArrayList<ConcreteComponent<?, ?, ?>>();
        ConcreteComponent<?, ?, ?> at = concrete;
        while (true) {
            if (path.contains(at))
                throw new IllegalArgumentException(target.token() + " goes round in a circle: " + names(path) + " -> " + name(at));
            path.add(at);
            if (!at.virtual().equals(leaf))
                throw new IllegalArgumentException(name(concrete) + "'s " + target.token() + " is taken from " + name(at)
                        + ", a concrete leaf of " + at.virtual().token() + ", not of " + leaf.token());
            if (!(at instanceof Drawn drawn))
                throw new IllegalArgumentException(name(concrete) + "'s " + target.token() + " is taken from " + name(at) + ", which says nothing of how it is drawn");
            Drawing<?> found = null;
            for (Drawing<?> d : drawn.drawings(style.style())) if (d.target().equals(target)) { found = d; break; }
            if (found == null)
                throw new IllegalArgumentException(name(concrete) + "'s " + target.token() + " is taken from " + name(at) + ", which draws no " + target.token());
            switch (found) {
                case Drawing.As<?> as -> at = as.to();
                case Drawing.Own<?> own -> { return new Resolved(own.answer().bodies(), null, at); }
                case Drawing.Painted<?> painted -> { return new Resolved(painted(taxonomy, leaf, painted, style, name(at)), null, at); }
                case Drawing.None<?> none -> { return new Resolved(Map.of(), null, at); }
                case Drawing.Animated an -> { return new Resolved(Map.of(), an.effect(), at); }
            }
        }
    }

    /** A painted drawing's bodies, its colours as the style writes them; along a scale only on a leaf with the COLOUR axis. */
    private static Map<State, String> painted(Taxonomy taxonomy, VirtualComponent<?> leaf, Drawing.Painted<?> painted, InStyle style, String who) {
        var bodies = new EnumMap<State, String>(State.class);
        painted.answer().paints().forEach((state, paint) -> {
            if (paint instanceof Paint.Along<?, ?> a && !taxonomy.extents(leaf).contains(ExtentAxis.COLOUR))
                throw new IllegalArgumentException(who + "'s " + painted.target().token() + " moves along " + a.scale().token()
                        + ", and " + leaf.token() + " declares no COLOUR axis: only a leaf that varies its colour by degree moves along a scale");
            bodies.put(state, RenderColor.INSTANCE.body(style, painted.target(), paint));
        });
        return bodies;
    }

    /** {@code ClassicCursorBorder (minesweeper-classic)}. */
    static String name(ConcreteComponent<?, ?, ?> c) {
        String localStyle;
        try { localStyle = c.localStyle().token(); } catch (RuntimeException e) { localStyle = "?"; }
        return c.getClass().getSimpleName() + " (" + localStyle + ")";
    }

    private static String names(List<ConcreteComponent<?, ?, ?>> path) {
        return String.join(" -> ", path.stream().map(ResolveDrawing::name).toList());
    }
}
