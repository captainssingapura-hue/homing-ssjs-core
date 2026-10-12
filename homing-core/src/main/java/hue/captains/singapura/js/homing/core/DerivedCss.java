package hue.captains.singapura.js.homing.core;

import java.util.List;

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
