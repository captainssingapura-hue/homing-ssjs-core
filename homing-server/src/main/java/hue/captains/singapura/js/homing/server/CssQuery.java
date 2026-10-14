package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.tao.http.action.Param;

/**
 * What {@code /css-content} is asked: a sheet by its canonical name, and what each kind of derived sheet varies with -
 * the theme, the style, the palette - each by its own name. A sheet reads only its own: a style's sheet the style, a
 * palette's the palette.
 */
public record CssQuery(String className, String theme, String style, String palette) implements Param._QueryString {

    /** What the request names for what the sheet varies with; null when it names nothing. */
    public String slugFor(DerivedCss.VariesWith variesWith) {
        String slug = switch (variesWith) {
            case THEME -> theme;
            case STYLE -> style;
            case PALETTE -> palette;
        };
        return slug == null || slug.isBlank() ? null : slug;
    }
}
