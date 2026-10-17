package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Part;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The walk from a node, a state and a target to the one semantic class an element wears for them
 * in a theme (RFC 0067, D6 and appendix Castings). The tree is the taxonomy's and the same in every
 * theme; only the answers differ, so a theme that answers more precisely is found by the same walk,
 * and a theme switch walks it again.
 *
 * <ul>
 *   <li><b>The path.</b> At rest, a look: the casting's lineage, most precise first - along its
 *       role's lineage at its component, then the same at each of its component's parents, ending at
 *       the root on its own; a functional role is set aside. In any other state, a treatment, common
 *       to every role: the role is set aside and the path goes up the component's lineage alone.</li>
 *   <li><b>The layers.</b> Every target is asked of the style, colour's included. The worn style
 *       is asked first, all the way up the path; only what it leaves unanswered there is its base's,
 *       asked the same way, and so on down the chain. A style's coarse answer outweighs its base's
 *       precise one.</li>
 *   <li><b>The answers.</b> Its own CSS answers the states it lists, and a painted colour the states
 *       it paints - written in the colours the theme's palettes give, and along a scale only for a
 *       casting whose node declares the COLOUR axis; the walk goes on for the other states. A
 *       {@link Answer.Redirect redirect} is resolved again from its node, in the theme
 *       worn - late, so it follows what the worn style says of that node. {@link Answer.FromBase}
 *       asks the base the rest of the path, from the node it is said at, in the states it names;
 *       in the others the walk goes on up the style.</li>
 *   <li><b>Nothing.</b> A look nothing answers, the roots included, is a theme incomplete; a state
 *       nothing answers is no treatment, and the element shows its look at rest.</li>
 * </ul>
 *
 * A redirect that comes back to a node it has passed through is refused.
 */
public record ResolveSemanticClass() implements StatelessFunctionalObject {

    public static final ResolveSemanticClass INSTANCE = new ResolveSemanticClass();

    public Resolution resolve(Taxonomy taxonomy, Dressed theme, Casting query, State state, Target target) {
        var notes = new ArrayList<String>();
        if (state == State.REST && query.functional())
            notes.add(query.role().name().value() + " is a functional role: the part is cast for its look as Standalone");
        if (state != State.REST && !query.standalone())
            notes.add("a state is a treatment, common to every role: " + query.role().name().value() + " is set aside");
        if (!castable(taxonomy, query))
            notes.add("no slot of the taxonomy casts " + query.component().name().value() + " as " + query.role().name().value()
                    + ": no owner can cast it so");
        Layer top = theme.layer();
        var steps = new ArrayList<Resolution.Step>();
        var passed = new LinkedHashSet<Casting>();
        passed.add(query);
        Resolution.Outcome outcome = walk(taxonomy, theme, query, top, top, path(taxonomy, query, state), state, target, steps, passed);
        return new Resolution(theme.toString(), query, state, target, notes, steps, outcome);
    }

    /** The nodes asked for a casting in a state, most precise first. */
    private static List<Casting> path(Taxonomy taxonomy, Casting c, State state) {
        if (state == State.REST) return c.lineage(taxonomy);
        var walk = new ArrayList<Casting>();
        for (ComponentNode n : taxonomy.fallback(c.component())) walk.add(new Casting((Taxon) n));
        return walk;
    }

    private static Resolution.Outcome walk(Taxonomy taxonomy, Dressed theme, Casting query, Layer top, Layer layer, List<Casting> path, State state, Target target,
                                           List<Resolution.Step> steps, Set<Casting> passed) {
        var tokens = SemanticTokens.INSTANCE;
        for (int i = 0; i < path.size(); i++) {
            Casting node = path.get(i);
            switch (layer.answer(node, target)) {
                case null -> steps.add(new Resolution.Step(layer.token(), node, "-"));
                case Answer.Own<?> own -> {
                    String body = own.bodies().get(state);
                    if (body == null) {
                        steps.add(new Resolution.Step(layer.token(), node, "answers it, not " + tokens.state(state)));
                        continue;
                    }
                    steps.add(new Resolution.Step(layer.token(), node, "answered"));
                    return new Resolution.Answered(layer.token(), node, tokens.className(node, state, target), body);
                }
                case Answer.Painted<?, ?> painted -> {
                    Paint<?, ?> paint = painted.paints().get(state);
                    if (paint == null) {
                        steps.add(new Resolution.Step(layer.token(), node, "answers it, not " + tokens.state(state)));
                        continue;
                    }
                    if (paint instanceof Paint.Along<?, ?> a && !taxonomy.extents(query.component()).contains(ExtentAxis.COLOUR))
                        throw new IllegalStateException(tokens.casting(query) + " " + target.token() + " is answered along " + a.scale().token()
                                + " at " + tokens.casting(node) + " by " + layer.token() + ", and " + query.component().name().value()
                                + " declares no COLOUR axis: only a casting that varies its colour by degree moves along a scale");
                    steps.add(new Resolution.Step(layer.token(), node, "answered"));
                    return new Resolution.Answered(layer.token(), node, tokens.className(node, state, target),
                            RenderColor.INSTANCE.body(theme, (Target.Color) target, paint));
                }
                case Answer.Redirect<?> r -> {
                    steps.add(new Resolution.Step(layer.token(), node, "redirected to " + tokens.casting(r.to())));
                    if (!passed.add(r.to()))
                        throw new IllegalStateException("a redirect comes back to " + tokens.casting(r.to()) + " on " + target.token() + ": "
                                + String.join(" > ", passed.stream().map(tokens::casting).toList()) + " > " + tokens.casting(r.to()));
                    return walk(taxonomy, theme, query, top, top, path(taxonomy, state == State.REST ? r.to() : r.to().alone(), state), state, target, steps, passed);
                }
                case Answer.Animated a -> {
                    if (state != State.REST) {
                        steps.add(new Resolution.Step(layer.token(), node, "answers it, not " + tokens.state(state)));
                        continue;
                    }
                    steps.add(new Resolution.Step(layer.token(), node, "answered: " + a.effect().token()));
                    return new Resolution.Answered(layer.token(), node, tokens.className(node, state, target),
                            Effects.INSTANCE.moving(a.effect()), a.effect());
                }
                case Answer.FromBase<?> f -> {
                    if (!f.asks(state)) {
                        steps.add(new Resolution.Step(layer.token(), node, "its base's, not " + tokens.state(state)));
                        continue;
                    }
                    steps.add(new Resolution.Step(layer.token(), node, "its base's, from here"));
                    return walk(taxonomy, theme, query, top, layer.base(), path.subList(i, path.size()), state, target, steps, passed);
                }
            }
        }
        if (layer.base() != null) return walk(taxonomy, theme, query, top, layer.base(), path, state, target, steps, passed);
        return state == State.REST ? new Resolution.Unanswered() : new Resolution.Silent();
    }

    /** Whether a casting is one the taxonomy has: on its own, or a role some slot casts its component in. */
    private static boolean castable(Taxonomy taxonomy, Casting c) {
        if (c.standalone()) return true;
        for (Part<?, ?> p : taxonomy.parts()) if (p.bases().contains(c.component()) && p.role().equals(c.role())) return true;
        return false;
    }
}
