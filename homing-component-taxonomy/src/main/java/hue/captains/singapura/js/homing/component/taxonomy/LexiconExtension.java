package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * What a Maven module adds to a lexicon: the words only its components show. The lexicon it extends
 * is a common one - the words every deployment of it has - and the module's words are in it if and
 * only if the module is on the classpath, as a {@link TaxonomyExtension}'s leaves are in the tree. A
 * word is filed under the base's branches, or under one the module declares beside it.
 *
 * <p>A word of a module's own is drawn only where a style writes it: the module writes it in the
 * base style every style is written over, so that every style draws it, and in any other where it
 * should look otherwise.</p>
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}: a module names its extension in
 * {@code META-INF/services/hue.captains.singapura.js.homing.component.taxonomy.LexiconExtension},
 * and a public record with no components is one the loader can make.</p>
 */
public interface LexiconExtension {

    /** The lexicon this adds to, by its source's type. */
    Class<? extends LexiconSource> extending();

    /** The words the module adds, in the order it lists them. */
    List<Word<?>> words();
}
