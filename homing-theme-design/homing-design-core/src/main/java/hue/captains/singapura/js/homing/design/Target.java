package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;

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
 * <p>Every leaf is a {@link CssGroup}: <b>the group is the target</b>, virtual
 * and derived. It declares no class of its own and exports nothing — a
 * component wears {@code DesignClass.of(Danger.class, Color.Surface.class)},
 * the graph counts the surface group as a dependency, and the surface sheet
 * under a design is one generated rule per pair the closure wears. Nothing
 * else of the module graph changes.</p>
 *
 * <p>Names repeat across branches ({@code Face} could one day appear under
 * {@code Sound}), so a leaf's path is {@code Branch.Leaf} and its token is
 * {@code branch-leaf}: {@code color-surface}, {@code motion-transform}.</p>
 */
public sealed interface Target permits
        Target.Color, Target.Shape, Target.Size, Target.Type, Target.Motion,
        Target.Effect, Target.Affordance, Target.Sound, Target.Asset {

    /** The properties this leaf owns; disjoint from its siblings'. Empty for non-CSS carriers. */
    Set<String> properties();

    /** The states the template offers a slot for; always contains {@link State#REST}. */
    Set<State> states();

    default Carrier carrier() { return Carrier.CSS; }

    /** {@code branch-leaf}, from the type. */
    default String token() { return Trees.targetToken(getClass()); }

    // ── helpers for the leaves ────────────────────────────────────────────
    Set<State> REST_ONLY    = EnumSet.of(State.REST);
    Set<State> INTERACTIVE  = EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED,
                                         State.SELECTED, State.CURRENT, State.CHECKED, State.INVALID, State.EXPANDED, State.HIGHLIGHTED);
    Set<State> POINTER      = EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED);

    // ═════════════════════════════════════════════════════════════════════
    //  Color — what is painted with colour
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Color extends Target permits Color.Surface, Color.Ink, Color.Edge, Color.Fill, Color.Stroke, Color.Scrollbar {
        /**
         * The face an element presents: a colour, and an image layer over it — a
         * wash (a gradient), or a pattern, which is an image with a tile size and
         * a repeat. A gradient is an image in CSS, so it goes where an image goes.
         */
        record Surface() implements Color, CssGroup<Surface> {
            public static final Surface INSTANCE = new Surface();
            @Override public List<CssClass<Surface>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("background-color", "background-image", "background-size", "background-repeat"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The colour of what is written on it. */
        record Ink() implements Color, CssGroup<Ink> {
            public static final Ink INSTANCE = new Ink();
            @Override public List<CssClass<Ink>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The colour of its border and outline. */
        record Edge() implements Color, CssGroup<Edge> {
            public static final Edge INSTANCE = new Edge();
            @Override public List<CssClass<Edge>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("border-color", "outline-color", "column-rule-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG fill. */
        record Fill() implements Color, CssGroup<Fill> {
            public static final Fill INSTANCE = new Fill();
            @Override public List<CssClass<Fill>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("fill"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG stroke colour. */
        record Stroke() implements Color, CssGroup<Stroke> {
            public static final Stroke INSTANCE = new Stroke();
            @Override public List<CssClass<Stroke>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("stroke"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The scrollbar's two colours. */
        record Scrollbar() implements Color, CssGroup<Scrollbar> {
            public static final Scrollbar INSTANCE = new Scrollbar();
            @Override public List<CssClass<Scrollbar>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("scrollbar-color"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Shape — the geometry a design owns
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Shape extends Target permits Shape.Corner, Shape.Rule, Shape.Shadow, Shape.Clip {
        record Corner() implements Shape, CssGroup<Corner> {
            public static final Corner INSTANCE = new Corner();
            @Override public List<CssClass<Corner>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("border-radius"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Border and outline width and style — the line, not its colour. */
        record Rule() implements Shape, CssGroup<Rule> {
            public static final Rule INSTANCE = new Rule();
            @Override public List<CssClass<Rule>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("border-width", "border-style", "outline-width", "outline-style", "outline-offset"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED, State.INVALID, State.SELECTED); }
        }
        record Shadow() implements Shape, CssGroup<Shadow> {
            public static final Shadow INSTANCE = new Shadow();
            @Override public List<CssClass<Shadow>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("box-shadow"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // depth is a state too: a selected or highlighted thing lifts
        }
        record Clip() implements Shape, CssGroup<Clip> {
            public static final Clip INSTANCE = new Clip();
            @Override public List<CssClass<Clip>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("clip-path"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Size — density; the space a design owns inside and between
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Size extends Target permits Size.Inset, Size.Gap, Size.Extent {
        record Inset() implements Size, CssGroup<Inset> {
            public static final Inset INSTANCE = new Inset();
            @Override public List<CssClass<Inset>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("padding"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Gap() implements Size, CssGroup<Gap> {
            public static final Gap INSTANCE = new Gap();
            @Override public List<CssClass<Gap>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("gap"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** The minimum a control or a row must be to be usable — a design's density, not a layout's size. */
        record Extent() implements Size, CssGroup<Extent> {
            public static final Extent INSTANCE = new Extent();
            @Override public List<CssClass<Extent>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("min-height", "min-width"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Type — how text is set
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Type extends Target permits Type.Face, Type.Weight, Type.Scale, Type.Treatment, Type.Decoration {
        record Face() implements Type, CssGroup<Face> {
            public static final Face INSTANCE = new Face();
            @Override public List<CssClass<Face>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("font-family"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Weight() implements Type, CssGroup<Weight> {
            public static final Weight INSTANCE = new Weight();
            @Override public List<CssClass<Weight>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("font-weight"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.SELECTED, State.CURRENT); }
        }
        record Scale() implements Type, CssGroup<Scale> {
            public static final Scale INSTANCE = new Scale();
            @Override public List<CssClass<Scale>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("font-size", "line-height"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Spacing, case, style and features — everything about the setting that is not face, weight or size. */
        record Treatment() implements Type, CssGroup<Treatment> {
            public static final Treatment INSTANCE = new Treatment();
            @Override public List<CssClass<Treatment>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("letter-spacing", "word-spacing", "text-transform", "font-style",
                    "font-variant-caps", "font-variant-numeric", "font-feature-settings"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Decoration() implements Type, CssGroup<Decoration> {
            public static final Decoration INSTANCE = new Decoration();
            @Override public List<CssClass<Decoration>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("text-decoration-line", "text-decoration-style", "text-decoration-thickness", "text-underline-offset"); }
            @Override public Set<State> states() { return POINTER; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Motion — what CSS can move; the moment is a state, not a leaf
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Motion extends Target permits Motion.Ease, Motion.Transform, Motion.Animate {
        /** How changes travel: the transition. One per element, so one leaf. */
        record Ease() implements Motion, CssGroup<Ease> {
            public static final Ease INSTANCE = new Ease();
            @Override public List<CssClass<Ease>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("transition"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Where the element goes per state — the press, the lift, the nudge. */
        record Transform() implements Motion, CssGroup<Transform> {
            public static final Transform INSTANCE = new Transform();
            @Override public List<CssClass<Transform>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("transform"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** Keyframed motion — arrival, attention, a spinner. */
        record Animate() implements Motion, CssGroup<Animate> {
            public static final Animate INSTANCE = new Animate();
            @Override public List<CssClass<Animate>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("animation"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID); }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Effect — optical treatments of the whole element
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Effect extends Target permits Effect.Opacity, Effect.Filter, Effect.Blend {
        record Opacity() implements Effect, CssGroup<Opacity> {
            public static final Opacity INSTANCE = new Opacity();
            @Override public List<CssClass<Opacity>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("opacity"); }
            @Override public Set<State> states() { return POINTER; }
        }
        record Filter() implements Effect, CssGroup<Filter> {
            public static final Filter INSTANCE = new Filter();
            @Override public List<CssClass<Filter>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("filter", "backdrop-filter"); }
            @Override public Set<State> states() { return INTERACTIVE; }   // a lift a table row can show — filter paints where box-shadow does not
        }
        record Blend() implements Effect, CssGroup<Blend> {
            public static final Blend INSTANCE = new Blend();
            @Override public List<CssClass<Blend>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("mix-blend-mode"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Affordance — what the pointer is told
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Affordance extends Target permits Affordance.Cursor, Affordance.Select {
        record Cursor() implements Affordance, CssGroup<Cursor> {
            public static final Cursor INSTANCE = new Cursor();
            @Override public List<CssClass<Cursor>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("cursor"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.DISABLED); }
        }
        record Select() implements Affordance, CssGroup<Select> {
            public static final Select INSTANCE = new Select();
            @Override public List<CssClass<Select>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of("user-select"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Sound — not CSS; a cue per state
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Sound extends Target permits Sound.Cue {
        record Cue() implements Sound, CssGroup<Cue> {
            public static final Cue INSTANCE = new Cue();
            @Override public List<CssClass<Cue>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID, State.SELECTED); }
            @Override public Carrier carrier() { return Carrier.AUDIO; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Asset — not CSS; a picture the semantic is shown by
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Asset extends Target permits Asset.Icon, Asset.Illustration {
        record Icon() implements Asset, CssGroup<Icon> {
            public static final Icon INSTANCE = new Icon();
            @Override public List<CssClass<Icon>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
        record Illustration() implements Asset, CssGroup<Illustration> {
            public static final Illustration INSTANCE = new Illustration();
            @Override public List<CssClass<Illustration>> cssClasses() { return List.of(); }
            @Override public Set<String> properties() { return Set.of(); }
            @Override public Set<State> states() { return REST_ONLY; }
            @Override public Carrier carrier() { return Carrier.ASSET; }
        }
    }

    /** Every leaf of the tree, in declaration order — the one place the tree is walked. */
    static List<Target> leaves() { return Trees.targetLeaves(); }
}
