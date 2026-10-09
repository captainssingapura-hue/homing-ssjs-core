package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a walk found for a node in a state on a target, and how: the nodes it asked, most precise
 * first, up to the one that answered; the notes on why it walked as it did; and the outcome - the
 * one class the element wears for it, or none.
 *
 * @param query   the node asked for
 * @param state   the state
 * @param target  the target
 * @param notes   why the walk is the one it is: a functional role set aside, a role set aside for a state
 * @param steps   every node asked, in order, the last the one that answered when one did
 * @param outcome the class, or why there is none
 */
public record Resolution(Casting query, State state, Target target, List<String> notes, List<Step> steps, Outcome outcome)
        implements ValueObject {

    public Resolution {
        notes = List.copyOf(notes);
        steps = List.copyOf(steps);
    }

    /** A node asked, and whether the theme answered it. */
    public record Step(Casting node, boolean answered) implements ValueObject {}

    /** The one class an element wears for it, or why there is none. */
    public sealed interface Outcome extends ValueObject permits Answered, Silent, Unanswered {}

    /** The node that answered, and the class it is worn as. */
    public record Answered(Casting node, String className) implements Outcome {}

    /** A state no component answers: nothing is worn for it, and the element shows its look at rest. */
    public record Silent() implements Outcome {}

    /** A look at rest that nothing answers, the root included: the theme is incomplete. */
    public record Unanswered() implements Outcome {}

    /** The class worn, or null when there is none. */
    public String className() { return outcome instanceof Answered a ? a.className() : null; }

    /** The walk told, a line at a time. */
    public List<String> explain() {
        var tokens = SemanticTokens.INSTANCE;
        var out = new ArrayList<String>();
        out.add("query    " + tokens.casting(query) + "  " + tokens.state(state) + "  " + tokens.target(target));
        for (String n : notes) out.add("note     " + n);
        int width = 0;
        for (Step s : steps) width = Math.max(width, tokens.casting(s.node()).length());
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            String node = tokens.casting(s.node());
            out.add((i == 0 ? "walk     " : "         ") + String.format("%2d. ", i + 1) + node
                    + " ".repeat(width - node.length() + 2) + (s.answered() ? "answered" : "-"));
        }
        out.add(switch (outcome) {
            case Answered a -> "result   " + a.className();
            case Silent s -> "result   (none) - no component answers " + tokens.target(target) + " when " + tokens.state(state)
                    + ": nothing is worn for it, and the element shows its look at rest";
            case Unanswered u -> "result   UNANSWERED - the theme answers " + tokens.target(target) + " at rest nowhere,"
                    + " not even at the root; a theme answers every target at the root";
        });
        if (outcome instanceof Answered a)
            out.add("         " + (a.node().equals(query) ? "the theme answers the node itself"
                    : "the nearest the theme answers: " + a.node() + ", " + (steps.size() - 1) + (steps.size() == 2 ? " step" : " steps") + " up"));
        return List.copyOf(out);
    }
}
