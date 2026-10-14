package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * What a deployment styles with (RFC 0067, appendix Two Classes per Leaf): its leaves, its colour tree, the styles and
 * the palettes it serves - the first of each its default - and the two sheets a page wears, each by its own slug and
 * swapped on its own (D19): a style's sheet, the same in every palette, and a palette's, the same under every style.
 * Refused when it lists nothing, or two of one token.
 *
 * @param taxonomy the leaves
 * @param colours  the colour tree
 * @param styles   the styles served, the default first
 * @param palettes the palettes served, the default first
 */
public record Styling(Taxonomy taxonomy, ColorTree colours, List<DesignStyle<?>> styles, List<ColorPalette<?>> palettes) {

    public Styling {
        styles = List.copyOf(styles);
        palettes = List.copyOf(palettes);
        var problems = new ArrayList<String>();
        if (styles.isEmpty()) problems.add("it serves no style");
        if (palettes.isEmpty()) problems.add("it serves no palette");
        var seen = new HashSet<String>();
        for (DesignStyle<?> s : styles) if (!seen.add(s.token())) problems.add("two styles are " + s.token());
        seen.clear();
        for (ColorPalette<?> p : palettes) if (!seen.add(p.token())) problems.add("two palettes are " + p.token());
        if (!problems.isEmpty()) throw new IllegalArgumentException("a styling cannot be made: " + String.join("; ", problems));
    }

    /** The style a page wears until the address or a pick says otherwise. */
    public DesignStyle<?> defaultStyle() { return styles.get(0); }

    /** The palette a page wears until the address or a pick says otherwise. */
    public ColorPalette<?> defaultPalette() { return palettes.get(0); }

    /** A style served, by its token. */
    public Optional<DesignStyle<?>> style(String token) { return styles.stream().filter(s -> s.token().equals(token)).findFirst(); }

    /** A palette served, by its token. */
    public Optional<ColorPalette<?>> palette(String token) { return palettes.stream().filter(p -> p.token().equals(token)).findFirst(); }

    /** A style's sheet, by its token: one class for every leaf. Refused for a style not served. */
    public String styleSheet(String token) {
        return StyleSheet.INSTANCE.write(taxonomy, style(token).orElseThrow(() -> new IllegalArgumentException("no style served is " + token)));
    }

    /** A palette's sheet, by its token: every --fp- variable its scheme sees. Refused for a palette not served. */
    public String paletteSheet(String token) {
        return PaletteSheet.INSTANCE.write(colours, palette(token).orElseThrow(() -> new IllegalArgumentException("no palette served is " + token))).css();
    }
}
