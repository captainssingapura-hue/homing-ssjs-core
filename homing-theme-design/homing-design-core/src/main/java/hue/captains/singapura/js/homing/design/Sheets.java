package hue.captains.singapura.js.homing.design;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The served CSS, generated from a {@link Deployment.Resolution}: one sheet
 * per physical target the closure reaches, and one root sheet of the design's
 * bindings. A design wrote none of it; the template is the target's and the
 * values are the design's, joined by variable names nobody typed.
 *
 * <p>The template for a class: one rule on its token, with each bound
 * property reading its variable, and each bound non-rest state nested on the
 * state's selector, falling back to the rest variable. A {@link Impl.Body} is
 * emitted verbatim inside the class's rule. {@link Impl.Silence} emits
 * nothing. Only what the design bound is emitted — the sheet is per design,
 * so there is nothing to leave a slot open for.</p>
 *
 * <p>The root sheet is {@code :root { --… }} for {@link Mode#LIGHT}, and one
 * {@code @media} block per other mode the design bound anything under.</p>
 */
public final class Sheets {

    private Sheets() {}

    /**
     * The order of rules within a target sheet — the one thing that decides
     * which of two classes on one element, on one target, wins, since they
     * are equal in specificity. A component is free to wear two (a base and a
     * look beside it), so the order must be fixed and known: by the
     * semantic's branch, the thing an element <i>is</i> before what it
     * <i>says</i> before what it <i>does</i> —
     * {@code Layer < Text < Box < Brand < Structure < Emphasis < Feedback < Pairing < Interaction}
     * — then by leaf name. A branch this list does not know sorts between
     * Structure and Emphasis, by its name. Same deployment, same sheet, every
     * start.
     */
    static final List<Class<?>> PRECEDENCE = List.of(
            Layer.class, Text.class, Box.class, Brand.class, Structure.class,
            Emphasis.class, Feedback.class, Pairing.class, Interaction.class);

    static int precedence(Class<?> branch) {
        int i = PRECEDENCE.indexOf(branch);
        return i >= 0 ? i * 2 : PRECEDENCE.indexOf(Structure.class) * 2 + 1;   // unknown: after Structure, before Emphasis
    }

    static final java.util.Comparator<DesignClass<?>> BY_PRECEDENCE = java.util.Comparator
            .<DesignClass<?>>comparingInt(dc -> precedence(Trees.branchOf(dc.semantic())))
            .thenComparing(dc -> Trees.branchOf(dc.semantic()).getSimpleName())
            .thenComparing(dc -> dc.semantic().getSimpleName());

    /** Every target sheet, keyed by target token, in tree order; only targets the resolution reaches; rules in {@link #PRECEDENCE}. */
    public static Map<String, String> targetSheets(Deployment.Resolution resolution) {
        var byTarget = new LinkedHashMap<Target, List<Map.Entry<DesignClass<?>, Impl>>>();
        for (Target t : Target.leaves()) byTarget.put(t, new ArrayList<>());
        resolution.impls().forEach((pair, impl) -> { if (impl instanceof Impl.Silence) return; byTarget.get(pair.targetLeaf()).add(Map.entry(pair, impl)); });
        var out = new LinkedHashMap<String, String>();
        byTarget.forEach((target, entries) -> {
            if (entries.isEmpty() || target.carrier() != Carrier.CSS) return;
            entries.sort(Map.Entry.comparingByKey(BY_PRECEDENCE));
            var sb = new StringBuilder("/* ").append(target.token()).append(" — generated; the template is the target's, the values the design's */\n");
            if (entries.stream().anyMatch(e -> scales(e.getValue()))) sb.append(Extent.PROPERTY).append('\n');
            for (var e : entries) sb.append(rule(e.getKey(), e.getValue()));
            out.put(target.token(), sb.toString());
        });
        return out;
    }

    /** One class's rule. */
    public static String rule(DesignClass<?> dc, Impl impl) {
        return switch (impl) {
            case Impl.Bindings b -> template(dc, b);
            case Impl.Body body -> "." + dc.cssName() + " {\n" + indent(body.css()) + "}\n";
            default -> "";
        };
    }

    private static String template(DesignClass<?> dc, Impl.Bindings b) {
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        // every (state, property) bound in any mode gets a slot; the value comes from :root
        var slots = new EnumMap<State, java.util.Set<String>>(State.class);
        b.values().forEach((mode, states) -> states.forEach((state, props) ->
                props.keySet().forEach(p -> slots.computeIfAbsent(state, s -> new java.util.TreeSet<>()).add(property(dc, p)))));
        if (slots.isEmpty()) return "";
        var sb = new StringBuilder(".").append(dc.cssName()).append(" {\n");
        for (String p : slots.getOrDefault(State.REST, java.util.Set.of()))
            sb.append("    ").append(p).append(": ").append(value(dc, b, p, State.REST, false)).append(";\n");
        slots.forEach((state, props) -> {
            if (state == State.REST) return;
            sb.append("    ").append(state.selector()).append(" {\n");
            for (String p : props)
                sb.append("        ").append(p).append(": ")
                  .append(value(dc, b, p, state, rest.containsKey(p) || rest.containsKey(Impl.Bindings.SOLE))).append(";\n");
            sb.append("    }\n");
        });
        return sb.append("}\n").toString();
    }

    /** Whether any property of the word is anchored at both ends — and so renders as an interpolation. */
    static boolean scales(Impl impl) {
        if (!(impl instanceof Impl.Bindings b)) return false;
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        return rest.keySet().stream().anyMatch(k -> b.anchored(Extent.ZERO, k) && b.anchored(Extent.NEG, k));
    }

    /**
     * One property's value in the rule: the variable, falling back to rest for
     * a state — or, for a property anchored at zero and at −1, the
     * interpolation the element's extent drives: the pole by the sign of the
     * extent, then from the neutral by its magnitude, in oklab — rectangular, so
     * a low-chroma neutral and a saturated pole meet along a straight line and
     * the way between them never turns through a third hue. The pole is
     * chosen first so a colour at −½ is the negative pole at half strength,
     * never a hue between the two poles.
     */
    private static String value(DesignClass<?> dc, Impl.Bindings b, String property, State state, boolean fallsBackToRest) {
        String key = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of()).containsKey(Impl.Bindings.SOLE) ? Impl.Bindings.SOLE : property;
        if (!(b.anchored(Extent.ZERO, key) && b.anchored(Extent.NEG, key))) return ref(dc, property, state, Extent.FULL, fallsBackToRest);
        String full = ref(dc, property, state, Extent.FULL, fallsBackToRest);
        String zero = ref(dc, property, state, Extent.ZERO, fallsBackToRest);
        String neg  = ref(dc, property, state, Extent.NEG,  fallsBackToRest);
        return "color-mix(in oklab, " + zero + " calc((1 - abs(var(" + Extent.VAR + "))) * 100%), "
             + "color-mix(in oklab, " + neg + " calc((1 - sign(var(" + Extent.VAR + "))) / 2 * 100%), " + full + "))";
    }

    /** {@code var(--…)} for one anchor at one state, falling back to the rest anchor where the rest binds it. */
    private static String ref(DesignClass<?> dc, String property, State state, Extent extent, boolean fallsBackToRest) {
        String own = variable(dc, property, state) + extent.suffix();
        if (state == State.REST || !fallsBackToRest) return "var(" + own + ")";
        return "var(" + own + ", var(" + variable(dc, property, State.REST) + extent.suffix() + "))";
    }

    /** The root sheet: the design's bindings as custom properties, per mode. */
    public static String rootSheet(Deployment.Resolution resolution) {
        var perMode = new EnumMap<Mode, Map<String, String>>(Mode.class);
        resolution.impls().forEach((dc, impl) -> {
            if (!(impl instanceof Impl.Bindings b)) return;
            for (Extent extent : Extent.values())
                b.anchors(extent).forEach((mode, states) -> states.forEach((state, props) -> props.forEach((p, v) ->
                        perMode.computeIfAbsent(mode, m -> new TreeMap<>()).put(variable(dc, property(dc, p), state) + extent.suffix(), v))));
        });
        var sb = new StringBuilder();
        perMode.forEach((mode, vars) -> {
            String open = mode == Mode.LIGHT ? "" : "@media " + mode.media() + " {\n";
            String pad = mode == Mode.LIGHT ? "" : "    ";
            sb.append(open).append(pad).append(":root {\n");
            vars.forEach((k, v) -> sb.append(pad).append("    ").append(k).append(": ").append(v).append(";\n"));
            sb.append(pad).append("}\n").append(mode == Mode.LIGHT ? "" : "}\n");
        });
        return sb.toString();
    }

    // ── names ─────────────────────────────────────────────────────────────

    /** The real property for a binding key — {@link Impl.Bindings#SOLE} resolves to the target's one property. */
    static String property(DesignClass<?> dc, String key) {
        return key.equals(Impl.Bindings.SOLE) ? dc.targetLeaf().properties().iterator().next() : key;
    }

    /** {@code --<token>[-<property>][-<state>]}. */
    static String variable(DesignClass<?> dc, String property, State state) {
        return Trees.variable(dc, property) + state.suffix();
    }

    private static String indent(String css) {
        var sb = new StringBuilder();
        for (String line : css.strip().split("\n")) sb.append("    ").append(line.strip()).append('\n');
        return sb.toString();
    }
}
