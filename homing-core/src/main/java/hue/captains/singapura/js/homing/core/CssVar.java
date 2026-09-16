package hue.captains.singapura.js.homing.core;

/**
 * RFC 0002-ext1 Phase 09 — typed CSS custom property reference.
 *
 * <p>Each {@code CssVar} is a singleton instance representing one CSS variable
 * name (e.g. {@code "--color-accent"}). Deployments collect their vocabulary
 * as static constants in a class like {@code StudioVars} or {@code DemoVars}:</p>
 *
 * <pre>
 *   public final class StudioVars {
 *       public static final CssVar COLOR_SURFACE        = new CssVar("--color-surface");
 *       public static final CssVar COLOR_SURFACE_RAISED = new CssVar("--color-surface-raised");
 *       public static final CssVar SPACE_4              = new CssVar("--space-4");
 *       // …
 *   }
 * </pre>
 *
 * <p>Use sites:</p>
 * <ul>
 *   <li>{@link Themed#requiredVars()} — declares which vars a class depends on (machine-readable contract).</li>
 *   <li>{@link PaletteProvision#values()} — keys the per-theme binding of a {@link PaletteClass}.</li>
 *   <li>Class bodies — reference via {@link #ref()} to produce {@code "var(--color-surface)"}.</li>
 * </ul>
 *
 * @param name the variable name including the leading {@code "--"}
 */
public record CssVar(String name) {

    public CssVar {
        if (name == null || !name.startsWith("--")) {
            throw new IllegalArgumentException(
                    "CssVar name must start with \"--\": got " + name);
        }
    }

    /** Returns {@code "var(--name)"} — for use in CSS body strings. */
    public String ref() {
        return "var(" + name + ")";
    }
}
