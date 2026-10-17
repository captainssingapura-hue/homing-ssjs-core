package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A components taxonomy read from the component modules an instance registers at bootstrap - a flat list, explicitly,
 * nothing found on the classpath. Each module's components in the order registered; their parts with them,
 * transitively; the branches, roles, states and events from them. The base's catalogue of roles and its trees of states
 * and events are whole whatever is registered, so a component is built from their words. Refused - every problem at
 * once - when a module is registered twice or a component is listed twice, by one module or by two: a component is one
 * jar's. What is listed is then read under the same rules as any taxonomy ({@link ReadTaxonomy}).
 */
public record ComponentModules() implements StatelessFunctionalObject {

    public static final ComponentModules INSTANCE = new ComponentModules();

    /**
     * The taxonomy of these modules.
     *
     * @param modules the component modules registered, in order
     * @param roles   the base's catalogue of roles
     * @param states  the base's types of state
     * @param events  the base's types of event
     */
    public Taxonomy read(List<? extends ComponentModule> modules, List<? extends Role<?>> roles,
                         List<? extends StateType<?>> states, List<? extends EventType<?>> events) {
        var problems = new ArrayList<String>();
        var registered = new HashSet<Class<?>>();
        var listedBy = new LinkedHashMap<Component<?>, ComponentModule>();
        for (ComponentModule m : modules) {
            if (!registered.add(m.getClass())) { problems.add(name(m) + " is registered twice"); continue; }
            for (Component<?> c : m.components()) {
                ComponentModule before = listedBy.putIfAbsent(c, m);
                if (before != null)
                    problems.add(c.token() + (before == m ? " is listed twice by " + name(m) : " is listed by " + name(before) + " and by " + name(m))
                            + ": a component is one jar's");
            }
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the component modules are refused:\n  " + String.join("\n  ", problems));
        return new ReadTaxonomy().read(List.copyOf(listedBy.keySet()), roles, states, events);
    }

    /** Which registered module lists a component; none when only a part brings it. */
    public Optional<ComponentModule> lister(List<? extends ComponentModule> modules, Component<?> component) {
        for (ComponentModule m : modules) if (m.components().contains(component)) return Optional.of(m);
        return Optional.empty();
    }

    /** Each component of a taxonomy read from these modules, with the module that brings it: its own, or its owner's by a part, transitively. */
    public Map<Component<?>, ComponentModule> bringers(List<? extends ComponentModule> modules, Taxonomy taxonomy) {
        var out = new LinkedHashMap<Component<?>, ComponentModule>();
        for (ComponentModule m : modules) for (Component<?> c : m.components()) out.putIfAbsent(c, m);
        boolean grew = true;
        while (grew) {
            grew = false;
            for (Part<?, ?> p : taxonomy.parts())
                for (Component<?> base : p.bases())
                    if (out.containsKey(p.owner()) && !out.containsKey(base)) { out.put(base, out.get(p.owner())); grew = true; }
        }
        return out;
    }

    private static String name(ComponentModule m) { return m.getClass().getSimpleName(); }
}
