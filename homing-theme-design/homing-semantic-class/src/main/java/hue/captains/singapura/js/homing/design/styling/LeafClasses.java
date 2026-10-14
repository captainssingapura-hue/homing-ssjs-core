package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentLeaf;
import hue.captains.singapura.js.homing.component.taxonomy.Importances;
import hue.captains.singapura.js.homing.component.taxonomy.Pseudo;
import hue.captains.singapura.js.homing.component.taxonomy.StateType;
import hue.captains.singapura.js.homing.component.taxonomy.StateValue;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The style-aware classes of a leaf, generated from its type (RFC 0067, appendix Two Classes per Leaf): its class,
 * {@code styled-<token>}; a class for each value but the rest of each application state it lists,
 * {@code styled-<token>--<word>}, worn while the state holds; and its pseudo states, drawn with their pseudo-classes
 * inside its class, never with a class of their own. Both in the leaf's precedence, the least important first, so in a
 * sheet the later wins.
 */
public record LeafClasses() implements StatelessFunctionalObject {

    public static final LeafClasses INSTANCE = new LeafClasses();

    /** {@code styled-plain-button}, {@code styled-dialog-go}. */
    public String styled(ComponentLeaf leaf) { return "styled-" + leaf.token(); }

    /** {@code styled-tab--selected}: the leaf's class and the value's design word. */
    public String state(ComponentLeaf leaf, StateType<?> state, StateValue value) { return styled(leaf) + "--" + state.word(value); }

    /**
     * One of a leaf's states as it is drawn, in the leaf's precedence: an application state's value, worn as a class, or a
     * pseudo state, drawn with its pseudo-class.
     *
     * @param state    the type of state
     * @param value    the value drawn; for a pseudo state, the one its pseudo-class shows
     * @param drawnBy  the class worn while it holds - {@code .styled-tab--selected} - or the pseudo-class - {@code :hover}
     * @param level    the level the leaf gives it: {@code tertiary}
     */
    public record Drawn(StateType<?> state, StateValue value, String drawnBy, String level) implements ValueObject {
        /** Whether it is a pseudo state, drawn by its pseudo-class. */
        public boolean pseudo() { return drawnBy.startsWith(":"); }
    }

    /** What a leaf wears: its class, and its states, in its precedence. */
    public record Listing(ComponentLeaf leaf, String styled, List<Drawn> states) implements ValueObject {
        public Listing { states = List.copyOf(states); }
    }

    /** A leaf's classes and states, in its precedence. */
    public Listing of(Taxonomy taxonomy, ComponentLeaf leaf) {
        var importances = Importances.INSTANCE;
        var drawn = new ArrayList<Drawn>();
        for (StateType<?> s : taxonomy.precedence(leaf)) {
            String level = importances.levelFor(leaf, s).map(l -> NodeName.ofType(l, "").value()).orElse("unranked");
            List<StateValue> values = s.values();
            for (StateValue v : values.subList(1, values.size()))
                drawn.add(new Drawn(s, v, s instanceof Pseudo p ? p.pseudoClass() : "." + state(leaf, s, v), level));
        }
        return new Listing(leaf, styled(leaf), drawn);
    }

    /** Every leaf's, the components then the parts. */
    public List<Listing> listing(Taxonomy taxonomy) {
        return taxonomy.leaves().stream().map(l -> of(taxonomy, l)).toList();
    }

    /**
     * Every leaf described for the page, as {@code StyledLeaf} wears it: a frozen object, by token, of each leaf's token,
     * its class, and each application state it lists with every value it has - the rest's class null - in its
     * precedence; no pseudo state, which the browser draws.
     *
     * <pre>{@code
     * Object.freeze({
     *     "tab": Object.freeze({ token: "tab", styled: "styled-tab", states: Object.freeze({
     *         "selected": Object.freeze({ "false": null, "true": "styled-tab--selected" }) }) })
     * })
     * }</pre>
     */
    public String script(Taxonomy taxonomy) {
        var leaves = new ArrayList<String>();
        for (ComponentLeaf leaf : taxonomy.leaves()) {
            var states = new ArrayList<String>();
            for (StateType<?> s : taxonomy.precedence(leaf)) {
                if (s instanceof Pseudo) continue;
                List<StateValue> values = s.values();
                var classes = new ArrayList<String>();
                for (int i = 0; i < values.size(); i++)
                    classes.add(quoted(values.get(i).token()) + ": " + (i == 0 ? "null" : quoted(state(leaf, s, values.get(i)))));
                states.add(quoted(s.name().value()) + ": Object.freeze({ " + String.join(", ", classes) + " })");
            }
            leaves.add("    " + quoted(leaf.token()) + ": Object.freeze({ token: " + quoted(leaf.token()) + ", styled: " + quoted(styled(leaf))
                    + ", states: Object.freeze({" + (states.isEmpty() ? "" : " " + String.join(", ", states) + " ") + "}) })");
        }
        return "Object.freeze({\n" + String.join(",\n", leaves) + "\n})";
    }

    private static String quoted(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }

    /** The listing, a line a class or a pseudo state: {@code plain-button  .styled-plain-button}, then each state indented. */
    public List<String> lines(Taxonomy taxonomy) {
        var out = new ArrayList<String>();
        for (Listing l : listing(taxonomy)) {
            out.add(l.leaf().token() + "  ." + l.styled());
            for (Drawn d : l.states())
                out.add("    " + d.drawnBy() + "  " + d.state().name().value() + "=" + d.value().token() + "  " + d.level());
        }
        return List.copyOf(out);
    }
}
