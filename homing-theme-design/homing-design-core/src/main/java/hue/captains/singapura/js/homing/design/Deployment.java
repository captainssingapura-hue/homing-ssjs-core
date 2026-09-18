package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An application's requirement set — the design classes its components wear
 * — against one design and its extensions. Resolves every required pair to a
 * flat {@link Impl} and reports what does not resolve. This is where
 * completeness lives: over the set, never over a design's idea of itself.
 *
 * <p>Resolution of a pair: the design's answer, or exactly one extension's
 * (a second is a {@link Finding.Kind#DOUBLE}); none is
 * {@link Finding.Kind#MISSING}. Every answer is validated against the pair's
 * target: carrier, properties, states, and — for a body — that it writes only
 * the target's properties and nests only on self and elements.</p>
 *
 * @param required   the design classes the closure wears
 * @param design     the design to fulfil them
 * @param extensions the products' fulfilments of their own classes
 */
public record Deployment(Set<DesignClass<?>> required, Design design, List<DesignExtension> extensions) {

    public Deployment { required = Set.copyOf(required); extensions = List.copyOf(extensions); }

    public static Deployment of(Set<DesignClass<?>> required, Design design) {
        return new Deployment(required, design, List.of());
    }

    /** The requirement set of a closure: every pair any class of any group in it wears. */
    public static Set<DesignClass<?>> wornBy(Collection<? extends CssGroup<?>> groups) {
        var out = new LinkedHashSet<DesignClass<?>>();
        for (CssGroup<?> g : groups)
            for (CssClass<?> c : g.cssClasses())
                for (Wearable w : c.wears())
                    if (w instanceof DesignClass<?> dc) out.add(dc);
        return out;
    }

    /** One thing that did not resolve, or resolved wrongly. */
    public record Finding(Kind kind, DesignClass<?> designClass, String detail) {
        public enum Kind { MISSING, DOUBLE, CARRIER_MISMATCH, INVALID_BINDING, INVALID_BODY }
        @Override public String toString() { return kind + " " + (designClass == null ? "" : designClass) + (detail.isBlank() ? "" : " — " + detail); }
    }

    /** Every required pair resolved, in the order required; plus every finding. Complete iff findings is empty. */
    public record Resolution(Map<DesignClass<?>, Impl> impls, List<Finding> findings) {
        public boolean complete() { return findings.isEmpty(); }
    }

    public Resolution resolve() {
        var findings = new ArrayList<Finding>();
        var impls = new LinkedHashMap<DesignClass<?>, Impl>();
        for (var pair : required) {
            Impl own = design.impl(pair);
            var fromExtensions = new ArrayList<Map.Entry<String, Impl>>();
            for (var e : extensions) { Impl i = e.impl(pair); if (i != null) fromExtensions.add(Map.entry(e.slug(), i)); }
            if (own != null && !fromExtensions.isEmpty()) {
                findings.add(new Finding(Finding.Kind.DOUBLE, pair, "answered by " + design.slug() + " and " + fromExtensions.get(0).getKey()));
                continue;
            }
            if (fromExtensions.size() > 1) {
                findings.add(new Finding(Finding.Kind.DOUBLE, pair, "answered by " + fromExtensions.get(0).getKey() + " and " + fromExtensions.get(1).getKey()));
                continue;
            }
            Impl impl = own != null ? own : fromExtensions.isEmpty() ? null : fromExtensions.get(0).getValue();
            if (impl == null) { findings.add(new Finding(Finding.Kind.MISSING, pair, "no answer from " + design.slug())); continue; }
            validate(pair, impl, findings);
            impls.put(pair, impl);
        }
        return new Resolution(Map.copyOf(impls), List.copyOf(findings));
    }

    // ── validation against the target ─────────────────────────────────────

    private static void validate(DesignClass<?> pair, Impl impl, List<Finding> findings) {
        Target target = pair.targetLeaf();
        if (impl.carrier() != target.carrier()) {
            findings.add(new Finding(Finding.Kind.CARRIER_MISMATCH, pair, impl.carrier() + " impl for a " + target.carrier() + " target")); return;
        }
        switch (impl) {
            case Impl.Bindings b -> validateBindings(pair, target, b, findings);
            case Impl.Body body -> validateBody(pair, target, body, findings);
            default -> {}
        }
    }

    private static void validateBindings(DesignClass<?> pair, Target target, Impl.Bindings b, List<Finding> findings) {
        b.values().forEach((mode, states) -> states.forEach((state, props) -> {
            if (!target.states().contains(state))
                findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, target.token() + " offers no slot for state " + state));
            props.keySet().forEach(p -> {
                if (p.equals(Impl.Bindings.SOLE)) {
                    if (target.properties().size() != 1)
                        findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, target.token() + " owns " + target.properties().size() + " properties; name one"));
                } else if (!target.properties().contains(p))
                    findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, target.token() + " does not own " + p));
            });
        }));
    }

    private static final Pattern DECLARATION = Pattern.compile("(?m)^\\s*([a-z-]+)\\s*:");
    private static final Pattern NESTED = Pattern.compile("(?m)^\\s*([^{;\\n]+?)\\s*\\{");

    private static void validateBody(DesignClass<?> pair, Target target, Impl.Body body, List<Finding> findings) {
        Matcher m = DECLARATION.matcher(body.css());
        while (m.find()) if (!target.properties().contains(m.group(1)))
            findings.add(new Finding(Finding.Kind.INVALID_BODY, pair, "body writes " + m.group(1) + ", which " + target.token() + " does not own"));
        Matcher n = NESTED.matcher(body.css());
        while (n.find()) {
            String prelude = n.group(1).trim();
            if (prelude.startsWith("@")) continue;                              // media, supports
            if (prelude.contains(".") || prelude.contains("#"))
                findings.add(new Finding(Finding.Kind.INVALID_BODY, pair, "body nests a class or id selector: " + prelude));
        }
    }
}
