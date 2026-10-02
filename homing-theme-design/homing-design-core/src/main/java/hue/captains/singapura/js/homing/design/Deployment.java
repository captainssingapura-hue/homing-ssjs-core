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
 * <p>A refined word ({@code Control.Button.Base}) is asked for up its
 * lineage: the design and the extensions are asked at the word worn, then at
 * its parent, and the first level with an answer is the answer — so a design
 * speaks once for every control and again only for the buttons, or the base
 * button, it has an opinion on.</p>
 *
 * @param required   the design classes the closure wears
 * @param scaled     those of them some class wears with an {@link Extent} — its element sets one — so the design must anchor them at zero and at −1
 * @param grown      those of them some class wears with a number on a {@link Growth} axis — its element sets one — so the design must give every property a ratio on that axis
 * @param design     the design to fulfil them
 * @param extensions the products' fulfilments of their own classes
 */
public record Deployment(Set<DesignClass<?>> required, Set<DesignClass<?>> scaled, Map<Growth, Set<DesignClass<?>>> grown, Design design, List<DesignExtension> extensions) {

    public Deployment {
        // in order — the order of resolution is the order the closure wore them; the sheet has its own (Sheets.PRECEDENCE)
        required = java.util.Collections.unmodifiableSet(new LinkedHashSet<>(required));
        scaled = java.util.Collections.unmodifiableSet(new LinkedHashSet<>(scaled));
        var g = new java.util.EnumMap<Growth, Set<DesignClass<?>>>(Growth.class);
        grown.forEach((axis, pairs) -> g.put(axis, java.util.Collections.unmodifiableSet(new LinkedHashSet<>(pairs))));
        grown = java.util.Collections.unmodifiableMap(g);
        extensions = List.copyOf(extensions);
    }

    public Deployment(Set<DesignClass<?>> required, Set<DesignClass<?>> scaled, Design design, List<DesignExtension> extensions) {
        this(required, scaled, Map.of(), design, extensions);
    }

    public Deployment(Set<DesignClass<?>> required, Design design, List<DesignExtension> extensions) {
        this(required, Set.of(), Map.of(), design, extensions);
    }

    public static Deployment of(Set<DesignClass<?>> required, Design design) {
        return new Deployment(required, Set.of(), Map.of(), design, List.of());
    }

    public static Deployment of(Set<DesignClass<?>> required, Set<DesignClass<?>> scaled, Design design) {
        return new Deployment(required, scaled, Map.of(), design, List.of());
    }

    public static Deployment of(Set<DesignClass<?>> required, Set<DesignClass<?>> scaled, Map<Growth, Set<DesignClass<?>>> grown, Design design) {
        return new Deployment(required, scaled, grown, design, List.of());
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

    /** The pairs some class in the closure wears with a number on each {@link Growth} axis — {@code sizes()}, {@code aspects()}: subsets of {@link #wornBy}, each of whose properties the design must give a ratio on that axis. */
    public static Map<Growth, Set<DesignClass<?>>> grownBy(Collection<? extends CssGroup<?>> groups) {
        var out = new java.util.EnumMap<Growth, Set<DesignClass<?>>>(Growth.class);
        for (Growth axis : Growth.values()) out.put(axis, new LinkedHashSet<>());
        for (CssGroup<?> g : groups)
            for (CssClass<?> c : g.cssClasses()) {
                for (Wearable w : c.sizes()) if (w instanceof DesignClass<?> dc) out.get(Growth.SIZE).add(dc);
                for (Wearable w : c.aspects()) if (w instanceof DesignClass<?> dc) out.get(Growth.ASPECT).add(dc);
            }
        return out;
    }

    /** The pairs some class in the closure wears with an {@link Extent}: a subset of {@link #wornBy}, each of which the design must anchor at every extent. */
    public static Set<DesignClass<?>> scaledBy(Collection<? extends CssGroup<?>> groups) {
        var out = new LinkedHashSet<DesignClass<?>>();
        for (CssGroup<?> g : groups)
            for (CssClass<?> c : g.cssClasses())
                for (Wearable w : c.extents()) if (w instanceof DesignClass<?> dc) out.add(dc);
        return out;
    }

    /** One thing that did not resolve, or resolved wrongly. */
    public record Finding(Kind kind, DesignClass<?> designClass, String detail) {
        public enum Kind { MISSING, DOUBLE, CARRIER_MISMATCH, INVALID_BINDING, INVALID_BODY, DANGLING_REFERENCE, LITERAL_COLOUR, MISSING_ANCHOR, MISSING_RATIO }
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
            Impl impl = null;
            boolean doubled = false;
            for (var at : pair.lineage()) {   // the word worn, then its parents: the first level anyone answers
                Impl own = design.impl(at);
                var fromExtensions = new ArrayList<Map.Entry<String, Impl>>();
                for (var e : extensions) { Impl i = e.impl(at); if (i != null) fromExtensions.add(Map.entry(e.slug(), i)); }
                if (own != null && !fromExtensions.isEmpty()) {
                    findings.add(new Finding(Finding.Kind.DOUBLE, pair, "answered by " + design.slug() + " and " + fromExtensions.get(0).getKey() + (at == pair ? "" : " at " + at.cssName())));
                    doubled = true;
                    break;
                }
                if (fromExtensions.size() > 1) {
                    findings.add(new Finding(Finding.Kind.DOUBLE, pair, "answered by " + fromExtensions.get(0).getKey() + " and " + fromExtensions.get(1).getKey() + (at == pair ? "" : " at " + at.cssName())));
                    doubled = true;
                    break;
                }
                impl = own != null ? own : fromExtensions.isEmpty() ? null : fromExtensions.get(0).getValue();
                if (impl != null) break;
            }
            if (doubled) continue;
            if (impl == null) {
                var lineage = pair.lineage();
                findings.add(new Finding(Finding.Kind.MISSING, pair, "no answer from " + design.slug() + (lineage.size() == 1 ? "" : ", nor at " + lineage.get(lineage.size() - 1).cssName())));
                continue;
            }
            validate(pair, impl, findings);
            if (scaled.contains(pair)) checkAnchors(pair, impl, findings);
            for (Growth axis : Growth.values())
                if (grown.getOrDefault(axis, Set.of()).contains(pair)) checkRatios(pair, impl, axis, findings);
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
        for (Extent extent : Extent.values()) {
            if (extent != Extent.FULL && !b.anchors(extent).isEmpty() && !pair.onColourPlane())
                findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, "anchored at " + extent + " — an extent is a colour's; " + target.token() + " is not on the colour plane"));
            validateSlots(pair, target, b.anchors(extent), findings);
        }
        checkInterpolatedValuesAreOneColour(pair, b, findings);
        checkRatiosAreOnLengths(pair, b, findings);
    }

    // A length a ratio may multiply: one number with or without a unit — 8px, 1.5, 0, .5rem — never a list, a keyword or a colour.
    private static final Pattern LENGTH = Pattern.compile("^-?(\\d+\\.?\\d*|\\.\\d+)[a-zA-Z%]*$");

    /**
     * A ratio multiplies a length: it is refused on the colour plane, on a
     * property the word does not bind, and on a value that is not one number
     * — a list, a keyword, {@code auto} — at any state or mode, since the
     * power applies at every state alike. A ratio of exactly 1 renders the
     * value plainly, so a keyword may carry one: that is how a design says a
     * border-style stays beside a border-width that grows.
     */
    private static void checkRatiosAreOnLengths(DesignClass<?> pair, Impl.Bindings b, List<Finding> findings) {
        if (b.ratios().values().stream().allMatch(Map::isEmpty)) return;
        if (pair.onColourPlane()) { findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, "a ratio is a length's; " + pair.targetLeaf().token() + " is on the colour plane")); return; }
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        b.ratios().forEach((axis, byProperty) -> byProperty.forEach((key, ratio) -> {
            if (ratio <= 0) findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, Sheets.property(pair, key) + " grows by " + ratio + " — a ratio is positive"));
            if (!rest.containsKey(key)) { findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, Sheets.property(pair, key) + " has a ratio but no value at rest")); return; }
            if (ratio == 1.0) return;   // stays: renders plainly, so a keyword may hold — border-style: solid, ratio 1
            b.values().forEach((mode, states) -> states.forEach((state, props) -> {
                String v = props.get(key);
                if (v != null && !LENGTH.matcher(v.strip()).matches())
                    findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, Sheets.property(pair, key) + " grows, but its value" + (state == State.REST ? "" : " on " + state) + (mode == Mode.LIGHT ? "" : " in " + mode) + " is not one length: '" + v + "'"));
            }));
        }));
    }

    /**
     * A pair a class wears with a number on an axis must have a ratio on
     * that axis for every property its word binds at rest — 1 to say the
     * property stays — so a design decides what grows and what holds, and
     * nothing is left to a default.
     */
    private static void checkRatios(DesignClass<?> pair, Impl impl, Growth axis, List<Finding> findings) {
        String worn = "worn with " + (axis == Growth.SIZE ? "a size" : "an aspect");
        if (!(impl instanceof Impl.Bindings b)) {
            findings.add(new Finding(Finding.Kind.MISSING_RATIO, pair, worn + ", but the word is " + (impl instanceof Impl.Body ? "a body" : "silence") + " — only bindings grow"));
            return;
        }
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        for (String key : rest.keySet())
            if (b.ratio(key, axis) == null)
                findings.add(new Finding(Finding.Kind.MISSING_RATIO, pair, worn + ", but " + Sheets.property(pair, key) + " has no ratio on " + axis.var() + " — 1 says it stays"));
    }

    /**
     * A word anchored at both ends renders as an interpolation, and
     * {@code color-mix} takes one colour: a value that is a list — a colour
     * per side of a border — cannot be mixed, and the browser drops the whole
     * declaration. Which side is strong is the rule's shape to say, not the
     * word's colour; the design is told so rather than left with an edge that
     * silently falls back to the ink.
     */
    private static void checkInterpolatedValuesAreOneColour(DesignClass<?> pair, Impl.Bindings b, List<Finding> findings) {
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        for (String key : rest.keySet()) {
            if (!(b.anchored(Extent.ZERO, key) && b.anchored(Extent.NEG, key))) continue;
            for (Extent extent : Extent.values())
                b.anchors(extent).forEach((mode, states) -> states.forEach((state, props) -> {
                    String v = props.get(key);
                    int n = v == null ? 1 : topLevelTokens(v);
                    if (n > 1)
                        findings.add(new Finding(Finding.Kind.INVALID_BINDING, pair, Sheets.property(pair, key) + " at " + extent.value() + (state == State.REST ? "" : " on " + state) + (mode == Mode.LIGHT ? "" : " in " + mode)
                                + " is a list of " + n + " — an interpolation mixes one colour; a colour per side is the rule's shape, not the word's colour"));
                }));
        }
    }

    /** How many values a CSS value is: tokens separated by whitespace outside parentheses, so {@code rgba(1, 2, 3, .4)} is one. */
    static int topLevelTokens(String value) {
        int n = 0, depth = 0; boolean inToken = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            if (Character.isWhitespace(c) && depth == 0) { inToken = false; continue; }
            if (!inToken) { inToken = true; n++; }
        }
        return n;
    }

    /**
     * A pair a class wears with an extent must be anchored at zero and at −1 for
     * every property its word binds at rest — the design says what neutral looks
     * like here and what the meaning turned the other way looks like; neither
     * is defaulted, because neutral is not always transparent and not every
     * meaning has an opposite.
     */
    private static void checkAnchors(DesignClass<?> pair, Impl impl, List<Finding> findings) {
        if (!(impl instanceof Impl.Bindings b)) {
            findings.add(new Finding(Finding.Kind.MISSING_ANCHOR, pair, "worn with an extent, but the word is " + (impl instanceof Impl.Body ? "a body" : "silence") + " — only bindings anchor"));
            return;
        }
        var rest = b.values().getOrDefault(Mode.LIGHT, Map.of()).getOrDefault(State.REST, Map.of());
        for (String key : rest.keySet())
            for (Extent extent : List.of(Extent.ZERO, Extent.NEG))
                if (!b.anchored(extent, key))
                    findings.add(new Finding(Finding.Kind.MISSING_ANCHOR, pair, "worn with an extent, but " + Sheets.property(pair, key) + " has no anchor at " + extent.value()));
    }

    private static void validateSlots(DesignClass<?> pair, Target target, Map<Mode, Map<State, Map<String, String>>> values, List<Finding> findings) {
        values.forEach((mode, states) -> states.forEach((state, props) -> {
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
            for (Extent extent : Extent.values())
                b.anchors(extent).forEach((mode, states) -> states.forEach((state, props) -> props.keySet().forEach(p -> {
                    // an invalid binding is already a finding; it emits nothing
                    if (p.equals(Impl.Bindings.SOLE) ? owned.size() != 1 : !owned.contains(p)) return;
                    emitted.add(Sheets.variable(dc, Sheets.property(dc, p), state) + extent.suffix());
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
            case Impl.Bindings b -> {
                var out = new ArrayList<String>();
                for (Extent extent : Extent.values()) b.anchors(extent).values().forEach(s -> s.values().forEach(p -> out.addAll(p.values())));
                yield out;
            }
            case Impl.Body body -> List.of(body.css());
            default -> List.of();
        };
    }
}
