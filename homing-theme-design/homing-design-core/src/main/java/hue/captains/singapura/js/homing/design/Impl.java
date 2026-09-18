package hue.captains.singapura.js.homing.design;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A fulfilment of one design class by one design — already flattened: an
 * impl never refers to another design class, another design, or a mode of
 * computing; whatever a provider delegated to, this is the result.
 *
 * <p>Sealed by carrier. CSS has three forms: {@link Bindings} into the
 * target's template (the norm), a whole {@link Body} (the edge case), or
 * {@link Silence} (an explicit nothing — a decision, not a gap). Sound and
 * assets have their own forms.</p>
 */
public sealed interface Impl permits Impl.Css, Impl.Audio, Impl.Asset, Impl.Silence {

    Carrier carrier();

    /** Explicit nothing. Counts as fulfilment; renders nothing; valid for any carrier. */
    record Silence(Carrier carrier) implements Impl {
        public static Silence css()   { return new Silence(Carrier.CSS); }
        public static Silence audio() { return new Silence(Carrier.AUDIO); }
        public static Silence asset() { return new Silence(Carrier.ASSET); }
    }

    // ── CSS ───────────────────────────────────────────────────────────────

    sealed interface Css extends Impl permits Bindings, Body {
        @Override default Carrier carrier() { return Carrier.CSS; }
    }

    /**
     * Values for the template's variable slots: per mode, per state, per
     * property. {@link Mode#LIGHT} × {@link State#REST} is the base every other
     * slot falls back to. A property the target does not own, or a state its
     * template does not offer, is refused at resolution — not here, because
     * bindings are built without a class in hand.
     */
    record Bindings(Map<Mode, Map<State, Map<String, String>>> values) implements Css {
        public Bindings { values = deepCopy(values); }

        /** One property (the target owns exactly one), at rest, in light. */
        public static Bindings of(String value) { return new Bindings(Map.of()).at(State.REST, value); }

        public static Bindings none() { return new Bindings(Map.of()); }

        /** Bind the target's single property at a state, in light. */
        public Bindings at(State state, String value) { return in(Mode.LIGHT, state, Bindings.SOLE, value); }

        /** Bind a named property at a state, in light. */
        public Bindings at(State state, String property, String value) { return in(Mode.LIGHT, state, property, value); }

        /** Bind the target's single property at a state, in a mode. */
        public Bindings in(Mode mode, State state, String value) { return in(mode, state, SOLE, value); }

        public Bindings in(Mode mode, State state, String property, String value) {
            var copy = deepCopy(values);
            copy.computeIfAbsent(mode, m -> new EnumMap<>(State.class))
                .computeIfAbsent(state, s -> new LinkedHashMap<>())
                .put(property, value);
            return new Bindings(copy);
        }

        /** The marker for "the target's only property", resolved against the class at render. */
        public static final String SOLE = "*";

        public boolean isEmpty() { return values.isEmpty(); }

        private static Map<Mode, Map<State, Map<String, String>>> deepCopy(Map<Mode, Map<State, Map<String, String>>> in) {
            var out = new EnumMap<Mode, Map<State, Map<String, String>>>(Mode.class);
            in.forEach((m, states) -> {
                var s2 = new EnumMap<State, Map<String, String>>(State.class);
                states.forEach((s, props) -> s2.put(s, new LinkedHashMap<>(props)));
                out.put(m, s2);
            });
            return out;
        }
    }

    /**
     * The whole rule for this class, replacing the template's. Still confined
     * to the target's properties, longhand, nesting only on self ({@code &})
     * and elements — checked at resolution. The escape hatch for a texture, a
     * gradient with stops, an ornament with {@code content:}.
     */
    record Body(String css) implements Css {}

    // ── Audio ─────────────────────────────────────────────────────────────

    /** A cue per state: an opaque spec the audio runtime understands. */
    record Audio(Map<State, String> cues) implements Impl {
        public Audio { cues = Map.copyOf(cues); }
        @Override public Carrier carrier() { return Carrier.AUDIO; }
    }

    // ── Asset ─────────────────────────────────────────────────────────────

    /** A reference the asset runtime resolves: an SVG, an image, by name. */
    record Asset(String ref) implements Impl {
        @Override public Carrier carrier() { return Carrier.ASSET; }
    }
}
