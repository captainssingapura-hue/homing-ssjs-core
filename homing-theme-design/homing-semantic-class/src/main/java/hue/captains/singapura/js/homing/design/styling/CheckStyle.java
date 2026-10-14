package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ExtentAxis;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.Palettable;
import hue.captains.singapura.js.homing.design.semantic.ColorImports;
import hue.captains.singapura.js.homing.design.semantic.ColorImportsExtension;
import hue.captains.singapura.js.homing.design.semantic.ColorImportsExtensions;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.StyleImports;
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
 *   <li>rules every casting it answers content for, once, in its family's files, and none it leaves absent; every
 *       casting is of a family (D30);</li>
 *   <li>has no file that is no family's - {@code <family>.css}, or a module's {@code <family>.<module>.css} - and none
 *       given twice on the classpath, one hiding the other.</li>
 * </ul>
 *
 * <p>A family's file rules its castings' classes, each once, in the family's order - each component on its own, then in
 * each role it is cast in - and nothing else. Inside a casting's rule, each state the leaf lists, drawn on the leaf's
 * own element - {@code &.styled-tab--selected} for an application state's value, {@code &:hover} for a pseudo state -
 * each once and holding declarations alone (D12). Every one of those selectors weighs the same, so the order the file
 * draws them in is what decides between two states at once: the leaf's precedence, the least important first, which a
 * soft check reports when it is not (D22, D23). No two states combined in one selector, and no {@code !important}.</p>
 *
 * <p>Beyond its rule and its states, what is allowed by name and nothing else (D29): a pseudo-element of the leaf's own
 * element, each one the check lists ({@link #PSEUDO_ELEMENTS}), nested in the casting's rule - {@code &::placeholder} -
 * once, holding declarations alone; an {@code @media} for one of the reader's settings it lists ({@link
 * #READER_SETTINGS}), nested in the casting's rule, once each, holding what the rule may hold; and, at the top of the
 * family's file, an {@code @keyframes} named after a casting the file rules - {@code tab} or {@code tab-<word>} - served
 * with it. Every declaration in them is held to the same colour rules.</p>
 *
 * <p>Its colours, every one a variable:</p>
 * <ul>
 *   <li>{@code var(--fp-<leaf>)}, a palettable colour of the tree that the style's scheme sees, never assigned in a
 *       leaf's file - its palette's sheet defines it (D5, D7);</li>
 *   <li>{@code --fixed-<leaf>}, {@code --it-<leaf>}: assigned in the leaf's own rule, each to the one value the tree
 *       gives it, and named only where so assigned (D13);</li>
 *   <li>{@code --along-<scale>}: the same, and only on a leaf with the colour axis (D14);</li>
 *   <li>{@code --current-<leaf>}: the same, CSS's {@code currentColor} - the colour the element is given - at the share
 *       the tree gives it (D31, D32);</li>
 *   <li>none of the four falling back, and no literal colour anywhere else - a hex, a colour function, a mix, a named
 *       or a system colour; {@code currentColor}, {@code transparent}, {@code inherit} and their like are CSS's own
 *       (D6).</li>
 * </ul>
 *
 * <p>Each component's design uses the colours it imports, and no other (D32): a colour of the tree named in a rule of
 * the component - on its own or in a role, a state, a pseudo-element, a keyframe - is one its imports list, and each
 * import is used. A colour named only in the value of a colour of its own - a scale's knots - is the tree's, not the
 * design's. The style's own files are held to its {@link DesignStyle#colorImports()}; a module's files to the module's
 * {@link ColorImportsExtension} - each to the files beside it, so the same on any classpath. An import is of a component
 * the style draws, in the tree, and one the style's scheme sees.</p>
 *
 * Every problem is found at once, each naming its file and line.
 */
public record CheckStyle() implements StatelessFunctionalObject {

    public static final CheckStyle INSTANCE = new CheckStyle();

    /** The pseudo-elements a casting's rule may draw, each by name (D29): parts of its own element the browser makes. */
    public static final List<String> PSEUDO_ELEMENTS = List.of("::before", "::after", "::placeholder", "::marker", "::selection",
            "::backdrop", "::file-selector-button", "::picker(select)", "::picker-icon", "::checkmark",
            "::-webkit-slider-thumb", "::-webkit-slider-runnable-track", "::-moz-range-thumb", "::-moz-range-track",
            "::-moz-range-progress");

    /** The reader's settings a casting's rule may answer by an {@code @media} nested in it (D29). */
    public static final List<String> READER_SETTINGS = List.of("(prefers-reduced-motion: reduce)", "(prefers-reduced-transparency: reduce)",
            "(prefers-contrast: more)", "(prefers-contrast: less)", "(forced-colors: active)");

    private static final Pattern KEYFRAME = Pattern.compile("(from|to|\\d+(\\.\\d+)?%)(\\s*,\\s*(from|to|\\d+(\\.\\d+)?%))*");

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

    /** A style over every casting of the taxonomy: its answer for each, its family files, and each file. */
    public Checked check(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style) {
        var problems = new ArrayList<String>();
        var reports = new ArrayList<String>();
        var files = StyleFiles.INSTANCE;
        if (tree.scheme(style).isEmpty()) {
            problems.add(style.token() + " requires no scheme: its type argument is the marker, a variable, or no abstract scheme");
            return new Checked(problems, reports);
        }
        boolean base = !(style instanceof Derives<?, ?>);
        if (!base && StyleSheet.INSTANCE.baseLayer(style).isEmpty()) problems.add(style.token() + " derives from a style that derives, in the end, from itself");
        var heads = new LinkedHashMap<String, ComponentBranch>();
        for (ComponentBranch h : taxonomy.heads()) heads.put(h.token(), h);
        for (String twice : files.twice(style))
            problems.add(files.name(style, twice) + " is given twice on the classpath, one hiding the other: a module adding to a family "
                    + "gives a file of its own, <family>.<module>.css (D30)");
        var drawn = new LinkedHashMap<Casting, String>();
        var used = new LinkedHashMap<String, Map<Component<?>, Map<String, String>>>();
        for (String present : files.present(style)) {
            Optional<String> family = files.family(present);
            if (family.isEmpty() || !heads.containsKey(family.get())) {
                problems.add(files.name(style, present) + " is no family's file: a style's file is <family>.css, or <family>.<module>.css, "
                        + "and the families are " + (heads.isEmpty() ? "none - no branch heads one" : String.join(", ", heads.keySet())));
                continue;
            }
            Checked f = new FamilyFile(taxonomy, tree, style, present, heads.get(family.get()), drawn,
                    used.computeIfAbsent(files.module(present).orElse(""), k -> new LinkedHashMap<>())).check(files.read(style, present).orElse(""));
            problems.addAll(f.problems());
            reports.addAll(f.reports());
        }
        for (Casting leaf : taxonomy.castings()) {
            Optional<ComponentBranch> head = taxonomy.head(leaf.cast());
            if (head.isEmpty()) {
                problems.add(leaf.token() + " is of no family: no branch above " + leaf.cast().token() + " heads one, so no file of "
                        + style.token() + " can rule it (D30)");
                continue;
            }
            switch (style.style(leaf)) {
                case null -> problems.add(style.token() + " answers nothing for " + leaf.token() + ": content, or absence");
                case StylingRes.CssContent c -> {
                    if (!drawn.containsKey(leaf)) problems.add(style.token() + " answers content for " + leaf.token() + ", and no file of "
                            + head.get().token() + "'s rules it: " + files.name(style, files.file(head.get())) + ", or a module's");
                }
                case StylingRes.Absence a -> {
                    if (base) problems.add(style.token() + " is a base layer, and answers absence for " + leaf.token()
                            + ": a base layer answers content for every leaf, an extension's too (D26)");
                    if (drawn.containsKey(leaf)) problems.add(drawn.get(leaf) + ": " + style.token() + " rules " + leaf.token()
                            + ", which it does not answer: it answers absence, so the base layer's is used - answer content, or remove the rule");
                }
            }
        }
        problems.addAll(imports(taxonomy, tree, style, used));
        return new Checked(problems, reports);
    }

    /**
     * Each design held to its imports, both ways - a colour used and not imported, one imported and used nowhere - and
     * each import to the taxonomy, the style and the tree (D32): the style's own for its own files, each module's for
     * the module's.
     *
     * @param used by contributor - the style's own files, {@code ""}, or a module's - each component's colours and
     *             where each is first used
     */
    private static List<String> imports(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Map<String, Map<Component<?>, Map<String, String>>> used) {
        var problems = new ArrayList<String>();
        Class<?> scheme = tree.scheme(style).orElseThrow();
        var declared = new LinkedHashMap<String, Map<Component<?>, List<ColorLeaf<?, ?, ?>>>>();
        var whose = new LinkedHashMap<String, String>();
        whose.put("", style.token() + "'s colorImports()");
        declare(taxonomy, tree, style, scheme, whose.get(""), style.colorImports(), declared.computeIfAbsent("", k -> new LinkedHashMap<>()), problems);
        var modules = new LinkedHashSet<String>();
        for (String present : StyleFiles.INSTANCE.present(style)) StyleFiles.INSTANCE.module(present).ifPresent(modules::add);
        for (ColorImportsExtension e : ColorImportsExtensions.INSTANCE.of(style)) {
            String module = e.module();
            boolean listed = false;
            for (StyleImports<?> si : e.imports()) {
                if (si.style() != style.getClass()) continue;
                String who = module + "'s ColorImportsExtension, for " + style.token();
                if (listed) {
                    problems.add(e.getClass().getSimpleName() + " lists " + style.token() + " twice: a module's imports in a style are listed once");
                    continue;
                }
                listed = true;
                if (whose.containsKey(module)) {
                    problems.add(e.getClass().getSimpleName() + " imports for " + module + " in " + style.token() + ", for which another already does: one module, one extension");
                    continue;
                }
                if (!modules.contains(module))
                    problems.add(who + " imports colours, and " + module + " gives " + style.token() + " no file: <family>." + module + ".css (D32)");
                whose.put(module, who);
                declare(taxonomy, tree, style, scheme, who, si.imports(), declared.computeIfAbsent(module, k -> new LinkedHashMap<>()), problems);
            }
        }
        var contributors = new LinkedHashSet<String>(declared.keySet());
        contributors.addAll(used.keySet());
        for (String contributor : contributors) {
            Map<Component<?>, List<ColorLeaf<?, ?, ?>>> imports = declared.getOrDefault(contributor, Map.of());
            Map<Component<?>, Map<String, String>> uses = used.getOrDefault(contributor, Map.of());
            String who = whose.getOrDefault(contributor, contributor + "'s ColorImportsExtension, for " + style.token());
            for (Map.Entry<Component<?>, Map<String, String>> u : uses.entrySet()) {
                if (taxonomy.castings().stream().noneMatch(c -> c.cast().equals(u.getKey()) && style.style(c) instanceof StylingRes.CssContent))
                    continue;   // a rule of what the style draws not: refused as that
                var imported = new HashSet<String>();
                for (ColorLeaf<?, ?, ?> c : imports.getOrDefault(u.getKey(), List.of())) imported.add(c.token());
                for (Map.Entry<String, String> colour : u.getValue().entrySet())
                    if (!imported.contains(colour.getKey()))
                        problems.add(colour.getValue() + ": " + u.getKey().token() + "'s design uses " + colour.getKey() + ", which it does not import: "
                                + who + " lists each colour a design uses (D32)");
            }
            for (Map.Entry<Component<?>, List<ColorLeaf<?, ?, ?>>> i : imports.entrySet()) {
                Map<String, String> its = uses.getOrDefault(i.getKey(), Map.of());
                for (ColorLeaf<?, ?, ?> c : i.getValue())
                    if (!its.containsKey(c.token()))
                        problems.add(who + " imports " + c.token() + " for " + i.getKey().token() + ", and "
                                + (contributor.isEmpty() ? "its own files" : contributor + "'s files") + " use it nowhere: a design imports what it uses (D32)");
            }
        }
        return problems;
    }

    /** One contributor's imports, each of a component the style draws, once, and each colour one of the tree the style's scheme sees. */
    private static void declare(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Class<?> scheme, String who,
                                List<? extends ColorImports<?>> imports, Map<Component<?>, List<ColorLeaf<?, ?, ?>>> into, List<String> problems) {
        for (ColorImports<?> i : imports) {
            Component<?> component = i.component();
            List<Casting> castings = taxonomy.castings().stream().filter(c -> c.cast().equals(component)).toList();
            if (castings.isEmpty()) {
                problems.add(who + " imports colours for " + component.token() + ", which is no component of the taxonomy");
                continue;
            }
            if (castings.stream().noneMatch(c -> style.style(c) instanceof StylingRes.CssContent))
                problems.add(who + " imports colours for " + component.token() + ", which " + style.token()
                        + " does not draw: its base's design is worn, and that design's imports with it (D32)");
            if (into.containsKey(component)) {
                problems.add(who + " imports colours for " + component.token() + " twice: a design's imports are listed once");
                continue;
            }
            var colours = new ArrayList<ColorLeaf<?, ?, ?>>();
            for (ColorLeaf<?, ?, ?> c : i.colors()) {
                if (!tree.has(c)) problems.add(who + " imports " + c.token() + " for " + component.token() + ", which the tree has not");
                else if (!tree.sees(scheme, c))
                    problems.add(who + " imports " + c.token() + " for " + component.token() + ": it is " + tree.schemeName(tree.scheme(c))
                            + "'s, which " + style.token() + "'s scheme, " + tree.schemeName(scheme) + ", does not see");
                colours.add(c);
            }
            into.put(component, colours);
        }
    }

    /**
     * One of a style's family files, as written: {@code control.css}, or a module's, {@code control.extension.css} - the
     * castings of the family its name says, each ruled once, in the family's order, each held to its leaf and the tree.
     */
    public Checked file(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, String file, String css) {
        Optional<String> family = StyleFiles.INSTANCE.family(file);
        ComponentBranch head = family.flatMap(f -> taxonomy.heads().stream().filter(h -> h.token().equals(f)).findFirst()).orElse(null);
        if (head == null)
            return new Checked(List.of(StyleFiles.INSTANCE.name(style, file) + " is no family's file"), List.of());
        return new FamilyFile(taxonomy, tree, style, file, head, new LinkedHashMap<>(), new LinkedHashMap<>()).check(css);
    }

    /** The check of one family file: its castings' rules, each once, in the family's order, each held by {@link OneFile}. */
    private static final class FamilyFile {

        private final Taxonomy taxonomy;
        private final ColorTree tree;
        private final DesignStyle<?> style;
        private final ComponentBranch head;
        private final String name;
        private final Map<Casting, String> drawn;
        private final List<String> problems = new ArrayList<>();
        private final List<String> reports = new ArrayList<>();
        private final Map<Casting, OneFile> ruled = new LinkedHashMap<>();
        private final Map<Component<?>, Map<String, String>> used;

        FamilyFile(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, String file, ComponentBranch head, Map<Casting, String> drawn,
                   Map<Component<?>, Map<String, String>> used) {
            this.taxonomy = taxonomy;
            this.tree = tree;
            this.style = style;
            this.head = head;
            this.name = StyleFiles.INSTANCE.name(style, file);
            this.drawn = drawn;
            this.used = used;
        }

        Checked check(String css) {
            var family = taxonomy.family(head);
            var byClass = new LinkedHashMap<String, Casting>();
            for (Casting c : taxonomy.castings()) byClass.put("." + LeafClasses.INSTANCE.of(taxonomy, c).styled(), c);
            ReadCss.Split split = ReadCss.INSTANCE.split(css);
            for (String p : split.problems()) problems.add(name + ":" + p);
            int last = -1;
            Casting lastDrawn = null;
            boolean any = false;
            var keyframes = new ArrayList<ReadCss.AtRule>();
            for (ReadCss.Piece piece : split.pieces()) {
                switch (piece.item()) {
                    case ReadCss.Declaration d -> at(d, d.property() + " is declared outside any rule");
                    case ReadCss.AtRule a when squeeze(a.prelude()).startsWith("keyframes ") -> keyframes.add(a);
                    case ReadCss.AtRule a -> at(a, "@" + a.prelude() + ": a family's file holds no at-rule but @keyframes named after a "
                            + "casting it rules; a reader's setting is nested in the casting's rule (D29)");
                    case ReadCss.Rule r -> {
                        Casting c = byClass.get(squeeze(r.selector()));
                        if (c == null) {
                            at(r, r.selector() + " is no casting's rule: a family's file rules each of its castings' classes, "
                                    + ".styled-<casting>, alone, its states nested in it (D9, D10)");
                            continue;
                        }
                        int index = family.indexOf(c);
                        if (index < 0) {
                            String other = taxonomy.head(c.cast()).map(ComponentBranch::token).orElse(null);
                            at(r, r.selector() + " is " + (other == null ? "of no family" : other + "'s, ruled in " + other + "'s files")
                                    + ", not " + head.token() + "'s");
                            continue;
                        }
                        any = true;
                        if (drawn.containsKey(c)) {
                            at(r, r.selector() + " is ruled twice: first at " + drawn.get(c) + " - each casting once, in its family's files");
                            continue;
                        }
                        drawn.put(c, name + ":" + r.line());
                        if (index < last)
                            at(r, r.selector() + " is ruled after .styled-" + lastDrawn.token() + ", which comes after it in "
                                    + head.token() + "'s order: " + String.join(", ", family.stream().map(Casting::token).toList()));
                        else {
                            last = index;
                            lastDrawn = c;
                        }
                        OneFile one = new OneFile(taxonomy, tree, style, c, name);
                        one.check(r);
                        ruled.put(c, one);
                    }
                }
            }
            var named = new LinkedHashSet<String>();
            for (ReadCss.AtRule k : keyframes) {
                String keyframe = squeeze(k.prelude()).substring("keyframes ".length()).trim();
                if (!named.add(keyframe)) {
                    at(k, "@keyframes " + keyframe + " is written twice");
                    continue;
                }
                Casting of = StyleFiles.INSTANCE.namedAfter(keyframe, family).orElse(null);
                if (of == null) {
                    at(k, "@keyframes " + keyframe + " is named after none of " + head.token() + "'s castings: <casting> or <casting>-<word> (D29)");
                    continue;
                }
                OneFile one = ruled.get(of);
                if (one == null) {
                    at(k, "@keyframes " + keyframe + " is " + of.token() + "'s, which this file does not rule: it is served with its casting's rule (D29)");
                    continue;
                }
                one.keyframes(k);
            }
            for (Map.Entry<Casting, OneFile> one : ruled.entrySet()) {
                Checked c = one.getValue().finish();
                problems.addAll(c.problems());
                reports.addAll(c.reports());
                Map<String, String> its = used.computeIfAbsent(one.getKey().cast(), k -> new LinkedHashMap<>());
                one.getValue().uses().forEach(its::putIfAbsent);
            }
            if (!any && split.problems().isEmpty() && problems.isEmpty())
                problems.add(name + ": rules none of " + head.token() + "'s castings: a family's file rules its castings, or is not there");
            return new Checked(problems, reports);
        }

        private void at(ReadCss.Item item, String says) { problems.add(name + ":" + item.line() + ": " + says); }
    }

    /** The check of one casting's rule: what it found as it went. */
    private static final class OneFile {

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

        OneFile(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting leaf, String name) {
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
