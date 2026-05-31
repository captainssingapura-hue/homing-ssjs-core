package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.js.homing.core.Widget;

import java.util.Objects;

/**
 * Identity + parameters + location for a single widget instance in a
 * persisted workspace. The redesign unified linkage (where the widget
 * lives) with identity (what kind it is and how it was constructed) —
 * each {@code WidgetInstance} names its own {@link WidgetLocation}, so
 * there's one source of truth per widget instead of cross-referenced
 * maps that could desynchronise.
 *
 * <h2>Strongly-typed all the way down</h2>
 *
 * <p>Per the {@code Names Are Types} doctrine, every field is a typed
 * record: {@link WidgetInstanceId} for identity, {@link WidgetKind} for
 * the registry-key kind, {@link Widget._Param} for the widget's typed
 * constructor parameters, {@link WidgetTitle} for the cached display
 * label, {@link WidgetLocation} for the sealed-ADT location.</p>
 *
 * <p>The {@code Widget._Param} field is the framework's typed parameter
 * marker — each concrete widget declares its own {@code Params} record
 * implementing this interface. The framework holds the typed value
 * directly; serialisation happens at the store boundary (Cycle 4) by
 * walking the record's components reflectively, not by interpreting a
 * pre-serialised {@code Map<String, Object>}. The type itself carries
 * the data structure — no JSON intermediate needed.</p>
 *
 * @param id       stable identity assigned at construction time
 * @param kind     registry-key naming what kind of widget this is
 * @param params   the widget's typed {@code Params} record
 *                 (an instance of {@link Widget._Param})
 * @param title    last-known display label
 * @param location where the widget currently lives (pane / modal)
 * @since RFC 0029 cycle 1
 */
public record WidgetInstance(
        WidgetInstanceId id,
        WidgetKind       kind,
        Widget._Param    params,
        WidgetTitle      title,
        WidgetLocation   location
) {

    public WidgetInstance {
        Objects.requireNonNull(id,       "WidgetInstance.id");
        Objects.requireNonNull(kind,     "WidgetInstance.kind");
        Objects.requireNonNull(params,   "WidgetInstance.params");
        Objects.requireNonNull(title,    "WidgetInstance.title");
        Objects.requireNonNull(location, "WidgetInstance.location");
    }
}
