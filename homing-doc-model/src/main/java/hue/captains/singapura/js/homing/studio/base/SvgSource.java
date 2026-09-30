package hue.captains.singapura.js.homing.studio.base;

/**
 * A doc whose data is an SVG image: what an {@code SvgSegment} shows inline. Its
 * {@link #contents()} is the SVG markup itself - text that is the doc's own, not a
 * rendering of it.
 *
 * <p>The model names only this much. Where the markup comes from is the implementer's:
 * {@code SvgDoc} reads it from a typed SVG group served by the ES-module layer, which the
 * model does not know - so {@code SvgDoc} lives with that layer and implements this.</p>
 */
public interface SvgSource extends Doc {

    @Override default String contentType()   { return "image/svg+xml"; }
    @Override default String fileExtension() { return ".svg"; }
}
