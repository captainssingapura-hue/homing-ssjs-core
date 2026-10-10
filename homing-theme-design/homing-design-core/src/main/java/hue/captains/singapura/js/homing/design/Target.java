package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.DerivedCss;

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
 * <p>Every leaf is a {@link DerivedCss}: <b>the sheet is the target</b>, derived
 * for each theme. It declares no class of its own and exports nothing — a
 * component wears {@code DesignClass.of(Danger.class, Color.Surface.class)},
 * the theme's owner loads the surface sheet beside the wearer's group, and the
 * surface sheet under a design is one generated rule per pair the closure
 * wears (RFC 0064, D25).</p>
 *
 * <p>Names repeat across branches ({@code Face} could one day appear under
 * {@code Sound}), so a leaf's path is {@code Branch.Leaf} and its token is
 * {@code branch-leaf}: {@code color-surface}, {@code motion-transform}.</p>
 */
public sealed interface Target permits
        Target.Color, Target.Shape, Target.Size, Target.Type, Target.Motion,
        Target.Effect, Target.Affordance, Target.Sound, Target.Asset {

    /** The properties this leaf owns; disjoint from its siblings', but a superseded leaf's and its successors'. Empty for non-CSS carriers. */
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
     * The precise leaves that answer this one's properties in the semantic classes, where this one
     * is kept only for the old design system, until it retires: {@link Color.Edge},
     * {@link Color.Surface}, {@link Shape.Rule}. Empty for a leaf the semantic classes answer. A
     * superseded leaf and its successors are the one exception to disjointness, and this is its
     * ledger: it only shrinks.
     */
    default List<Target> supersededBy() { return List.of(); }

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
     * What is painted with colour. Each leaf is a <b>semantic colour</b>: the set of physical
     * colours one meaning needs, answered as a whole and never mixed. A <b>physical colour</b> is a
     * slot CSS paints a colour in - a property, or, where CSS gives the slot no property of its own
     * (a shadow's colours, a gradient's stops), a longhand registered to stand in for it, set on the
     * element itself and never inherited.
     */
    sealed interface Color extends Target permits Color.Surface, Color.Ink, Color.Edge, Color.Fill, Color.Stroke, Color.Scrollbar,
            Color.Background, Color.Border, Color.Outline, Color.ColumnRule, Color.BoxShadow {

        /** Each registered colour: a colour, on the element itself, transparent until answered. */
        @Override default List<String> registrations() {
            return properties().stream().filter(p -> p.startsWith("--")).sorted()
                    .map(p -> "@property " + p + " { syntax: \"<color>\"; inherits: false; initial-value: transparent; }").toList();
        }

        /**
         * The face an element presents: a colour, and an image layer over it — a
         * wash (a gradient), or a pattern, which is an image with a tile size and
         * a repeat. A gradient is an image in CSS, so it goes where an image goes.
         * The old design system's: the semantic classes answer its colours with
         * {@link Background} and its geometry with {@link Shape.Background}.
         */
        record Surface() implements Color, DerivedCss<Surface> {
            public static final Surface INSTANCE = new Surface();
            @Override public Set<String> properties() { return Set.of("background-color", "background-image", "background-size", "background-repeat"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public List<Target> supersededBy() { return List.of(Background.INSTANCE, Shape.Background.INSTANCE); }
        }
        /** The colour of what is written on it. */
        record Ink() implements Color, DerivedCss<Ink> {
            public static final Ink INSTANCE = new Ink();
            @Override public Set<String> properties() { return Set.of("color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /**
         * The colour of its border and outline. The old design system's: the semantic classes
         * answer the box's line, the ring of attention and the rules between columns apart.
         */
        record Edge() implements Color, DerivedCss<Edge> {
            public static final Edge INSTANCE = new Edge();
            @Override public Set<String> properties() { return Set.of("border-color", "outline-color", "column-rule-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public List<Target> supersededBy() { return List.of(Border.INSTANCE, Outline.INSTANCE, ColumnRule.INSTANCE); }
        }
        /** SVG fill. */
        record Fill() implements Color, DerivedCss<Fill> {
            public static final Fill INSTANCE = new Fill();
            @Override public Set<String> properties() { return Set.of("fill"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG stroke colour. */
        record Stroke() implements Color, DerivedCss<Stroke> {
            public static final Stroke INSTANCE = new Stroke();
            @Override public Set<String> properties() { return Set.of("stroke"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The scrollbar's two colours. */
        record Scrollbar() implements Color, DerivedCss<Scrollbar> {
            public static final Scrollbar INSTANCE = new Scrollbar();
            @Override public Set<String> properties() { return Set.of("scrollbar-color"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /**
         * A box's background: the ground, and every colour a gradient or a pattern is drawn in on it
         * - a ramp's three stops and a pattern's marks. One set, since what is drawn over the ground
         * shows it through; the gradient's and the pattern's geometry is {@link Shape.Background}'s.
         */
        record Background() implements Color, DerivedCss<Background> {
            public static final Background INSTANCE = new Background();
            @Override public Set<String> properties() {
                return Set.of("background-color", "--background-ramp-from", "--background-ramp-via", "--background-ramp-to", "--background-mark");
            }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The box's line: its border. */
        record Border() implements Color, DerivedCss<Border> {
            public static final Border INSTANCE = new Border();
            @Override public Set<String> properties() { return Set.of("border-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The ring drawn around a box for attention: on focus, and the keys' marks. */
        record Outline() implements Color, DerivedCss<Outline> {
            public static final Outline INSTANCE = new Outline();
            @Override public Set<String> properties() { return Set.of("outline-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The rules drawn between a box's columns. */
        record ColumnRule() implements Color, DerivedCss<ColumnRule> {
            public static final ColumnRule INSTANCE = new ColumnRule();
            @Override public Set<String> properties() { return Set.of("column-rule-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /**
         * A box's shadow: the dark it casts, the light on its own lit side or rim, the light it
         * gives off. CSS has no colour longhand for a shadow, so each is registered; the shadow's
         * geometry is {@link Shape.Shadow}'s.
         */
        record BoxShadow() implements Color, DerivedCss<BoxShadow> {
            public static final BoxShadow INSTANCE = new BoxShadow();
            @Override public Set<String> properties() { return Set.of("--box-shadow-cast", "--box-shadow-lit", "--box-shadow-glow"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Shape — the geometry a design owns
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Shape extends Target permits Shape.Corner, Shape.Rule, Shape.Shadow, Shape.Clip,
            Shape.Border, Shape.Outline, Shape.Background {
        record Corner() implements Shape, DerivedCss<Corner> {
            public static final Corner INSTANCE = new Corner();
            @Override public Set<String> properties() { return Set.of("border-radius"); }
            @Override public Set<State> states() { return STOOD_UP; }
        }
        /**
         * Border and outline width and style — the line, not its colour. The old design system's:
         * the semantic classes answer the box's line and the ring of attention apart.
         */
        record Rule() implements Shape, DerivedCss<Rule> {
            public static final Rule INSTANCE = new Rule();
            @Override public Set<String> properties() { return Set.of("border-width", "border-style", "outline-width", "outline-style", "outline-offset"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // a line may change with every state: dotted for current, dashed for highlighted
            @Override public List<Target> supersededBy() { return List.of(Border.INSTANCE, Outline.INSTANCE); }
        }
        /** A box's shadow, drawn in {@link Color.BoxShadow}'s colours: its offsets, blurs, spreads and insets. */
        record Shadow() implements Shape, DerivedCss<Shadow> {
            public static final Shadow INSTANCE = new Shadow();
            @Override public Set<String> properties() { return Set.of("box-shadow"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // depth is a state too: a selected or highlighted thing lifts
            @Override public List<Color> colouredBy() { return List.of(Color.BoxShadow.INSTANCE); }
        }
        /** The box's line: its border's width and style. */
        record Border() implements Shape, DerivedCss<Border> {
            public static final Border INSTANCE = new Border();
            @Override public Set<String> properties() { return Set.of("border-width", "border-style"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The ring drawn around a box for attention: its width, style and offset. */
        record Outline() implements Shape, DerivedCss<Outline> {
            public static final Outline INSTANCE = new Outline();
            @Override public Set<String> properties() { return Set.of("outline-width", "outline-style", "outline-offset"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /**
         * What is drawn over a box's ground: a gradient or a pattern, drawn in {@link Color.Background}'s
         * colours, or an image that needs none - its angle, stops, tile, repeat and place.
         */
        record Background() implements Shape, DerivedCss<Background> {
            public static final Background INSTANCE = new Background();
            @Override public Set<String> properties() { return Set.of("background-image", "background-size", "background-repeat", "background-position"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public List<Color> colouredBy() { return List.of(Color.Background.INSTANCE); }
        }
        record Clip() implements Shape, DerivedCss<Clip> {
            public static final Clip INSTANCE = new Clip();
            @Override public Set<String> properties() { return Set.of("clip-path"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Size — density; the space a design owns inside and between
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Size extends Target permits Size.Inset, Size.Gap, Size.Extent, Size.Proportion {
        /** The air inside a box — block and inline, each one length, so each can grow with the element's size. */
        record Inset() implements Size, DerivedCss<Inset> {
            public static final Inset INSTANCE = new Inset();
            @Override public Set<String> properties() { return Set.of("padding-block", "padding-inline"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Gap() implements Size, DerivedCss<Gap> {
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
        record Extent() implements Size, DerivedCss<Extent> {
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
        record Proportion() implements Size, DerivedCss<Proportion> {
            public static final Proportion INSTANCE = new Proportion();
            @Override public Set<String> properties() { return Set.of("aspect-ratio"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Type — how text is set
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Type extends Target permits Type.Face, Type.Weight, Type.Scale, Type.Treatment, Type.Decoration, Type.Glyph {
        record Face() implements Type, DerivedCss<Face> {
            public static final Face INSTANCE = new Face();
            @Override public Set<String> properties() { return Set.of("font-family"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Weight() implements Type, DerivedCss<Weight> {
            public static final Weight INSTANCE = new Weight();
            @Override public Set<String> properties() { return Set.of("font-weight"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.SELECTED, State.CURRENT); }
        }
        record Scale() implements Type, DerivedCss<Scale> {
            public static final Scale INSTANCE = new Scale();
            @Override public Set<String> properties() { return Set.of("font-size", "line-height"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Spacing, case, style and features — everything about the setting that is not face, weight or size. */
        record Treatment() implements Type, DerivedCss<Treatment> {
            public static final Treatment INSTANCE = new Treatment();
            @Override public Set<String> properties() { return Set.of("letter-spacing", "word-spacing", "text-transform", "font-style",
                    "font-variant-caps", "font-variant-numeric", "font-feature-settings"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Decoration() implements Type, DerivedCss<Decoration> {
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
        record Glyph() implements Type, DerivedCss<Glyph> {
            public static final Glyph INSTANCE = new Glyph();
            @Override public Set<String> properties() { return Set.of("content"); }
            @Override public Set<State> states() { return INTERACTIVE; }
            @Override public String pseudo() { return "::before"; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Motion — what CSS can move; the moment is a state, not a leaf
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Motion extends Target permits Motion.Ease, Motion.Transform, Motion.Animate {
        /** How changes travel: the transition. One per element, so one leaf. */
        record Ease() implements Motion, DerivedCss<Ease> {
            public static final Ease INSTANCE = new Ease();
            @Override public Set<String> properties() { return Set.of("transition"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Where the element goes per state — the press, the lift, the nudge. */
        record Transform() implements Motion, DerivedCss<Transform> {
            public static final Transform INSTANCE = new Transform();
            @Override public Set<String> properties() { return Set.of("transform"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** Keyframed motion — arrival, attention, a spinner. */
        record Animate() implements Motion, DerivedCss<Animate> {
            public static final Animate INSTANCE = new Animate();
            @Override public Set<String> properties() { return Set.of("animation"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID); }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Effect — optical treatments of the whole element
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Effect extends Target permits Effect.Opacity, Effect.Filter, Effect.Blend {
        record Opacity() implements Effect, DerivedCss<Opacity> {
            public static final Opacity INSTANCE = new Opacity();
            @Override public Set<String> properties() { return Set.of("opacity"); }
            @Override public Set<State> states() { return POINTER; }
        }
        record Filter() implements Effect, DerivedCss<Filter> {
            public static final Filter INSTANCE = new Filter();
            @Override public Set<String> properties() { return Set.of("filter", "backdrop-filter"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // a lift a table row can show — filter paints where box-shadow does not
        }
        record Blend() implements Effect, DerivedCss<Blend> {
            public static final Blend INSTANCE = new Blend();
            @Override public Set<String> properties() { return Set.of("mix-blend-mode"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Affordance — what the pointer is told
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Affordance extends Target permits Affordance.Cursor, Affordance.Select {
        record Cursor() implements Affordance, DerivedCss<Cursor> {
            public static final Cursor INSTANCE = new Cursor();
            @Override public Set<String> properties() { return Set.of("cursor"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.DISABLED, State.DRAGGING); }
        }
        record Select() implements Affordance, DerivedCss<Select> {
            public static final Select INSTANCE = new Select();
            @Override public Set<String> properties() { return Set.of("user-select"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Sound — not CSS; a cue per state
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Sound extends Target permits Sound.Cue {
        record Cue() implements Sound, DerivedCss<Cue> {
            public static final Cue INSTANCE = new Cue();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID, State.SELECTED); }
            @Override public Carrier carrier() { return Carrier.AUDIO; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Asset — not CSS; a picture the semantic is shown by
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Asset extends Target permits Asset.Icon, Asset.Illustration {
        record Icon() implements Asset, DerivedCss<Icon> {
            public static final Icon INSTANCE = new Icon();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
        record Illustration() implements Asset, DerivedCss<Illustration> {
            public static final Illustration INSTANCE = new Illustration();
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
    }

    /** Every leaf of the tree, in declaration order — the one place the tree is walked. */
    static List<Target> leaves() { return Trees.targetLeaves(); }
}
