package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.Set;

/**
 * One semantic class a style colours (RFC 0067, appendix Semantic Colours and Flavours): a node of the taxonomy - a
 * casting - a colour target, and which colour it uses: painted in semantic colours its scheme can see, a redirect to
 * another node, or its base's answer. The scheme is the style's, a type argument of the list it is answered in, so a
 * style can name no colour its scheme cannot see - and the scheme is held by the constructors alone, so no answer can
 * carry another's. Refused as it is written, as an {@link Implementation} is: a state the target offers no slot for, a
 * state answered on a role, a class redirected to itself.
 *
 * @param <S> the scheme of the style that says it
 * @param <T> the colour target
 */
public final class Coloring<S extends ColorScheme, T extends Target.Color> implements ValueObject {

    private final Casting node;
    private final T target;
    private final Answer<T> answer;

    /** Painted, in semantic colours its scheme can see. */
    public Coloring(Casting node, T target, Answer.Painted<S, T> answer) { this(node, target, (Answer<T>) answer, true); }

    /** As another node's on the same root, in the style worn. */
    public Coloring(Casting node, T target, Answer.Redirect<T> answer) { this(node, target, (Answer<T>) answer, true); }

    /** Its base's, from here up. */
    public Coloring(Casting node, T target, Answer.FromBase<T> answer) { this(node, target, (Answer<T>) answer, true); }

    /** A component or a branch on its own, painted. */
    public Coloring(Taxon node, T target, Answer.Painted<S, T> answer) { this(new Casting(node), target, answer); }

    /** A component or a branch on its own, as another node's. */
    public Coloring(Taxon node, T target, Answer.Redirect<T> answer) { this(new Casting(node), target, answer); }

    /** A component or a branch on its own, its base's. */
    public Coloring(Taxon node, T target, Answer.FromBase<T> answer) { this(new Casting(node), target, answer); }

    private Coloring(Casting node, T target, Answer<T> answer, boolean checked) {
        this.node = Objects.requireNonNull(node, "Coloring.node");
        this.target = Objects.requireNonNull(target, "Coloring.target");
        this.answer = Objects.requireNonNull(answer, "Coloring.answer");
        var tokens = SemanticTokens.INSTANCE;
        String where = tokens.casting(node) + " " + target.token();
        Set<State> states = switch (answer) {
            case Answer.Painted<?, ?> p -> p.paints().keySet();
            case Answer.FromBase<T> f -> f.only();
            default -> Set.of();
        };
        for (State state : states) {
            if (!target.states().contains(state))
                throw new IllegalArgumentException(where + ": " + target.token() + " offers no " + tokens.state(state) + "; it offers "
                        + String.join(", ", target.states().stream().map(tokens::state).toList()));
            if (answer instanceof Answer.Painted<?, ?> && state != State.REST && !node.standalone())
                throw new IllegalArgumentException(where + ": " + node + " is answered " + tokens.state(state)
                        + ": a state is a treatment, common to every role, and is answered on the component on its own");
        }
        if (answer instanceof Answer.Redirect<T> r && r.to().equals(node))
            throw new IllegalArgumentException(where + " is redirected to itself");
    }

    /** The casting answered. */
    public Casting node() { return node; }

    /** The colour target. */
    public T target() { return target; }

    /** What is said for them. */
    public Answer<T> answer() { return answer; }

    @Override public boolean equals(Object o) {
        return o instanceof Coloring<?, ?> c && c.node.equals(node) && c.target.equals(target) && c.answer.equals(answer);
    }

    @Override public int hashCode() { return Objects.hash(node, target, answer); }

    @Override public String toString() { return "Coloring[" + SemanticTokens.INSTANCE.casting(node) + " " + target.token() + " " + answer + "]"; }
}
