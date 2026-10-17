package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0064, D25 — the theme's owner: the derived sheets, and the switch. A provided group is the
 * same in every theme and the {@link CssClassManager} loads it once; what varies is derived - a
 * design's words, the house's look - and is loaded here under the theme worn, the steward's answer
 * else the server's default, and fetched again on a switch, flipped in one pass, the old theme's
 * sheets retired. It follows the store, so another tab's pick reaches this one.
 */
public record ThemeSheets() implements EsModule<ThemeSheets> {

    public static final ThemeSheets INSTANCE = new ThemeSheets();

    public record ThemeSheetsInstance() implements Exportable._Constant<ThemeSheets> {}

    @Override
    public ImportsFor<ThemeSheets> imports() {
        return ImportsFor.<ThemeSheets>builder()
                .add(new ModuleImports<>(List.of(new CssDependencyGraph.createCssDependencyGraph()), CssDependencyGraph.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssLoadProcedure.createCssLoadProcedure()), CssLoadProcedure.INSTANCE))
                .add(new ModuleImports<>(List.of(new CssLinks.CssLink()), CssLinks.INSTANCE))
                .add(new ModuleImports<>(List.of(new PreferenceSteward.PreferenceViewInstance()), PreferenceSteward.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<ThemeSheets> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ThemeSheetsInstance()));
    }
}
