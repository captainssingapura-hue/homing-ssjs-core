package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The declarations of one casting's rule, every colour held to the tree and the style's scheme (RFC 0067, appendices Two
 * Classes per Leaf and The Style Jars): nothing !important, the palette's variables only named, a colour of its own
 * assigned the one value the tree gives it, and no literal colour. It keeps what it finds in the problems it is given,
 * and the tree's colours used, as it goes. Run by {@link CastingRule}, which reads the rule's shape.
 */
final class CastingColours {

    private static final Pattern VAR = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)\\s*(,)?");
    private static final Pattern HEX = Pattern.compile("(?<![\\w-])#[0-9a-fA-F]{3,8}(?![\\w-])");
    private static final Pattern FUNCTION = Pattern.compile(
            "(?i)(?<![\\w-])(rgba?|hsla?|hwb|lab|lch|oklab|oklch|color|color-mix|light-dark|device-cmyk|contrast-color)\\(");
    private static final Pattern WORD = Pattern.compile("(?<![\\w#.-])[A-Za-z][\\w-]*");
    private static final Pattern STRING = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'");
    private static final Pattern URL = Pattern.compile("(?i)url\\([^)]*\\)");

    private final Taxonomy taxonomy;
    private final ColorTree tree;
    private final DesignStyle<?> style;
    private final Casting leaf;
    private final ColorScheme scheme;
    private final String name;
    private final List<String> problems;
    private final Map<String, ColorNode<?, ?, ?>> nodes = new HashMap<>();
    /** The implementation's own spectra, by name: read before the tree's scale of the same name (D41). */
    private final Map<String, ColorScale<?, ?>> spectra = new HashMap<>();
    private final Set<String> assigned = new HashSet<>();
    private final Map<String, Integer> used = new LinkedHashMap<>();
    private final Map<String, String> uses = new LinkedHashMap<>();

    /** For one casting's rule, its findings kept in {@code problems}, with the implementation's own spectra (D41). */
    CastingColours(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting leaf, String name,
                   List<? extends ColorScale<?, ?>> spectra, List<String> problems) {
        this.taxonomy = taxonomy;
        this.tree = tree;
        this.style = style;
        this.leaf = leaf;
        this.scheme = tree.scheme(style).orElse(null);
        this.name = name;
        this.problems = problems;
        for (ColorNode<?, ?, ?> n : tree.nodes()) nodes.put(n.token(), n);
        for (ColorScale<?, ?> s : spectra) this.spectra.put(s.token(), s);
    }

    /** Its colours of its own, each assigned where it is used. */
    void allAssigned() {
        for (Map.Entry<String, Integer> u : used.entrySet())
            if (!assigned.contains(u.getKey()))
                problems.add(name + ":" + u.getValue() + ": var(" + u.getKey() + ") is named, and not assigned in the leaf's own rule: "
                        + "a colour of its own is assigned where it is used (D13, D14)");
    }

    /** The tree's colours its declarations use, each by its token, and where first: none named only in a colour of its own's value (D32). */
    Map<String, String> uses() { return uses; }

    /** A declaration, in the leaf's own rule - {@code where} none - or in a state's, a pseudo-element's, an @media's, a keyframe. */
    void declaration(ReadCss.Declaration d, String where) {
        String property = d.property(), value = d.value();
        if (value.toLowerCase(Locale.ROOT).contains("!important"))
            at(d, property + " is !important, which overrules the leaf's precedence: the order of its states decides (D22)");
        boolean local = false;
        if (property.startsWith("--fp-"))
            at(d, property + " is the palette's, defined by its sheet: a casting's rule names it, var(" + property + "), and never assigns it (D5)");
        else if (CastingWords.LOCAL.all().stream().anyMatch(property::startsWith)) {
            local = true;
            if (where != null) at(d, property + " is assigned in " + where + ": a colour of the leaf's own is assigned in the leaf's own rule (D13, D14)");
            if (!assigned.add(property)) at(d, property + " is assigned twice");
            local(d);
        }
        names(d, local);
        if (!local) literals(d);
    }

    /** A colour assigned in the leaf's own CSS: the one value the tree gives it. */
    private void local(ReadCss.Declaration d) {
        String property = d.property();
        String kind = property.startsWith("--fixed-") ? "--fixed-" : property.startsWith("--it-") ? "--it-"
                : property.startsWith("--current-") ? "--current-" : "--along-";
        String token = property.substring(kind.length());
        ColorNode<?, ?, ?> node = kind.equals("--along-") && spectra.containsKey(token) ? spectra.get(token) : nodes.get(token);
        boolean own = node != null && spectra.get(token) == node;
        var colours = LocalColours.INSTANCE;
        String expected = switch (kind) {
            case "--fixed-" -> node instanceof Fixed<?, ?> f ? colours.fixed(f) : null;
            case "--it-" -> node instanceof Interpolated<?, ?> i ? colours.interpolated(i) : null;
            case "--current-" -> node instanceof CurrentColor<?, ?> k ? colours.current(k) : null;
            default -> node instanceof ColorScale<?, ?> s ? colours.along(s) : null;
        };
        if (expected == null) {
            at(d, property + " names no " + (kind.equals("--fixed-") ? "fixed colour" : kind.equals("--it-") ? "interpolated colour"
                    : kind.equals("--current-") ? "share of the current colour" : "colour scale")
                    + " of the tree" + (node == null ? "" : ": " + token + " is " + kindOf(node)));
            return;
        }
        if (!own && !tree.sees(scheme, node)) {
            at(d, property + ": " + token + " is " + tree.schemeName(tree.scheme(node)) + "'s, which " + style.token() + "'s scheme, "
                    + tree.schemeName(scheme) + ", does not see");
            return;
        }
        if (kind.equals("--along-") && !taxonomy.extents(leaf.cast()).contains(ExtentAxis.COLOUR))
            at(d, property + " moves a colour by the element's degree, and " + leaf.token() + " has no colour axis: only a leaf with it may (D14)");
        if (!ReadCss.INSTANCE.squeeze(d.value()).equals(ReadCss.INSTANCE.squeeze(expected)))
            at(d, property + " is " + expected + (own ? " by its own spectrum" : " by the tree") + ", not " + d.value());
    }

    /**
     * Every variable a value names: a palette's must be one the style's scheme sees; a local one, assigned; none falling
     * back. Each a use of the design's - but in the value of a colour of its own, where the tree's value names them (D32).
     */
    private void names(ReadCss.Declaration d, boolean local) {
        Matcher m = VAR.matcher(d.value());
        while (m.find()) {
            String variable = m.group(1);
            boolean colour = variable.startsWith("--fp-") || CastingWords.LOCAL.all().stream().anyMatch(variable::startsWith);
            if (colour && m.group(2) != null)
                at(d, "var(" + variable + ", ...) falls back: a palette's sheet is complete and a local colour assigned, so no colour ever does (D5)");
            if (variable.startsWith("--fp-")) {
                String token = variable.substring("--fp-".length());
                ColorNode<?, ?, ?> node = nodes.get(token);
                if (!(node instanceof Palettable<?, ?>))
                    at(d, variable + " names no palettable colour of the tree" + (node == null ? "" : ": " + token + " is " + kindOf(node)));
                else if (!tree.sees(scheme, node))
                    at(d, variable + ": " + token + " is " + tree.schemeName(tree.scheme(node)) + "'s, which " + style.token() + "'s scheme, "
                            + tree.schemeName(scheme) + ", does not see (D7)");
                if (node instanceof Palettable<?, ?> && !local) uses.putIfAbsent(token, name + ":" + d.line());
            } else if (colour) {
                used.putIfAbsent(variable, d.line());
                String prefix = CastingWords.LOCAL.all().stream().filter(variable::startsWith).findFirst().orElseThrow();
                String token = variable.substring(prefix.length());
                if (!local && (nodes.containsKey(token) || spectra.containsKey(token))) uses.putIfAbsent(token, name + ":" + d.line());
            }
        }
    }

    /** No literal colour: every colour a variable. */
    private void literals(ReadCss.Declaration d) {
        String value = URL.matcher(STRING.matcher(d.value()).replaceAll("\"\"")).replaceAll("url()");
        String why = ": a style-aware file names its colours by variable, var(--fp-) or one assigned in the leaf's rule (D13)";
        for (Matcher m = HEX.matcher(value); m.find(); ) at(d, m.group() + " is a literal colour" + why);
        for (Matcher m = FUNCTION.matcher(value); m.find(); )
            at(d, m.group(1).toLowerCase(Locale.ROOT).equals("color-mix")
                    ? "color-mix( makes a colour the tree does not have: a mix is an interpolated colour, or one along a scale, assigned as the tree gives it (D13, D14)"
                    : m.group() + " writes a literal colour" + why);
        for (Matcher m = WORD.matcher(value); m.find(); ) {
            String word = m.group().toLowerCase(Locale.ROOT);
            if (CastingWords.NAMED.all().contains(word)) at(d, m.group() + " is a named colour" + why);
            else if (CastingWords.SYSTEM.all().contains(word)) at(d, m.group() + " is a system colour" + why);
        }
    }

    private void at(ReadCss.Item item, String says) { problems.add(name + ":" + item.line() + ": " + says); }

    private String kindOf(ColorNode<?, ?, ?> node) {
        return switch (node) {
            case Palettable<?, ?> p -> "a palettable colour, var(--fp-" + p.token() + ")";
            case Fixed<?, ?> f -> "a fixed colour, --fixed-" + f.token();
            case Interpolated<?, ?> i -> "an interpolated colour, --it-" + i.token();
            case CurrentColor<?, ?> k -> "the current colour at a share, --current-" + k.token();
            case ColorScale<?, ?> s -> "a colour scale, --along-" + s.token();
            default -> "a branch";
        };
    }
}
