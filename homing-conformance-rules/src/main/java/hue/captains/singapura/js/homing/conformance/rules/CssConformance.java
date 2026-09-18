package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupResolver;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.PaletteClass;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.util.CssClassName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RFC 0066 — the laws over a deployment's CSS graph, as conformance findings.
 *
 * <p>The JS rules see one served module's text; the crate checks see the crate
 * declarations. These see the third thing a deployment is made of: its
 * {@link CssGroup}s, their classes, the tokens the bodies read, the palettes
 * that declare them, and the provisions that bind them. A finding is keyed by
 * the GROUP's class name — a served module — so the grader, the baseline, the
 * report and the studio treat it like any other.</p>
 *
 * <p>Inputs are core types only: the crate closure (which groups exist, and
 * which crate serves each) and the provisions a deployment registers (from
 * which the priors — the palette groups — are derived). A library with no
 * registry passes none and gets the graph-side rules alone.</p>
 *
 * <p>Phase 1 reads DECLARED bodies only. A theme's override blocks are checked
 * when the per-theme rendering pass lands ({@code css-token-declared-themed});
 * until then an override that reads an undeclared token fails the way it
 * always did — silently, in that theme alone.</p>
 *
 * <table>
 *   <tr><th>Rule</th><th>Law</th><th>Refuses</th></tr>
 *   <tr><td>{@code css-palette-complete}</td><td>completeness</td>
 *       <td>a theme without exactly one provision per prior; a provision missing a declared token; a dark re-binding of a token the light one never bound</td></tr>
 *   <tr><td>{@code css-no-shadowing}</td><td>2</td>
 *       <td>a provision binding a token in a declared family its palette does not declare</td></tr>
 *   <tr><td>{@code css-token-declared}</td><td>1</td>
 *       <td>a body reading a token no palette the class reaches declares, unless the class names it in {@code runtimeVars()}</td></tr>
 *   <tr><td>{@code css-prior-is-palette}</td><td>—</td>
 *       <td>a prior that holds no {@link PaletteClass}, or that declares dependencies</td></tr>
 *   <tr><td>{@code css-nested-reference-declared}</td><td>4</td>
 *       <td>a body naming a class in a nested selector that is neither in its group nor a declared dependency</td></tr>
 *   <tr><td>{@code css-crate-reach}</td><td>D9</td>
 *       <td>a group leaning on another crate's group — by an edge, or by reading a prior's tokens — from a crate that does not require it</td></tr>
 *   <tr><td>{@code css-no-literal-family}</td><td>6</td>
 *       <td>a literal {@code font-family} or {@code border-radius} — the literal half of {@code var(--x, literal)} included — where the family is declared</td></tr>
 * </table>
 */
public final class CssConformance {

    public static final RuleId PALETTE_COMPLETE  = new RuleId("css-palette-complete");
    public static final RuleId NO_SHADOWING      = new RuleId("css-no-shadowing");
    public static final RuleId TOKEN_DECLARED    = new RuleId("css-token-declared");
    public static final RuleId PRIOR_IS_PALETTE  = new RuleId("css-prior-is-palette");
    public static final RuleId NESTED_DECLARED   = new RuleId("css-nested-reference-declared");
    public static final RuleId CRATE_REACH       = new RuleId("css-crate-reach");
    public static final RuleId NO_LITERAL_FAMILY = new RuleId("css-no-literal-family");

    public static final List<RuleId> ALL = List.of(PALETTE_COMPLETE, NO_SHADOWING, TOKEN_DECLARED,
            PRIOR_IS_PALETTE, NESTED_DECLARED, CRATE_REACH, NO_LITERAL_FAMILY);

    private CssConformance() {}

    /** Every finding of every rule, over the closure and the deployment's provisions. */
    public static List<Finding> check(Collection<? extends Crate> closure, List<PaletteProvision<?, ?>> provisions) {
        var graph = Graph.of(closure, provisions);
        var out = new ArrayList<Finding>();
        out.addAll(paletteComplete(graph));
        out.addAll(noShadowing(graph));
        out.addAll(priorIsPalette(graph));
        out.addAll(tokenDeclared(graph));
        out.addAll(nestedDeclared(graph));
        out.addAll(crateReach(graph));
        out.addAll(noLiteralFamily(graph));
        return List.copyOf(out);
    }

    // ── The graph the rules read ─────────────────────────────────────────────

    /** The deployment's CSS graph, resolved once: groups by crate, priors, provisions, declared tokens. */
    static final class Graph {
        final Map<CssGroup<?>, Crate> crateOf = new LinkedHashMap<>();
        final List<PaletteProvision<?, ?>> provisions;
        final List<CssGroup<?>> priors = new ArrayList<>();
        final Set<CssVar> priorTokens = new LinkedHashSet<>();
        final Set<String> families = new TreeSet<>();
        /** Every token any palette in the closure or among the priors declares. */
        final Set<CssVar> allDeclared = new LinkedHashSet<>();

        private Graph(List<PaletteProvision<?, ?>> provisions) { this.provisions = provisions; }

        static Graph of(Collection<? extends Crate> closure, List<PaletteProvision<?, ?>> provisions) {
            var g = new Graph(provisions);
            for (Crate c : closure) {
                for (CrateEntry e : c.entries()) {
                    EsModule<?> m = e.module();
                    if (m instanceof CssGroup<?> group) g.crateOf.put(group, c);
                }
            }
            for (PaletteProvision<?, ?> p : provisions) {
                CssGroup<?> palette = p.group();
                if (g.priors.stream().noneMatch(have -> have.getClass() == palette.getClass())) g.priors.add(palette);
            }
            for (CssGroup<?> p : g.priors) g.priorTokens.addAll(declaredBy(p));
            for (CssGroup<?> group : g.crateOf.keySet()) g.allDeclared.addAll(declaredBy(group));
            g.allDeclared.addAll(g.priorTokens);
            for (CssVar v : g.allDeclared) g.families.add(familyOf(v.name()));
            return g;
        }

        /** The groups to check: everything crated, plus the priors (crated or not). */
        List<CssGroup<?>> groups() {
            var out = new ArrayList<CssGroup<?>>(crateOf.keySet());
            for (CssGroup<?> p : priors) if (out.stream().noneMatch(have -> have.getClass() == p.getClass())) out.add(p);
            return out;
        }

        /** The tokens a class may read: the priors', plus every palette reached through its group's dependencies. */
        Set<CssVar> reachableTokens(CssGroup<?> group) {
            var out = new LinkedHashSet<CssVar>(priorTokens);
            for (CssGroup<?> dep : CssGroupResolver.resolve(List.of(group))) out.addAll(declaredBy(dep));
            return out;
        }

        Crate crateOf(CssGroup<?> group) {
            for (var e : crateOf.entrySet()) if (e.getKey().getClass() == group.getClass()) return e.getValue();
            return null;
        }
    }

    static Set<CssVar> declaredBy(CssGroup<?> group) {
        var out = new LinkedHashSet<CssVar>();
        for (CssClass<?> c : group.cssClasses()) if (c instanceof PaletteClass<?> p) out.addAll(p.declares());
        return out;
    }

    /** {@code --color-text-primary} → {@code --color-}: the family a token belongs to. */
    static String familyOf(String name) {
        int dash = name.indexOf('-', 2);
        return dash < 0 ? name : name.substring(0, dash + 1);
    }

    private static String id(CssGroup<?> g) { return g.getClass().getCanonicalName(); }
    private static String cls(CssClass<?> c) { return c.getClass().getSimpleName(); }

    // ── Rules over the registry ──────────────────────────────────────────────

    static List<Finding> paletteComplete(Graph g) {
        var out = new ArrayList<Finding>();
        var slugs = new LinkedHashSet<String>();
        for (var p : g.provisions) slugs.add(p.theme().slug());
        for (String slug : slugs) {
            for (CssGroup<?> prior : g.priors) {
                var mine = g.provisions.stream()
                        .filter(p -> p.theme().slug().equals(slug) && p.group().getClass() == prior.getClass()).toList();
                if (mine.size() != 1) {
                    out.add(new Finding(id(prior), PALETTE_COMPLETE,
                            "theme '" + slug + "' provides this palette " + mine.size() + " times; exactly once is complete"));
                    continue;
                }
                var p = mine.get(0);
                var missing = new TreeSet<String>();
                for (CssVar v : declaredBy(prior)) if (!p.values().containsKey(v)) missing.add(v.name());
                if (!missing.isEmpty()) {
                    out.add(new Finding(id(prior), PALETTE_COMPLETE, "theme '" + slug + "' does not bind " + missing));
                }
                var darkStray = new TreeSet<String>();
                for (CssVar v : p.darkValues().keySet()) if (!p.values().containsKey(v)) darkStray.add(v.name());
                if (!darkStray.isEmpty()) {
                    out.add(new Finding(id(prior), PALETTE_COMPLETE,
                            "theme '" + slug + "' re-binds in dark what it never bound in light: " + darkStray));
                }
            }
        }
        return out;
    }

    static List<Finding> noShadowing(Graph g) {
        var out = new ArrayList<Finding>();
        for (var p : g.provisions) {
            var declared = declaredBy(p.group());
            var stray = new TreeSet<String>();
            var keys = new LinkedHashSet<>(p.values().keySet());
            keys.addAll(p.darkValues().keySet());
            for (CssVar v : keys) {
                if (declared.contains(v)) continue;
                if (g.families.contains(familyOf(v.name()))) stray.add(v.name());
            }
            if (!stray.isEmpty()) {
                out.add(new Finding(id(p.group()), NO_SHADOWING, "theme '" + p.theme().slug()
                        + "' binds tokens in a declared family its palette does not declare (escaped token or typo): " + stray));
            }
        }
        return out;
    }

    // ── Rules over the graph ─────────────────────────────────────────────────

    static List<Finding> priorIsPalette(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            if (!group.prior()) continue;
            boolean holds = group.cssClasses().stream().anyMatch(c -> c instanceof PaletteClass<?>);
            if (!holds) out.add(new Finding(id(group), PRIOR_IS_PALETTE,
                    "a prior that holds no PaletteClass: only a palette is prior; a group others lean on is named in their dependsOn()"));
            if (!CssImportsFor.dependenciesOf(group).isEmpty()) out.add(new Finding(id(group), PRIOR_IS_PALETTE,
                    "a prior declares no dependencies"));
        }
        return out;
    }

    /** {@code var(--name} — the token a body reads; the fallback, if any, is read by {@link #LITERAL_FALLBACK}. */
    static final Pattern TOKEN_READ = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)");

    static List<Finding> tokenDeclared(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            Set<CssVar> reachable = null;
            for (CssClass<?> c : group.cssClasses()) {
                String body = c.body();
                if (body == null) continue;
                var undeclared = new TreeSet<String>();
                Matcher m = TOKEN_READ.matcher(body);
                while (m.find()) {
                    CssVar v = new CssVar(m.group(1));
                    if (c.runtimeVars().contains(v)) continue;
                    if (reachable == null) reachable = g.reachableTokens(group);
                    if (!reachable.contains(v)) undeclared.add(v.name());
                }
                if (!undeclared.isEmpty()) {
                    out.add(new Finding(id(group), TOKEN_DECLARED, cls(c) + " reads " + undeclared
                            + " — declared by no palette the class reaches, and not in runtimeVars()"));
                }
            }
        }
        return out;
    }

    /**
     * A design word the class wears or reads, named by its variable: {@code --<pair>} or
     * {@code --<pair>-<property>}, a state suffix or not. The binding is the design's, emitted
     * with the pair; the body only reads it.
     */
    static boolean readsWord(CssClass<?> c, CssVar v) {
        for (var list : List.of(c.wears(), c.reads()))
            for (Wearable w : list) {
                String stem = "--" + w.cssName();
                if (v.name().equals(stem) || v.name().startsWith(stem + "-")) return true;
            }
        return false;
    }

    /** A class name inside a body: a nested selector naming another class. */
    static final Pattern CLASS_REF = Pattern.compile("(?<![\\w-])\\.([a-z][a-z0-9]*(?:-[a-z0-9]+)*)(?![\\w-])");

    static List<Finding> nestedDeclared(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            var own = new HashSet<String>();
            for (CssClass<?> c : group.cssClasses()) own.add(CssClassName.toCssName(c.getClass()));
            for (CssClass<?> c : group.cssClasses()) {
                String body = c.body();
                if (body == null) continue;
                var declared = new HashSet<String>();
                for (CssClass<?> d : c.dependsOn()) declared.add(CssClassName.toCssName(d.getClass()));
                var undeclared = new TreeSet<String>();
                Matcher m = CLASS_REF.matcher(stripValues(body));
                while (m.find()) {
                    String name = m.group(1);
                    if (own.contains(name) || declared.contains(name)) continue;
                    undeclared.add(name);
                }
                if (!undeclared.isEmpty()) {
                    out.add(new Finding(id(group), NESTED_DECLARED, cls(c) + " names " + undeclared
                            + " in a nested selector — a class of no group here, and not in dependsOn()"));
                }
            }
        }
        return out;
    }

    /** Drop declaration VALUES (after the colon, to the semicolon) and url()/strings, leaving selectors. */
    static String stripValues(String body) {
        return body.replaceAll("\"[^\"]*\"|'[^']*'", "\"\"")
                   .replaceAll("url\\([^)]*\\)", "url()")
                   .replaceAll(":[^;{}]*;", ";");
    }

    static List<Finding> crateReach(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            Crate mine = g.crateOf(group);
            if (mine == null) continue;
            var required = new HashSet<String>();
            required.add(mine.name());
            for (Crate r : mine.requires()) required.add(r.name());
            // Explicit edges.
            for (CssGroup<?> dep : CssImportsFor.dependenciesOf(group)) {
                Crate theirs = g.crateOf(dep);
                if (theirs == null || required.contains(theirs.name())) continue;
                out.add(new Finding(id(group), CRATE_REACH, "depends on " + id(dep) + " in crate '" + theirs.name()
                        + "', which crate '" + mine.name() + "' does not require"));
            }
            // The implicit edge: reading a prior's tokens.
            var reads = new LinkedHashSet<CssVar>();
            for (CssClass<?> c : group.cssClasses()) {
                String body = c.body();
                if (body == null) continue;
                Matcher m = TOKEN_READ.matcher(body);
                while (m.find()) reads.add(new CssVar(m.group(1)));
            }
            for (CssGroup<?> prior : g.priors) {
                Crate theirs = g.crateOf(prior);
                if (theirs == null || required.contains(theirs.name())) continue;
                var declared = declaredBy(prior);
                var read = new TreeSet<String>();
                for (CssVar v : reads) if (declared.contains(v)) read.add(v.name());
                if (!read.isEmpty()) {
                    out.add(new Finding(id(group), CRATE_REACH, "reads " + read + " from the prior " + id(prior)
                            + " in crate '" + theirs.name() + "', which crate '" + mine.name() + "' does not require"));
                }
            }
        }
        return out;
    }

    /** The families Law 6 polices, and the property each governs. */
    static final Map<String, String> LITERAL_FAMILY = Map.of(
            "font-family",   "--font-",
            "border-radius", "--radius-");
    static final Pattern DECLARATION = Pattern.compile("(?m)^\\s*([a-z-]+)\\s*:\\s*([^;{}]+);");
    static final Pattern LITERAL_FALLBACK = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)\\s*,\\s*([^)]+)\\)");
    static final Set<String> NEUTRAL = Set.of("inherit", "initial", "unset", "0", "none", "transparent", "currentColor");

    static List<Finding> noLiteralFamily(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            for (CssClass<?> c : group.cssClasses()) {
                String body = c.body();
                if (body == null) continue;
                var literal = new TreeSet<String>();
                Matcher d = DECLARATION.matcher(body);
                while (d.find()) {
                    String property = d.group(1), value = d.group(2).trim();
                    String family = LITERAL_FAMILY.get(property);
                    if (family == null || !g.families.contains(family)) continue;
                    if (NEUTRAL.contains(value)) continue;
                    if (!value.contains("var(")) literal.add(property + ": " + value);
                }
                Matcher f = LITERAL_FALLBACK.matcher(body);
                while (f.find()) {
                    String fallback = f.group(2).trim();
                    if (!g.families.contains(familyOf(f.group(1)))) continue;
                    if (!fallback.startsWith("var(") && !NEUTRAL.contains(fallback)) literal.add("fallback " + fallback);
                }
                if (!literal.isEmpty()) {
                    out.add(new Finding(id(group), NO_LITERAL_FAMILY, cls(c) + " writes a literal where a token family is declared: " + literal));
                }
            }
        }
        return out;
    }
}
