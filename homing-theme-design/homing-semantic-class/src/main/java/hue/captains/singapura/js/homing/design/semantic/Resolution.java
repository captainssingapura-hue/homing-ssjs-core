package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a walk found for a node in a state on a target, and how: the nodes it asked, layer by layer,
 * most precise first, up to the one that answered; the notes on why it walked as it did; and the
 * outcome - the one class the element wears for it, and what it says, or none.
 *
 * @param theme   the theme walked: {@code sketchy in harbour}
 * @param query   the node asked for
 * @param state   the state
 * @param target  the target
 * @param notes   why the walk is the one it is: a functional role set aside, a role set aside for a state
 * @param steps   every node asked, in order, the last the one that answered when one did
 * @param outcome the class, or why there is none
 */
public record Resolution(String theme, Casting query, State state, Target target, List<String> notes, List<Step> steps, Outcome outcome)
        implements ValueObject {

    public Resolution {
        notes = List.copyOf(notes);
        steps = List.copyOf(steps);
    }

    /** A node asked of a layer, and what the layer said: {@code -}, {@code answered}, a redirect, its base's. */
    public record Step(String layer, Casting node, String said) implements ValueObject {}

    /** The one class an element wears for it, or why there is none. */
    public sealed interface Outcome extends ValueObject permits Answered, Silent, Unanswered {}

    /**
     * The layer and the node that answered, the class it is worn as, and its body.
     *
     * @param layer     the style or palette that answered
     * @param node      the node it answered at
     * @param className the answering class: the node's, the state's, the target's
     * @param body      what it says; empty for an answer that says nothing
     * @param effect    the animation effect it is, where it answered one; null otherwise
     */
    public record Answered(String layer, Casting node, String className, String body, AnimationEffect effect) implements Outcome {
        /** An answer of CSS: no effect. */
        public Answered(String layer, Casting node, String className, String body) { this(layer, node, className, body, null); }
    }

    /** A state no component answers: nothing is worn for it, and the element shows its look at rest. */
    public record Silent() implements Outcome {}

    /** A look at rest that nothing answers, the roots included: the theme is incomplete. */
    public record Unanswered() implements Outcome {}

    /** The class worn, or null when there is none. */
    public String className() { return outcome instanceof Answered a ? a.className() : null; }

    /** The walk told, a line at a time. */
    public List<String> explain() {
        var tokens = SemanticTokens.INSTANCE;
        var out = new ArrayList<String>();
        out.add("query    " + tokens.casting(query) + "  " + tokens.state(state) + "  " + tokens.target(target));
        out.add("theme    " + theme);
        for (String n : notes) out.add("note     " + n);
        int layers = 0, nodes = 0;
        for (Step s : steps) {
            layers = Math.max(layers, s.layer().length());
            nodes = Math.max(nodes, tokens.casting(s.node()).length());
        }
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            String node = tokens.casting(s.node());
            out.add((i == 0 ? "walk     " : "         ") + String.format("%2d. ", i + 1) + s.layer() + " ".repeat(layers - s.layer().length() + 2)
                    + node + " ".repeat(nodes - node.length() + 2) + s.said());
        }
        out.add(switch (outcome) {
            case Answered a -> "result   " + a.className() + " from " + a.layer();
            case Silent s -> "result   (none) - nothing answers " + tokens.target(target) + " when " + tokens.state(state)
                    + ": nothing is worn for it, and the element shows its look at rest";
            case Unanswered u -> "result   UNANSWERED - " + theme + " answers " + tokens.target(target) + " at rest nowhere,"
                    + " not even at the root; a theme answers every target at the root";
        });
        if (outcome instanceof Answered a)
            out.add("         " + (a.node().equals(query) && steps.size() == 1 ? a.layer() + " answers the node itself"
                    : "asked " + steps.size() + (steps.size() == 1 ? " time" : " times") + ": " + a.layer() + " answers at " + a.node()));
        return List.copyOf(out);
    }
}
