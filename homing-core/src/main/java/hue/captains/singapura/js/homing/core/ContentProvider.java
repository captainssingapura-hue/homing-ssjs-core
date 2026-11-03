package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * Provide actual script content for the given module.
 *  <br> excluding imports and exports which will be manged separately.
 * @param <M>
 */
public interface ContentProvider<M extends EsModule> extends StatelessFunctionalObject {
    /** The module's script, line by line, without its imports and exports. */
    List<String> content();
}
