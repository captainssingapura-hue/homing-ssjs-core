package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * Converts a {@link CssClass} record's snake_case simple name
 * to its kebab-case CSS class name.
 */
public record CssClassName() implements StatelessFunctionalObject {

    public static final CssClassName INSTANCE = new CssClassName();

    /**
     * Converts snake_case record name to kebab-case CSS class name.
     * <p>Example: {@code pg_theme_btn} → {@code "pg-theme-btn"}</p>
     */
    @SuppressWarnings("rawtypes")
    public String toCssName(Class<? extends CssClass> clazz) {
        return clazz.getSimpleName().replace('_', '-');
    }
}
