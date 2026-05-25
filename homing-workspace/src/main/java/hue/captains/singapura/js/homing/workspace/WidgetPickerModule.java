package hue.captains.singapura.js.homing.workspace;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * RFC 0025 Ext1b Mechanism 2 — the widget picker UI.
 *
 * <p>Vends the {@code WidgetPicker} JS class: an <b>ephemeral, stateless</b>
 * tile grid + optional params form that renders into a caller-supplied
 * host element. One instance per "add widget" intent; goes out of scope
 * after pick or cancel. Holds no state, owns no branches, knows nothing
 * about lifecycle — pure UI.</p>
 *
 * <h2>Wiring contract</h2>
 *
 * <p>The chrome creates a fresh picker per add-intent and mounts it into
 * any host element it controls (typically the content area of an empty
 * tab spawned by {@code MultiTabPane.onAddTab}; could also be a modal
 * body or a sidebar):</p>
 *
 * <pre>{@code
 * const picker = new WidgetPicker({
 *     entries     : pickerEntries,           // already filtered (PINNED removed)
 *     disabledIds : { "MyWidget": true },    // optional — greys those tiles
 *     onPick      : function (entry, params) { ... },
 *     onCancel    : function () { ... }
 * });
 * picker.mountInto(hostEl);
 * }</pre>
 *
 * <p>Entry data comes from {@link WidgetEntriesJson} (a JSON array
 * emitted into the page by {@link WorkspaceShell#widgetEntriesJson()} —
 * one entry per registered {@link WidgetEntry} with display metadata,
 * lifecycle hint, dynamic-import module URL, and Params record schema).
 * The chrome filters PINNED entries out of {@code pickerEntries} (they
 * auto-spawn at boot, never user-addable).</p>
 *
 * <h2>onPick semantics</h2>
 *
 * <ul>
 *   <li>{@code onPick(entry, params)} with a non-null {@code params}:
 *       user confirmed a widget; {@code params} is the value map (empty
 *       object for paramless widgets, or values from the form).</li>
 *   <li>{@code onPick(entry, null)}: user clicked a disabled tile (e.g.
 *       SINGLETON already-open). Caller decides what to do —
 *       typically focus the existing instance.</li>
 *   <li>{@code onCancel()}: user clicked Cancel in the params form.
 *       Picker self-dismisses; caller cleans up its host (typically
 *       removes the empty picker tab).</li>
 * </ul>
 *
 * <h2>Modal? No.</h2>
 *
 * <p>The picker doesn't construct a Modal anymore. Earlier iterations
 * (b.2a / b.2b) had the picker open its own Modal then stage the
 * picked widget in a second Modal that the user dragged to dock; that
 * flow was retired in b.2c for per-pane "+" + inline rendering. The
 * Modal primitive still earns its keep for the drag controller's
 * chip-detach gesture, just not here.</p>
 *
 * <p>See {@code Rfc0025Ext1Doc.md} Mechanism 2 Decisions section for
 * the full design journey (b.2a → b.2b → b.2c → b.2d).</p>
 *
 * @since RFC 0025 Ext1b — Mechanism 2 (Widget Selector / Picker)
 */
public record WidgetPickerModule() implements DomModule<WidgetPickerModule> {

    /** The single export — the {@code WidgetPicker} JS class. */
    public record WidgetPicker() implements Exportable._Constant<WidgetPickerModule> {}

    public static final WidgetPickerModule INSTANCE = new WidgetPickerModule();

    @Override
    public ImportsFor<WidgetPickerModule> imports() {
        return ImportsFor.<WidgetPickerModule>builder()
                .add(new ModuleImports<>(
                        List.of(
                                new WidgetPickerStyles.hwp_grid(),
                                new WidgetPickerStyles.hwp_group_label(),
                                new WidgetPickerStyles.hwp_tile(),
                                new WidgetPickerStyles.hwp_tile_disabled(),
                                new WidgetPickerStyles.hwp_tile_icon(),
                                new WidgetPickerStyles.hwp_tile_label(),
                                new WidgetPickerStyles.hwp_tile_desc(),
                                new WidgetPickerStyles.hwp_form(),
                                new WidgetPickerStyles.hwp_form_row(),
                                new WidgetPickerStyles.hwp_form_label(),
                                new WidgetPickerStyles.hwp_form_input(),
                                new WidgetPickerStyles.hwp_form_actions(),
                                new WidgetPickerStyles.hwp_form_btn(),
                                new WidgetPickerStyles.hwp_form_btn_primary()),
                        WidgetPickerStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WidgetPickerModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new WidgetPicker()));
    }
}
