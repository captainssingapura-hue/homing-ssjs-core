package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Optional;

/**
 * A sheet the server DERIVES for each theme - from a design's words, or from the house's styles
 * over its taxonomy - as against a {@link CssGroup}, which is PROVIDED: declared by hand, its
 * classes' bodies the same in every theme (RFC 0064, D25). Only a derived sheet varies with the
 * theme, so only a derived sheet is fetched again when the theme changes, and never by the CSS
 * manager: by the theme's own owner, {@code ThemeSheets}.
 *
 * <p>It declares no classes. The classes in it are named by whoever wears them - a component's look
 * by its casting, a mark by its word - and what it exports, if anything, only lets a module import
 * it so that it loads.</p>
 *
 * @param <D> self-type
 */
public interface DerivedCss<D extends DerivedCss<D>> extends EsModule<D> {

    /**
     * What a derived sheet varies with, each swapped on its own (RFC 0067, appendix Two Classes per Leaf): the theme -
     * a sheet made from the walk, for every theme; the style - a style-aware sheet, the same in every palette; or the
     * palette - a palette's sheet, the same under every style. Each is asked for by its own name on the address,
     * {@code ?theme=}, {@code ?style=}, {@code ?palette=}, and kept as a preference of that name.
     */
    enum VariesWith {
        THEME("theme"), STYLE("style"), PALETTE("palette");

        private final String param;

        VariesWith(String param) { this.param = param; }

        /** The name it is asked for by, on the address and among the preferences. */
        public String param() { return param; }
    }

    /** What this sheet varies with; the theme, unless it says. */
    default VariesWith variesWith() { return VariesWith.THEME; }

    /**
     * What it is served under when the request names nothing, and the theme's owner's fallback until the address or a
     * stored pick says otherwise: a style's token, a palette's. None, unless it says: a sheet varying with the theme
     * takes the deployment's default theme.
     */
    default Optional<String> fallback() { return Optional.empty(); }

    @Override
    default ImportsFor<D> imports() {
        return ImportsFor.noImports();
    }

    @SuppressWarnings("unchecked")
    @Override
    default ExportsOf<D> exports() {
        return new ExportsOf<>((D) this, List.of());
    }
}
