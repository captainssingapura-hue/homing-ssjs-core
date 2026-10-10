package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.ContentProvider;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-generates the JS module for a {@link DerivedCss}: it hands the sheet to the theme's owner,
 * {@link ThemeSheets}, which loads it under the theme worn and fetches it again on a switch (RFC
 * 0064, D25) - never to the CSS manager, which loads provided groups once. Each thing the sheet
 * exports, so that a module may import it and have it load, is bound to the sheet's name.
 *
 * <p>{@code defaultTheme} is the deployment's default, the owner's fallback until the address or
 * the stored pick says otherwise; null where the deployment has none.</p>
 */
public record DerivedCssContentProvider<D extends DerivedCss<D>>(
        D sheet, String defaultTheme, ModuleNameResolver nameResolver
) implements ContentProvider<D> {

    @Override
    public List<String> content() {
        String name = sheet.getClass().getCanonicalName();
        var lines = new ArrayList<String>();
        lines.add("import { ThemeSheetsInstance as _themes } from \"" + nameResolver.resolve(ThemeSheets.INSTANCE).basePath() + "\";");
        lines.add("await _themes.load([\"" + name + "\"], " + (defaultTheme != null ? "\"" + defaultTheme + "\"" : "null") + ");");
        for (Exportable<?> e : sheet.exports().exports())
            lines.add("const " + e.getClass().getSimpleName() + " = \"" + name + "\";");
        return lines;
    }
}
