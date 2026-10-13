package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The extensions of a colour tree on the classpath: every {@link ColorExtension} the service loader finds through the
 * source's class loader that names the source as what it grafts onto, in the order of their class names - so a tree is
 * read alike whatever the order of the classpath.
 */
public record ColorExtensions() implements StatelessFunctionalObject {

    public static final ColorExtensions INSTANCE = new ColorExtensions();

    /** The extensions of this source on the classpath; none when no module on it grafts onto it. */
    public List<ColorExtension> of(Class<? extends ColorSource> source) {
        var out = new ArrayList<ColorExtension>();
        for (ColorExtension e : ServiceLoader.load(ColorExtension.class, source.getClassLoader()))
            if (e.extending() == source) out.add(e);
        out.sort(Comparator.comparing(e -> e.getClass().getName()));
        return List.copyOf(out);
    }
}
