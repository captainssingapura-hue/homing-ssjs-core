// =============================================================================
// WidgetParamsCodecRegistry — JS-side polymorphism boundary for the
// Widget._Param hierarchy. Each widget kind that participates in workspace
// state persistence registers its Params codec under its WidgetKind value;
// the WidgetInstance codec looks up the right codec by the kind
// discriminator on encode + decode paths.
//
// RFC 0035 P3 — refactored from `Object.freeze({...})` IIFE to class form
// with static methods. The registry is logically a singleton; expressing
// it as a class with static members keeps the storage encapsulated as a
// static class field (no top-level `const codecs` to collide).
//
// The registration step is the Java→JS analog of "the workspace declares
// what widget kinds it knows about" — a downstream consumer registers its
// own widget kinds explicitly. The framework refuses implicit discovery
// (per Codegen Over Reflection): the registry is populated by explicit
// register() calls, never by classpath scanning or runtime introspection.
//
// Doctrine alignment:
//   - CodeGen as Functions — registered codecs are Functional Objects
//     (transformTo / transformFrom pairs). Provenance (hand-written vs
//     generated) is invisible.
//   - Names Are Types — the registry keys are WidgetKind value strings,
//     never raw arbitrary identifiers. (The .value extraction happens at
//     the boundary; the registry uses the underlying string as Map key
//     for native Map performance — this is the JSON-object-keys exception
//     to the Names-Are-Types doctrine, same as the workspace state's
//     widgetsById serialisation.)
// =============================================================================

class WidgetParamsCodecRegistry {

    /** widgetKind.value (string) → { transformTo, transformFrom } */
    static #codecs = new Map();

    /** Singletons-by-static — no instances. */
    constructor() {
        throw new TypeError("WidgetParamsCodecRegistry is a singleton — use static methods");
    }

    /**
     * Register a Params codec under the given widget kind value.
     * The codec must satisfy the TransformationFunctions contract:
     * an object with transformTo(value) and transformFrom(wire).
     * Subsequent registrations under the same kind value overwrite.
     */
    static register(widgetKindValue, codec) {
        if (typeof widgetKindValue !== "string" || widgetKindValue.length === 0) {
            throw new TypeError(
                "WidgetParamsCodecRegistry.register: widgetKindValue must be a non-empty string");
        }
        if (!codec
            || typeof codec.transformTo   !== "function"
            || typeof codec.transformFrom !== "function") {
            throw new TypeError(
                "WidgetParamsCodecRegistry.register: codec must have transformTo + transformFrom methods");
        }
        WidgetParamsCodecRegistry.#codecs.set(widgetKindValue, codec);
    }

    /**
     * Look up the codec for the given widget kind value. Throws if
     * no codec is registered — fails loudly rather than silently
     * dropping a widget's params at save time.
     */
    static get(widgetKindValue) {
        const c = WidgetParamsCodecRegistry.#codecs.get(widgetKindValue);
        if (!c) {
            throw new Error(
                "WidgetParamsCodecRegistry: no codec registered for widget kind '"
                + widgetKindValue + "'");
        }
        return c;
    }

    /**
     * True if a codec is registered for the given widget kind.
     * Used by tests and by the future MissingWidgetPlaceholder
     * (RFC 0029 Cycle 7) to dispatch on whether a saved widget's
     * kind is still known.
     */
    static has(widgetKindValue) {
        return WidgetParamsCodecRegistry.#codecs.has(widgetKindValue);
    }

    /** Reset — for tests; not exposed in production wiring. */
    static _clear() {
        WidgetParamsCodecRegistry.#codecs.clear();
    }
}
