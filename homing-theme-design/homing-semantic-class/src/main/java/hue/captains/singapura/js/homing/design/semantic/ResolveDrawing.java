package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A concrete leaf's drawing of a target in a theme, its redirects followed: its own CSS or nothing, and
 * the concrete leaf that said it. A redirect lands on a concrete leaf of the same leaf, which draws the
 * target; one that goes astray - to another leaf, to one that draws nothing of it, round in a circle -
 * is refused. A drawing taken from a component of the taxonomy is the theme's answer for it, resolved
 * as its own look is; one taken from a virtual leaf, from a component the taxonomy has not, or from one
 * the theme leaves unanswered at rest, is refused.
 */
record ResolveDrawing() implements StatelessFunctionalObject {

    static final ResolveDrawing INSTANCE = new ResolveDrawing();

    /** What a drawing comes to: its own CSS or nothing, who said it, and the component it was taken from, if any. */
    record Resolved(Drawing<?> drawing, ConcreteComponent<?, ?, ?> by, Component<?> of) {}

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
            if (found instanceof Drawing.As<?> as) { at = as.to(); continue; }
            if (found instanceof Drawing.Of<?> of) return new Resolved(taken(taxonomy, of, theme, name(at)), at, of.from());
            return new Resolved(found, at, null);
        }
    }

    /**
     * A drawing taken from a component: the theme's answer for it, standalone, at rest and in every state the
     * target offers. Refused when the component is a virtual leaf, one the taxonomy has not, or one the theme
     * leaves unanswered at rest.
     */
    Drawing<?> taken(Taxonomy taxonomy, Drawing.Of<?> of, Dressed theme, String who) {
        return own(taxonomy, of.target(), of.from(), theme, who);
    }

    private static <T extends Target> Drawing<T> own(Taxonomy taxonomy, T target, Component<?> from, Dressed theme, String who) {
        String said = who + "'s " + target.token() + " is taken from " + from.name().value();
        if (from instanceof VirtualComponent<?>)
            throw new IllegalArgumentException(said + ", a virtual leaf: the theme answers none of it; take another concrete leaf's with As");
        if (!taxonomy.components().contains(from))
            throw new IllegalArgumentException(said + ", which this taxonomy has not");
        var bodies = new EnumMap<State, String>(State.class);
        for (State s : target.states())
            if (ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme, new Casting(from), s, target).outcome() instanceof Resolution.Answered a)
                bodies.put(s, a.body());
        if (!bodies.containsKey(State.REST))
            throw new IllegalArgumentException(said + ", which " + theme.style().token() + " in " + theme.palette().token() + " leaves unanswered at rest");
        return new Drawing.Own<>(target, new Answer.Own<T>(bodies));
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
