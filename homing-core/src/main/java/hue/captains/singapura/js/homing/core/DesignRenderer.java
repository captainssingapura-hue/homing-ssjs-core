package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Optional;

/**
 * What the design side makes for a server: the sheets it derives, as any {@link CssRenderer}, and the modules made from
 * the design ({@link MadeByDesign}). Built with the design a site holds and handed to the server, so that a module or a
 * sheet stays an empty value that names no site, and the site's design reaches it by argument. The server never learns
 * what a design is.
 */
public interface DesignRenderer extends CssRenderer {

    /** The module's script, without its imports and exports, if this renderer makes it. */
    Optional<List<String>> script(EsModule<?> module);
}
