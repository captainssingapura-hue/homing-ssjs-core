package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Comparator;

/**
 * The order a module's imports are kept and written in: by the source's class name. It follows from what is imported and
 * nothing else, so a module serves the same text however its list is written, on every start of every server. A source
 * is a stateless singleton, known by its class alone, so no two of a module's sources tie.
 *
 * <p>The order carries no meaning: a module that needs another run first imports it, and a sheet that must follow another
 * says so by {@link CssClass#dependsOn()}.</p>
 */
public record ImportOrder() implements Comparator<Importable>, StatelessFunctionalObject, StatelessSingleton {

    public static final ImportOrder INSTANCE = new ImportOrder();

    /** Two sources, by their class names. */
    @Override public int compare(Importable a, Importable b) { return a.getClass().getName().compareTo(b.getClass().getName()); }
}
