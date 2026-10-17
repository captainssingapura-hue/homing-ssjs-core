package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0064, D25 — the one place a stylesheet link is made, shared by both owners of the page's
 * sheets: the {@link CssClassManager}, which loads provided groups once, and {@link ThemeSheets},
 * which loads derived sheets under the theme worn and swaps them on a switch.
 */
public record CssLinks() implements EsModule<CssLinks> {

    public static final CssLinks INSTANCE = new CssLinks();

    /** The link factory: {@code CssLink.append(href)}. */
    public record CssLink() implements Exportable._Class<CssLinks> {}

    @Override
    public ImportsFor<CssLinks> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CssLinks> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new CssLink()));
    }
}
