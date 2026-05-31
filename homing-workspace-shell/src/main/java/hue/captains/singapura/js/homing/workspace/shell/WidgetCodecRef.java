package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;

import java.util.Objects;

/**
 * Reference to one widget kind's Params codec JS module. The substrate
 * dynamic-imports the module and registers its export as the codec for
 * {@code widgetKind} in {@code WidgetParamsCodecRegistry} before
 * persistence attach.
 *
 * <p>Widget kinds without a {@code WidgetCodecRef} get the substrate's
 * identity codec ({@code transformTo}/{@code transformFrom} both
 * return empty objects) — only widgets that need real param round-trip
 * have to declare one.</p>
 *
 * <p>Wire shape:</p>
 * <pre>{@code
 * {
 *   "widgetKind": "MovingAnimalWidget",
 *   "moduleUrl":  "/module?class=...MovingAnimalParamsCodecModule",
 *   "exportName": "MovingAnimalParamsCodec"
 * }
 * }</pre>
 *
 * @param widgetKind  the widget's {@code simpleName} (matches
 *                    {@code WidgetEntry.simpleName} on the wire)
 * @param module      JS module that exports the codec
 * @param exportName  name of the codec export on the module
 *                    (an object with {@code transformTo} / {@code transformFrom})
 * @since post-RFC-0034 workspace chrome decomposition
 */
public record WidgetCodecRef(String widgetKind, DomModule<?> module, String exportName) {
    public WidgetCodecRef {
        Objects.requireNonNull(widgetKind, "WidgetCodecRef.widgetKind");
        Objects.requireNonNull(module,     "WidgetCodecRef.module");
        Objects.requireNonNull(exportName, "WidgetCodecRef.exportName");
        if (widgetKind.isBlank())
            throw new IllegalArgumentException("WidgetCodecRef.widgetKind must not be blank");
        if (exportName.isBlank())
            throw new IllegalArgumentException("WidgetCodecRef.exportName must not be blank");
    }

    /** Convenience factory matching the typical fluent shape. */
    public static WidgetCodecRef of(String widgetKind, DomModule<?> module, String exportName) {
        return new WidgetCodecRef(widgetKind, module, exportName);
    }
}
