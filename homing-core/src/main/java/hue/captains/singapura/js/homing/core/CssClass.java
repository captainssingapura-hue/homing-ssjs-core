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
     * The class's rule: its declarations, the same in every theme - a provided
     * class does not vary (RFC 0064, D25). What a theme says of the element it
     * says through the component's look; a body that reads a {@code var(--…)} reads a value set
     * elsewhere and says none itself.
     *
     * <p>Default: {@code null}, which the server renders as a comment that the
     * class has no body.</p>
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
