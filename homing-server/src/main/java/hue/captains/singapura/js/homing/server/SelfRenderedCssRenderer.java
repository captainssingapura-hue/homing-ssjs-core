package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;

import java.util.Optional;

/**
 * The renderer of every group that renders itself ({@link SelfRenderedCss}): its sheet is the
 * group's own answer for the slug, and it varies with the theme. Asked before the design side's
 * renderers, since a group that renders itself is nobody else's.
 */
public record SelfRenderedCssRenderer() implements CssRenderer {

    public static final SelfRenderedCssRenderer INSTANCE = new SelfRenderedCssRenderer();

    @Override public Optional<String> render(CssGroup<?> group, String themeSlug) {
        return group instanceof SelfRenderedCss self ? Optional.of(self.css(themeSlug)) : Optional.empty();
    }

    @Override public boolean owns(CssGroup<?> group) { return group instanceof SelfRenderedCss; }
}
