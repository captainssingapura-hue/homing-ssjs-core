package hue.captains.singapura.js.homing.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An application's requirement set — the union of the design classes its
 * components import — against one design and its extensions. Resolves every
 * required class to a flat {@link Impl} and reports what does not resolve.
 * This is where completeness lives: over the set, never over a design's idea
 * of itself.
 *
 * <p>Resolution of a class: the design's own provider, or an extension's
 * (exactly one of them — two is a {@link Finding.Kind#DOUBLE}); failing
 * both, the base design's, recursively; failing that,
 * {@link Finding.Kind#MISSING}. A lazy provider resolves through the same
 * path with cycle detection. Every resolved impl is validated against its
 * class's target: carrier, properties, states, and — for a body — that it
 * writes only the target's properties and nests only on self and elements.</p>
 *
 * @param required   the design classes the closure reaches
 * @param design     the design to fulfil them
 * @param extensions the products' fulfilments of their own classes
 */
public record Deployment(Set<Class<? extends DesignClass<?, ?>>> required, Design design, List<DesignExtension> extensions) {

    public Deployment { required = Set.copyOf(required); extensions = List.copyOf(extensions); }

    public static Deployment of(Set<Class<? extends DesignClass<?, ?>>> required, Design design) {
        return new Deployment(required, design, List.of());
    }

    /** One thing that did not resolve, or resolved wrongly. */
    public record Finding(Kind kind, Class<?> designClass, String detail) {
        public enum Kind { MISSING, DOUBLE, CARRIER_MISMATCH, INVALID_BINDING, INVALID_BODY, CYCLE, INVALID_CLASS }
        @Override public String toString() { return kind + " " + (designClass == null ? "" : designClass.getName()) + (detail.isBlank() ? "" : " — " + detail); }
    }

    /** Every required class resolved, in the order required; plus every finding. Complete iff findings is empty. */
    public record Resolution(Map<Class<? extends DesignClass<?, ?>>, Impl> impls, List<Finding> findings) {
        public boolean complete() { return findings.isEmpty(); }
    }

    public Resolution resolve() {
        var findings = new ArrayList<Finding>();
        var chain = Chain.of(design, extensions, findings);
        var resolver = new Memo(chain, findings);
        var impls = new LinkedHashMap<Class<? extends DesignClass<?, ?>>, Impl>();
        for (var cls : required) {
            try { Trees.coordinates(cls); }
            catch (IllegalArgumentException e) { findings.add(new Finding(Finding.Kind.INVALID_CLASS, cls, e.getMessage())); continue; }
            Impl impl = resolver.resolve(cls);
            if (impl != null) impls.put(cls, impl);
        }
        return new Resolution(Map.copyOf(impls), List.copyOf(findings));
    }

    // ── the provider chain: design (+ extensions) over its base, over its base … ──

    private record Chain(Map<Class<?>, ImplProvider<?>> own, Chain base) {
        static Chain of(Design d, List<DesignExtension> exts, List<Finding> findings) {
            var own = new HashMap<Class<?>, ImplProvider<?>>();
            var seen = new HashMap<Class<?>, String>();
            for (var p : d.providers()) put(own, seen, p, d.slug(), findings);
            for (var e : exts) for (var p : e.providers()) put(own, seen, p, e.slug(), findings);
            Chain b = d.base().map(bd -> Chain.of(bd, List.of(), findings)).orElse(null);
            return new Chain(own, b);
        }
        private static void put(Map<Class<?>, ImplProvider<?>> own, Map<Class<?>, String> seen, ImplProvider<?> p, String who, List<Finding> findings) {
            String prior = seen.put(p.designClass(), who);
            if (prior != null) findings.add(new Finding(Finding.Kind.DOUBLE, p.designClass(), "fulfilled by " + prior + " and " + who));
            else own.put(p.designClass(), p);
        }
    }

    /** Memoised, cycle-detecting resolution through the chain. */
    private static final class Memo implements ImplProvider.Resolver {
        private final Chain chain; private final List<Finding> findings;
        private final Map<Class<?>, Impl> done = new HashMap<>();
        private final Set<Class<?>> inFlight = new HashSet<>();
        Memo(Chain chain, List<Finding> findings) { this.chain = chain; this.findings = findings; }

        @Override public Impl resolve(Class<? extends DesignClass<?, ?>> cls) { return resolve(cls, chain); }

        @Override public Impl fromBase(Class<? extends DesignClass<?, ?>> cls) { return chain.base == null ? null : resolve(cls, chain.base); }

        private Impl resolve(Class<? extends DesignClass<?, ?>> cls, Chain at) {
            if (at == chain && done.containsKey(cls)) return done.get(cls);
            if (!inFlight.add(cls)) { findings.add(new Finding(Finding.Kind.CYCLE, cls, "a provider resolves to itself")); return null; }
            try {
                Impl impl = null;
                for (Chain c = at; c != null && impl == null; c = c.base) {
                    ImplProvider<?> p = c.own.get(cls);
                    if (p != null) impl = p.provide(this);
                }
                if (impl == null) { if (at == chain) findings.add(new Finding(Finding.Kind.MISSING, cls, "no provider in " + describe())); return null; }
                if (at == chain) { validate(cls, impl); done.put(cls, impl); }
                return impl;
            } finally { inFlight.remove(cls); }
        }

        private String describe() {
            var sb = new StringBuilder(); for (Chain c = chain; c != null; c = c.base) sb.append(sb.isEmpty() ? "" : " > ").append(c.own.size()).append(" providers"); return sb.toString();
        }

        private void validate(Class<? extends DesignClass<?, ?>> cls, Impl impl) {
            Target target = Trees.targetInstance(Trees.coordinates(cls).target());
            if (impl.carrier() != target.carrier()) {
                findings.add(new Finding(Finding.Kind.CARRIER_MISMATCH, cls, impl.carrier() + " impl for a " + target.carrier() + " target")); return;
            }
            switch (impl) {
                case Impl.Bindings b -> validateBindings(cls, target, b);
                case Impl.Body body -> validateBody(cls, target, body);
                default -> {}
            }
        }

        private void validateBindings(Class<?> cls, Target target, Impl.Bindings b) {
            b.values().forEach((mode, states) -> states.forEach((state, props) -> {
                if (!target.states().contains(state))
                    findings.add(new Finding(Finding.Kind.INVALID_BINDING, cls, target.token() + " offers no slot for state " + state));
                props.keySet().forEach(p -> {
                    if (p.equals(Impl.Bindings.SOLE)) {
                        if (target.properties().size() != 1)
                            findings.add(new Finding(Finding.Kind.INVALID_BINDING, cls, target.token() + " owns " + target.properties().size() + " properties; name one"));
                    } else if (!target.properties().contains(p))
                        findings.add(new Finding(Finding.Kind.INVALID_BINDING, cls, target.token() + " does not own " + p));
                });
            }));
        }

        private static final Pattern DECLARATION = Pattern.compile("(?m)^\\s*([a-z-]+)\\s*:");
        private static final Pattern NESTED = Pattern.compile("(?m)^\\s*([^{;\\n]+?)\\s*\\{");

        private void validateBody(Class<?> cls, Target target, Impl.Body body) {
            Matcher m = DECLARATION.matcher(body.css());
            while (m.find()) if (!target.properties().contains(m.group(1)))
                findings.add(new Finding(Finding.Kind.INVALID_BODY, cls, "body writes " + m.group(1) + ", which " + target.token() + " does not own"));
            Matcher n = NESTED.matcher(body.css());
            while (n.find()) {
                String prelude = n.group(1).trim();
                if (prelude.startsWith("@")) continue;                              // media, supports
                if (prelude.contains(".") || prelude.contains("#"))
                    findings.add(new Finding(Finding.Kind.INVALID_BODY, cls, "body nests a class or id selector: " + prelude));
            }
        }
    }

    /** The design classes a set of semantic leaves project to — a convenience for building a requirement set by meaning. */
    @SafeVarargs
    public static Set<Class<? extends DesignClass<?, ?>>> projectionsOf(Class<? extends Semantic>... leaves) {
        var out = new HashSet<Class<? extends DesignClass<?, ?>>>();
        for (var leaf : leaves) for (Class<?> nested : leaf.getDeclaredClasses())
            if (DesignClass.class.isAssignableFrom(nested)) { @SuppressWarnings("unchecked") var c = (Class<? extends DesignClass<?, ?>>) nested; out.add(c); }
        return out;
    }

    public Optional<Impl> implOf(Class<? extends DesignClass<?, ?>> cls) { return Optional.ofNullable(resolve().impls().get(cls)); }
}
