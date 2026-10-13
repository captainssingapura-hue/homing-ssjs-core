package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A concrete leaf's drawing of a target in a theme, its redirects followed: what it comes to - a body for each state it
 * answers, its colours written in the theme's palettes, or an effect, or nothing - and the concrete leaf that said it.
 * A redirect lands on a concrete leaf of the same leaf, which draws the target; one that goes astray - to another leaf,
 * to one that draws nothing of it, round in a circle - is refused. A drawing taken from a component of the taxonomy is
 * the theme's answer for it, resolved as its own look is; one taken from a virtual leaf, from a component the taxonomy
 * has not, or from one the theme leaves unanswered at rest, is refused. A colour moves along a scale only on a leaf
 * that declares the COLOUR axis.
 */
record ResolveDrawing() implements StatelessFunctionalObject {

    static final ResolveDrawing INSTANCE = new ResolveDrawing();

    /**
     * What a drawing comes to: a body for each state it answers - none for a drawing of nothing - or an effect, whole;
     * who said it, and the component it was taken from, if any.
     *
     * @param bodies the body for each state answered, written in the theme
     * @param effect the animation effect, where it draws one; null otherwise
     * @param by     the concrete leaf that said it
     * @param of     the component it was taken from, or null
     */
    record Resolved(Map<State, String> bodies, AnimationEffect effect, ConcreteComponent<?, ?, ?> by, Component<?> of) {
        Resolved { bodies = Collections.unmodifiableMap(bodies.isEmpty() ? Map.of() : new EnumMap<>(bodies)); }
    }

    Resolved resolve(Taxonomy taxonomy, ConcreteComponent<?, ?, ?> concrete, Target target, Dressed theme) {
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
            for (Drawing<?> d : drawn.drawings(theme)) if (d.target().equals(target)) { found = d; break; }
            if (found == null)
                throw new IllegalArgumentException(name(concrete) + "'s " + target.token() + " is taken from " + name(at) + ", which draws no " + target.token());
            switch (found) {
                case Drawing.As<?> as -> at = as.to();
                case Drawing.Of<?> of -> { return taken(taxonomy, of, theme, name(at), at); }
                case Drawing.Own<?> own -> { return new Resolved(own.answer().bodies(), null, at, null); }
                case Drawing.Painted<?> painted -> { return new Resolved(painted(taxonomy, leaf, painted, theme, name(at)), null, at, null); }
                case Drawing.None<?> none -> { return new Resolved(Map.of(), null, at, null); }
                case Drawing.Animated an -> { return new Resolved(Map.of(), an.effect(), at, null); }
            }
        }
    }

    /**
     * A drawing taken from a component: the theme's answer for it, standalone, at rest and in every state the target
     * offers. Refused when the component is a virtual leaf, one the taxonomy has not, or one the theme leaves
     * unanswered at rest.
     */
    Resolved taken(Taxonomy taxonomy, Drawing.Of<?> of, Dressed theme, String who, ConcreteComponent<?, ?, ?> by) {
        Target target = of.target();
        Component<?> from = of.from();
        String said = who + "'s " + target.token() + " is taken from " + from.name().value();
        if (from instanceof VirtualComponent<?>)
            throw new IllegalArgumentException(said + ", a virtual leaf: the theme answers none of it; take another concrete leaf's with As");
        if (!taxonomy.components().contains(from))
            throw new IllegalArgumentException(said + ", which this taxonomy has not");
        if (target instanceof Target.Animation.Effect) {   // an effect, whole: the one the theme answers the component at rest
            if (ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme, new Casting(from), State.REST, target).outcome() instanceof Resolution.Answered a
                    && a.effect() != null)
                return new Resolved(Map.of(), a.effect(), by, from);
            throw new IllegalArgumentException(said + ", which " + theme + " leaves unanswered at rest");
        }
        var bodies = new EnumMap<State, String>(State.class);
        for (State s : target.states())
            if (ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme, new Casting(from), s, target).outcome() instanceof Resolution.Answered a)
                bodies.put(s, a.body());
        if (!bodies.containsKey(State.REST))
            throw new IllegalArgumentException(said + ", which " + theme + " leaves unanswered at rest");
        return new Resolved(bodies, null, by, from);
    }

    /** A painted drawing's bodies, its colours written in the theme's palettes; along a scale only on a leaf with the COLOUR axis. */
    private static Map<State, String> painted(Taxonomy taxonomy, VirtualComponent<?> leaf, Drawing.Painted<?> painted, Dressed theme, String who) {
        var bodies = new EnumMap<State, String>(State.class);
        painted.answer().paints().forEach((state, paint) -> {
            if (paint instanceof Paint.Along<?, ?> a && !taxonomy.extents(leaf).contains(ExtentAxis.COLOUR))
                throw new IllegalArgumentException(who + "'s " + painted.target().token() + " moves along " + a.scale().token()
                        + ", and " + leaf.token() + " declares no COLOUR axis: only a leaf that varies its colour by degree moves along a scale");
            bodies.put(state, RenderColor.INSTANCE.body(theme, painted.target(), paint));
        });
        return bodies;
    }

    /** {@code ClassicCursorBorder (minesweeper-classic)}. */
    static String name(ConcreteComponent<?, ?, ?> c) {
        String design;
        try { design = c.design().token(); } catch (RuntimeException e) { design = "?"; }
        return c.getClass().getSimpleName() + " (" + design + ")";
    }

    private static String names(List<ConcreteComponent<?, ?, ?>> path) {
        return String.join(" -> ", path.stream().map(ResolveDrawing::name).toList());
    }
}
