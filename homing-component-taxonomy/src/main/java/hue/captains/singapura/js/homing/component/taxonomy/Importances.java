package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The importance of states, read off their types (RFC 0067, appendix Two Classes per Leaf), and a leaf's precedence:
 * when an element is in two states at once and both set one property, the more important wins - its level for the
 * leaf, an override where the leaf has one, else the state's own; two of one level in the order the leaf lists them.
 */
public record Importances() implements StatelessFunctionalObject {

    public static final Importances INSTANCE = new Importances();

    /** The six levels, the most important first: {@code Primary} to {@code Senary}. */
    public List<Class<?>> levels() { return List.of(Importance.class.getPermittedSubclasses()); }

    /** The level a type of state implements; none when it implements none, which the reader refuses. */
    public Optional<Class<?>> levelOf(StateType<?> state) {
        Set<Class<?>> levels = Set.copyOf(levels());
        for (Class<?> i : superinterfaces(state.getClass())) if (levels.contains(i)) return Optional.of(i);
        return Optional.empty();
    }

    /** A level's rank: 0 for the most important, {@code Primary}, to 5 for {@code Senary}. */
    public int rank(Class<?> level) {
        int r = levels().indexOf(level);
        if (r < 0) throw new IllegalArgumentException(level.getName() + " is no level of importance");
        return r;
    }

    /** The level a leaf gives one of its states: its own override, else the state's level. */
    public Optional<Class<?>> levelFor(Component<?> leaf, StateType<?> state) {
        for (ImportanceOverride<?> o : leaf.importance() == null ? List.<ImportanceOverride<?>>of() : leaf.importance())
            if (o != null && o.state().equals(state)) return Optional.of(o.level());
        return levelOf(state);
    }

    /**
     * A leaf's states in precedence: the least important first, so the last listed is the one that wins; two of one level
     * in the order the leaf lists them. A state with no level - the reader refuses one - comes first.
     */
    public List<StateType<?>> precedence(Component<?> leaf) {
        var states = new ArrayList<StateType<?>>(new LinkedHashSet<>(leaf.states() == null ? List.<StateType<?>>of() : leaf.states()));
        states.removeIf(s -> s == null);
        states.sort(Comparator.comparingInt((StateType<?> s) -> -levelFor(leaf, s).map(this::rank).orElse(levels().size())));
        return List.copyOf(states);
    }

    private static List<Class<?>> superinterfaces(Class<?> c) {
        var out = new LinkedHashSet<Class<?>>();
        var todo = new ArrayList<Class<?>>(List.of(c.getInterfaces()));
        while (!todo.isEmpty()) {
            Class<?> i = todo.remove(0);
            if (out.add(i)) todo.addAll(List.of(i.getInterfaces()));
        }
        return List.copyOf(out);
    }
}
