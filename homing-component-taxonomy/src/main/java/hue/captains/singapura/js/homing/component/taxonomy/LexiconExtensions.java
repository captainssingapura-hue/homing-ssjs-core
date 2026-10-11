package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The extensions of a lexicon on the classpath: every {@link LexiconExtension} the service loader
 * finds through the source's class loader that names the source as what it extends, in the order of
 * their class names - so a lexicon is read alike whatever the order of the classpath. A source lists
 * its own words first, then these.
 */
public record LexiconExtensions() implements StatelessFunctionalObject {

    public static final LexiconExtensions INSTANCE = new LexiconExtensions();

    /** The extensions of this source on the classpath; none when no module on it extends it. */
    public List<LexiconExtension> of(Class<? extends LexiconSource> source) {
        var out = new ArrayList<LexiconExtension>();
        for (LexiconExtension e : ServiceLoader.load(LexiconExtension.class, source.getClassLoader()))
            if (e.extending() == source) out.add(e);
        out.sort(Comparator.comparing(e -> e.getClass().getName()));
        return List.copyOf(out);
    }

    /** The source's own words, then each extension's. */
    public List<Word<?>> words(Class<? extends LexiconSource> source, List<? extends Word<?>> own) {
        var out = new ArrayList<Word<?>>(own);
        for (LexiconExtension e : of(source)) out.addAll(e.words());
        return List.copyOf(out);
    }
}
