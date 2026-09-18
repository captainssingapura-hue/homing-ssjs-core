package hue.captains.singapura.js.homing.core;

import java.util.List;
import java.util.Set;

/**
 * A CSS class declared within a {@link CssGroup}.
 * <p>Mirrors {@link SvgBeing} for {@link SvgGroup}: each implementing
 * record is an exportable constant whose simple name (snake_case)
 * maps 1:1 to a kebab-case CSS class name.</p>
 *
 * <p>RFC 0002-ext1: a record may override {@link #pseudoState()} to render
 * with a pseudo-class suffix (e.g. {@code .foo:hover}), or override
 * {@link #variants()} to have the framework auto-generate hover/focus/active
 * (etc.) variants alongside the base rule. For utility bases that want all
 * three common states, see {@link UtilityCssClass}.</p>
 *
 * @param <C> the CssGroup this class belongs to
 */
public interface CssClass<C extends CssGroup<C>> extends Exportable._Constant<C> {

    /**
     * Optional pseudo-class suffix appended to the rendered selector.
     * For example, returning {@code ":hover"} renders the rule as
     * {@code .kebab-name:hover { … }} instead of {@code .kebab-name { … }}.
     *
     * <p>Default: {@code null} (bare class selector). Existing records
     * are unaffected — this is a non-breaking addition.</p>
     */
    default String pseudoState() { return null; }

    /**
     * RFC 0066 — a selector that is not the class. The one legitimate case is
     * a rule over an element no class reaches: the page reset, {@code html,
     * body}. Such a class is still a node of the graph (others depend on it,
     * a theme may override it), still exports a handle nobody needs, and
     * renders with this selector instead of {@code .kebab-name}. Default
     * {@code null}: the class selector, with {@link #pseudoState()} appended.
     */
    default String selector() { return null; }

    /**
     * RFC 0066 Law 5 — custom properties this class reads that are set at
     * RUNTIME by the module that owns it ({@code el.style.setProperty("--hgr-guide-x", …)}),
     * never by a theme. Declared so the token law can tell a runtime value from
     * a palette token the theme forgot: everything a body reads through
     * {@code var(--…)} must be declared by a palette the class reaches, unless
     * it is named here. Default: none.
     */
    default Set<CssVar> runtimeVars() { return Set.of(); }

    /**
     * Pseudo-state variants the framework should auto-generate for this base.
     *
     * <p>For each state {@code s} in the returned set, the framework synthesizes
     * an additional CSS rule {@code .<s>-<kebab>:<s> { <body> }} (reusing the
     * base's body) and exposes a {@code .<s>} property on the JS-side handle
     * pointing to a dedicated variant CssClass instance.</p>
     *
     * <p>Recognized states: {@code "hover"}, {@code "focus"}, {@code "active"}.
     * Unknown states still emit CSS rules but get a plain {@code CssClass}
     * instance on the JS side (no dedicated subclass).</p>
     *
     * <p>Default: empty (no variants). Utility bases override; component
     * classes typically don't.</p>
     */
    default Set<String> variants() { return Set.of(); }

    /**
     * Inline CSS body for this class. When non-null, the framework renders the
     * class using this string and does <em>not</em> consult a per-theme impl
     * method. When null (default), the renderer falls back to looking up a
     * matching method on the impl via reflection — the legacy theme-bound
     * pattern.
     *
     * <p>RFC 0002-ext1 (Phase 05): inline bodies are the prerequisite for
     * theme-agnostic utility classes (Phase 06) and for the marker-shape
     * refactor (Phase 09). Class bodies that reference {@code var(--…)} CSS
     * custom properties resolve through the active theme's cascade — they
     * remain theme-aware via the variable layer without needing a per-theme
     * impl method.</p>
     *
     * <p>Default: {@code null} — the renderer uses the impl-method dispatch
     * path. Existing CssClass records are unaffected.</p>
     */
    default String body() { return null; }

    /**
     * RFC 0064 — the classes this one's rules lean on: a class whose body
     * assumes another's is present on the same element or an ancestor, or
     * that must follow another's in the cascade to win at equal specificity.
     * A dependency in another group makes that group a dependency of this
     * one — {@link CssGroup#cssImports()} derives it, so a group cannot forget
     * to import what its classes need.
     *
     * <p>Declared here, per class, because that is where the knowledge is:
     * the author of {@code tp_body} knows it lays out inside {@code md_body};
     * nobody else does. The client-side CSS manager loads groups in the order
     * these dependencies induce, and switches themes the same way.</p>
     *
     * <p>Default: none.</p>
     */
    default List<CssClass<?>> dependsOn() { return List.of(); }

    /**
     * What this class's element wears beside it: tokens applied with the
     * class by the client manager whenever this class is added, and removed
     * with it — so a component declares once, in Java, what its element means
     * and where that shows, and its JS adds one class as before. A worn
     * token's group is a dependency of this class's group, like
     * {@link #dependsOn()}; unlike it, a worn token is put on the element. This
     * class's own body carries structure and nothing a worn token says.
     * Default: none.
     */
    default List<? extends Wearable> wears() { return List.of(); }

    /**
     * What this class's element READS of a design without wearing it: the
     * pairs whose variables its body names by reference — {@code var(--…)}
     * of the pair's root binding — where wearing would not do. Two places:
     * a pseudo-state rule, which a worn pair cannot condition, so a frame
     * lit under {@code :focus-within} reads the design's ring colour into
     * its own rule; and a channel of the component's own, a custom property
     * an ancestor sets and every slot beneath reads, whose value is the
     * design's word passed down. Read, not worn: the pair's group is a
     * dependency and its binding is emitted, and nothing is put on the
     * element. The value is still the design's — a body reads a word, it
     * never says one. Default: none.
     */
    default List<? extends Wearable> reads() { return List.of(); }

    /**
     * The group this class belongs to, from its declaration: a {@code CssClass}
     * is a record nested in its group, and the group's {@code INSTANCE} is the
     * one object of that class. Used to derive group dependencies from class
     * dependencies.
     */
    static CssGroup<?> groupOf(CssClass<?> cls) {
        Class<?> enclosing = cls.getClass().getEnclosingClass();
        if (enclosing == null || !CssGroup.class.isAssignableFrom(enclosing)) {
            throw new IllegalStateException(
                    "CssClass " + cls.getClass().getName() + " is not nested in a CssGroup");
        }
        try {
            var field = enclosing.getDeclaredField("INSTANCE");
            field.trySetAccessible();          // a test's package-private group is still a group
            return (CssGroup<?>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "CssGroup " + enclosing.getName() + " has no public static INSTANCE", e);
        }
    }
}
