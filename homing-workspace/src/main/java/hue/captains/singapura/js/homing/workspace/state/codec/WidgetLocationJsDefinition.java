package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.DefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.WidgetLocation;

/**
 * Hand-written {@link DefinitionCodeGen} for the {@link WidgetLocation}
 * sealed hierarchy — the second concrete codec in the POC, exercising
 * <b>sealed-of-records</b> as a structural case beyond the
 * single-field-wrapper case {@code PaneId} covered.
 *
 * <p>The JS layout mirrors the Java structure: a namespace object
 * {@code WidgetLocation} holding the two variant classes ({@code InPane},
 * {@code InModal}) as members. {@code new WidgetLocation.InPane(...)} is
 * the natural construction; {@code instanceof WidgetLocation.InPane}
 * is the natural type check.</p>
 *
 * <p>The {@code InPane} variant carries a {@code paneId} of type
 * {@code PaneId} — codec composition is exercised here: the validation
 * uses {@code instanceof PaneId}, and the wire-form codec
 * ({@link WidgetLocationJsFunctions}) calls into {@code PaneIdCodec}
 * for that field.</p>
 *
 * <p>Per {@code CodeGen as Functions}, this hand-written implementation
 * is interchangeable with any future automated generator at the
 * interface level.</p>
 *
 * @since codec POC — sealed-ADT case
 */
public final class WidgetLocationJsDefinition implements DefinitionCodeGen {

    public static final WidgetLocationJsDefinition INSTANCE = new WidgetLocationJsDefinition();

    private WidgetLocationJsDefinition() {}

    @Override
    public String generate(ObjectDefinition<?> definition) {
        if (definition.type() != WidgetLocation.class) {
            throw new IllegalArgumentException(
                    "WidgetLocationJsDefinition only handles WidgetLocation; got: " + definition.simpleName());
        }
        return SOURCE;
    }

    /**
     * Hand-written ECMAScript namespace + variant classes for
     * {@link WidgetLocation}. Mirrors the Java sealed structure:
     * two variants accessed via {@code WidgetLocation.InPane} and
     * {@code WidgetLocation.InModal}. Both classes are frozen.
     *
     * <p>The namespace object itself is frozen so consumers cannot add
     * spurious variant kinds at runtime — closest JS equivalent of
     * the Java sealed contract.</p>
     *
     * <p>Depends on the {@code PaneId} class being loaded into the same
     * JS context first (the {@code InPane} constructor uses
     * {@code instanceof PaneId}).</p>
     */
    private static final String SOURCE = """
            const WidgetLocation = {
                InPane: class InPane {
                    constructor(paneId, tabIndex, isActive) {
                        if (!(paneId instanceof PaneId)) {
                            throw new TypeError(
                                "WidgetLocation.InPane.paneId: expected PaneId instance");
                        }
                        if (typeof tabIndex !== 'number' || !Number.isInteger(tabIndex) || tabIndex < 0) {
                            throw new RangeError(
                                "WidgetLocation.InPane.tabIndex: non-negative integer required, got " + tabIndex);
                        }
                        if (typeof isActive !== 'boolean') {
                            throw new TypeError(
                                "WidgetLocation.InPane.isActive: expected boolean, got " + typeof isActive);
                        }
                        this.paneId   = paneId;
                        this.tabIndex = tabIndex;
                        this.isActive = isActive;
                        Object.freeze(this);
                    }
                },
                InModal: class InModal {
                    constructor() {
                        Object.freeze(this);
                    }
                }
            };
            Object.freeze(WidgetLocation);
            """;
}
