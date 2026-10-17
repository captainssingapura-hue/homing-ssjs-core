package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.ContentProvider;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-generates the JS module for a {@link DerivedCss}: it hands the sheet to the theme's owner,
 * {@link ThemeSheets}, with what it varies with - the theme, the style or the palette - which loads
 * it under the one worn and fetches it again when that one changes (RFC 0064, D25; RFC 0067,
 * appendix Two Classes per Leaf) - never to the CSS manager, which loads provided groups once. Each
 * thing the sheet exports, so that a module may import it and have it load, is bound to the sheet's
 * name.
 *
 * <p>The owner's fallback, until the address or the stored pick says otherwise, is the sheet's own;
 * for a sheet varying with the theme that names none, {@code defaultTheme}, the deployment's - null
 * where the deployment has none.</p>
 */
public record DerivedCssContentProvider<D extends DerivedCss<D>>(
        D sheet, String defaultTheme, ModuleNameResolver nameResolver
) implements ContentProvider<D> {

    @Override
    public List<String> content() {
        String name = sheet.getClass().getCanonicalName();
        var lines = new ArrayList<String>();
        lines.add("import { ThemeSheetsInstance as _themes } from \"" + nameResolver.resolve(ThemeSheets.INSTANCE).basePath() + "\";");
        DerivedCss.VariesWith varies = sheet.variesWith();
        String fallback = sheet.fallback().orElse(varies == DerivedCss.VariesWith.THEME ? defaultTheme : null);
        lines.add("await _themes.load([\"" + name + "\"], " + (fallback != null ? "\"" + fallback + "\"" : "null")
                + ", \"" + varies.param() + "\");");
        for (Exportable<?> e : sheet.exports().exports())
            lines.add("const " + e.getClass().getSimpleName() + " = \"" + name + "\";");
        return lines;
    }
}
