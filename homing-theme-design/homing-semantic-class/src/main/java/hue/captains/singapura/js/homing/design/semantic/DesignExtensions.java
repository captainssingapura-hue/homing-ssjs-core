package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The designs on a classpath: every {@link DesignExtension} the service loader finds through a class
 * loader, in the order of their class names - so designs are read alike whatever the order of the
 * classpath - and their concrete leaves, each extension's in the order it lists them.
 */
public record DesignExtensions() implements StatelessFunctionalObject {

    public static final DesignExtensions INSTANCE = new DesignExtensions();

    /** Every concrete leaf the extensions found through this class loader declare. */
    public List<ConcreteComponent<?, ?, ?>> concretes(ClassLoader loader) {
        var extensions = new ArrayList<DesignExtension>();
        for (DesignExtension e : ServiceLoader.load(DesignExtension.class, loader)) extensions.add(e);
        extensions.sort(Comparator.comparing(e -> e.getClass().getName()));
        var out = new ArrayList<ConcreteComponent<?, ?, ?>>();
        for (DesignExtension e : extensions) out.addAll(e.concretes());
        return List.copyOf(out);
    }
}
