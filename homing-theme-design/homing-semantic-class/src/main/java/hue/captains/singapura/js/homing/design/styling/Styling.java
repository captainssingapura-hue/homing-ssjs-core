package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.Dressed;
import hue.captains.singapura.js.homing.design.semantic.Wear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What an instance styles with (RFC 0067, appendix The Style Jars): the second stage of its registration, where its
 * styles and its palettes meet. The styles it serves - each registered with the central manager, the default first -
 * the palettes, from the instance's registry of them, the default first, and which palette is each style's own, a
 * pairing neither a style nor a palette may know. It serves the two sheets a page wears, each by its own slug and
 * swapped on its own (appendix Two Classes per Leaf, D19): a style's sheet, aggregated by the manager and the same in
 * every palette, and a palette's, the same under every style. The themes - each style in each palette that fits it -
 * keep their slugs for what still names a theme by one word: the style's token alone in its own palette, the style's
 * then the palette's, joined by an underscore, in another.
 *
 * <p>The styles and the palettes are each registered, and held to their own checks, apart ({@link StyleManager},
 * {@link PaletteRegistry}, D47): here only their meeting is held. Refused when it serves no style, two of one token, a
 * style the manager does not register, a style with no palette of its own, or one not registered or not fitting it; and
 * when the styles and the palettes hold two different trees of one scheme - an instance registers its schemes once. A palette
 * nothing served wears is no problem.</p>
 *
 * @param manager  the central style manager: every registered style with its jars, a base layer among them
 * @param styles   the styles served, the default first
 * @param registry the instance's palettes, its registry of every jar's
 * @param own      each style's own palette
 */
public record Styling(StyleManager manager, List<DesignStyle<?>> styles, PaletteRegistry registry,
                      Map<DesignStyle<?>, ColorPalette<?>> own) {

    public Styling {
        styles = List.copyOf(styles);
        own = Map.copyOf(own);
        var problems = new ArrayList<String>();
        if (styles.isEmpty()) problems.add("it serves no style");
        var seen = new HashSet<String>();
        for (DesignStyle<?> s : styles) if (!seen.add(s.token())) problems.add("two styles are " + s.token());
        for (var e : manager.trees().entrySet()) {
            ColorTree theirs = RegisteredPalettes.INSTANCE.trees(registry).get(e.getKey());
            if (theirs != null && !theirs.equals(e.getValue()))
                problems.add("its styles and its palettes hold two different trees of " + e.getKey().getSimpleName() + ": an instance registers its schemes once");
        }
        List<ColorPalette<?>> registered = RegisteredPalettes.INSTANCE.palettes(registry);
        for (DesignStyle<?> s : styles) {
            if (manager.style(s.token()).isEmpty()) problems.add(s.token() + " is served, and the manager does not register it");
            ColorPalette<?> p = own.get(s);
            if (p == null) problems.add(s.token() + " has no palette of its own");
            else if (!registered.contains(p)) problems.add(s.token() + "'s own palette, " + p.token() + ", is not registered");
            else if (!Wear.INSTANCE.fits(s, p)) problems.add(s.token() + "'s own palette, " + p.token() + ", does not fit it");
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("a styling cannot be made: " + String.join("; ", problems));
    }

    /** The palettes served: every registered jar's, in the order registered, the default first. */
    public List<ColorPalette<?>> palettes() { return RegisteredPalettes.INSTANCE.palettes(registry); }

    /** The leaves the manager styles. */
    public Taxonomy taxonomy() { return manager.taxonomy(); }

    /** The colour tree a palette is written over: its scheme's, as the registry read it. */
    public ColorTree colours(ColorPalette<?> palette) { return RegisteredPalettes.INSTANCE.tree(registry, palette); }

    /** The colour tree a style is drawn over: its scheme's. */
    public ColorTree colours(DesignStyle<?> style) { return manager.tree(style); }

    /** The style a page wears until the address or a pick says otherwise. */
    public DesignStyle<?> defaultStyle() { return styles.get(0); }

    /** The palette a page wears until the address or a pick says otherwise. */
    public ColorPalette<?> defaultPalette() { return palettes().get(0); }

    /** A style served, by its token. */
    public Optional<DesignStyle<?>> style(String token) { return styles.stream().filter(s -> s.token().equals(token)).findFirst(); }

    /** A palette served, by its token. */
    public Optional<ColorPalette<?>> palette(String token) { return palettes().stream().filter(p -> p.token().equals(token)).findFirst(); }

    /** A style's sheet, by its token: one class for every casting, as the manager aggregates it. Refused for a style not served. */
    public String styleSheet(String token) {
        return manager.sheet(style(token).orElseThrow(() -> new IllegalArgumentException("no style served is " + token)));
    }

    /** A palette's sheet, by its token: every --fp- variable its scheme sees. Refused for a palette not served. */
    public String paletteSheet(String token) {
        ColorPalette<?> palette = palette(token).orElseThrow(() -> new IllegalArgumentException("no palette served is " + token));
        return PaletteSheet.INSTANCE.write(colours(palette), palette).css();
    }

    /** A style's own palette. */
    public ColorPalette<?> own(DesignStyle<?> style) {
        ColorPalette<?> p = own.get(style);
        if (p == null) throw new IllegalArgumentException("no style served is " + style.token());
        return p;
    }

    /** Whether a palette may dress a style: both served, and the palette of a scheme the style can wear (D22). */
    public boolean fits(DesignStyle<?> style, ColorPalette<?> palette) {
        return styles.contains(style) && palettes().contains(palette) && Wear.INSTANCE.fits(style, palette);
    }

    /** A style in a palette, by its slug: {@code glassmorphism} in its own, {@code glassmorphism_harbour} in another. */
    public String slug(DesignStyle<?> style, ColorPalette<?> palette) {
        return palette.equals(own(style)) ? style.token() : style.token() + "_" + palette.token();
    }

    /** A theme's slug. */
    public String slug(Dressed theme) { return slug(theme.style(), theme.palette()); }

    /** Every style in every palette that fits it, by its slug: each style in its own palette first, the default first of all. */
    public Map<String, Dressed> themes() {
        var out = new LinkedHashMap<String, Dressed>();
        for (DesignStyle<?> s : styles) out.put(s.token(), wear(s, own(s)));
        for (DesignStyle<?> s : styles)
            for (ColorPalette<?> p : palettes()) if (Wear.INSTANCE.fits(s, p)) out.putIfAbsent(slug(s, p), wear(s, p));
        return Collections.unmodifiableMap(out);
    }

    /**
     * The theme a page is worn in, from its slug: the style its first word names, in the palette its second names. A
     * style not served is the default style; a palette not served, not fitting, or not named, the style's own.
     */
    public Dressed dressed(String themeSlug) {
        String[] words = (themeSlug == null ? "" : themeSlug.strip()).split("_", 2);
        DesignStyle<?> style = style(words[0]).orElse(defaultStyle());
        ColorPalette<?> palette = words.length == 2 ? palette(words[1]).filter(p -> Wear.INSTANCE.fits(style, p)).orElse(own(style)) : own(style);
        return wear(style, palette);
    }

    private Dressed wear(DesignStyle<?> style, ColorPalette<?> palette) {
        return Wear.INSTANCE.of(colours(style), style, palette).orElseThrow(() -> new IllegalArgumentException(
                palette.token() + " does not fit " + style.token()));
    }
}
