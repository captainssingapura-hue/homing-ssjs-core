package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The check of one casting's rule (RFC 0067, appendices Two Classes per Leaf and The Style Jars): its declarations, each state
 * it draws in the casting's precedence, each pseudo-element and reader's setting allowed by name (D29), its keyframes, and
 * every colour held to the tree and the style's scheme ({@link CastingColours}) - what it found as it went. Run over one
 * implementation's CSS, its marks filled in, by {@link CheckDeclaration}.
 */
final class CastingRule {


    static final Pattern KEYFRAME = Pattern.compile("(from|to|\\d+(\\.\\d+)?%)(\\s*,\\s*(from|to|\\d+(\\.\\d+)?%))*");

    /**
     * What a check found.
     *
     * @param problems what refuses the style, each naming its file and line
     * @param reports  what the soft check says and does not refuse: a state drawn out of its leaf's precedence (D23)
     */
    public record Checked(List<String> problems, List<String> reports) implements Scalar {
        public Checked { problems = List.copyOf(problems); reports = List.copyOf(reports); }

        /** Whether nothing refuses it. */
        public boolean passes() { return problems.isEmpty(); }
    }

    private final Taxonomy taxonomy;
    private final DesignStyle<?> style;
    private final Casting leaf;
    private final ColorScheme scheme;
    private final String name;
    private final List<String> problems = new ArrayList<>();
    private final List<String> reports = new ArrayList<>();
    private final CastingColours colours;

    CastingRule(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting leaf, String name) {
        this(taxonomy, tree, style, leaf, name, List.of());
    }

    /** With the implementation's own spectra: colour scales over the tree's colours, at degrees of the style's own (D41). */
    CastingRule(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting leaf, String name, List<? extends ColorScale<?, ?>> spectra) {
        this.taxonomy = taxonomy;
        this.style = style;
        this.leaf = leaf;
        this.scheme = tree.scheme(style).orElse(null);
        this.name = name;
        this.colours = new CastingColours(taxonomy, tree, style, leaf, name, spectra, problems);
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
                case ReadCss.Rule r when KEYFRAME.matcher(ReadCss.INSTANCE.squeeze(r.selector())).matches() -> {
                    for (ReadCss.Item inner : r.body()) {
                        switch (inner) {
                            case ReadCss.Declaration d -> colours.declaration(d, "a keyframe");
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
        colours.allAssigned();
        return new Checked(problems, reports);
    }

    /** The tree's colours its declarations use, each by its token, and where first: none named only in a colour of its own's value (D32). */
    Map<String, String> uses() { return colours.uses(); }

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
        Highest highest = new Highest(null, -1);
        for (ReadCss.Item item : items) {
            switch (item) {
                case ReadCss.Declaration d -> colours.declaration(d, media == null ? null : "an @media of it");
                case ReadCss.AtRule a -> setting(a, listing, media, answered);
                case ReadCss.Rule r -> {
                    String selector = ReadCss.INSTANCE.squeeze(r.selector());
                    if (!selector.startsWith("&")) {
                        at(r, r.selector() + " inside " + listing.styled() + " is no state of it: a state is drawn on the leaf's own element, "
                                + "&.<state class> or &:<pseudo-class>");
                        continue;
                    }
                    String by = selector.substring(1);
                    if (by.startsWith("::")) pseudoElement(r, by, drawn);
                    else highest = state(r, by, listing, byDrawn, drawn, media, highest);
                }
            }
        }
    }

    /** The state that outranks the rest drawn so far, and its rank: none yet at -1. */
    private record Highest(String by, int rank) {}

    /** An at-rule in the casting's rule: only an @media for a reader's setting the check allows, answered once, in the rule itself. */
    private void setting(ReadCss.AtRule a, LeafClasses.Listing listing, String media, Set<String> answered) {
        String prelude = ReadCss.INSTANCE.squeeze(a.prelude());
        if (!prelude.matches("media\\b.*")) {
            at(a, "@" + a.prelude() + ": a casting's rule holds no at-rule but an @media for a reader's setting (D29)");
            return;
        }
        String setting = prelude.substring("media".length()).replace(" ", "");
        String allowed = CastingWords.READER_SETTINGS.all().stream().filter(s -> s.replace(" ", "").equals(setting)).findFirst().orElse(null);
        if (media != null) at(a, "@" + a.prelude() + " inside @media " + media + ": a reader's setting is answered in the casting's rule");
        else if (allowed == null) at(a, "@" + a.prelude() + " is no reader's setting the check allows: " + String.join(", ", CastingWords.READER_SETTINGS.all()) + " (D29)");
        else if (!answered.add(allowed)) at(a, "@media " + allowed + " is answered twice");
        else body(a.body() == null ? List.of() : a.body(), listing, allowed);
    }

    /** A pseudo-element: one the check allows by name, drawn once, of declarations alone. */
    private void pseudoElement(ReadCss.Rule r, String by, Set<String> drawn) {
        if (!CastingWords.PSEUDO_ELEMENTS.all().contains(by))
            at(r, r.selector() + " draws " + by + ", a pseudo-element the check does not allow by name: "
                    + String.join(", ", CastingWords.PSEUDO_ELEMENTS.all()) + " (D29)");
        else if (!drawn.add(by)) at(r, r.selector() + " is drawn twice");
        else for (ReadCss.Item inner : r.body()) {
            switch (inner) {
                case ReadCss.Declaration d -> colours.declaration(d, "a pseudo-element's rule");
                default -> at(inner, "a pseudo-element's rule holds declarations alone");
            }
        }
    }

    /** A state drawn: one the leaf lists, alone, once, of declarations alone, and reported out of the leaf's precedence; the highest drawn so far. */
    private Highest state(ReadCss.Rule r, String by, LeafClasses.Listing listing, Map<String, Integer> byDrawn, Set<String> drawn,
                          String media, Highest highest) {
        Integer rank = byDrawn.get(by);
        String combined = byDrawn.keySet().stream().filter(s -> by.startsWith(s) && by.length() > s.length()).findFirst().orElse(null);
        if (rank == null && combined != null) {
            at(r, r.selector() + " draws " + combined + " with more: each state is drawn alone, and where two hold the "
                    + "leaf's precedence decides (D22)");
            return highest;
        }
        if (rank == null) {
            at(r, r.selector() + " draws no state " + leaf.token() + " lists"
                    + (by.startsWith(":") ? ": a pseudo-class is drawn only for a pseudo state the leaf lists, and disabled, checked and invalid are application states, worn as classes (D21)"
                    : by.startsWith("." + listing.styled() + "--") ? ": its states are " + (byDrawn.isEmpty() ? "none" : String.join(", ", byDrawn.keySet()))
                    : ": a state's rule is &.<state class> or &:<pseudo-class>, nothing else"));
            return highest;
        }
        if (!drawn.add(by)) {
            at(r, r.selector() + " is drawn twice: each state once, in the leaf's precedence");
            return highest;
        }
        boolean outOfOrder = rank < highest.rank() && media == null;
        if (outOfOrder)
            reports.add(name + ":" + r.line() + ": " + by + " is drawn after " + highest.by() + ", which outranks it in "
                    + leaf.token() + "'s precedence: where both hold, the later wins, so " + by + " would (D22)");
        for (ReadCss.Item inner : r.body()) {
            switch (inner) {
                case ReadCss.Declaration d -> colours.declaration(d, "a state's rule");
                case ReadCss.Rule n -> at(n, n.selector() + " inside " + by + ": a state's rule holds declarations alone");
                case ReadCss.AtRule a -> at(a, "@" + a.prelude() + " inside " + by + ": a state's rule holds declarations alone");
            }
        }
        return outOfOrder ? highest : new Highest(by, rank);
    }

    private void at(ReadCss.Item item, String says) { problems.add(name + ":" + item.line() + ": " + says); }
}
