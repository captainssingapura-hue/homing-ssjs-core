package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;
import hue.captains.singapura.js.homing.core.util.CssClassName;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Auto-generates JS module content for a {@link CssGroup}.
 *
 * <p>Mirrors {@link hue.captains.singapura.js.homing.core.util.SvgGroupContentProvider}:
 * produces a header-only module that imports {@link CssClassManager},
 * loads the CSS file, and exports per-class JS handles.</p>
 *
 * <p>RFC 0002-ext1: per-class handles are emitted via {@code _css.cls(...)} which
 * returns either a {@code CssClass} or a {@code CssUtility} ES6 instance. For each
 * base, the {@link CssClass#variants()} set drives the variant map: each declared
 * state {@code s} contributes a {@code s: "<s>-<kebab>"} entry, which becomes a
 * dedicated variant property on the JS-side handle ({@code base.hover},
 * {@code base.focus}, etc.). No separate variant records are required — the
 * framework synthesizes everything from the base + its declared states.</p>
 */
public record CssGroupContentProvider<C extends CssGroup<C>>(
        C cssGroup, String theme, ModuleNameResolver nameResolver, List<CssGroup<?>> priors, Predicate<CssGroup<?>> varies
) implements ContentProvider<C> {

    public CssGroupContentProvider(C cssGroup, String theme, ModuleNameResolver nameResolver, List<CssGroup<?>> priors) {
        this(cssGroup, theme, nameResolver, priors, g -> true);
    }

    @Override
    public List<String> content() {
        List<String> lines = new ArrayList<>();
        String managerPath = nameResolver.resolve(CssClassManager.INSTANCE).basePath();
        String groupName = cssGroup.getClass().getCanonicalName();
        String themeArg = theme != null ? ", \"" + theme + "\"" : "";

        lines.add("import { CssClassManagerInstance as _css } from \"" + managerPath + "\";");
        // RFC 0064 — the group carries its transitive dependency subgraph as
        // DATA, derived from its classes' dependsOn() on the server, so the
        // client's CSS manager can load a lazily mounted widget's whole tree
        // — missing dependencies included, by name — and can order a theme
        // switch by the same graph. Not ES imports: a dependency's CSS is
        // needed, its handles are not.
        lines.add("await _css.loadCss(\"" + groupName + "\"" + themeArg + ", " + subgraphJs(cssGroup, priors, varies) + ");");

        for (CssClass<C> cls : cssGroup.cssClasses()) {
            String recordName = cls.getClass().getSimpleName();
            String cssName = CssClassName.toCssName(cls.getClass());
            var variants = cls.variants();
            // The design classes this class wears travel with the handle, as tokens:
            // the manager adds them to the element beside the class itself.
            String wears = cls.wears().isEmpty() ? "" : cls.wears().stream()
                    .map(w -> "\"" + CssClassName.toCssName(w.getClass()) + "\"")
                    .collect(java.util.stream.Collectors.joining(", ", ", [", "]"));
            if (variants.isEmpty()) {
                lines.add("const " + recordName + " = _css.cls(\"" + cssName + "\"" + (wears.isEmpty() ? "" : ", null" + wears) + ");");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("const ").append(recordName).append(" = _css.cls(\"")
                  .append(cssName).append("\", {");
                boolean first = true;
                for (String state : variants) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append(" ").append(state).append(": \"")
                      .append(state).append("-").append(cssName).append("\"");
                }
                sb.append(" }").append(wears).append(");");
                lines.add(sb.toString());
            }
        }
        return lines;
    }

    /**
     * {@code { "<fqcn>": { deps: ["<fqcn>", …], prior: bool }, … }} for the
     * group and everything it transitively depends on, in dependency order.
     * Class names need no escaping; the resolver refuses cycles.
     *
     * <p>RFC 0066 — the deployment's {@code priors} (the global palette) come
     * first, as nodes with no dependencies, and every non-prior node in the
     * closure lists them: the edge every class has by definition, written
     * here once rather than declared 179 times. A prior in the closure by its
     * own right (a group that is one) gains no dependencies — a prior declares
     * none — and a prior named twice is one node.</p>
     */
    static String subgraphJs(CssGroup<?> root, List<CssGroup<?>> priors) { return subgraphJs(root, priors, g -> true); }

    static String subgraphJs(CssGroup<?> root, List<CssGroup<?>> priors, Predicate<CssGroup<?>> varies) {
        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        List<String> priorNames = new ArrayList<>();
        for (CssGroup<?> p : priors) {
            String name = p.getClass().getCanonicalName();
            if (priorNames.contains(name)) continue;
            priorNames.add(name);
            if (!first) sb.append(", ");
            first = false;
            sb.append('"').append(name).append("\": { deps: [], prior: true }");
        }
        for (CssGroup<?> g : CssGroupResolver.resolve(List.of(root))) {
            String name = g.getClass().getCanonicalName();
            if (priorNames.contains(name)) continue;
            if (!first) sb.append(", ");
            first = false;
            sb.append('"').append(name).append("\": { deps: [");
            boolean firstDep = true;
            if (!g.prior()) {
                for (String p : priorNames) {
                    if (!firstDep) sb.append(", ");
                    firstDep = false;
                    sb.append('"').append(p).append('"');
                }
            }
            for (CssGroup<?> dep : CssImportsFor.dependenciesOf(g)) {
                if (!firstDep) sb.append(", ");
                firstDep = false;
                sb.append('"').append(dep.getClass().getCanonicalName()).append('"');
            }
            sb.append("]").append(g.prior() ? ", prior: true" : "").append(varies.test(g) ? "" : ", varies: false").append(" }");
        }
        return sb.append(" }").toString();
    }
}
