package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.FunctionsCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetLocation;

/**
 * Hand-written {@link FunctionsCodeGen} for the {@link WidgetLocation}
 * sealed hierarchy.
 *
 * <h2>Wire form — tagged union via a {@code kind} discriminator</h2>
 *
 * <p>The wire form for a sealed-of-records is a plain JS object with
 * a {@code kind} field naming the variant, plus the variant's payload
 * inline:</p>
 *
 * <pre>{@code
 *   InPane(PaneId("p1"), 5, true)
 *     →  { kind: "InPane", paneId: "p1", tabIndex: 5, isActive: true }
 *
 *   InModal()
 *     →  { kind: "InModal" }
 * }</pre>
 *
 * <p>This is the canonical tagged-union encoding. The discriminator's
 * field name ({@code "kind"}) is a codec-wide convention; the variant
 * names match the Java class names of the sealed permits.</p>
 *
 * <h2>Codec composition</h2>
 *
 * <p>The {@code paneId} field is a {@link hue.captains.singapura.js.homing.workspace.state.PaneId}
 * — a typed identifier. Encoding routes through {@code PaneIdCodec.transformTo},
 * which yields the underlying string; decoding routes through
 * {@code PaneIdCodec.transformFrom}, which validates the string and
 * constructs a {@code PaneId} instance. <b>Codec composition</b> is
 * the structural insight this POC ratifies — composite codecs delegate
 * to the codecs of their component types, transitively.</p>
 *
 * <p>The decoder validates the discriminator exhaustively; an unknown
 * {@code kind} fails loudly rather than silently producing
 * {@code undefined}.</p>
 *
 * @since codec POC — sealed-ADT case + codec composition
 */
public final class WidgetLocationJsFunctions implements FunctionsCodeGen {

    public static final WidgetLocationJsFunctions INSTANCE = new WidgetLocationJsFunctions();

    private WidgetLocationJsFunctions() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetLocation.class) {
            throw new IllegalArgumentException(
                    "WidgetLocationJsFunctions only handles WidgetLocation; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    /**
     * Hand-written ECMAScript codec for the {@link WidgetLocation}
     * sealed hierarchy. Encoder dispatches by {@code instanceof};
     * decoder dispatches by the {@code kind} discriminator field.
     * Composes with {@code PaneIdCodec} for the {@code paneId} field
     * of the {@code InPane} variant.
     */
    private static final String SOURCE = """
            const WidgetLocationCodec = {
                transformTo(loc) {
                    if (loc instanceof WidgetLocation.InPane) {
                        return {
                            kind:     'InPane',
                            paneId:   PaneIdCodec.transformTo(loc.paneId),
                            tabIndex: loc.tabIndex,
                            isActive: loc.isActive
                        };
                    }
                    if (loc instanceof WidgetLocation.InModal) {
                        return { kind: 'InModal' };
                    }
                    throw new TypeError(
                        "WidgetLocationCodec.transformTo: not a WidgetLocation variant");
                },
                transformFrom(wire) {
                    if (wire == null || typeof wire.kind !== 'string') {
                        throw new TypeError(
                            "WidgetLocationCodec.transformFrom: wire object missing 'kind' discriminator");
                    }
                    switch (wire.kind) {
                        case 'InPane':
                            return new WidgetLocation.InPane(
                                PaneIdCodec.transformFrom(wire.paneId),
                                wire.tabIndex,
                                wire.isActive);
                        case 'InModal':
                            return new WidgetLocation.InModal();
                        default:
                            throw new TypeError(
                                "WidgetLocationCodec.transformFrom: unknown kind '" + wire.kind + "'");
                    }
                }
            };
            """;
}
