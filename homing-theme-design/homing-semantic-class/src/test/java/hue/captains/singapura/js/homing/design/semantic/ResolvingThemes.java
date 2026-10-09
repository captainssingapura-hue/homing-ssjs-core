package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Button;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Cancel;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Committing;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Control;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.DangerButton;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.FloatingPane;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Go;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.PlainButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * The styles and palettes the resolution's cases are written in, over {@link ResolvingTaxonomy}:
 *
 * <ul>
 *   <li><b>sketch</b> - {@link Sketch} in {@link SketchColours}: a few roots, the buttons' corner, the
 *       danger button's own; Go's surface on the danger button, the ink of every button that commits.
 *       Answers with nothing in them: the walk, not the sheet.</li>
 *   <li><b>detailed</b> - {@link Detailed}, written over the sketch, in {@link DetailedColours}: the
 *       plain button's own corner and Cancel's on any button; what it leaves is the sketch's.</li>
 *   <li><b>valued</b> - {@link Valued} in {@link ValuedColours}: every root with a body, a frame, a
 *       treatment or two, and the floating pane's corner redirected to the danger button's.</li>
 *   <li><b>squared</b> - {@link Squared}, written over the valued, in its colours: every control
 *       square, which outweighs the valued's own precise corners; the plain button's corner its
 *       base's all the same; the buttons' rest shadow its own, their hover the valued's.</li>
 * </ul>
 */
final class ResolvingThemes {

    private ResolvingThemes() {}

    /** The themes by the names the cases give them. */
    static final Map<String, Dressed> BY_NAME = Map.of(
            "sketch", new Dressed(Sketch.INSTANCE, SketchColours.INSTANCE),
            "detailed", new Dressed(Detailed.INSTANCE, DetailedColours.INSTANCE),
            "valued", new Dressed(Valued.INSTANCE, ValuedColours.INSTANCE),
            "squared", new Dressed(Squared.INSTANCE, ValuedColours.INSTANCE));

    static final Function<String, Dressed> THEMES = name -> {
        Dressed d = BY_NAME.get(name);
        if (d == null) throw new IllegalArgumentException("no theme " + name);
        return d;
    };

    public record Sketch() implements DesignStyle {
        public static final Sketch INSTANCE = new Sketch();
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    rest(Button.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    rest(DangerButton.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    at(Root.INSTANCE, Target.Effect.Opacity.INSTANCE, State.DISABLED, ""));
        }
    }

    public record SketchColours() implements ColorPalette {
        public static final SketchColours INSTANCE = new SketchColours();
        @Override public List<Implementation<? extends Target.Color>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Color.Surface.INSTANCE, ""),
                    new Implementation<>(new Casting(DangerButton.INSTANCE, Go.INSTANCE), Target.Color.Surface.INSTANCE, new Answer.Own<>("")),
                    new Implementation<>(new Casting(Button.INSTANCE, Committing.INSTANCE), Target.Color.Ink.INSTANCE, new Answer.Own<>("")),
                    rest(FloatingPane.INSTANCE, Target.Color.Surface.INSTANCE, ""),
                    at(Button.INSTANCE, Target.Color.Surface.INSTANCE, State.DISABLED, ""));
        }
    }

    public record Detailed() implements DesignStyle, Derives<Sketch> {
        public static final Detailed INSTANCE = new Detailed();
        @Override public Sketch base() { return Sketch.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    new Implementation<>(new Casting(Button.INSTANCE, Cancel.INSTANCE), Target.Shape.Corner.INSTANCE, new Answer.Own<>("")));
        }
    }

    /** A palette has no base: the sketch's colours, and more. */
    public record DetailedColours() implements ColorPalette {
        public static final DetailedColours INSTANCE = new DetailedColours();
        @Override public List<Implementation<? extends Target.Color>> implementations() {
            var all = new ArrayList<Implementation<? extends Target.Color>>(SketchColours.INSTANCE.implementations());
            all.add(rest(Root.INSTANCE, Target.Color.Ink.INSTANCE, ""));
            all.add(at(DangerButton.INSTANCE, Target.Color.Surface.INSTANCE, State.DISABLED, ""));
            return List.copyOf(all);
        }
    }

    public record Valued() implements DesignStyle {
        public static final Valued INSTANCE = new Valued();
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 0;"),
                    rest(Root.INSTANCE, Target.Shape.Rule.INSTANCE, "border-width: 0; border-style: solid;"),
                    rest(Root.INSTANCE, Target.Shape.Shadow.INSTANCE, "box-shadow: none;"),
                    rest(Root.INSTANCE, Target.Shape.Clip.INSTANCE, "clip-path: none;"),
                    rest(Root.INSTANCE, Target.Size.Inset.INSTANCE, "padding-block: 0; padding-inline: 0;"),
                    rest(Root.INSTANCE, Target.Size.Gap.INSTANCE, "gap: normal;"),
                    rest(Root.INSTANCE, Target.Size.Extent.INSTANCE, "min-height: auto; min-width: auto;"),
                    rest(Root.INSTANCE, Target.Size.Proportion.INSTANCE, "aspect-ratio: auto;"),
                    rest(Root.INSTANCE, Target.Type.Face.INSTANCE, "font-family: inherit;"),
                    rest(Root.INSTANCE, Target.Type.Weight.INSTANCE, "font-weight: inherit;"),
                    rest(Root.INSTANCE, Target.Type.Scale.INSTANCE, "font-size: inherit; line-height: inherit;"),
                    rest(Root.INSTANCE, Target.Type.Treatment.INSTANCE, "letter-spacing: normal;"),
                    rest(Root.INSTANCE, Target.Type.Decoration.INSTANCE, "text-decoration-line: none;"),
                    rest(Root.INSTANCE, Target.Type.Glyph.INSTANCE, ""),
                    rest(Root.INSTANCE, Target.Motion.Ease.INSTANCE, "transition: none;"),
                    rest(Root.INSTANCE, Target.Motion.Transform.INSTANCE, "transform: none;"),
                    rest(Root.INSTANCE, Target.Motion.Animate.INSTANCE, "animation: none;"),
                    rest(Root.INSTANCE, Target.Effect.Opacity.INSTANCE, "opacity: 1;"),
                    rest(Root.INSTANCE, Target.Effect.Filter.INSTANCE, "filter: none;"),
                    rest(Root.INSTANCE, Target.Effect.Blend.INSTANCE, "mix-blend-mode: normal;"),
                    rest(Root.INSTANCE, Target.Affordance.Cursor.INSTANCE, "cursor: auto;"),
                    rest(Root.INSTANCE, Target.Affordance.Select.INSTANCE, "user-select: auto;"),
                    rest(Root.INSTANCE, Target.Sound.Cue.INSTANCE, ""),
                    rest(Root.INSTANCE, Target.Asset.Icon.INSTANCE, ""),
                    rest(Root.INSTANCE, Target.Asset.Illustration.INSTANCE, ""),
                    rest(Button.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 3px;"),
                    rest(DangerButton.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 2px;"),
                    rest(Button.INSTANCE, Target.Size.Inset.INSTANCE, """
                            padding-block: calc(8px * pow(1.3, var(--size)));
                            padding-inline: calc(18px * pow(1.3, var(--size)));
                            """),
                    new Implementation<>(FloatingPane.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(DangerButton.INSTANCE)),
                    at(Control.INSTANCE, Target.Effect.Opacity.INSTANCE, State.DISABLED, "opacity: .45;"),
                    new Implementation<>(Button.INSTANCE, Target.Shape.Shadow.INSTANCE,
                            new Answer.Own<Target.Shape.Shadow>(Map.of(State.HOVER, "box-shadow: 0 6px 16px rgba(0, 0, 0, .12);",
                                                                     State.DISABLED, "box-shadow: none;"))));
        }
    }

    public record ValuedColours() implements ColorPalette {
        public static final ValuedColours INSTANCE = new ValuedColours();
        @Override public List<Implementation<? extends Target.Color>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Color.Surface.INSTANCE, "background-color: transparent;"),
                    rest(Root.INSTANCE, Target.Color.Ink.INSTANCE, "color: inherit;"),
                    rest(Root.INSTANCE, Target.Color.Edge.INSTANCE, "border-color: currentColor;"),
                    rest(Root.INSTANCE, Target.Color.Fill.INSTANCE, "fill: currentColor;"),
                    rest(Root.INSTANCE, Target.Color.Stroke.INSTANCE, "stroke: none;"),
                    rest(Root.INSTANCE, Target.Color.Scrollbar.INSTANCE, "scrollbar-color: auto;"),
                    new Implementation<>(new Casting(DangerButton.INSTANCE, Go.INSTANCE), Target.Color.Surface.INSTANCE,
                            new Answer.Own<>("background-color: #c62828;")));
        }
    }

    public record Squared() implements DesignStyle, Derives<Valued> {
        public static final Squared INSTANCE = new Squared();
        @Override public Valued base() { return Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Control.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 0;"),
                    new Implementation<>(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.FromBase<>()),
                    rest(Button.INSTANCE, Target.Shape.Shadow.INSTANCE, "box-shadow: inset 0 0 0 1px;"));
        }
    }

    private static <T extends Target> Implementation<T> rest(Taxon node, T target, String body) {
        return new Implementation<>(node, target, new Answer.Own<>(body));
    }

    private static <T extends Target> Implementation<T> at(Taxon node, T target, State state, String body) {
        return new Implementation<>(node, target, new Answer.Own<T>(Map.of(state, body)));
    }
}
