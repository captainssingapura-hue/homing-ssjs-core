package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * A group of SVG resources that generates a single ES module.
 * Each SvgBeing in the group becomes a frozen marker object export.
 */
public interface SvgGroup<G extends SvgGroup<G>> extends EsModule<G> {

    /**
     * Svg Group by default has no import.
     * @return
     */
    @Override
    default ImportsFor<G> imports() {
        return new ImportsFor<>();
    }

    /** The SVG resources of the group, each an export of its module. */
    List<SvgBeing<G>> svgBeings();
}
