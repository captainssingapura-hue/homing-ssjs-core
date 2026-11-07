package hue.captains.singapura.js.homing.core;

/**
 * An ES module that wraps a 3rd party JavaScript library.
 * <p>
 * The JS content file handles the actual loading strategy:
 * <ul>
 *   <li>For ESM libraries: import from CDN and re-export</li>
 *   <li>For traditional libraries: dynamically load the script and re-export globals</li>
 * </ul>
 */
public interface ExternalModule<M extends ExternalModule<M>> extends EsModule<M> {
    @Override default ImportsFor<M> imports() { return new ImportsFor<>(); }
}
