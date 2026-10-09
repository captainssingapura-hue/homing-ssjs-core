package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Part;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The walk from a node, a state and a target to the one semantic class an element wears for them
 * under a theme (RFC 0067, appendix Castings). The tree is the taxonomy's and the same under every
 * theme; only the answers differ, so a theme that answers more precisely - a corner of the danger
 * button's own - is found by the same walk, and a theme switch walks it again.
 *
 * <ul>
 *   <li><b>At rest</b> - a look: the casting's lineage, most precise first - along its role's
 *       lineage at its component, then the same at each of its component's parents, ending at the
 *       root on its own. A functional role is set aside: such a part looks as its component on its
 *       own does. Nothing answering, the root included, is a theme incomplete.</li>
 *   <li><b>In any other state</b> - a treatment, common to every role: the role is set aside and the
 *       walk goes up the component's lineage alone, ending at the root. Nothing answering is no
 *       treatment: the element shows its look at rest.</li>
 * </ul>
 *
 * <p>The first node the theme answers is the answer, and its class is atomic: one class for the
 * node, the state and the target.</p>
 */
public record ResolveSemanticClass() implements StatelessFunctionalObject {

    public static final ResolveSemanticClass INSTANCE = new ResolveSemanticClass();

    public Resolution resolve(Taxonomy taxonomy, Answers answers, Casting query, State state, Target target) {
        var notes = new ArrayList<String>();
        List<Casting> walk;
        if (state == State.REST) {
            if (query.functional()) notes.add(query.role().name().value() + " is a functional role: the part is cast for its look as Standalone");
            walk = query.lineage(taxonomy);
        } else {
            if (!query.standalone()) notes.add("a state is a treatment, common to every role: " + query.role().name().value() + " is set aside");
            walk = new ArrayList<>();
            for (ComponentNode c : taxonomy.fallback(query.component())) walk.add(new Casting((Taxon) c));
        }
        if (!castable(taxonomy, query))
            notes.add("no slot of the taxonomy casts " + query.component().name().value() + " as " + query.role().name().value()
                    + ": no owner can cast it so");
        var steps = new ArrayList<Resolution.Step>();
        for (Casting node : walk) {
            boolean answered = answers.answers(node, state, target);
            steps.add(new Resolution.Step(node, answered));
            if (answered)
                return new Resolution(query, state, target, notes, steps,
                        new Resolution.Answered(node, SemanticTokens.INSTANCE.className(node, state, target)));
        }
        return new Resolution(query, state, target, notes, steps,
                state == State.REST ? new Resolution.Unanswered() : new Resolution.Silent());
    }

    /** Whether a casting is one the taxonomy has: on its own, or a role some slot casts its component in. */
    private static boolean castable(Taxonomy taxonomy, Casting c) {
        if (c.standalone()) return true;
        for (Part<?, ?> p : taxonomy.parts()) if (p.base().equals(c.component()) && p.role().equals(c.role())) return true;
        return false;
    }
}
