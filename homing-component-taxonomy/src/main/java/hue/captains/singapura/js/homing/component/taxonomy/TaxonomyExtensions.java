package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
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
}
