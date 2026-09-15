package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.CssClassManager;

import java.util.List;

/**
 * RFC 0064 — the CSS dependency graph seen from inside: every group the
 * page has loaded as a node in columns by wave — the order the manager's
 * plan would load or switch them in — with its dependencies, its badges
 * (a prior; named but never declared) and its sheets per theme. A plan bar
 * asks the manager what a switch would do without doing it, and can run it.
 *
 * <p>Draws from {@code snapshot()} and {@code plan()} — frozen data — and
 * refreshes on {@code onThemeApplied}; the one mutator it reaches is
 * {@code switchTheme}, behind a deliberate button.</p>
 */
public record CssGraphRendererModule() implements DomModule<CssGraphRendererModule> {

    public record renderCssGraph() implements Exportable._Constant<CssGraphRendererModule> {}

    public static final CssGraphRendererModule INSTANCE = new CssGraphRendererModule();

    @Override
    public ImportsFor<CssGraphRendererModule> imports() {
        return ImportsFor.<CssGraphRendererModule>builder()
                .add(new ModuleImports<>(List.of(new CssClassManager.CssClassManagerInstance()),
                        CssClassManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new CssGraphStyles.cg_root(), new CssGraphStyles.cg_head(), new CssGraphStyles.cg_title(),
                        new CssGraphStyles.cg_worn(), new CssGraphStyles.cg_btn(), new CssGraphStyles.cg_bar(),
                        new CssGraphStyles.cg_select(), new CssGraphStyles.cg_note(), new CssGraphStyles.cg_note_err(),
                        new CssGraphStyles.cg_waves(), new CssGraphStyles.cg_wave(), new CssGraphStyles.cg_wave_head(),
                        new CssGraphStyles.cg_node(), new CssGraphStyles.cg_node_id(), new CssGraphStyles.cg_badge(),
                        new CssGraphStyles.cg_badge_prior(), new CssGraphStyles.cg_badge_unknown(),
                        new CssGraphStyles.cg_deps(), new CssGraphStyles.cg_sheets(), new CssGraphStyles.cg_sheet_pending()
                ), CssGraphStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CssGraphRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderCssGraph()));
    }
}
