package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The check of one casting's rule (RFC 0067, appendices Two Classes per Leaf and The Style Jars): its declarations, each state
 * it draws in the casting's precedence, each pseudo-element and reader's setting allowed by name (D29), its keyframes, and
 * every colour held to the tree and the style's scheme - what it found as it went. Run over one implementation's CSS,
 * its marks filled in, by {@link CheckDeclaration}.
 */
final class CastingRule {

    /** The pseudo-elements a casting's rule may draw, each by name (D29): parts of its own element the browser makes. */
    static final List<String> PSEUDO_ELEMENTS = List.of("::before", "::after", "::placeholder", "::marker", "::selection",
            "::backdrop", "::file-selector-button", "::picker(select)", "::picker-icon", "::checkmark",
            "::-webkit-slider-thumb", "::-webkit-slider-runnable-track", "::-moz-range-thumb", "::-moz-range-track",
            "::-moz-range-progress");

    /** The reader's settings a casting's rule may answer by an {@code @media} nested in it (D29). */
    static final List<String> READER_SETTINGS = List.of("(prefers-reduced-motion: reduce)", "(prefers-reduced-transparency: reduce)",
            "(prefers-contrast: more)", "(prefers-contrast: less)", "(forced-colors: active)");

    static final Pattern KEYFRAME = Pattern.compile("(from|to|\\d+(\\.\\d+)?%)(\\s*,\\s*(from|to|\\d+(\\.\\d+)?%))*");

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

    /** Spaces gathered to one, and none beside a bracket or a comma; lower case. */
    static String squeeze(String s) {
        return s.trim().replaceAll("\\s+", " ").replaceAll(" ?([(),]) ?", "$1").toLowerCase(Locale.ROOT);
    }

    static final Set<String> NAMED = Set.of(
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

    static final Set<String> SYSTEM = Set.of(
            "accentcolor", "accentcolortext", "activetext", "buttonborder", "buttonface", "buttontext", "canvas", "canvastext",
            "field", "fieldtext", "graytext", "highlight", "highlighttext", "linktext", "mark", "marktext", "selecteditem",
            "selecteditemtext", "visitedtext");
    private static final Pattern VAR = Pattern.compile("var\\(\\s*(--[A-Za-z0-9_-]+)\\s*(,)?");
    private static final Pattern HEX = Pattern.compile("(?<![\\w-])#[0-9a-fA-F]{3,8}(?![\\w-])");
    private static final Pattern FUNCTION = Pattern.compile(
            "(?i)(?<![\\w-])(rgba?|hsla?|hwb|lab|lch|oklab|oklch|color|color-mix|light-dark|device-cmyk|contrast-color)\\(");
    private static final Pattern WORD = Pattern.compile("(?<![\\w#.-])[A-Za-z][\\w-]*");
    private static final Pattern STRING = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)*'");
    private static final Pattern URL = Pattern.compile("(?i)url\\([^)]*\\)");
    private static final Set<String> LOCAL = Set.of("--fixed-", "--it-", "--along-", "--current-");

    private final Taxonomy taxonomy;
    private final ColorTree tree;
    private final DesignStyle<?> style;
    private final Casting leaf;
    private final Class<?> scheme;
    private final String name;
    private final Map<String, ColorNode<?, ?, ?>> nodes = new HashMap<>();
    private final List<String> problems = new ArrayList<>();
    private final List<String> reports = new ArrayList<>();
    private final Set<String> assigned = new HashSet<>();
    private final Map<String, Integer> used = new LinkedHashMap<>();
    private final Map<String, String> uses = new LinkedHashMap<>();

    CastingRule(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting leaf, String name) {
        this.taxonomy = taxonomy;
        this.tree = tree;
        this.style = style;
        this.leaf = leaf;
        this.scheme = tree.scheme(style).orElse(null);
        this.name = name;
        for (ColorNode<?, ?, ?> n : tree.nodes()) nodes.put(n.token(), n);
    }

    void check(ReadCss.Rule rule) {
        if (scheme == null) {
            problems.add(name + ": " + style.token() + " requires no scheme, so no colour can be held to it");
            return;
        }
        LeafClasses.Listing listing = LeafClasses.INSTANCE.of(taxonomy, leaf);
        body(rule.body(), listing, null);
    }

    /** An @keyframes named after the casting: from, to and percentages, each holding declarations alone (D29). */
    void keyframes(ReadCss.AtRule k) {
        if (scheme == null) return;
        for (ReadCss.Item item : k.body() == null ? List.<ReadCss.Item>of() : k.body()) {
            switch (item) {
                case ReadCss.Rule r when KEYFRAME.matcher(squeeze(r.selector())).matches() -> {
                    for (ReadCss.Item inner : r.body()) {
                        switch (inner) {
                            case ReadCss.Declaration d -> declaration(d, "a keyframe");
                            default -> at(inner, "a keyframe holds declarations alone");
                        }
                    }
                }
                case ReadCss.Rule r -> at(r, r.selector() + " is no keyframe: from, to, or a percentage");
                default -> at(item, "an @keyframes holds keyframes alone: from, to, or a percentage");
            }
        }
    }

    /** What it found, its colours of its own each assigned where they are used. */
    Checked finish() {
        for (Map.Entry<String, Integer> u : used.entrySet())
            if (!assigned.contains(u.getKey()))
                problems.add(name + ":" + u.getValue() + ": var(" + u.getKey() + ") is named, and not assigned in the leaf's own rule: "
                        + "a colour of its own is assigned where it is used (D13, D14)");
        return new Checked(problems, reports);
    }

    /** The tree's colours its declarations use, each by its token, and where first: none named only in a colour of its own's value (D32). */
    Map<String, String> uses() { return uses; }

    /**
     * The casting's rule - or an @media of it, {@code media} its setting: its declarations, then each state drawn in
     * it, in the leaf's precedence; each pseudo-element it lists by name; and, in the rule itself, each reader's
     * setting answered once (D29).
     */
    private void body(List<ReadCss.Item> items, LeafClasses.Listing listing, String media) {
        var byDrawn = new LinkedHashMap<String, Integer>();
        for (LeafClasses.Drawn d : listing.states()) byDrawn.put(d.drawnBy(), byDrawn.size());
        var drawn = new LinkedHashSet<String>();
        var answered = new LinkedHashSet<String>();
        String highest = null;
        int highestAt = -1;
        for (ReadCss.Item item : items) {
            switch (item) {
                case ReadCss.Declaration d -> declaration(d, media == null ? null : "an @media of it");
                case ReadCss.AtRule a -> {
                    String prelude = squeeze(a.prelude());
                    if (!prelude.matches("media\\b.*")) {
                        at(a, "@" + a.prelude() + ": a casting's rule holds no at-rule but an @media for a reader's setting (D29)");
                        continue;
                    }
                    String setting = prelude.substring("media".length()).replace(" ", "");
                    String allowed = READER_SETTINGS.stream().filter(s -> s.replace(" ", "").equals(setting)).findFirst().orElse(null);
                    if (media != null) at(a, "@" + a.prelude() + " inside @media " + media + ": a reader's setting is answered in the casting's rule");
                    else if (allowed == null) at(a, "@" + a.prelude() + " is no reader's setting the check allows: " + String.join(", ", READER_SETTINGS) + " (D29)");
                    else if (!answered.add(allowed)) at(a, "@media " + allowed + " is answered twice");
                    else body(a.body() == null ? List.of() : a.body(), listing, allowed);
                }
                case ReadCss.Rule r -> {
                    String selector = squeeze(r.selector());
                    if (!selector.startsWith("&")) {
                        at(r, r.selector() + " inside " + listing.styled() + " is no state of it: a state is drawn on the leaf's own element, "
                                + "&.<state class> or &:<pseudo-class>");
                        continue;
                    }
                    String by = selector.substring(1);
                    Integer rank = byDrawn.get(by);
                    String combined = byDrawn.keySet().stream().filter(s -> by.startsWith(s) && by.length() > s.length()).findFirst().orElse(null);
                    if (by.startsWith("::")) {
                        if (!PSEUDO_ELEMENTS.contains(by))
                            at(r, r.selector() + " draws " + by + ", a pseudo-element the check does not allow by name: "
                                    + String.join(", ", PSEUDO_ELEMENTS) + " (D29)");
                        else if (!drawn.add(by)) at(r, r.selector() + " is drawn twice");
                        else for (ReadCss.Item inner : r.body()) {
                            switch (inner) {
                                case ReadCss.Declaration d -> declaration(d, "a pseudo-element's rule");
                                default -> at(inner, "a pseudo-element's rule holds declarations alone");
                            }
                        }
                        continue;
                    }
                    if (rank == null && combined != null) {
                        at(r, r.selector() + " draws " + combined + " with more: each state is drawn alone, and where two hold the "
                                + "leaf's precedence decides (D22)");
                        continue;
                    }
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
                    if (rank < highestAt && media == null)
                        reports.add(name + ":" + r.line() + ": " + by + " is drawn after " + highest + ", which outranks it in "
                                + leaf.token() + "'s precedence: where both hold, the later wins, so " + by + " would (D22)");
                    else {
                        highestAt = rank;
                        highest = by;
                    }
                    for (ReadCss.Item inner : r.body()) {
                        switch (inner) {
                            case ReadCss.Declaration d -> declaration(d, "a state's rule");
                            case ReadCss.Rule n -> at(n, n.selector() + " inside " + by + ": a state's rule holds declarations alone");
                            case ReadCss.AtRule a -> at(a, "@" + a.prelude() + " inside " + by + ": a state's rule holds declarations alone");
                        }
                    }
                }
            }
        }
    }

    /** A declaration, in the leaf's own rule - {@code where} none - or in a state's, a pseudo-element's, an @media's, a keyframe. */
    private void declaration(ReadCss.Declaration d, String where) {
        String property = d.property(), value = d.value();
        if (value.toLowerCase(Locale.ROOT).contains("!important"))
            at(d, property + " is !important, which overrules the leaf's precedence: the order of its states decides (D22)");
        boolean local = false;
        if (property.startsWith("--fp-"))
            at(d, property + " is the palette's, defined by its sheet: a casting's rule names it, var(" + property + "), and never assigns it (D5)");
        else if (LOCAL.stream().anyMatch(property::startsWith)) {
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
        ColorNode<?, ?, ?> node = nodes.get(token);
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
        if (!tree.sees(scheme, node)) {
            at(d, property + ": " + token + " is " + tree.schemeName(tree.scheme(node)) + "'s, which " + style.token() + "'s scheme, "
                    + tree.schemeName(scheme) + ", does not see");
            return;
        }
        if (kind.equals("--along-") && !taxonomy.extents(leaf.cast()).contains(ExtentAxis.COLOUR))
            at(d, property + " moves a colour by the element's degree, and " + leaf.token() + " has no colour axis: only a leaf with it may (D14)");
        if (!squeeze(d.value()).equals(squeeze(expected)))
            at(d, property + " is " + expected + " by the tree, not " + d.value());
    }

    /**
     * Every variable a value names: a palette's must be one the style's scheme sees; a local one, assigned; none falling
     * back. Each a use of the design's - but in the value of a colour of its own, where the tree's value names them (D32).
     */
    private void names(ReadCss.Declaration d, boolean local) {
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
                if (node instanceof Palettable<?, ?> && !local) uses.putIfAbsent(token, name + ":" + d.line());
            } else if (colour) {
                used.putIfAbsent(variable, d.line());
                String prefix = LOCAL.stream().filter(variable::startsWith).findFirst().orElseThrow();
                String token = variable.substring(prefix.length());
                if (!local && nodes.containsKey(token)) uses.putIfAbsent(token, name + ":" + d.line());
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
            case CurrentColor<?, ?> k -> "the current colour at a share, --current-" + k.token();
            case ColorScale<?, ?> s -> "a colour scale, --along-" + s.token();
            default -> "a branch";
        };
    }
}
