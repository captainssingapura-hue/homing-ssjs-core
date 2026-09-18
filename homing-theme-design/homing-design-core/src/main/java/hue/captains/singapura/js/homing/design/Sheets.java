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

    /** Every target sheet, keyed by target token, in tree order; only targets the resolution reaches. */
    public static Map<String, String> targetSheets(Deployment.Resolution resolution) {
        var byTarget = new LinkedHashMap<Target, List<Map.Entry<DesignClass<?>, Impl>>>();
        for (Target t : Target.leaves()) byTarget.put(t, new ArrayList<>());
        resolution.impls().forEach((pair, impl) -> { if (impl instanceof Impl.Silence) return; byTarget.get(pair.targetLeaf()).add(Map.entry(pair, impl)); });
        var out = new LinkedHashMap<String, String>();
        byTarget.forEach((target, entries) -> {
            if (entries.isEmpty() || target.carrier() != Carrier.CSS) return;
            var sb = new StringBuilder("/* ").append(target.token()).append(" — generated; the template is the target's, the values the design's */\n");
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
            sb.append("    ").append(p).append(": var(").append(variable(dc, p, State.REST)).append(");\n");
        slots.forEach((state, props) -> {
            if (state == State.REST) return;
            sb.append("    ").append(state.selector()).append(" {\n");
            for (String p : props)
                sb.append("        ").append(p).append(": var(").append(variable(dc, p, state))
                  .append(rest.containsKey(p) || rest.containsKey(Impl.Bindings.SOLE) ? ", var(" + variable(dc, p, State.REST) + ")" : "")
                  .append(");\n");
            sb.append("    }\n");
        });
        return sb.append("}\n").toString();
    }

    /** The root sheet: the design's bindings as custom properties, per mode. */
    public static String rootSheet(Deployment.Resolution resolution) {
        var perMode = new EnumMap<Mode, Map<String, String>>(Mode.class);
        resolution.impls().forEach((dc, impl) -> {
            if (!(impl instanceof Impl.Bindings b)) return;
            b.values().forEach((mode, states) -> states.forEach((state, props) -> props.forEach((p, v) ->
                    perMode.computeIfAbsent(mode, m -> new TreeMap<>()).put(variable(dc, property(dc, p), state), v))));
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
