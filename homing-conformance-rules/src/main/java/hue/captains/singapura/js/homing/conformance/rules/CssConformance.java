package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.core.util.CssClassName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RFC 0066 — the laws over a deployment's CSS graph, as conformance findings.
 *
 * <p>The JS rules see one served module's text; the crate checks see the crate
 * declarations. These see the third thing a deployment is made of: its
 * {@link CssGroup}s, their classes, and the variables and classes their bodies
 * name. A finding is keyed by the GROUP's class name — a served module — so the
 * grader, the baseline, the report and the studio treat it like any other.</p>
 *
 * <p>The input is the crate closure alone (which groups exist, and which crate
 * serves each). The rules over a theme registry's palette provisions retired with
 * the global palette (RFC 0067): a theme is a design, and a design's completeness
 * is its deployment's to prove.</p>
 *
 * <table>
 *   <tr><th>Rule</th><th>Law</th><th>Refuses</th></tr>
 *   <tr><td>{@code css-token-declared}</td><td>1</td>
 *       <td>a body reading a variable that is neither a design word the class wears or {@code reads()}, nor named in its {@code runtimeVars()}</td></tr>
 *   <tr><td>{@code css-nested-reference-declared}</td><td>4</td>
 *       <td>a body naming a class in a nested selector that is neither in its group nor a declared dependency</td></tr>
 *   <tr><td>{@code css-crate-reach}</td><td>D9</td>
 *       <td>a group depending on another crate's group from a crate that does not require it</td></tr>
 * </table>
 */
public final class CssConformance {

    public static final RuleId TOKEN_DECLARED    = new RuleId("css-token-declared");
    public static final RuleId NESTED_DECLARED   = new RuleId("css-nested-reference-declared");
    public static final RuleId CRATE_REACH       = new RuleId("css-crate-reach");

    public static final List<RuleId> ALL = List.of(TOKEN_DECLARED, NESTED_DECLARED, CRATE_REACH);

    private CssConformance() {}

    /** Every finding of every rule, over the closure. */
    public static List<Finding> check(Collection<? extends Crate> closure) {
        var graph = Graph.of(closure);
        var out = new ArrayList<Finding>();
        out.addAll(tokenDeclared(graph));
        out.addAll(nestedDeclared(graph));
        out.addAll(crateReach(graph));
        return List.copyOf(out);
    }

    // ── The graph the rules read ─────────────────────────────────────────────

    /** The deployment's CSS graph, resolved once: its groups, each by the crate that serves it. */
    static final class Graph {
        final Map<CssGroup<?>, Crate> crateOf = new LinkedHashMap<>();

        static Graph of(Collection<? extends Crate> closure) {
            var g = new Graph();
            for (Crate c : closure) {
                for (CrateEntry e : c.entries()) {
                    EsModule<?> m = e.module();
                    if (m instanceof CssGroup<?> group) g.crateOf.put(group, c);
                }
            }
            return g;
        }

        /** The groups to check: everything crated. */
        List<CssGroup<?>> groups() { return new ArrayList<>(crateOf.keySet()); }

        Crate crateOf(CssGroup<?> group) {
            for (var e : crateOf.entrySet()) if (e.getKey().getClass() == group.getClass()) return e.getValue();
            return null;
        }
    }

    private static String id(CssGroup<?> g) { return g.getClass().getCanonicalName(); }
    private static String cls(CssClass<?> c) { return c.getClass().getSimpleName(); }

    // ── Rules over the graph ─────────────────────────────────────────────────

    /** {@code var(--name} — the variable a body reads. */
    static final Pattern TOKEN_READ = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)");

    static List<Finding> tokenDeclared(Graph g) {
        var out = new ArrayList<Finding>();
        for (CssGroup<?> group : g.groups()) {
            for (CssClass<?> c : group.cssClasses()) {
                String body = c.body();
                if (body == null) continue;
                var undeclared = new TreeSet<String>();
                Matcher m = TOKEN_READ.matcher(body);
                while (m.find()) {
                    CssVar v = new CssVar(m.group(1));
                    if (c.runtimeVars().contains(v)) continue;
                    if (readsWord(c, v)) continue;                  // a design word the class wears or reads: its binding is emitted with the pair
                    undeclared.add(v.name());
                }
                if (!undeclared.isEmpty()) {
                    out.add(new Finding(id(group), TOKEN_DECLARED, cls(c) + " reads " + undeclared
                            + " — neither a design word the class wears or reads, nor in runtimeVars()"));
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
            for (CssGroup<?> dep : CssImportsFor.dependenciesOf(group)) {
                Crate theirs = g.crateOf(dep);
                if (theirs == null || required.contains(theirs.name())) continue;
                out.add(new Finding(id(group), CRATE_REACH, "depends on " + id(dep) + " in crate '" + theirs.name()
                        + "', which crate '" + mine.name() + "' does not require"));
            }
        }
        return out;
    }
}
