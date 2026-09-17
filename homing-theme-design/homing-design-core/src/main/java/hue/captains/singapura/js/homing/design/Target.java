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
                                         State.SELECTED, State.CURRENT, State.CHECKED, State.INVALID, State.EXPANDED);
    Set<State> POINTER      = EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED);

    // ═════════════════════════════════════════════════════════════════════
    //  Color — what is painted with colour
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Color extends Target permits Color.Surface, Color.Ink, Color.Edge, Color.Fill, Color.Stroke, Color.Scrollbar {
        /** The face an element presents. */
        record Surface() implements Color {
            public static final Surface INSTANCE = new Surface();
            @Override public Set<String> properties() { return Set.of("background-color", "background-image"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The colour of what is written on it. */
        record Ink() implements Color {
            public static final Ink INSTANCE = new Ink();
            @Override public Set<String> properties() { return Set.of("color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The colour of its border and outline. */
        record Edge() implements Color {
            public static final Edge INSTANCE = new Edge();
            @Override public Set<String> properties() { return Set.of("border-color", "outline-color", "column-rule-color"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG fill. */
        record Fill() implements Color {
            public static final Fill INSTANCE = new Fill();
            @Override public Set<String> properties() { return Set.of("fill"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** SVG stroke colour. */
        record Stroke() implements Color {
            public static final Stroke INSTANCE = new Stroke();
            @Override public Set<String> properties() { return Set.of("stroke"); }
            @Override public Set<State> states() { return INTERACTIVE; }
        }
        /** The scrollbar's two colours. */
        record Scrollbar() implements Color {
            public static final Scrollbar INSTANCE = new Scrollbar();
            @Override public Set<String> properties() { return Set.of("scrollbar-color"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Shape — the geometry a design owns
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Shape extends Target permits Shape.Corner, Shape.Rule, Shape.Shadow, Shape.Clip {
        record Corner() implements Shape {
            public static final Corner INSTANCE = new Corner();
            @Override public Set<String> properties() { return Set.of("border-radius"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** Border and outline width and style — the line, not its colour. */
        record Rule() implements Shape {
            public static final Rule INSTANCE = new Rule();
            @Override public Set<String> properties() { return Set.of("border-width", "border-style", "outline-width", "outline-style", "outline-offset"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.DISABLED, State.INVALID, State.SELECTED); }
        }
        record Shadow() implements Shape {
            public static final Shadow INSTANCE = new Shadow();
            @Override public Set<String> properties() { return Set.of("box-shadow"); }
            @Override public Set<State> states() { return POINTER; }
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
    sealed interface Size extends Target permits Size.Inset, Size.Gap, Size.Extent {
        record Inset() implements Size {
            public static final Inset INSTANCE = new Inset();
            @Override public Set<String> properties() { return Set.of("padding"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        record Gap() implements Size {
            public static final Gap INSTANCE = new Gap();
            @Override public Set<String> properties() { return Set.of("gap"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
        /** The minimum a control or a row must be to be usable — a design's density, not a layout's size. */
        record Extent() implements Size {
            public static final Extent INSTANCE = new Extent();
            @Override public Set<String> properties() { return Set.of("min-height", "min-width"); }
            @Override public Set<State> states() { return REST_ONLY; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Type — how text is set
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Type extends Target permits Type.Face, Type.Weight, Type.Scale, Type.Treatment, Type.Decoration {
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
            @Override public Set<String> properties() { return Set.of("text-decoration-line", "text-decoration-style", "text-decoration-thickness", "text-underline-offset"); }
            @Override public Set<State> states() { return POINTER; }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Motion — what CSS can move; the moment is a state, not a leaf
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Motion extends Target permits Motion.Ease, Motion.Transform, Motion.Animate {
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
        /** Keyframed motion — arrival, attention, a spinner. */
        record Animate() implements Motion {
            public static final Animate INSTANCE = new Animate();
            @Override public Set<String> properties() { return Set.of("animation"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.HOVER, State.ACTIVE, State.FOCUS, State.INVALID); }
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  Effect — optical treatments of the whole element
    // ═════════════════════════════════════════════════════════════════════
    sealed interface Effect extends Target permits Effect.Opacity, Effect.Filter, Effect.Blend {
        record Opacity() implements Effect {
            public static final Opacity INSTANCE = new Opacity();
            @Override public Set<String> properties() { return Set.of("opacity"); }
            @Override public Set<State> states() { return POINTER; }
        }
        record Filter() implements Effect {
            public static final Filter INSTANCE = new Filter();
            @Override public Set<String> properties() { return Set.of("filter", "backdrop-filter"); }
            @Override public Set<State> states() { return POINTER; }
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
    sealed interface Affordance extends Target permits Affordance.Cursor, Affordance.Select {
        record Cursor() implements Affordance {
            public static final Cursor INSTANCE = new Cursor();
            @Override public Set<String> properties() { return Set.of("cursor"); }
            @Override public Set<State> states() { return EnumSet.of(State.REST, State.DISABLED); }
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
    sealed interface Sound extends Target permits Sound.Cue {
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
    sealed interface Asset extends Target permits Asset.Icon, Asset.Illustration {
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
