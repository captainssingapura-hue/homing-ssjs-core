package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete leaf's drawing of a target in a theme, its redirects followed: its own CSS or nothing, and
 * the concrete leaf that said it. A redirect lands on a concrete leaf of the same leaf, which draws the
 * target; one that goes astray - to another leaf, to one that draws nothing of it, round in a circle -
 * is refused.
 */
record ResolveDrawing() implements StatelessFunctionalObject {

    static final ResolveDrawing INSTANCE = new ResolveDrawing();

    /** What a drawing comes to: its own CSS or nothing, and who said it. */
    record Resolved(Drawing<?> drawing, ConcreteComponent<?, ?, ?> by) {}

    Resolved resolve(ConcreteComponent<?, ?, ?> concrete, Target target, Dressed theme) {
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
            return new Resolved(found, at);
        }
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
