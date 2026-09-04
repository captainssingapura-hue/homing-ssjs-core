package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.server.HrefManager;
import hue.captains.singapura.js.homing.studio.base.theme.ThemePicker;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * Shared view-element builders used by every studio AppModule. Each export
 * is a JS function that takes a props object and returns a {@code Node} —
 * the consumer composes these instead of authoring HTML strings.
 *
 * <p>This is the pre-RFC-0003 shim that lets studio modules honour the
 * Pure-Component Views and Owned References doctrines today, before the
 * {@code Component<C>} / {@code ComponentImpl<C, TH>} primitive lands.
 * When RFC 0003 ships, these functions become the v0 of the studio's
 * {@code Component} library; the call sites keep working with minimal
 * change because the shape (function-of-props returning DOM) is the same
 * surface the consumer sees.</p>
 *
 * <p>Exports:</p>
 * <ul>
 *   <li>{@link Header} — page header with brand + breadcrumbs.</li>
 *   <li>{@link Brand} — clickable brand link (used inside Header).</li>
 *   <li>{@link Card} — title + summary + badge + open-link card; for doc browser, doctrine catalogue, etc.</li>
 *   <li>{@link Pill} — icon + label + desc launcher tile; for studio catalogue, journeys catalogue.</li>
 *   <li>{@link Section} — labelled grid section ("Apps", "Plans", "All doctrines", …).</li>
 *   <li>{@link Footer} — page footer (consumer composes its own children inside).</li>
 * </ul>
 */
public record StudioElements() implements DomModule<StudioElements> {

    public record Header()          implements Exportable._Constant<StudioElements> {}
    public record Brand()           implements Exportable._Constant<StudioElements> {}
    public record Card()            implements Exportable._Constant<StudioElements> {}
    public record Pill()            implements Exportable._Constant<StudioElements> {}
    public record NavLink()         implements Exportable._Constant<StudioElements> {}
    public record Section()         implements Exportable._Constant<StudioElements> {}
    /** Vertical list container — counterpart to {@link Section} for prose-like rows (objectives, acceptance, decisions). */
    public record Listing()         implements Exportable._Constant<StudioElements> {}
    /** A row inside a {@link Listing}: optional left-marker, label, description, optional href. */
    public record ListItem()        implements Exportable._Constant<StudioElements> {}
    public record Footer()          implements Exportable._Constant<StudioElements> {}
    /** Tracker-shaped builders — used by PlanAppModule's auto-generated JS. */
    public record StatusBadge()     implements Exportable._Constant<StudioElements> {}
    public record OverallProgress() implements Exportable._Constant<StudioElements> {}
    public record StepCard()        implements Exportable._Constant<StudioElements> {}
    public record DecisionCard()    implements Exportable._Constant<StudioElements> {}
    public record TodoList()        implements Exportable._Constant<StudioElements> {}
    public record Panel()           implements Exportable._Constant<StudioElements> {}
    public record MetricsTable()    implements Exportable._Constant<StudioElements> {}

    public static final StudioElements INSTANCE = new StudioElements();

    @Override
    public ImportsFor<StudioElements> imports() {
        return ImportsFor.<StudioElements>builder()
                // HrefManager is normally auto-injected for modules that import an
                // AppLink, but StudioElements doesn't navigate to any specific app
                // — it just sets href on whatever URL the consumer passes. Import
                // explicitly so the JS body can use HrefManagerInstance / href.
                .add(new ModuleImports<>(List.of(new ThemePicker.mountThemePickerButton()),
                        ThemePicker.INSTANCE))
                .add(new ModuleImports<>(List.of(new HrefManager.HrefManagerInstance()),
                        HrefManager.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        // Header / Brand chrome
                        new StudioStyles.st_header(),
                        new StudioStyles.st_brand(),
                        new StudioStyles.st_brand_dot(),
                        new StudioStyles.st_brand_logo(),
                        new StudioStyles.st_brand_word(),
                        new StudioStyles.st_breadcrumbs(),
                        new StudioStyles.st_crumb(),
                        new StudioStyles.st_crumb_sep(),
                        // Card chrome
                        new StudioStyles.st_card(),
                        new StudioStyles.st_card_title(),
                        new StudioStyles.st_card_summary(),
                        new StudioStyles.st_card_meta(),
                        new StudioStyles.st_card_link(),
                        new StudioStyles.st_badge(),
                        // Pill chrome
                        new StudioStyles.st_app_pill(),
                        new StudioStyles.st_app_pill_dark(),
                        new StudioStyles.st_app_pill_icon(),
                        new StudioStyles.st_app_pill_label(),
                        new StudioStyles.st_app_pill_desc(),
                        // Section + Listing + Footer
                        new StudioStyles.st_section(),
                        new StudioStyles.st_section_title(),
                        new StudioStyles.st_grid(),
                        new StudioStyles.st_list(),
                        new StudioStyles.st_list_item(),
                        new StudioStyles.st_list_item_marker(),
                        new StudioStyles.st_list_item_body(),
                        new StudioStyles.st_list_item_label(),
                        new StudioStyles.st_list_item_desc(),
                        new StudioStyles.st_list_item_met(),
                        new StudioStyles.st_footer(),
                        // Tracker chrome
                        new StudioStyles.st_status_badge(),
                        new StudioStyles.st_status_not_started(),
                        new StudioStyles.st_status_in_progress(),
                        new StudioStyles.st_status_blocked(),
                        new StudioStyles.st_status_done(),
                        new StudioStyles.st_overall_progress(),
                        new StudioStyles.st_overall_bar(),
                        new StudioStyles.st_overall_fill(),
                        new StudioStyles.st_overall_pct(),
                        new StudioStyles.st_step_card(),
                        new StudioStyles.st_step_head(),
                        new StudioStyles.st_step_id(),
                        new StudioStyles.st_step_label(),
                        new StudioStyles.st_step_summary(),
                        new StudioStyles.st_step_progress(),
                        new StudioStyles.st_step_progress_bar(),
                        new StudioStyles.st_step_progress_fill(),
                        new StudioStyles.st_step_meta(),
                        // (st_card_title / st_card_summary already imported above in the Card chrome block)
                        new StudioStyles.st_panel(),
                        new StudioStyles.st_panel_title(),
                        new StudioStyles.st_task_list(),
                        new StudioStyles.st_task_item(),
                        new StudioStyles.st_task_done(),
                        new StudioStyles.st_task_box()
                ), StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<StudioElements> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new Header(), new Brand(), new Card(), new Pill(), new NavLink(),
                new Section(), new Listing(), new ListItem(), new Footer(),
                new StatusBadge(), new OverallProgress(), new StepCard(), new DecisionCard(),
                new TodoList(), new Panel(), new MetricsTable()
        ));
    }
}
