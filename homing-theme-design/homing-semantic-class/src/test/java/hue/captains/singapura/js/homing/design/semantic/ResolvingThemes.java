package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.color.ColorScheme.Base;
import hue.captains.singapura.js.homing.design.color.Paint;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Alarm;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Clear;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Copy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Danger;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Paper;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Raised;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Risk;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Warning;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Button;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Cancel;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Committing;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Control;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.DangerButton;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.FloatingPane;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Go;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.PlainButton;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The styles the resolution's cases are written in, over {@link ResolvingTaxonomy}, worn in {@link ResolvingColours}'
 * palettes:
 *
 * <ul>
 *   <li><b>sketch</b> - {@link Sketch}: a few roots, the buttons' corner, the danger button's own; Go's ground on the
 *       danger button, the ink of every button that commits. Its bodies say nothing: the walk, not the sheet.</li>
 *   <li><b>detailed</b> - {@link Detailed}, written over the sketch: the plain button's own corner, Cancel's on any
 *       button, the root's ink; what it leaves is the sketch's.</li>
 *   <li><b>valued</b> - {@link Valued}: every root with a body and a colour, a frame, a treatment or two, the floating
 *       pane's corner redirected to the danger button's, the danger button's ink along the risk scale and the plain
 *       button's border the warning.</li>
 *   <li><b>squared</b> - {@link Squared}, written over the valued: every control square, which outweighs the valued's
 *       own precise corners; the plain button's corner its base's all the same; the buttons' rest shadow its own, their
 *       hover the valued's.</li>
 * </ul>
 *
 * Every theme is worn in {@link Paper}.
 */
final class ResolvingThemes {

    private ResolvingThemes() {}

    private static final Target.Color.Background BACKGROUND = Target.Color.Background.INSTANCE;
    private static final Target.Color.Text INK = Target.Color.Text.INSTANCE;

    /** The colour tree the themes are worn over, read once. */
    static final ColorTree COLOURS = ResolvingColours.INSTANCE.read();

    /** The themes by the names the cases give them. */
    static final Map<String, Dressed> BY_NAME = Map.of(
            "sketch", new Dressed(COLOURS, Sketch.INSTANCE, Paper.INSTANCE),
            "detailed", new Dressed(COLOURS, Detailed.INSTANCE, Paper.INSTANCE),
            "valued", new Dressed(COLOURS, Valued.INSTANCE, Paper.INSTANCE),
            "squared", new Dressed(COLOURS, Squared.INSTANCE, Paper.INSTANCE));

    static final Function<String, Dressed> THEMES = name -> {
        Dressed d = BY_NAME.get(name);
        if (d == null) throw new IllegalArgumentException("no theme " + name);
        return d;
    };


    public record Sketch() implements DesignStyle<Base> {
        public static final Sketch INSTANCE = new Sketch();
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    rest(Button.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    rest(DangerButton.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    at(Root.INSTANCE, Target.Effect.Opacity.INSTANCE, State.DISABLED, ""));
        }
        @Override public List<Coloring<Base, ?>> colorings() {
            return List.of(
                    new Coloring<>(Root.INSTANCE, BACKGROUND, new Answer.Painted<>(new Paint.Ground<>(new Page()))),
                    new Coloring<>(new Casting(DangerButton.INSTANCE, Go.INSTANCE), BACKGROUND, new Answer.Painted<>(new Paint.Ground<>(new Danger()))),
                    new Coloring<>(new Casting(Button.INSTANCE, Committing.INSTANCE), INK, new Answer.Painted<>(new Paint.With<>(new Copy()))),
                    new Coloring<>(FloatingPane.INSTANCE, BACKGROUND, new Answer.Painted<>(new Paint.Ground<>(new Raised()))),
                    new Coloring<>(Button.INSTANCE, BACKGROUND, new Answer.Painted<>(Map.of(State.DISABLED, new Paint.Ground<>(new Raised())))));
        }
    }

    public record Detailed() implements Derives<Base, Sketch> {
        public static final Detailed INSTANCE = new Detailed();
        @Override public Sketch base() { return Sketch.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, ""),
                    new Implementation<>(new Casting(Button.INSTANCE, Cancel.INSTANCE), Target.Shape.Corner.INSTANCE, new Answer.Own<>("")));
        }
        @Override public List<Coloring<Base, ?>> colorings() {
            return List.of(
                    new Coloring<>(Root.INSTANCE, INK, new Answer.Painted<>(new Paint.With<>(new Copy()))),
                    new Coloring<>(DangerButton.INSTANCE, BACKGROUND, new Answer.Painted<>(Map.of(State.DISABLED, new Paint.Ground<>(new Raised())))));
        }
    }

    public record Valued() implements DesignStyle<Base> {
        public static final Valued INSTANCE = new Valued();
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Root.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 0;"),
                    rest(Root.INSTANCE, Target.Shape.Border.INSTANCE, "border-width: 0; border-style: solid;"),
                    rest(Root.INSTANCE, Target.Shape.Outline.INSTANCE, "outline-width: 0; outline-style: none; outline-offset: 0;"),
                    rest(Root.INSTANCE, Target.Shape.Background.INSTANCE,
                            "background-image: none; background-size: auto; background-repeat: repeat; background-position: 0% 0%;"),
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
                    new Implementation<>(Root.INSTANCE, Target.Animation.Effect.INSTANCE, new Answer.Animated(AnimationEffect.None.INSTANCE)),
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
                    rest(PlainButton.INSTANCE, Target.Motion.Transform.INSTANCE, "transform: none;"),
                    rest(Button.INSTANCE, Target.Size.Inset.INSTANCE, """
                            padding-block: calc(8px * pow(1.3, var(--size)));
                            padding-inline: calc(18px * pow(1.3, var(--size)));
                            """),
                    new Implementation<>(FloatingPane.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.Redirect<>(DangerButton.INSTANCE)),
                    at(Control.INSTANCE, Target.Effect.Opacity.INSTANCE, State.DISABLED, "opacity: .45;"),
                    new Implementation<>(Button.INSTANCE, Target.Shape.Shadow.INSTANCE,
                            new Answer.Own<Target.Shape.Shadow>(Map.of(State.HOVER, "box-shadow: 0 6px 16px color-mix(in srgb, var(--box-shadow-cast) 12%, transparent);",
                                                                     State.DISABLED, "box-shadow: none;"))));
        }
        @Override public List<Coloring<Base, ?>> colorings() {
            return List.of(
                    new Coloring<>(Root.INSTANCE, BACKGROUND, new Answer.Painted<>(new Paint.Ground<>(new Clear()))),
                    new Coloring<>(Root.INSTANCE, INK, new Answer.Painted<>(new Paint.Inherited<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.Border.INSTANCE, new Answer.Painted<>(new Paint.Typography<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.Outline.INSTANCE, new Answer.Painted<>(new Paint.Typography<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.ColumnRule.INSTANCE, new Answer.Painted<>(new Paint.Typography<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.Fill.INSTANCE, new Answer.Painted<>(new Paint.Typography<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.Stroke.INSTANCE, new Answer.Painted<>(new Paint.Nothing<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.Scrollbar.INSTANCE, new Answer.Painted<>(new Paint.Inherited<>())),
                    new Coloring<>(Root.INSTANCE, Target.Color.BoxShadow.INSTANCE, new Answer.Painted<>(new Paint.Shadow<>(new Copy(), new Page(), new Alarm()))),
                    new Coloring<>(new Casting(DangerButton.INSTANCE, Go.INSTANCE), BACKGROUND, new Answer.Painted<>(new Paint.Ground<>(new Alarm()))),
                    new Coloring<>(DangerButton.INSTANCE, INK, new Answer.Painted<>(new Paint.Along<>(new Risk()))),
                    new Coloring<>(PlainButton.INSTANCE, Target.Color.Border.INSTANCE, new Answer.Painted<>(new Paint.With<>(new Warning()))));
        }
    }

    public record Squared() implements Derives<Base, Valued> {
        public static final Squared INSTANCE = new Squared();
        @Override public Valued base() { return Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(
                    rest(Control.INSTANCE, Target.Shape.Corner.INSTANCE, "border-radius: 0;"),
                    new Implementation<>(PlainButton.INSTANCE, Target.Shape.Corner.INSTANCE, new Answer.FromBase<>()),
                    rest(Button.INSTANCE, Target.Shape.Shadow.INSTANCE, "box-shadow: inset 0 0 0 1px;"),
                    // the plain button still at rest as its base has it, and moved under the hand as this style moves every button
                    new Implementation<>(Button.INSTANCE, Target.Motion.Transform.INSTANCE, new Answer.Own<Target.Motion.Transform>(
                            Map.of(State.REST, "transform: translateY(0);", State.HOVER, "transform: translateY(-1px);"))),
                    new Implementation<>(PlainButton.INSTANCE, Target.Motion.Transform.INSTANCE, new Answer.FromBase<>(Set.of(State.REST))));
        }
    }

    private static <T extends Target.Colorless> Implementation<T> rest(Taxon node, T target, String body) {
        return new Implementation<>(node, target, new Answer.Own<>(body));
    }

    private static <T extends Target.Colorless> Implementation<T> at(Taxon node, T target, State state, String body) {
        return new Implementation<>(node, target, new Answer.Own<T>(Map.of(state, body)));
    }
}
