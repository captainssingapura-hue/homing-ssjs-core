package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignId;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.Mode;
import hue.captains.singapura.js.homing.design.Palette;
import hue.captains.singapura.js.homing.design.State;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Emphasis.*;
import static hue.captains.singapura.js.homing.design.Feedback.*;
import static hue.captains.singapura.js.homing.design.Interaction.*;
import static hue.captains.singapura.js.homing.design.Layer.*;
import static hue.captains.singapura.js.homing.design.Pairing.*;
import static hue.captains.singapura.js.homing.design.Structure.*;
import static hue.captains.singapura.js.homing.design.Text.*;
import static hue.captains.singapura.js.homing.studio.themes.Bind.*;

/**
 * A palette from seeds: thirteen colours, light and dark, and the house rule
 * for spreading them over every colour pair the studio wears — the layers,
 * the inks, the edges, the interaction states, the prose. Write the seeds
 * and the rule does the rest, so a new set of colours is a dozen lines and
 * complete by construction. Harbour, the house colours, is the first set;
 * Forest and Sunset are two more, and they colour any physique.
 *
 * <p>Colour any physique, but not all equally well: each is anchored to the
 * design it was crafted for and names the others it suits — Clay is
 * Neumorphism's and moulds nothing under a design that casts no shadow;
 * Marker is Sketchy's and is also black-on-white brutalism. The naming is by
 * {@link DesignId}, so a palette points at a design without loading its
 * words, and the compiler checks the pointer.</p>
 *
 * <p>Feedback — danger, success, warning — is the same in every seed
 * palette: a signal is a signal whatever the brand.</p>
 *
 * @param id          the palette's identity; its slug is the suffix of every cross that wears it
 * @param label       the picker's name for it
 * @param inspiration one line on where it comes from
 * @param anchor      the design it was crafted for — the one it is the default colours of
 * @param compatible  the other designs it suits, as {@link #fits(DesignId...)} names them
 * @param light       the seeds in daylight
 * @param dark        the seeds at night
 */
public record SeedPalette(DesignId id, String label, String inspiration, DesignId anchor, Set<DesignId> compatible,
                          Seeds light, Seeds dark) implements Palette {

    /** A palette anchored to one design and, until {@link #fits(DesignId...)} says otherwise, suiting no other. */
    public SeedPalette(String slug, String label, String inspiration, DesignId anchor, Seeds light, Seeds dark) {
        this(new DesignId(slug), label, inspiration, anchor, Set.of(), light, dark);
    }

    public SeedPalette { compatible = Set.copyOf(compatible); }

    /** This palette, also offered for the designs named. */
    public SeedPalette fits(DesignId... designs) {
        var all = new java.util.LinkedHashSet<>(compatible);
        all.addAll(java.util.Arrays.asList(designs));
        return new SeedPalette(id, label, inspiration, anchor, all, light, dark);
    }

    /**
     * The thirteen: four layers, four inks, a title ink, the accent in three
     * roles, and the hairline.
     */
    public record Seeds(String surface, String raised, String recessed, String inverted,
                        String text, String muted, String onInverted, String onInvertedMuted,
                        String title, String accent, String accentEmphasis, String accentOn, String border) {}

    /** The house colours — navy, gold, a cool white page. Editorial's; flat and quiet enough for the moulded, the drawn and the chromed. */
    public static final SeedPalette HARBOUR = new SeedPalette("harbour", "Harbour",
            "The house colours — navy and gold on a cool white page.", HomingEditorial.ID,
            new Seeds("#FAFBFD", "#FFFFFF", "#F1F4F9", "#111936", "#3B4A6B", "#64748B", "#FFFFFF", "#CADCFC", "#1E2761", "#F4B942", "#C8921E", "#111936", "#E2E8F0"),
            new Seeds("#0F1320", "#1A1F36", "#232943", "#111936", "#E2E8F0", "#94A3B8", "#E2E8F0", "#B8C9F2", "#8FA3D8", "#F4B942", "#E0A833", "#111936", "#2D3454"))
            .fits(HomingNeumorphism.ID, HomingSketchy.ID, HomingNeoFuturism.ID);

    /** Green and earth, with honey for the accents. Written with the house rule, so Editorial's; opaque and flat, so brutalism's too. */
    public static final SeedPalette FOREST = new SeedPalette("forest", "Forest",
            "Green and earth tones, with honey for the accents.", HomingEditorial.ID,
            new Seeds("#F4F8F2", "#FFFFFF", "#E8EFE3", "#1A3829", "#2A3D2E", "#5C7561", "#FFFFFF", "#C8E6C9", "#2D5F3F", "#D4A04C", "#A6781E", "#1A3829", "#D4DFCC"),
            new Seeds("#0E1A12", "#1A2A1F", "#243528", "#1A3829", "#DDEBD8", "#94B59C", "#DDEBD8", "#A8D5B0", "#7BAB85", "#D4A04C", "#B5873A", "#1A3829", "#2E4034"))
            .fits(HomingNeumorphism.ID, HomingSketchy.ID, HomingNeoBrutalism.ID);

    /** Warm coral and terracotta — a dusk palette. Editorial's, like Forest, and offered where Forest is. */
    public static final SeedPalette SUNSET = new SeedPalette("sunset", "Sunset",
            "Warm coral and terracotta — a dusk palette.", HomingEditorial.ID,
            new Seeds("#FFF5EB", "#FFFFFF", "#F5E8DA", "#7A2E2E", "#4A2D1A", "#8B6F4E", "#FFFFFF", "#FFD4A8", "#B85450", "#FF8C42", "#D2691E", "#7A2E2E", "#E8D5C0"),
            new Seeds("#1A0F08", "#2A1A10", "#3A2418", "#7A2E2E", "#FFE4D1", "#C9A78B", "#FFE4D1", "#FFB67A", "#E89580", "#FF8C42", "#FFA363", "#7A2E2E", "#4A3424"))
            .fits(HomingNeumorphism.ID, HomingSketchy.ID, HomingNeoBrutalism.ID);

    /** One grey-blue clay — the raised layer the same colour as the page, which is what neumorphism moulds. Under flat cards it is dull but coherent. */
    public static final SeedPalette CLAY = new SeedPalette("clay", "Clay",
            "One grey-blue clay, the raised layer the same colour as the page.", HomingNeumorphism.ID,
            new Seeds("#E0E5EC", "#E0E5EC", "#D6DBE3", "#D1D9E6", "#3B4A5E", "#7C8A9E", "#3B4A5E", "#6B7A90", "#2D3A4B", "#6C8CFF", "#4F6FE0", "#FFFFFF", "#CBD3DF"),
            new Seeds("#2B2F36", "#2B2F36", "#262A30", "#1F2328", "#D5DAE2", "#8B93A1", "#D5DAE2", "#8B93A1", "#E6EAF0", "#8FA5FF", "#6C8CFF", "#1B1F26", "#3A3F48"))
            .fits(HomingEditorial.ID);

    /** Black marker on white paper: one ink for text, lines and the primary alike; a grey for what is said quietly. Sketchy's — and black-on-white brutalism is the classic. */
    public static final SeedPalette MARKER = new SeedPalette("marker", "Marker",
            "Black marker on white paper — one ink for the text, the lines and the primary alike.", HomingSketchy.ID,
            new Seeds("#FFFFFF", "#FFFFFF", "#F7F7F9", "#333333", "#212529", "#555555", "#FFFFFF", "#CCCCCC", "#212529", "#333333", "#555555", "#FFFFFF", "#333333"),
            new Seeds("#212529", "#212529", "#2A2C2E", "#F8F9FA", "#DEE2E6", "#ADB5BD", "#212529", "#555555", "#F8F9FA", "#F8F9FA", "#DEE2E6", "#000000", "#DEE2E6"))
            .fits(HomingNeoBrutalism.ID, HomingEditorial.ID);

    private static final Map<DesignId, Map<DesignClass<?>, Impl>> WORDS = new java.util.concurrent.ConcurrentHashMap<>();

    @Override public Impl impl(DesignClass<?> pair) {
        return pair.onColourPlane() ? WORDS.computeIfAbsent(id, s -> words()).get(pair) : null;
    }

    // ── the rule: the seeds over the pairs ───────────────────────────────

    private Map<DesignClass<?>, Impl> words() {
        Seeds l = light, d = dark;
        var w = new LinkedHashMap<DesignClass<?>, Impl>();
        java.util.function.Consumer<Map.Entry<DesignClass<?>, Impl>> put = e -> w.put(e.getKey(), e.getValue());

        // layers
        put.accept(sfc(of(Base.class, Color.Surface.class), l.surface, d.surface));
        put.accept(ink(of(Base.class, Color.Scrollbar.class), l.muted + " " + l.surface, d.muted + " " + d.surface));
        put.accept(sfc(of(Raised.class, Color.Surface.class), l.raised, d.raised));
        put.accept(Map.entry(of(Raised.class, Color.Edge.class), Impl.Bindings.none()
                .at(State.REST, "border-color", l.border).at(State.FOCUS, "border-color", l.accent)
                .in(Mode.DARK, State.REST, "border-color", d.border).in(Mode.DARK, State.FOCUS, "border-color", d.accent)));
        put.accept(sfc(of(Recessed.class, Color.Surface.class), l.recessed, d.recessed));
        put.accept(sfc(of(Inverted.class, Color.Surface.class), l.inverted, d.inverted));

        // inks
        put.accept(ink(of(Body.class, Color.Ink.class), l.text, d.text));
        put.accept(ink(of(Heading.class, Color.Ink.class), l.title, d.title));
        put.accept(ink(of(Display.class, Color.Ink.class), l.title, d.title));
        put.accept(ink(of(Lede.class, Color.Ink.class), l.muted, d.muted));
        put.accept(ink(of(Kicker.class, Color.Ink.class), l.accentEmphasis, d.accentEmphasis));
        put.accept(Map.entry(of(Link.class, Color.Ink.class), Impl.Bindings.of(l.muted).at(State.HOVER, l.title)
                .in(Mode.DARK, State.REST, d.muted).in(Mode.DARK, State.HOVER, d.title)));
        put.accept(sfc(of(Code.class, Color.Surface.class), l.recessed, d.recessed));
        put.accept(ink(of(Code.class, Color.Ink.class), l.title, d.title));

        // emphasis
        put.accept(sfc(of(Primary.class, Color.Surface.class), l.accent, d.accent));
        put.accept(ink(of(Primary.class, Color.Ink.class), l.accent, d.accent));
        put.accept(edg(of(Primary.class, Color.Edge.class), l.accent, d.accent));
        put.accept(ink(of(OnPrimary.class, Color.Ink.class), l.accentOn, d.accentOn));
        put.accept(sfc(of(Secondary.class, Color.Surface.class), l.accentEmphasis, d.accentEmphasis));
        put.accept(sfc(of(Tertiary.class, Color.Surface.class), l.border, d.border));
        put.accept(ink(of(Muted.class, Color.Ink.class), l.muted, d.muted));
        put.accept(edg(of(Muted.class, Color.Edge.class), l.muted, d.muted));

        // pairings
        put.accept(ink(of(OnInverted.class, Color.Ink.class), l.onInverted, d.onInverted));
        put.accept(Map.entry(of(OnInvertedMuted.class, Color.Ink.class), Impl.Bindings.of(l.onInvertedMuted).at(State.HOVER, l.accent)
                .in(Mode.DARK, State.REST, d.onInvertedMuted).in(Mode.DARK, State.HOVER, d.accent)));
        put.accept(edg(of(OnInverted.class, Color.Edge.class), l.border, d.border));

        // feedback — the same signals in every seed palette
        put.accept(surface(of(Danger.class, Color.Surface.class), "rgba(220, 38, 38, 0.10)"));
        put.accept(one(of(Danger.class, Color.Ink.class), "#7F1D1D", "#FCA5A5"));
        put.accept(edge(of(Danger.class, Color.Edge.class), "rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) rgba(220, 38, 38, 0.35) #DC2626"));
        put.accept(surface(of(Success.class, Color.Surface.class), "rgba(34, 139, 34, 0.12)"));
        // the one word that scales, for now: a good mark at 1, a bad one at -1 (danger's ink), an unremarkable one at 0 (the muted ink)
        put.accept(scaled(of(Success.class, Color.Ink.class), "#7F1D1D", "#FCA5A5", l.muted, d.muted, "#1B5E20", "#86EFAC"));
        put.accept(edge(of(Success.class, Color.Edge.class), "rgba(34, 139, 34, 0.35)"));
        put.accept(surface(of(Warning.class, Color.Surface.class), "rgba(202, 138, 4, 0.12)"));
        put.accept(one(of(Warning.class, Color.Ink.class), "#713F12", "#FDE68A"));
        put.accept(edge(of(Warning.class, Color.Edge.class), "rgba(202, 138, 4, 0.35)"));

        // interaction — nothing at rest; the states are the slots. Hover tints in
        // the accent, selected inverts, current tints lightly, checked and focus
        // edge in the accent. One class on the element, the attribute says the rest.
        put.accept(Map.entry(of(Interactive.class, Color.Surface.class), Impl.Bindings.none()
                .at(State.REST, "background-color", "transparent")
                .at(State.HOVER, "background-color", tint(l.accent, 15))
                .at(State.SELECTED, "background-color", l.inverted)
                .at(State.CURRENT, "background-color", tint(l.accent, 7))
                .at(State.HIGHLIGHTED, "background-color", tint(l.accent, 28))
                .in(Mode.DARK, State.HIGHLIGHTED, "background-color", tint(d.accent, 28))
                .in(Mode.DARK, State.HOVER, "background-color", tint(d.accent, 15))
                .in(Mode.DARK, State.SELECTED, "background-color", d.inverted)
                .in(Mode.DARK, State.CURRENT, "background-color", tint(d.accent, 7))));
        put.accept(Map.entry(of(Interactive.class, Color.Ink.class), Impl.Bindings.of("inherit")
                .at(State.SELECTED, l.onInverted).at(State.CURRENT, l.title).at(State.HIGHLIGHTED, l.title)
                .in(Mode.DARK, State.SELECTED, d.onInverted).in(Mode.DARK, State.CURRENT, d.title).in(Mode.DARK, State.HIGHLIGHTED, d.title)));
        put.accept(Map.entry(of(Interactive.class, Color.Edge.class), Impl.Bindings.none()
                .at(State.REST, "border-color", "transparent")
                .at(State.HOVER, "border-color", l.border)
                .at(State.SELECTED, "border-color", l.inverted)
                .at(State.CURRENT, "border-color", l.accent)
                .at(State.HIGHLIGHTED, "border-color", l.accent)
                .in(Mode.DARK, State.HIGHLIGHTED, "border-color", d.accent)
                .at(State.CHECKED, "border-color", l.accent)
                .at(State.FOCUS, "outline-color", l.accent)
                .in(Mode.DARK, State.HOVER, "border-color", d.border)
                .in(Mode.DARK, State.SELECTED, "border-color", d.inverted)
                .in(Mode.DARK, State.CURRENT, "border-color", d.accent)
                .in(Mode.DARK, State.CHECKED, "border-color", d.accent)
                .in(Mode.DARK, State.FOCUS, "outline-color", d.accent)));
        // Selectable — a row, a cell, an option — is coloured exactly as Interactive: the one word, under both pairs
        w.put(of(Selectable.class, Color.Surface.class), w.get(of(Interactive.class, Color.Surface.class)));
        w.put(of(Selectable.class, Color.Ink.class),     w.get(of(Interactive.class, Color.Ink.class)));
        w.put(of(Selectable.class, Color.Edge.class),    w.get(of(Interactive.class, Color.Edge.class)));
        put.accept(sfc(of(Selected.class, Color.Surface.class), l.inverted, d.inverted));
        put.accept(Map.entry(of(Selected.class, Color.Ink.class), Impl.Bindings.of(l.onInverted).at(State.HOVER, l.accent)
                .in(Mode.DARK, State.REST, d.onInverted).in(Mode.DARK, State.HOVER, d.accent)));
        put.accept(edg(of(Selected.class, Color.Edge.class), l.inverted, d.inverted));
        put.accept(sfc(of(Current.class, Color.Surface.class), tint(l.accent, 7), tint(d.accent, 7)));
        put.accept(ink(of(Current.class, Color.Ink.class), l.title, d.title));
        put.accept(edg(of(Current.class, Color.Edge.class), l.accent, d.accent));
        put.accept(ring(of(Focus.class, Color.Edge.class), "color-mix(in srgb, " + l.accent + " 55%, " + l.border + ")", "color-mix(in srgb, " + d.accent + " 55%, " + d.border + ")"));
        put.accept(sfc(of(Backdrop.class, Color.Surface.class), tint(l.surface, 64), tint(d.surface, 64)));   // the wash a mask lays over what is waiting

        // structure — hairlines in the border, the marks in the accent
        put.accept(edg(of(Divider.class, Color.Edge.class), l.accent, d.accent));
        put.accept(edg(of(Hairline.class, Color.Edge.class), l.border, d.border));
        put.accept(edg(of(Cap.class, Color.Edge.class), l.border, d.border));
        put.accept(edg(of(Spine.class, Color.Edge.class), l.border, d.border));
        put.accept(edg(of(Rail.class, Color.Edge.class), l.border, d.border));
        put.accept(edg(of(Lattice.class, Color.Edge.class), l.border, d.border));
        put.accept(Map.entry(of(Marker.class, Color.Edge.class), Impl.Bindings.none()
                .at(State.REST, "border-color", "transparent").at(State.HOVER, "border-color", l.accent)
                .in(Mode.DARK, State.HOVER, "border-color", d.accent)));
        put.accept(Map.entry(of(Bar.class, Color.Edge.class), Impl.Bindings.none()
                .at(State.REST, "border-color", bar(l.border, l.accent)).at(State.HOVER, "border-color", bar(l.border, l.accentEmphasis))
                .in(Mode.DARK, State.REST, "border-color", bar(d.border, d.accent)).in(Mode.DARK, State.HOVER, "border-color", bar(d.border, d.accentEmphasis))));

        // prose — the document's elements, by reference to the pairs above: every colour
        // stays a variable, so prose follows dark mode and a live palette like the rest
        put.accept(body(of(Prose.class, Color.Ink.class), """
                color: %s;
                h1, h2, h3 { color: %s; }
                h4 { color: %s; }
                a { color: %s; }
                a:hover { color: %s; }
                blockquote { color: %s; }
                code { color: %s; }
                pre { color: %s; }
                pre code { color: inherit; }
                th { color: %s; }
                """.formatted(ink(Body.class), ink(Heading.class), ink(Kicker.class), ink(Kicker.class), ink(Heading.class), ink(Muted.class), ink(Code.class), ink(OnInvertedMuted.class), ink(OnInverted.class))));
        put.accept(body(of(Prose.class, Color.Surface.class), """
                code { background-color: %s; }
                pre { background-color: %s; }
                pre code { background-color: transparent; }
                th { background-color: %s; }
                tr:nth-child(even) td { background-color: %s; }
                """.formatted(sfc(Recessed.class), sfc(Inverted.class), sfc(Inverted.class), sfc(Recessed.class))));
        put.accept(body(of(Prose.class, Color.Edge.class), """
                h1 { border-color: %s; }
                blockquote { border-color: %s; }
                th, td { border-color: %s; }
                hr { border-color: %s; }
                """.formatted(edg(Primary.class), edg(Primary.class), edg(Hairline.class), edg(Hairline.class))));
        return Map.copyOf(w);
    }

    // ── references: a prose body reads the pair, never the seed ──────────
    private static String ink(Class<? extends hue.captains.singapura.js.homing.design.Semantic> s) { return of(s, Color.Ink.class).var(); }
    private static String sfc(Class<? extends hue.captains.singapura.js.homing.design.Semantic> s) { return of(s, Color.Surface.class).var("background-color"); }
    private static String edg(Class<? extends hue.captains.singapura.js.homing.design.Semantic> s) { return of(s, Color.Edge.class).var("border-color"); }

    // ── small words: a dark re-binding only where it differs ─────────────
    private static Map.Entry<DesignClass<?>, Impl> sfc(DesignClass<?> c, String l, String d) { return l.equals(d) ? surface(c, l) : surface(c, l, d); }
    private static Map.Entry<DesignClass<?>, Impl> ink(DesignClass<?> c, String l, String d) { return l.equals(d) ? one(c, l) : one(c, l, d); }
    private static Map.Entry<DesignClass<?>, Impl> edg(DesignClass<?> c, String l, String d) { return l.equals(d) ? edge(c, l) : edge(c, l, d); }
    private static String tint(String colour, int pct) { return "color-mix(in srgb, " + colour + " " + pct + "%, transparent)"; }
    private static String bar(String hair, String mark) { return hair + " " + hair + " " + hair + " " + mark; }
}
