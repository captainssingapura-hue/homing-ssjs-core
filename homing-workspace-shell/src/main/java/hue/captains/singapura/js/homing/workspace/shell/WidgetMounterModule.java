package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code WidgetMounter} — Phase 15 of the workspace-shell chrome.
 * Stateless helper that turns a widget {@code entry} + params into a
 * mounted controller. Encapsulates the dynamic-import +
 * {@code construct(branch, params, workspaceCtx)} + controller-shape
 * validation flow that V1 inlines in three places (picker spawn,
 * pinned spawn, replay restore).
 *
 * <p>Functional Object: single method {@code mount(branch, entry,
 * params, workspaceCtx) → Promise<controller>}. No instance state. The
 * dynamic import is the only side effect; the rest is straight typed
 * dispatch.</p>
 *
 * <p>Used by {@link PinnedTabSpawnerModule} (Phase 14) and will be used
 * by {@code PickerTabFlow} (Phase 13) and {@code ReplayEngine} (Phase 9)
 * as those land.</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 15
 */
public record WidgetMounterModule() implements DomModule<WidgetMounterModule> {

    public static final WidgetMounterModule INSTANCE = new WidgetMounterModule();

    /** The {@code WidgetMounter} JS class. */
    public record WidgetMounter() implements Exportable._Class<WidgetMounterModule> {}

    @Override
    public ImportsFor<WidgetMounterModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<WidgetMounterModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WidgetMounter()));
    }
}
