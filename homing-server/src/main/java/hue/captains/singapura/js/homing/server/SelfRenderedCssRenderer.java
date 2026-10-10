package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;

import java.util.Optional;

/**
 * The renderer of every derived sheet that renders itself ({@link SelfRenderedCss}): its sheet is
 * its own answer for the slug. Asked before the design side's renderers, since a sheet that renders
 * itself is nobody else's.
 */
public record SelfRenderedCssRenderer() implements CssRenderer {

    public static final SelfRenderedCssRenderer INSTANCE = new SelfRenderedCssRenderer();

    @Override public Optional<String> render(DerivedCss<?> sheet, String themeSlug) {
        return sheet instanceof SelfRenderedCss self ? Optional.of(self.css(themeSlug)) : Optional.empty();
    }

    @Override public boolean owns(DerivedCss<?> sheet) { return sheet instanceof SelfRenderedCss; }
}
