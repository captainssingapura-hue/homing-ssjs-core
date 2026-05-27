package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.js.homing.core.Widget;

/**
 * The observable surface of a single widget instance in a running
 * workspace — what {@link WorkspaceCapture} reads to build a
 * {@link WidgetInstance} for persistence.
 *
 * <p>This is the read-only view-shape, parallel to {@link LiveWorkspace}.
 * Live widgets implement this interface so the capture pass can walk
 * them generically; mock implementations (records, in tests) exercise
 * the capture logic without a DOM.</p>
 *
 * <p>Per the {@code Names Are Types} doctrine, every accessor returns a
 * typed record — no raw {@code String} or {@code UUID} ever escapes
 * across the contract.</p>
 *
 * @since RFC 0029 cycle 2
 */
public interface LiveWidget {

    /** Stable identity assigned at widget construction time. */
    WidgetInstanceId id();

    /** Registry-key naming what kind of widget this is. */
    WidgetKind kind();

    /** The widget's typed {@code Params} record (the type carries the data structure). */
    Widget._Param params();

    /** Current display label. */
    WidgetTitle title();

    /** Where the widget currently lives (pane / modal). */
    WidgetLocation location();
}
