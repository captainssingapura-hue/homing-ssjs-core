package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.tao.ontology.Scalar;

/**
 * Downstream-supplied metadata that {@link AppHtmlGetAction} threads into the
 * served HTML envelope — currently the brand label that follows each app's
 * title in the document {@code <title>}.
 *
 * <p>{@code homing-server} has no dependency on {@code homing-studio-base},
 * so the bootstrap that knows about {@code StudioBrand} is responsible for
 * adapting it into this small record. Studios without a brand pass
 * {@link #DEFAULT}; the action falls back to a generic label.</p>
 *
 * @param label brand label shown in the browser tab. Never {@code null} —
 *              an empty studio constructs {@link #DEFAULT}.
 */
public record AppMeta(String label) implements Scalar {

    /** Sentinel default for studios that don't register a brand. */
    public static final AppMeta DEFAULT = new AppMeta("Homing");

    public AppMeta {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("AppMeta.label must not be blank");
        }
    }
}
