package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentLeaf;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.StylingRes;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The conformance check of a style's files (RFC 0067, appendix Two Classes per Leaf), starting from the Java leaves
 * (D7): for every leaf, the style's answer (D25) and its file (D9) - so a leaf without CSS and CSS without a leaf are
 * both found - and every file held to its leaf and to the colour tree.
 *
 * <p>A style:</p>
 * <ul>
 *   <li>requires a scheme, and a base layer - one that derives from nothing - answers content for every leaf, an
 *       extension's included (D26);</li>
 *   <li>has a file for every leaf it answers content for, and no file for one it leaves absent, nor one that is no
 *       leaf's.</li>
 * </ul>
 *
 * <p>A leaf's file rules its own class, once, and nothing else; inside it, each state the leaf lists, drawn on the leaf's
 * own element - {@code &.styled-tab--selected} for an application state's value, {@code &:hover} for a pseudo state -
 * each once and holding declarations alone (D12). Every one of those selectors weighs the same, so the order the file
 * draws them in is what decides between two states at once: the leaf's precedence, the least important first, which a
 * soft check reports when it is not (D22, D23). No {@code !important}, and no at-rule, for now.</p>
 *
 * <p>Its colours, every one a variable:</p>
 * <ul>
 *   <li>{@code var(--fp-<leaf>)}, a palettable colour of the tree that the style's scheme sees, never assigned in a
 *       leaf's file - its palette's sheet defines it (D5, D7);</li>
 *   <li>{@code --fixed-<leaf>}, {@code --it-<leaf>}: assigned in the leaf's own rule, each to the one value the tree
 *       gives it, and named only where so assigned (D13);</li>
 *   <li>{@code --along-<scale>}: the same, and only on a leaf with the colour axis (D14);</li>
 *   <li>none of the four falling back, and no literal colour anywhere else - a hex, a colour function, a mix, a named
 *       or a system colour; {@code currentColor}, {@code transparent}, {@code inherit} and their like are CSS's own
 *       (D6).</li>
 * </ul>
 *
 * Every problem is found at once, each naming its file and line.
 */
public record CheckStyle() implements StatelessFunctionalObject {

    public static final CheckStyle INSTANCE = new CheckStyle();

    /**
     * What a check found.
     *
     * @param problems what refuses the style, each naming its file and line
     * @param reports  what the soft check says and does not refuse: a state drawn out of its leaf's precedence (D23)
     */
    public record Checked(List<String> problems, List<String> reports) implements ValueObject {
        public Checked { problems = List.copyOf(problems); reports = List.copyOf(reports); }

        /** Whether nothing refuses it. */
        public boolean passes() { return problems.isEmpty(); }
    }

    /**
     * Styles checked together, refused with every problem at once.
     *
     * @return the soft check's reports
     */
    public List<String> require(Taxonomy taxonomy, ColorTree tree, List<? extends DesignStyle<?>> styles) {
        var problems = new ArrayList<String>();
        var reports = new ArrayList<String>();
        for (DesignStyle<?> style : styles) {
            Checked c = check(taxonomy, tree, style);
            problems.addAll(c.problems());
            reports.addAll(c.reports());
        }
        if (!problems.isEmpty()) throw new IllegalArgumentException("the styles' files are refused:\n  " + String.join("\n  ", problems));
        return List.copyOf(reports);
    }

    /** A style over every leaf of the taxonomy: its answer for each, its files, and each file. */
    public Checked check(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style) {
        var problems = new ArrayList<String>();
        var reports = new ArrayList<String>();
        var files = StyleFiles.INSTANCE;
        if (tree.scheme(style).isEmpty()) {
            problems.add(style.token() + " requires no scheme: its type argument is the marker, a variable, or no abstract scheme");
            return new Checked(problems, reports);
        }
        boolean base = !(style instanceof Derives<?, ?>);
        if (!base && baseLayer(style).isEmpty()) problems.add(style.token() + " derives from a style that derives, in the end, from itself");
        var named = new HashSet<String>();
        for (ComponentLeaf leaf : taxonomy.leaves()) {
            named.add(leaf.token() + ".css");
            StylingRes answer = style.style(leaf);
            Optional<String> css = files.read(style, leaf);
            switch (answer) {
                case null -> problems.add(style.token() + " answers nothing for " + leaf.token() + ": content, or absence");
                case StylingRes.CssContent c -> {
                    if (css.isEmpty()) problems.add(style.token() + " answers content for " + leaf.token() + ", and has no file at " + files.path(style, leaf));
                    else {
                        Checked f = file(taxonomy, tree, style, leaf, css.get());
                        problems.addAll(f.problems());
                        reports.addAll(f.reports());
                    }
                }
                case StylingRes.Absence a -> {
                    if (base) problems.add(style.token() + " is a base layer, and answers absence for " + leaf.token()
                            + ": a base layer answers content for every leaf, an extension's too (D26)");
                    if (css.isPresent()) problems.add(files.name(style, leaf) + " is a file " + style.token() + " does not answer: it answers absence for "
                            + leaf.token() + ", so the base layer's is used - answer content, or remove the file");
                }
            }
        }
        for (String present : files.present(style))
            if (!named.contains(present)) problems.add(style.token() + "/" + present + " is no leaf's file: no leaf of the taxonomy is "
                    + (present.endsWith(".css") ? present.substring(0, present.length() - 4) : present + ", and a leaf's file is <leaf>.css"));
        return new Checked(problems, reports);
    }

    /** The base layer a style's absence lands on: the style it derives from, and so up, to one that derives from nothing. */
    public Optional<DesignStyle<?>> baseLayer(DesignStyle<?> style) {
        var passed = new HashSet<Class<?>>();
        DesignStyle<?> at = style;
        while (at instanceof Derives<?, ?> d) {
            if (!passed.add(at.getClass())) return Optional.empty();
            at = d.base();
        }
        return Optional.of(at);
    }

    /** One of a style's files, as written, for its leaf. */
    public Checked file(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, ComponentLeaf leaf, String css) {
        return new OneFile(taxonomy, tree, style, leaf).check(css);
    }

    /** The check of one file: what it found as it went. */
    private static final class OneFile {

        private static final Pattern VAR = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)\\s*(,)?");
        private static final Pattern HEX = Pattern.compile("(?<![\\w-])#[0-9a-fA-F]{3,8}(?![\\w-])");
        private static final Pattern FUNCTION = Pattern.compile(
                "(?i)(?<![\\w-])(rgba?|hsla?|hwb|lab|lch|oklab|oklch|color|color-mix|light-dark|device-cmyk|contrast-color)\\(");
        private static final Pattern WORD = Pattern.compile("(?<![\\w#.-])[A-Za-z][\\w-]*");
        private static final Pattern STRING = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'");
        private static final Pattern URL = Pattern.compile("(?i)url\\([^)]*\\)");
        private static final Set<String> LOCAL = Set.of("--fixed-", "--it-", "--along-");

        private final Taxonomy taxonomy;
        private final ColorTree tree;
        private final DesignStyle<?> style;
        private final ComponentLeaf leaf;
        private final Class<?> scheme;
        private final String name;
        private final Map<String, ColorNode<?, ?, ?>> nodes = new HashMap<>();
        private final List<String> problems = new ArrayList<>();
        private final List<String> reports = new ArrayList<>();
        private final Set<String> assigned = new HashSet<>();
        private final Map<String, Integer> used = new LinkedHashMap<>();

        OneFile(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, ComponentLeaf leaf) {
            this.taxonomy = taxonomy;
            this.tree = tree;
            this.style = style;
            this.leaf = leaf;
            this.scheme = tree.scheme(style).orElse(null);
            this.name = StyleFiles.INSTANCE.name(style, leaf);
            for (ColorNode<?, ?, ?> n : tree.nodes()) nodes.put(n.token(), n);
        }

        Checked check(String css) {
            if (scheme == null) {
                problems.add(name + ": " + style.token() + " requires no scheme, so no colour can be held to it");
                return new Checked(problems, reports);
            }
            ReadCss.Read read = ReadCss.INSTANCE.read(css);
            for (String p : read.problems()) problems.add(name + ":" + p);
            LeafClasses.Listing listing = LeafClasses.INSTANCE.of(taxonomy, leaf);
            String own = "." + listing.styled();
            ReadCss.Rule rule = null;
            for (ReadCss.Item item : read.items()) {
                switch (item) {
                    case ReadCss.Rule r when same(r.selector(), own) -> {
                        if (rule == null) rule = r;
                        else at(r, own + " is ruled twice: a leaf's file rules its class once, its states nested in it");
                    }
                    case ReadCss.Rule r -> at(r, r.selector() + " is not this leaf's: its file rules " + own + " alone, its states nested in it (D9, D10)");
                    case ReadCss.Declaration d -> at(d, d.property() + " is declared outside any rule");
                    case ReadCss.AtRule a -> at(a, "@" + a.prelude() + ": no at-rule is in a leaf's file, for now");
                }
            }
            if (rule == null) {
                problems.add(name + ": no rule for " + own + ": a leaf's file rules its own class");
                return new Checked(problems, reports);
            }
            leafRule(rule, listing);
            for (Map.Entry<String, Integer> u : used.entrySet())
                if (!assigned.contains(u.getKey()))
                    problems.add(name + ":" + u.getValue() + ": var(" + u.getKey() + ") is named, and not assigned in the leaf's own rule: "
                            + "a colour of its own is assigned where it is used (D13, D14)");
            return new Checked(problems, reports);
        }

        /** The leaf's rule: its declarations, then each state drawn in it, in the leaf's precedence. */
        private void leafRule(ReadCss.Rule rule, LeafClasses.Listing listing) {
            var byDrawn = new LinkedHashMap<String, Integer>();
            for (LeafClasses.Drawn d : listing.states()) byDrawn.put(d.drawnBy(), byDrawn.size());
            var drawn = new LinkedHashSet<String>();
            String highest = null;
            int highestAt = -1;
            for (ReadCss.Item item : rule.body()) {
                switch (item) {
                    case ReadCss.Declaration d -> declaration(d, true);
                    case ReadCss.AtRule a -> at(a, "@" + a.prelude() + ": no at-rule is in a leaf's file, for now");
                    case ReadCss.Rule r -> {
                        String selector = squeeze(r.selector());
                        if (!selector.startsWith("&")) {
                            at(r, r.selector() + " inside " + listing.styled() + " is no state of it: a state is drawn on the leaf's own element, "
                                    + "&.<state class> or &:<pseudo-class>");
                            continue;
                        }
                        String by = selector.substring(1);
                        Integer rank = byDrawn.get(by);
                        if (rank == null) {
                            at(r, r.selector() + " draws no state " + leaf.token() + " lists"
                                    + (by.startsWith(":") ? ": a pseudo-class is drawn only for a pseudo state the leaf lists, and disabled, checked and invalid are application states, worn as classes (D21)"
                                    : by.startsWith("." + listing.styled() + "--") ? ": its states are " + (byDrawn.isEmpty() ? "none" : String.join(", ", byDrawn.keySet()))
                                    : ": a state's rule is &.<state class> or &:<pseudo-class>, nothing else"));
                            continue;
                        }
                        if (!drawn.add(by)) {
                            at(r, r.selector() + " is drawn twice: each state once, in the leaf's precedence");
                            continue;
                        }
                        if (rank < highestAt)
                            reports.add(name + ":" + r.line() + ": " + by + " is drawn after " + highest + ", which outranks it in "
                                    + leaf.token() + "'s precedence: where both hold, the later wins, so " + by + " would (D22)");
                        else {
                            highestAt = rank;
                            highest = by;
                        }
                        for (ReadCss.Item inner : r.body()) {
                            switch (inner) {
                                case ReadCss.Declaration d -> declaration(d, false);
                                case ReadCss.Rule n -> at(n, n.selector() + " inside " + by + ": a state's rule holds declarations alone");
                                case ReadCss.AtRule a -> at(a, "@" + a.prelude() + ": no at-rule is in a leaf's file, for now");
                            }
                        }
                    }
                }
            }
        }

        /** A declaration, in the leaf's own rule or a state's. */
        private void declaration(ReadCss.Declaration d, boolean own) {
            String property = d.property(), value = d.value();
            if (value.toLowerCase(Locale.ROOT).contains("!important"))
                at(d, property + " is !important, which overrules the leaf's precedence: the order of its states decides (D22)");
            boolean local = false;
            if (property.startsWith("--fp-"))
                at(d, property + " is the palette's, defined by its sheet: a leaf's file names it, var(" + property + "), and never assigns it (D5)");
            else if (LOCAL.stream().anyMatch(property::startsWith)) {
                local = true;
                if (!own) at(d, property + " is assigned in a state's rule: a colour of the leaf's own is assigned in the leaf's own rule (D13, D14)");
                if (!assigned.add(property)) at(d, property + " is assigned twice");
                local(d);
            }
            names(d);
            if (!local) literals(d);
        }

        /** A colour assigned in the leaf's own CSS: the one value the tree gives it. */
        private void local(ReadCss.Declaration d) {
            String property = d.property();
            String kind = property.startsWith("--fixed-") ? "--fixed-" : property.startsWith("--it-") ? "--it-" : "--along-";
            String token = property.substring(kind.length());
            ColorNode<?, ?, ?> node = nodes.get(token);
            var colours = LocalColours.INSTANCE;
            String expected = switch (kind) {
                case "--fixed-" -> node instanceof Fixed<?, ?> f ? colours.fixed(f) : null;
                case "--it-" -> node instanceof Interpolated<?, ?> i ? colours.interpolated(i) : null;
                default -> node instanceof ColorScale<?, ?> s ? colours.along(s) : null;
            };
            if (expected == null) {
                at(d, property + " names no " + (kind.equals("--fixed-") ? "fixed colour" : kind.equals("--it-") ? "interpolated colour" : "colour scale")
                        + " of the tree" + (node == null ? "" : ": " + token + " is " + kindOf(node)));
                return;
            }
            if (!tree.sees(scheme, node)) {
                at(d, property + ": " + token + " is " + tree.schemeName(tree.scheme(node)) + "'s, which " + style.token() + "'s scheme, "
                        + tree.schemeName(scheme) + ", does not see");
                return;
            }
            if (kind.equals("--along-") && !taxonomy.extents(leaf).contains(ExtentAxis.COLOUR))
                at(d, property + " moves a colour by the element's degree, and " + leaf.token() + " has no colour axis: only a leaf with it may (D14)");
            if (!squeeze(d.value()).equals(squeeze(expected)))
                at(d, property + " is " + expected + " by the tree, not " + d.value());
        }

        /** Every variable a value names: a palette's must be one the style's scheme sees; a local one, assigned; none falling back. */
        private void names(ReadCss.Declaration d) {
            Matcher m = VAR.matcher(d.value());
            while (m.find()) {
                String variable = m.group(1);
                boolean colour = variable.startsWith("--fp-") || LOCAL.stream().anyMatch(variable::startsWith);
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
                } else if (colour) used.putIfAbsent(variable, d.line());
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
                if (NAMED.contains(word)) at(d, m.group() + " is a named colour" + why);
                else if (SYSTEM.contains(word)) at(d, m.group() + " is a system colour" + why);
            }
        }

        private void at(ReadCss.Item item, String says) { problems.add(name + ":" + item.line() + ": " + says); }

        private static String kindOf(ColorNode<?, ?, ?> node) {
            return switch (node) {
                case Palettable<?, ?> p -> "a palettable colour, var(--fp-" + p.token() + ")";
                case Fixed<?, ?> f -> "a fixed colour, --fixed-" + f.token();
                case Interpolated<?, ?> i -> "an interpolated colour, --it-" + i.token();
                case ColorScale<?, ?> s -> "a colour scale, --along-" + s.token();
                default -> "a branch";
            };
        }

        private static boolean same(String selector, String own) { return squeeze(selector).equals(own); }
    }

    /** Spaces gathered to one, and none beside a bracket or a comma; lower case. */
    static String squeeze(String s) {
        return s.trim().replaceAll("\\s+", " ").replaceAll(" ?([(),]) ?", "$1").toLowerCase(Locale.ROOT);
    }

    private static final Set<String> NAMED = Set.of(
            "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue",
            "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk",
            "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki",
            "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
            "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue",
            "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
            "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink", "indianred",
            "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral",
            "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon",
            "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime",
            "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple",
            "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue",
            "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange",
            "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff",
            "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple", "red", "rosybrown", "royalblue", "saddlebrown",
            "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray",
            "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet",
            "wheat", "white", "whitesmoke", "yellow", "yellowgreen");

    private static final Set<String> SYSTEM = Set.of(
            "accentcolor", "accentcolortext", "activetext", "buttonborder", "buttonface", "buttontext", "canvas", "canvastext",
            "field", "fieldtext", "graytext", "highlight", "highlighttext", "linktext", "mark", "marktext", "selecteditem",
            "selecteditemtext", "visitedtext");
}
