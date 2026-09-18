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

    public Deployment {
        // in order — the order of resolution is the order the closure wore them; the sheet has its own (Sheets.PRECEDENCE)
        required = java.util.Collections.unmodifiableSet(new LinkedHashSet<>(required));
        extensions = List.copyOf(extensions);
    }

    public static Deployment of(Set<DesignClass<?>> required, Design design) {
        return new Deployment(required, design, List.of());
    }

    /** The requirement set of a closure: every pair any class of any group in it wears — or reads, since a reference needs a binding to land on. */
    public static Set<DesignClass<?>> wornBy(Collection<? extends CssGroup<?>> groups) {
        var out = new LinkedHashSet<DesignClass<?>>();
        for (CssGroup<?> g : groups)
            for (CssClass<?> c : g.cssClasses()) {
                for (Wearable w : c.wears()) if (w instanceof DesignClass<?> dc) out.add(dc);
                for (Wearable w : c.reads()) if (w instanceof DesignClass<?> dc) out.add(dc);   // read by reference: the word must exist and its binding be emitted
            }
        return out;
    }

    /** One thing that did not resolve, or resolved wrongly. */
    public record Finding(Kind kind, DesignClass<?> designClass, String detail) {
        public enum Kind { MISSING, DOUBLE, CARRIER_MISMATCH, INVALID_BINDING, INVALID_BODY, DANGLING_REFERENCE, LITERAL_COLOUR }
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
        checkReferences(impls, findings);
        checkColourLiterals(impls, findings);
        return new Resolution(java.util.Collections.unmodifiableMap(impls), List.copyOf(findings));
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

    // A declaration is `property: value;` on its own line — `tr:nth-child(even) td {` is a selector, not a `tr` property.
    private static final Pattern DECLARATION = Pattern.compile("(?m)^\\s*([a-z-]+)\\s*:\\s*[^{;\\n]*;");
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
            // On self, a body may take only a State's slot or a pseudo-element: a
            // design does not invent a state of the component — the component does,
            // by wearing another class. Elements below are the design's to address.
            for (String part : prelude.split(",")) {
                String p = part.trim();
                if (!p.startsWith("&")) continue;
                if (p.startsWith("&::")) continue;
                if (p.contains(".") || p.contains("#")) continue;                 // reported above

                if (!SELF_STATES.contains(p))
                    findings.add(new Finding(Finding.Kind.INVALID_BODY, pair, "body nests a state of its own on self: " + p + " — a State's slot or a pseudo-element only"));
            }
        }
    }

    /** Every {@code &…} prelude a {@link State} names, split on commas. */
    private static final Set<String> SELF_STATES = java.util.Arrays.stream(State.values())
            .flatMap(s -> java.util.Arrays.stream(s.selector().split(",")))
            .map(String::trim).filter(s -> !s.isEmpty())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    // ── references between words ──────────────────────────────────────────

    private static final Pattern REFERENCE = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)");

    /**
     * A word may read another's value — {@link DesignClass#var()} — which is
     * how a physique names the palette's colour without carrying it. The
     * browser resolves it against the root binding; here we make sure there
     * is one: every variable any impl reads is a variable this resolution
     * emits, at rest, for a pair that is required. Otherwise the reference
     * dangles, silently, in the browser — so it is a finding here.
     */
    private static void checkReferences(Map<DesignClass<?>, Impl> impls, List<Finding> findings) {
        var emitted = new java.util.HashSet<String>();
        impls.forEach((dc, impl) -> {
            if (!(impl instanceof Impl.Bindings b)) return;
            var owned = dc.targetLeaf().properties();
            b.values().forEach((mode, states) -> states.forEach((state, props) -> props.keySet().forEach(p -> {
                // an invalid binding is already a finding; it emits nothing
                if (p.equals(Impl.Bindings.SOLE) ? owned.size() != 1 : !owned.contains(p)) return;
                emitted.add(Sheets.variable(dc, Sheets.property(dc, p), state));
            })));
        });
        impls.forEach((dc, impl) -> {
            for (String text : textsOf(impl)) {
                Matcher m = REFERENCE.matcher(text);
                while (m.find()) if (!emitted.contains(m.group(1)))
                    findings.add(new Finding(Finding.Kind.DANGLING_REFERENCE, dc, "reads " + m.group(1) + ", which no required pair binds"));
            }
        });
    }

    // ── where a colour may be said ────────────────────────────────────────

    private static final Pattern COLOUR_LITERAL = Pattern.compile("#[0-9A-Fa-f]{3,8}\\b|\\b(?:rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\\(");

    /**
     * A colour literal is valid in exactly one place: a {@link Impl.Bindings}
     * on the colour plane — the palette's own word, bound to a variable. A body
     * on any plane, and any word off the colour plane, says a colour by
     * reference ({@link DesignClass#var()}) or not at all. That is what keeps
     * the planes orthogonal — a physique carries no colour, so any palette
     * colours it — and keeps every colour a variable, so a palette can be
     * changed under a page without touching a rule.
     */
    private static void checkColourLiterals(Map<DesignClass<?>, Impl> impls, List<Finding> findings) {
        impls.forEach((dc, impl) -> {
            boolean allowed = impl instanceof Impl.Bindings && dc.onColourPlane();
            if (allowed) return;
            for (String text : textsOf(impl)) {
                Matcher m = COLOUR_LITERAL.matcher(text);
                while (m.find())
                    findings.add(new Finding(Finding.Kind.LITERAL_COLOUR, dc, (impl instanceof Impl.Body ? "body" : "word off the colour plane")
                            + " carries " + m.group() + (m.group().endsWith("(") ? "…)" : "") + " — say it by reference, or bind it on the colour plane"));
            }
        });
    }

    private static List<String> textsOf(Impl impl) {
        return switch (impl) {
            case Impl.Bindings b -> { var out = new ArrayList<String>(); b.values().values().forEach(s -> s.values().forEach(p -> out.addAll(p.values()))); yield out; }
            case Impl.Body body -> List.of(body.css());
            default -> List.of();
        };
    }
}
