package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;
import hue.captains.singapura.js.homing.studio.base.ui.StudioElements;

import java.util.List;

/**
 * Shared JS renderer for {@link PlanAppHost} (RFC 0005-ext1). Consumes the JSON
 * served by {@link PlanGetAction} — the full plan payload with brand,
 * breadcrumbs, name, summary, all phases, decisions, and acceptance — and emits
 * either the index view or the per-phase detail view based on the URL phase param.
 *
 * @since RFC 0005-ext1
 */
public record PlanHostRenderer() implements DomModule<PlanHostRenderer> {

    public record renderPlanHost() implements Exportable._Constant<PlanHostRenderer> {}

    public static final PlanHostRenderer INSTANCE = new PlanHostRenderer();

    @Override
    public ImportsFor<PlanHostRenderer> imports() {
        return ImportsFor.<PlanHostRenderer>builder()
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(new DocReader.link()), DocReader.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioElements.Header(),
                        new StudioElements.NavLink(),
                        new StudioElements.Card(),
                        new StudioElements.Listing(),
                        new StudioElements.ListItem(),
                        new StudioElements.Footer(),
                        new StudioElements.StatusBadge(),
                        new StudioElements.OverallProgress(),
                        new StudioElements.TodoList(),
                        new StudioElements.MetricsTable(),
                        new StudioElements.Panel()
                ), StudioElements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new StudioStyles.st_root(),
                        new StudioStyles.st_main(),
                        new StudioStyles.st_kicker(),
                        new StudioStyles.st_title(),
                        new StudioStyles.st_subtitle(),
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_grid(),
                        new StudioStyles.st_loading(),
                        new StudioStyles.st_error()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<PlanHostRenderer> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new renderPlanHost()));
    }
}
