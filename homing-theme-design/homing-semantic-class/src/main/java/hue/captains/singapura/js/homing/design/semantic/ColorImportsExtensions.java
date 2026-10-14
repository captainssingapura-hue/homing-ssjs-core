package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The modules' imports on the classpath (RFC 0067, appendix Two Classes per Leaf, D32): every {@link
 * ColorImportsExtension} the service loader finds through a style's class loader, in the order of their class names - so
 * a check reads alike whatever the order of the classpath.
 */
public record ColorImportsExtensions() implements StatelessFunctionalObject {

    public static final ColorImportsExtensions INSTANCE = new ColorImportsExtensions();

    /** Every module's imports a style's class loader finds; none when no module on it imports any. */
    public List<ColorImportsExtension> of(DesignStyle<?> style) {
        ClassLoader loader = style.getClass().getClassLoader();
        var out = new ArrayList<ColorImportsExtension>();
        for (ColorImportsExtension e : ServiceLoader.load(ColorImportsExtension.class, loader != null ? loader : ClassLoader.getSystemClassLoader()))
            out.add(e);
        out.sort(Comparator.comparing(e -> e.getClass().getName()));
        return List.copyOf(out);
    }
}
