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
 *
 * <p>{@code defaultTheme} is the deployment's default — the theme the client's
 * manager falls back to until the address or the stored pick says otherwise. It
 * is the server's, never the request's: a module varies by no context, so its
 * URL is its class alone.</p>
 */
public record CssGroupContentProvider<C extends CssGroup<C>>(
        C cssGroup, String defaultTheme, ModuleNameResolver nameResolver, Predicate<CssGroup<?>> varies
) implements ContentProvider<C> {

    public CssGroupContentProvider(C cssGroup, String defaultTheme, ModuleNameResolver nameResolver) {
        this(cssGroup, defaultTheme, nameResolver, g -> true);
    }

    @Override
    public List<String> content() {
        List<String> lines = new ArrayList<>();
        String managerPath = nameResolver.resolve(CssClassManager.INSTANCE).basePath();
        String groupName = cssGroup.getClass().getCanonicalName();
        // Always the second argument, null where the deployment has no theme: the
        // manager reads its arguments by position, and the subgraph is the third.
        String themeArg = defaultTheme != null ? ", \"" + defaultTheme + "\"" : ", null";

        lines.add("import { CssClassManagerInstance as _css } from \"" + managerPath + "\";");
        // RFC 0064 — the group carries its transitive dependency subgraph as
        // DATA, derived from its classes' dependsOn() on the server, so the
        // client's CSS manager can load a lazily mounted widget's whole tree
        // — missing dependencies included, by name — and can order a theme
        // switch by the same graph. Not ES imports: a dependency's CSS is
        // needed, its handles are not.
        lines.add("await _css.loadCss(\"" + groupName + "\"" + themeArg + ", " + subgraphJs(cssGroup, varies) + ");");

        for (CssClass<C> cls : cssGroup.cssClasses()) {
            String recordName = cls.getClass().getSimpleName();
            String cssName = CssClassName.toCssName(cls.getClass());
            var variants = cls.variants();
            // The design classes this class wears travel with the handle, as tokens:
            // the manager adds them to the element beside the class itself.
            String wears = cls.wears().isEmpty() ? "" : cls.wears().stream()
                    .map(w -> "\"" + w.cssName() + "\"")
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
     * {@code { "<fqcn>": { deps: ["<fqcn>", …] }, … }} for the group and
     * everything it transitively depends on, in dependency order. Class names
     * need no escaping; the resolver refuses cycles.
     */
    static String subgraphJs(CssGroup<?> root) { return subgraphJs(root, g -> true); }

    static String subgraphJs(CssGroup<?> root, Predicate<CssGroup<?>> varies) {
        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        for (CssGroup<?> g : CssGroupResolver.resolve(List.of(root))) {
            if (!first) sb.append(", ");
            first = false;
            sb.append('"').append(g.getClass().getCanonicalName()).append("\": { deps: [");
            boolean firstDep = true;
            for (CssGroup<?> dep : CssImportsFor.dependenciesOf(g)) {
                if (!firstDep) sb.append(", ");
                firstDep = false;
                sb.append('"').append(dep.getClass().getCanonicalName()).append('"');
            }
            sb.append("]").append(varies.test(g) ? "" : ", varies: false").append(" }");
        }
        return sb.append(" }").toString();
    }
}
