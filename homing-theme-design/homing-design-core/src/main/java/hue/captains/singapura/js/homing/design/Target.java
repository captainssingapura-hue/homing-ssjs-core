package hue.captains.singapura.js.homing.design;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The physical tree — <b>sealed</b>. What a medium can express, branch by
 * branch, leaf by leaf; the framework owns it and a design cannot add to it,
 * which is correct: CSS did not give a design a new property either.
 *
 * <p>A branch is one medium; its leaves partition the medium's properties,
 * so two design classes on one element never write the same property and
 * cascade order is irrelevant. Every leaf declares the properties it owns
 * — a shorthand only where every longhand is the leaf's own; {@code border} and
 * {@code background} cross leaves and are never owned — the {@link State}s its
 * template offers a slot for, and the {@link Carrier} that delivers it. Every switch over the tree is
 * exhaustive: a rule cannot meet a target it does not know, a renderer
 * cannot serve one, and a new leaf breaks every switch until it is handled —
 * the price of closing the tree, paid where it should be.</p>
 *
 * <p>A leaf is a value: a semantic class answers a node of the taxonomy on it (RFC 0067), and the
 * house's look is one sheet of those classes, derived per theme. No leaf is a sheet of its own.</p>
 *
 * <p>Names repeat across branches ({@code Face} could one day appear under
 * {@code Sound}), so a leaf's path is {@code Branch.Leaf} and its token is
 * {@code branch-leaf}: {@code color-background}, {@code motion-transform}.</p>
 *
 * <p>Colour stands apart from every other branch (RFC 0067, appendix Semantic Colours and Flavours): a
 * style answers it in semantic colours, in cases each colour leaf offers, and never in declarations of its
 * own; every other branch is {@link Colorless}, answered in declarations that name no colour.</p>
 */
public sealed interface Target permits Target.Color, Target.Colorless {

    /**
     * Every branch but colour: answered in declarations of its own properties, which name no colour - a
     * shape drawn in colour reads the registered colours of the leaf that colours it ({@link #colouredBy()}).
     */
    sealed interface Colorless extends Target permits Target.Shape, Target.Size, Target.Type, Target.Motion, Target.Animation,
            Target.Effect, Target.Affordance, Target.Sound, Target.Asset {}

    /** The properties this leaf owns; disjoint from its siblings'. Empty for non-CSS carriers. */
    Set<String> properties();

    /** The states the template offers a slot for; always contains {@link State#REST}. */
    Set<State> states();

    default Carrier carrier() { return Carrier.CSS; }

    /**
     * The pseudo-element the leaf's properties are written on, or null for
     * the element itself: {@code Type.Glyph} writes {@code content} on
     * {@code ::before}, since content paints nowhere else.
     */
    default String pseudo() { return null; }

    /** {@code branch-leaf}, from the type. */
    default String token() { return Trees.targetToken(getClass()); }

    /**
     * The colour leaves whose registered colours a body on this leaf may read: a box's shadow is
     * drawn in the box shadow's colours, a background's gradient and pattern in the background's.
     * Its geometry is the style's, its colours the palette's; this is where the two meet.
     */
    default List<Color> colouredBy() { return List.of(); }

    /** What its registered properties need declared at the head of a sheet; none for a leaf that registers none. */
    default List<String> registrations() { return List.of(); }

    // ── helpers for the leaves ────────────────────────────────────────────
    Set<State> REST_ONLY    = EnumSet.of(State.REST);
    Set<State> INTERACTIVE  = EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED,
                                         State.SELECTED, State.CURRENT, State.CHECKED, State.PRESSED, State.INVALID, State.EXPANDED, State.HIGHLIGHTED,
                                         State.CANDIDATE, State.LENT, State.HELD, State.DRAGGING,
                                         // the physical register: a design answers depth in whatever plane it honestly
                                         // uses for it - the shadow usually, a lit rim, a step of tone - or in none
                                         State.ELEVATED, State.SUNKEN);
    Set<State> POINTER      = EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED);
    /** At rest, and stood up: a size or a corner that differs for a thing laid vertically - a fader's cap - and by nothing else. */
    Set<State> STOOD_UP     = EnumSet.of(State.REST, State.VERTICAL);

    // ═════════════════════════════════════════════════════════════════════
    //  Color — what is painted with colour
    // ═════════════════════════════════════════════════════════════════════
    /**
     * What is painted with colour. Each leaf owns the colour properties one thing is painted in,
     * answered as a whole and never mixed - most one, a background five, a shadow three. Where CSS
     * has no property for a colour (a shadow's colours, a gradient's stops), a longhand is registered
     * to stand in for it, set on the element itself and never inherited.
     *
     * <p>A style answers a leaf in one of the cases it offers ({@code Paint}), naming semantic
     * colours - never a value. Which cases a leaf offers is what CSS can do with its properties, said
     * by the markers below, so no case lands where nobody means it.</p>
     */
    sealed interface Color extends Target permits Color.Ink, Color.Fill, Color.Stroke, Color.Scrollbar,
            Color.Background, Color.Border, Color.Outline, Color.ColumnRule, Color.BoxShadow {

        /** Each registered colour: a colour, on the element itself, transparent until answered. */
        @Override default List<String> registrations() {
            return properties().stream().filter(p -> p.startsWith("--")).sorted()
                    .map(p -> "@property " + p + " { syntax: \"<color>\"; inherits: false; initial-value: transparent; }").toList();
        }

        /** A target that writes one colour. */
        sealed interface Single permits Ink, Fill, Stroke, Border, Outline, ColumnRule {}

        /** A target whose property CSS inherits: it may take the colour of what the element stands on. */
        sealed interface Inheritable permits Ink, Fill, Stroke, Scrollbar {}

        /** A leaf that may paint nothing. */
        sealed interface Omissible permits Fill, Stroke {}

        /** A leaf that may take the element's typography colour, whole - CSS's {@code currentColor}. */
        sealed interface Typographic permits Fill, Border, Outline, ColumnRule {}

        /** A leaf whose colour may move along a scale, by the element's degree: on a casting with the COLOUR axis. */
        sealed interface Scalable permits Ink, Background {}

        /** The colour of what is written on it. */
        record Ink() implements Color, Single, Inheritable, Scalable {
            public static final Ink INSTANCE = new Ink();
            @Override public Set<String> properties() { return Set.of("color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG fill. */
        record Fill() implements Color, Single, Inheritable, Omissible, Typographic {
            public static final Fill INSTANCE = new Fill();
            @Override public Set<String> properties() { return Set.of("fill"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG stroke colour. */
        record Stroke() implements Color, Single, Inheritable, Omissible {
            public static final Stroke INSTANCE = new Stroke();
            @Override public Set<String> properties() { return Set.of("stroke"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The scrollbar's two colours. */
        record Scrollbar() implements Color, Inheritable {
            public static final Scrollbar INSTANCE = new Scrollbar();
            @Override public Set<String> properties() { return Set.of("scrollbar-color"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /**
         * A box's background: the ground, and every colour a gradient or a pattern is drawn in on it
         * - a ramp's three stops and a pattern's marks. One set, since what is drawn over the ground
         * shows it through; the gradient's and the pattern's geometry is {@link Shape.Background}'s.
         */
        record Background() implements Color, Scalable {
            public static final Background INSTANCE = new Background();
            @Override public Set<String> properties() {
                return Set.of("background-color", "--background-ramp-from", "--background-ramp-via", "--background-ramp-to", "--background-mark");
            }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The box's line: its border. */
        record Border() implements Color, Single, Typographic {
            public static final Border INSTANCE = new Border();
            @Override public Set<String> properties() { return Set.of("border-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The ring drawn around a box for attention: on focus, and the keys' marks. */
        record Outline() implements Color, Single, Typographic {
            public static final Outline INSTANCE = new Outline();
            @Override public Set<String> properties() { return Set.of("outline-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The rules drawn between a box's columns. */
        record ColumnRule() implements Color, Single, Typographic {
            public static final ColumnRule INSTANCE = new ColumnRule();
            @Override public Set<String> properties() { return Set.of("column-rule-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /**
         * A box's shadow: the dark it casts, the light on its own lit side or rim, the light it
         * gives off. CSS has no colour longhand for a shadow, so each is registered; the shadow's
         * geometry is {@link Shape.Shadow}'s.
         */
        record BoxShadow() implements Color {
            public static final BoxShadow INSTANCE = new BoxShadow();
            @Override public Set<String> properties() { return Set.of("--box-shadow-cast", "--box-shadow-lit", "--box-shadow-glow"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Shape — the geometry a design owns
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Shape extends Colorless permits Shape.Corner, Shape.Shadow, Shape.Clip,
            Shape.Border, Shape.Outline, Shape.Background {
        record Corner() implements Shape {
            public static final Corner INSTANCE = new Corner();
            @Override public Set<String> properties() { return Set.of("border-radius"); }
            @Override public Set<State> states() { return STOOD_UP; }
        }
        /** A box's shadow, drawn in {@link Color.BoxShadow}'s colours: its offsets, blurs, spreads and insets. */
        record Shadow() implements Shape {
            public static final Shadow INSTANCE = new Shadow();
            @Override public Set<String> properties() { return Set.of("box-shadow"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // depth is a state too: a selected or highlighted thing lifts
            @Override public List<Color> colouredBy() { return List.of(Color.BoxShadow.INSTANCE); }
        }
        /** The box's line: its border's width and style. */
        record Border() implements Shape {
            public static final Border INSTANCE = new Border();
            @Override public Set<String> properties() { return Set.of("border-width", "border-style"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The ring drawn around a box for attention: its width, style and offset. */
        record Outline() implements Shape {
            public static final Outline INSTANCE = new Outline();
            @Override public Set<String> properties() { return Set.of("outline-width", "outline-style", "outline-offset"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /**
         * What is drawn over a box's ground: a gradient or a pattern, drawn in {@link Color.Background}'s
         * colours, or an image that needs none - its angle, stops, tile, repeat and place.
         */
        record Background() implements Shape {
            public static final Background INSTANCE = new Background();
            @Override public Set<String> properties() { return Set.of("background-image", "background-size", "background-repeat", "background-position"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public List<Color> colouredBy() { return List.of(Color.Background.INSTANCE); }
        }
        record Clip() implements Shape {
            public static final Clip INSTANCE = new Clip();
            @Override public Set<String> properties() { return Set.of("clip-path"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Size — density; the space a design owns inside and between
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Size extends Colorless permits Size.Inset, Size.Gap, Size.Extent, Size.Proportion {
        /** The air inside a box — block and inline, each one length, so each can grow with the element's size. */
        record Inset() implements Size {
            public static final Inset INSTANCE = new Inset();
            @Override public Set<String> properties() { return Set.of("padding-block", "padding-inline"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Gap() implements Size {
            public static final Gap INSTANCE = new Gap();
            @Override public Set<String> properties() { return Set.of("gap"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /**
         * The extent of a box: the least a control or a row must be to be
         * usable, or — for a box whose measure is its own, a card — the
         * measure itself. A design's density, not a layout's size; a host may
         * cap it, never stretch it.
         */
        record Extent() implements Size {
            public static final Extent INSTANCE = new Extent();
            @Override public Set<String> properties() { return Set.of("min-height", "min-width", "inline-size", "block-size"); }
            @Override public Set<State> states() { return STOOD_UP; }
        }
        /**
         * The proportion of a box whose measure is its own, not its content's
         * — a card: its inline size is its host's, its block size follows from
         * this, and what is inside fits the box. A design's: an index card,
         * a square tile, a wide plate.
         */
        record Proportion() implements Size {
            public static final Proportion INSTANCE = new Proportion();
            @Override public Set<String> properties() { return Set.of("aspect-ratio"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Type — how text is set
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Type extends Colorless permits Type.Face, Type.Weight, Type.Scale, Type.Treatment, Type.Decoration, Type.Glyph {
        record Face() implements Type {
            public static final Face INSTANCE = new Face();
            @Override public Set<String> properties() { return Set.of("font-family"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Weight() implements Type {
            public static final Weight INSTANCE = new Weight();
            @Override public Set<String> properties() { return Set.of("font-weight"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.SELECTED, State.CURRENT); }
        }
        record Scale() implements Type {
            public static final Scale INSTANCE = new Scale();
            @Override public Set<String> properties() { return Set.of("font-size", "line-height"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Spacing, case, style and features — everything about the setting that is not face, weight or size. */
        record Treatment() implements Type {
            public static final Treatment INSTANCE = new Treatment();
            @Override public Set<String> properties() { return Set.of("letter-spacing", "word-spacing", "text-transform", "font-style",
                    "font-variant-caps", "font-variant-numeric", "font-feature-settings"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Decoration() implements Type {
            public static final Decoration INSTANCE = new Decoration();
            @Override public Set<String> properties() { return Set.of("text-decoration-line", "text-decoration-style", "text-decoration-thickness", "text-underline-offset", "text-shadow"); }   // a glow or a relief is a decoration of the letters too
            @Override public Set<State> states() { return POINTER; }
        }
        /**
         * The glyph a mark shows for a meaning — an {@link Icon} word's
         * picture: {@code content} on {@code ::before}, a symbol, an emoji or a
         * {@code url()}. The element that wears it is the mark; what it says is
         * the design's.
         */
        record Glyph() implements Type {
            public static final Glyph INSTANCE = new Glyph();
            @Override public Set<String> properties() { return Set.of("content"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public String pseudo() { return "::before"; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Motion — what CSS can move; the moment is a state, not a leaf
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Motion extends Colorless permits Motion.Ease, Motion.Transform {
        /** How changes travel: the transition. One per element, so one leaf. */
        record Ease() implements Motion {
            public static final Ease INSTANCE = new Ease();
            @Override public Set<String> properties() { return Set.of("transition"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Where the element goes per state — the press, the lift, the nudge. */
        record Transform() implements Motion {
            public static final Transform INSTANCE = new Transform();
            @Override public Set<String> properties() { return Set.of("transform"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Animation — what moves on its own, as a whole: an effect
    // ═════════════════════════════════════════════════════════════════════
    /**
     * What moves on its own (RFC 0050 · Episode 2, appendix The Copied Range): an {@link AnimationEffect} - the
     * dashes march, a band of tint sweeps across - answered whole and never in CSS. A design names the effect;
     * its keyframes, its timing and the properties it needs are the effect's, sealed, and the framework writes
     * them, with the effect's still dual, where motion is reduced or the effect is switched off.
     */
    sealed interface Animation extends Colorless permits Animation.Effect {
        /**
         * The effect an element wears: answered with an {@link AnimationEffect}, never with a body. It owns
         * {@code animation}; the properties the effect needs besides are the effect's to take, on the element
         * it is worn on, and no other answer there may write them.
         */
        record Effect() implements Animation {
            public static final Effect INSTANCE = new Effect();
            @Override public Set<String> properties() { return Set.of("animation"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Effect — optical treatments of the whole element
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Effect extends Colorless permits Effect.Opacity, Effect.Filter, Effect.Blend {
        record Opacity() implements Effect {
            public static final Opacity INSTANCE = new Opacity();
            @Override public Set<String> properties() { return Set.of("opacity"); }
            @Override public Set<State> states() { return POINTER; }
        }
        record Filter() implements Effect {
            public static final Filter INSTANCE = new Filter();
            @Override public Set<String> properties() { return Set.of("filter", "backdrop-filter"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // a lift a table row can show — filter paints where box-shadow does not
        }
        record Blend() implements Effect {
            public static final Blend INSTANCE = new Blend();
            @Override public Set<String> properties() { return Set.of("mix-blend-mode"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Affordance — what the pointer is told
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Affordance extends Colorless permits Affordance.Cursor, Affordance.Select {
        record Cursor() implements Affordance {
            public static final Cursor INSTANCE = new Cursor();
            @Override public Set<String> properties() { return Set.of("cursor"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.DISABLED, State.DRAGGING); }
        }
        record Select() implements Affordance {
            public static final Select INSTANCE = new Select();
            @Override public Set<String> properties() { return Set.of("user-select"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Sound — not CSS; a cue per state
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Sound extends Colorless permits Sound.Cue {
        record Cue() implements Sound {
            public static final Cue INSTANCE = new Cue();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID, State.SELECTED); }
            @Override public Carrier carrier() { return Carrier.AUDIO; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Asset — not CSS; a picture the semantic is shown by
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Asset extends Colorless permits Asset.Icon, Asset.Illustration {
        record Icon() implements Asset {
            public static final Icon INSTANCE = new Icon();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
        record Illustration() implements Asset {
            public static final Illustration INSTANCE = new Illustration();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
    }

    /** Every leaf of the tree, in declaration order — the one place the tree is walked. */
    static List<Target> leaves() { return Trees.targetLeaves(); }
}
