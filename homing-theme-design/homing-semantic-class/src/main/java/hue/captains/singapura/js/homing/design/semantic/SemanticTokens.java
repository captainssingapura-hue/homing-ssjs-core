package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.RoleNode;
import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Word;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The words the semantic classes are spelt in, written and read. Every node, state and target has
 * a token in kebab case, which never holds an underscore and never two hyphens running, so the
 * joints between them cannot be mistaken:
 *
 * <ul>
 *   <li>a casting: {@code danger-button} on its own, {@code danger-button_go} in a role - the
 *       component's token and the role's, joined by one underscore;</li>
 *   <li>a look, a casting on a target at rest: {@code danger-button_go__shape-corner};</li>
 *   <li>a treatment, a component in a state on a target: {@code button__effect-opacity--disabled}.</li>
 *   <li>a concrete leaf - a virtual leaf as a design draws it - on a target: {@code
 *       minesweeper-classic___cursor-border__shape-outline}, the design's token and the leaf's joined by
 *       three underscores, which no casting ever holds; its states nested within the one class.</li>
 * </ul>
 */
public record SemanticTokens() implements StatelessFunctionalObject {

    public static final SemanticTokens INSTANCE = new SemanticTokens();

    /** {@code danger-button}, or {@code danger-button_go} in a role. */
    public String casting(Casting c) {
        return c.standalone() ? c.component().name().value() : c.component().name().value() + "_" + c.role().name().value();
    }

    /** {@code rest}, {@code disabled}. */
    public String state(State s) { return s.name().toLowerCase(Locale.ROOT).replace('_', '-'); }

    /** {@code shape-corner}. */
    public String target(Target t) { return t.token(); }

    /** The class for a casting's look on a target at rest: {@code danger-button_go__shape-corner}. */
    public String look(Casting c, Target t) { return casting(c) + "__" + target(t); }

    /** The class for a component's treatment in a state on a target: {@code button__effect-opacity--disabled}. */
    public String treatment(Casting c, State s, Target t) {
        if (s == State.REST) throw new IllegalArgumentException("rest is a look, not a treatment");
        if (!c.standalone()) throw new IllegalArgumentException(c + ": a treatment is a component's on its own");
        return casting(c) + "__" + target(t) + "--" + state(s);
    }

    /** The class for a node in a state on a target: a look at rest, a treatment otherwise. */
    public String className(Casting c, State s, Target t) { return s == State.REST ? look(c, t) : treatment(c, s, t); }

    /**
     * A concrete leaf: a design's drawing of a virtual leaf - {@code minesweeper-classic___cursor-border}. Its
     * design's token and its leaf's are joined by three underscores, which no casting holds, so a design's class
     * is never the house's, whatever a design is named.
     */
    public String concrete(Design<?, ?> design, VirtualComponent<?> leaf) { return design.token() + "___" + leaf.token(); }

    /** The class a concrete leaf wears on a target, its states nested within: {@code minesweeper-classic___cursor-border__shape-outline}. */
    public String drawn(Design<?, ?> design, VirtualComponent<?> leaf, Target t) { return concrete(design, leaf) + "__" + target(t); }

    /**
     * The class a mark wears to show a word of a lexicon: {@code word-close}. It holds no {@code __}, so it
     * is never a look's nor a treatment's, whatever a component is named.
     */
    public String word(Word<?> w) { return "word-" + w.token(); }

    /** The keyframes an animation effect runs: {@code effect-marching-ants} - a name of keyframes, never a class's. */
    public String effect(AnimationEffect e) { return "effect-" + e.token(); }

    /**
     * The still dual of the class an animation effect is worn by - the effect without any motion, which an element
     * is given when the effect is switched off: {@code house-grid___copied-border__animation-effect-still}.
     */
    public String still(String effectClass) { return effectClass + "-still"; }

    // ── reading ─────────────────────────────────────────────────────────────

    /** The casting a token names in a taxonomy: {@code danger-button}, {@code danger-button_go}. */
    public Casting castingOf(Taxonomy taxonomy, String token) {
        String[] parts = token.split("_", -1);
        if (parts.length > 2 || parts[0].isEmpty() || (parts.length == 2 && parts[1].isEmpty()))
            throw new IllegalArgumentException("'" + token + "' is no casting: a component's token, and a role's after one underscore");
        Taxon component = componentOf(taxonomy, parts[0]);
        RoleNode role = parts.length == 1 ? RoleRoot.INSTANCE : roleOf(taxonomy, parts[1]);
        return new Casting(component, role);
    }

    /** The state a token names: {@code rest}, {@code disabled}. */
    public State stateOf(String token) {
        for (State s : State.values()) if (state(s).equals(token)) return s;
        throw new IllegalArgumentException("'" + token + "' is no state; one of " + String.join(", ", states()));
    }

    /** The target leaf a token names: {@code shape-corner}; refused for a leaf kept only for the old design system. */
    public Target targetOf(String token) {
        for (Target t : leaves()) if (t.token().equals(token)) return t;
        throw new IllegalArgumentException("'" + token + "' is no target; one of " + String.join(", ", targets()));
    }

    /** Every state's token, in the cascade's order. */
    public List<String> states() {
        var out = new ArrayList<String>();
        for (State s : State.values()) out.add(state(s));
        return out;
    }

    /** Every target leaf's token the semantic classes answer, in the tree's order. */
    public List<String> targets() {
        var out = new ArrayList<String>();
        for (Target t : leaves()) out.add(t.token());
        return out;
    }

    /** The target leaves the semantic classes answer, in the tree's order: every leaf. */
    public List<Target> leaves() { return Target.leaves(); }

    /** Of those, the ones a sheet carries: every CSS leaf. */
    public List<Target> cssLeaves() { return leaves().stream().filter(t -> t.carrier() == Carrier.CSS).toList(); }

    private static Taxon componentOf(Taxonomy taxonomy, String token) {
        if (Root.INSTANCE.token().equals(token)) return Root.INSTANCE;
        for (ComponentNode n : taxonomy.nodes())
            if (n instanceof Taxon t && t.token().equals(token)) return t;
        throw new IllegalArgumentException("'" + token + "' is no component or branch of the taxonomy");
    }

    private static RoleNode roleOf(Taxonomy taxonomy, String token) {
        for (RoleNode n : taxonomy.roleBranches()) if (n.name().value().equals(token)) return n;
        for (RoleNode n : taxonomy.roles()) if (n.name().value().equals(token)) return n;
        throw new IllegalArgumentException("'" + token + "' is no role or branch of the role catalogue");
    }
}
