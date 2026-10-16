package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The extensions of a taxonomy on the classpath: every {@link TaxonomyExtension} the service loader
 * finds through the source's class loader that names the source as what it extends, in the order of
 * their class names - so a tree is read alike whatever the order of the classpath. A source lists
 * its own leaves first, then these.
 */
public record TaxonomyExtensions() implements StatelessFunctionalObject {

    public static final TaxonomyExtensions INSTANCE = new TaxonomyExtensions();

    /** The extensions of this source on the classpath; none when no module on it extends it. */
    public List<TaxonomyExtension> of(Class<? extends TaxonomySource> source) {
        var out = new ArrayList<TaxonomyExtension>();
        for (TaxonomyExtension e : ServiceLoader.load(TaxonomyExtension.class, source.getClassLoader()))
            if (e.extending() == source) out.add(e);
        out.sort(Comparator.comparing(e -> e.getClass().getName()));
        return List.copyOf(out);
    }

    /** The source's own components, then each extension's. */
    public List<Component<?>> components(Class<? extends TaxonomySource> source, List<? extends Component<?>> own) {
        var out = new ArrayList<Component<?>>(own);
        for (TaxonomyExtension e : of(source)) out.addAll(e.components());
        return List.copyOf(out);
    }

    /** The source's own types of state, then each extension's. */
    public List<StateType<?>> states(Class<? extends TaxonomySource> source, List<? extends StateType<?>> own) {
        var out = new ArrayList<StateType<?>>(own);
        for (TaxonomyExtension e : of(source)) out.addAll(e.states());
        return List.copyOf(out);
    }

    /** The source's own types of event, then each extension's. */
    public List<EventType<?>> events(Class<? extends TaxonomySource> source, List<? extends EventType<?>> own) {
        var out = new ArrayList<EventType<?>>(own);
        for (TaxonomyExtension e : of(source)) out.addAll(e.events());
        return List.copyOf(out);
    }

    /** The source's own roles, then each extension's. */
    public List<Role<?>> roles(Class<? extends TaxonomySource> source, List<? extends Role<?>> own) {
        var out = new ArrayList<Role<?>>(own);
        for (TaxonomyExtension e : of(source)) out.addAll(e.roles());
        return List.copyOf(out);
    }

    // ── registered explicitly: the extensions an instance names, and nothing found on the classpath ──

    /**
     * A source's taxonomy read with the extensions registered for it, explicitly, in the order registered - nothing
     * looked up on the classpath. Refused, every problem at once, when an extension extends another taxonomy or is
     * registered twice; the tree is then read as any is ({@link ReadTaxonomy}), so what an extension declares is held to
     * the same rules as the source's own.
     *
     * @param source     the taxonomy the extensions add to
     * @param components the source's own components
     * @param roles      the source's own role catalogue
     * @param states     the source's own types of state
     * @param events     the source's own types of event
     * @param extensions the extensions registered for it
     */
    public Taxonomy read(Class<? extends TaxonomySource> source, List<? extends Component<?>> components, List<? extends Role<?>> roles,
                         List<? extends StateType<?>> states, List<? extends EventType<?>> events, List<? extends TaxonomyExtension> extensions) {
        var problems = new ArrayList<String>();
        var registered = new HashSet<Class<?>>();
        for (TaxonomyExtension e : extensions) {
            if (!registered.add(e.getClass())) problems.add(e.getClass().getName() + " is registered twice");
            if (e.extending() != source)
                problems.add(e.getClass().getName() + " extends " + e.extending().getSimpleName() + ", and is registered for " + source.getSimpleName());
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the taxonomy's extensions are refused:\n  " + String.join("\n  ", problems));
        var allComponents = new ArrayList<Component<?>>(components);
        var allRoles = new ArrayList<Role<?>>(roles);
        var allStates = new ArrayList<StateType<?>>(states);
        var allEvents = new ArrayList<EventType<?>>(events);
        for (TaxonomyExtension e : extensions) {
            allComponents.addAll(e.components());
            allRoles.addAll(e.roles());
            allStates.addAll(e.states());
            allEvents.addAll(e.events());
        }
        return new ReadTaxonomy().read(allComponents, allRoles, allStates, allEvents);
    }
}
