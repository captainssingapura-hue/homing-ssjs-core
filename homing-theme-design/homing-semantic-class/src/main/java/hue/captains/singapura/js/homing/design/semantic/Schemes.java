package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * The schemes things are in, as each says it (RFC 0067, D49): a style's, a palette's, a local style's, a declaration's - by
 * the scheme each answers, never read off its type - and how schemes stand to one another, by the scheme each says it
 * is on top of. A scheme is a stateless object: any two of one scheme are the same.
 */
public record Schemes() implements StatelessFunctionalObject {

    public static final Schemes INSTANCE = new Schemes();

    /** The scheme a style requires. */
    public Optional<ColorScheme> of(DesignStyle<?> style) { return Optional.ofNullable(style.scheme()); }

    /** The scheme a palette implements. */
    public Optional<ColorScheme> of(ColorPalette<?> palette) { return Optional.ofNullable(palette.scheme()); }

    /** The scheme a local style is in (D33). */
    public Optional<ColorScheme> of(LocalStyle<?, ?> localStyle) { return Optional.ofNullable(localStyle.scheme()); }

    /** The scheme a declaration declares. */
    public Optional<ColorScheme> of(SchemeDeclaration<?> declaration) { return Optional.ofNullable(declaration.scheme()); }

    /** The scheme below one: the scheme it says it is on top of, or none for a scheme from scratch. */
    public Optional<ColorScheme> below(ColorScheme scheme) { return scheme.below(); }

    /** Whether two are one scheme: a scheme is stateless, every object of it the same. */
    public boolean same(ColorScheme a, ColorScheme b) { return a != null && b != null && a.getClass() == b.getClass(); }

    /** Whether a scheme sees what is in another: its own, and what every scheme below it has. */
    public boolean sees(ColorScheme scheme, ColorScheme of) {
        var passed = new HashSet<Class<?>>();
        for (ColorScheme at = scheme; at != null && passed.add(at.getClass()); at = at.below().orElse(null))
            if (same(at, of)) return true;
        return false;
    }

    /** A scheme's layers: the schemes from its base up to it. */
    public List<ColorScheme> layers(ColorScheme scheme) {
        var out = new ArrayList<ColorScheme>();
        var passed = new HashSet<Class<?>>();
        for (ColorScheme at = scheme; at != null && passed.add(at.getClass()); at = at.below().orElse(null)) out.add(0, at);
        return out;
    }

    /** The scheme's name, as messages say it. */
    public String name(ColorScheme scheme) { return scheme == null ? "no scheme" : scheme.name(); }
}
