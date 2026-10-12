package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * The treatments a sheet has a rule for, read from the answers rather than searched for. A
 * treatment exists only where some answer names its state, so:
 *
 * <ol>
 *   <li><b>the answers</b> give the treated slots: every state but rest that an answer of its own
 *       names, on its node and target - and, for a redirect, every state its target offers, since a
 *       redirect takes its whole class;</li>
 *   <li><b>the taxonomy</b> carries each slot down to every component whose lineage holds its node,
 *       a treatment being the component's, common to every role;</li>
 *   <li><b>the resolver</b> confirms each, in the theme's own walk - the worn style first, its base's,
 *       a redirect followed, a silence that ends it - and keeps those that land on an answer that
 *       says something.</li>
 * </ol>
 *
 * The walk's rules stay the resolver's alone; this decides only which slots are worth asking.
 * Over several themes it asks the chains that differ, never their pairings: a style over its bases
 * for every target that is not colour, a palette for colour - a style worn in a palette makes no
 * treatment and takes none away.
 */
public record DeriveTreatments() implements StatelessFunctionalObject {

    public static final DeriveTreatments INSTANCE = new DeriveTreatments();

    /** A treated slot an answer names: a node, a target, a state. */
    private record Slot(ComponentNode node, Target target, State state) {}

    /** The treatments a theme's sheet has a rule for, in the sheet's order: a state at a time in the cascade's order, then the components, then the targets. */
    public List<Treatment> of(Taxonomy taxonomy, Dressed theme) { return union(taxonomy, List.of(theme)); }

    /** The treatments any of the themes has a rule for, in the sheet's order. */
    public List<Treatment> union(Taxonomy taxonomy, Collection<Dressed> themes) {
        var byStyle = new LinkedHashMap<DesignStyle, Dressed>();
        var byPalette = new LinkedHashMap<ColorPalette, Dressed>();
        for (Dressed d : themes) {
            byStyle.putIfAbsent(d.style(), d);
            byPalette.putIfAbsent(d.palette(), d);
        }
        // 1. the treated slots, from every layer of every chain
        var slots = new LinkedHashSet<Slot>();
        var layers = new LinkedHashSet<Layer>();
        for (Dressed d : byStyle.values()) layers.addAll(d.layers());
        for (Dressed d : byPalette.values()) layers.addAll(d.layers());
        for (Layer layer : layers)
            layer.answers().forEach((key, answer) -> {
                Target t = key.target();
                if (t.carrier() != Carrier.CSS) return;
                ComponentNode node = key.node().component();
                switch (answer) {
                    case Answer.Own<?> own -> own.bodies().keySet().forEach(s -> { if (s != State.REST) slots.add(new Slot(node, t, s)); });
                    case Answer.Redirect<?> r -> t.states().forEach(s -> { if (s != State.REST) slots.add(new Slot(node, t, s)); });
                    case Answer.FromBase<?> f -> { }   // the base's own answers are slots of the base's layer
                }
            });
        // 2. down the taxonomy: every component whose lineage holds the slot's node - a virtual leaf never, a design draws it
        var under = new HashMap<ComponentNode, List<Component<?>>>();
        for (Component<?> c : taxonomy.components()) {
            if (c instanceof VirtualComponent<?>) continue;
            for (ComponentNode n : taxonomy.fallback(c)) under.computeIfAbsent(n, k -> new ArrayList<>()).add(c);
        }
        // 3. confirmed by the resolver, in the chains that answer the target
        var treated = new LinkedHashSet<Treatment>();
        var asked = new LinkedHashSet<Treatment>();
        for (Slot slot : slots)
            for (Component<?> c : under.getOrDefault(slot.node(), List.of())) {
                var candidate = new Treatment(c, slot.state(), slot.target());
                if (!asked.add(candidate)) continue;
                Collection<Dressed> chains = slot.target() instanceof Target.Color ? byPalette.values() : byStyle.values();
                for (Dressed d : chains) {
                    Resolution r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, d, new Casting(c), slot.state(), slot.target());
                    if (r.outcome() instanceof Resolution.Answered a && !a.body().isBlank()) { treated.add(candidate); break; }
                }
            }
        return sorted(taxonomy, treated);
    }

    private static List<Treatment> sorted(Taxonomy taxonomy, Collection<Treatment> treatments) {
        var component = new HashMap<Component<?>, Integer>();
        for (Component<?> c : taxonomy.components()) component.put(c, component.size());
        var target = new HashMap<Target, Integer>();
        for (Target t : SemanticTokens.INSTANCE.cssLeaves()) target.put(t, target.size());
        return treatments.stream().sorted(Comparator.<Treatment>comparingInt(t -> t.state().ordinal())
                .thenComparingInt(t -> component.get(t.component()))
                .thenComparingInt(t -> target.get(t.target()))).toList();
    }

    /** The treatments by component, in the taxonomy's order; each component's in the sheet's order. */
    public Map<Component<?>, List<Treatment>> byComponent(Taxonomy taxonomy, List<Treatment> treatments) {
        var out = new LinkedHashMap<Component<?>, List<Treatment>>();
        for (Component<?> c : taxonomy.components()) {
            var mine = treatments.stream().filter(t -> t.component().equals(c)).toList();
            if (!mine.isEmpty()) out.put(c, mine);
        }
        return out;
    }
}
