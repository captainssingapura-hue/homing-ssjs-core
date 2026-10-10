package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * What a Maven module adds to a taxonomy: the components it declares beside its own code, and the
 * roles only they name. The taxonomy it extends is a common one - a base tree every deployment of
 * it has - and the module's leaves are in the tree if and only if the module is on the classpath:
 * a deployment that leaves the module out never sees them. A component library extends its own
 * base this way, and so does any downstream that extends or customises it.
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}: a module names its extension in
 * {@code META-INF/services/hue.captains.singapura.js.homing.component.taxonomy.TaxonomyExtension},
 * and a public record with no components is one the loader can make. The base lists its own leaves,
 * then the extensions' ({@link TaxonomyExtensions}); how the tree is read does not change, only
 * what it is read from. A build stage may later write the list out, so nothing is looked up when
 * serving.</p>
 *
 * <p>A leaf its module realizes but no extension lists is caught where the implementations are
 * graded: the module implements a leaf the taxonomy does not declare.</p>
 */
public interface TaxonomyExtension {

    /** The taxonomy this adds to, by its source's type. */
    Class<? extends TaxonomySource> extending();

    /** The components the module declares, in the order it lists them. */
    List<Component<?>> components();

    /** The roles only its components name; none by default, its parts named in the base's words. */
    default List<Role<?>> roles() { return List.of(); }
}
