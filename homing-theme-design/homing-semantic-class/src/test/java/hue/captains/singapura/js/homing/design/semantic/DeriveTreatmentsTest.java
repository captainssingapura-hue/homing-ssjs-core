package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.Control;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.DangerButton;
import hue.captains.singapura.js.homing.design.semantic.ResolvingTaxonomy.PlainButton;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The treatments read from the answers are exactly those a search of every component, state and
 * target finds - in every fixture theme, through a redirect that brings a state the component's
 * own lineage never names, and past a silence that ends its base's treatment - and over several
 * themes, the union of theirs.
 */
class DeriveTreatmentsTest {

    private static final Taxonomy TAXONOMY = ResolvingTaxonomy.INSTANCE.read();
    private static final ColorPalette COLOURS = ResolvingThemes.ValuedColours.INSTANCE;

    /** The danger button's shadow is the plain button's, which answers focus itself: a state only the redirect brings. */
    public record Redirecting() implements DesignStyle, Derives<ResolvingThemes.Valued> {
        @Override public ResolvingThemes.Valued base() { return ResolvingThemes.Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(DangerButton.INSTANCE, Target.Shape.Shadow.INSTANCE, new Answer.Redirect<>(PlainButton.INSTANCE)),
                           new Implementation<>(PlainButton.INSTANCE, Target.Shape.Shadow.INSTANCE,
                                   new Answer.Own<Target.Shape.Shadow>(Map.of(State.FOCUS, "box-shadow: none;"))));
        }
    }

    /** A control that cannot be used says nothing of its opacity: its base's treatment ends here. */
    public record Silencing() implements DesignStyle, Derives<ResolvingThemes.Valued> {
        @Override public ResolvingThemes.Valued base() { return ResolvingThemes.Valued.INSTANCE; }
        @Override public List<Implementation<?>> implementations() {
            return List.of(new Implementation<>(Control.INSTANCE, Target.Effect.Opacity.INSTANCE,
                    new Answer.Own<Target.Effect.Opacity>(Map.of(State.DISABLED, ""))));
        }
    }

    @Test
    void theTreatmentsReadFromTheAnswers_areThoseASearchFinds_inEveryTheme() {
        var themes = new ArrayList<>(ResolvingThemes.BY_NAME.values());
        themes.add(new Dressed(new Redirecting(), COLOURS));
        themes.add(new Dressed(new Silencing(), COLOURS));
        for (Dressed theme : themes)
            assertEquals(searched(List.of(theme)), DeriveTreatments.INSTANCE.of(TAXONOMY, theme), theme.toString());
        assertEquals(searched(themes), DeriveTreatments.INSTANCE.union(TAXONOMY, themes), "over several themes, the union of theirs");
    }

    @Test
    void aRedirect_bringsItsTargetsStates_andASilence_endsItsBases() {
        var redirecting = DeriveTreatments.INSTANCE.of(TAXONOMY, new Dressed(new Redirecting(), COLOURS));
        var shadow = Target.Shape.Shadow.INSTANCE;
        assertTrue(redirecting.contains(new Treatment(DangerButton.INSTANCE, State.FOCUS, shadow)), "the plain button's focus, the danger button's through the redirect");
        assertTrue(redirecting.contains(new Treatment(DangerButton.INSTANCE, State.HOVER, shadow)), "and the base's hover, through the plain button's walk");
        var opacity = Target.Effect.Opacity.INSTANCE;
        assertTrue(DeriveTreatments.INSTANCE.of(TAXONOMY, ResolvingThemes.BY_NAME.get("valued")).contains(new Treatment(PlainButton.INSTANCE, State.DISABLED, opacity)));
        assertFalse(DeriveTreatments.INSTANCE.of(TAXONOMY, new Dressed(new Silencing(), COLOURS)).contains(new Treatment(PlainButton.INSTANCE, State.DISABLED, opacity)),
                "the worn style's silence ends the walk: no rule");
    }

    /** Every component, in every state but rest, on every CSS target that offers it: what some theme answers, saying something. */
    private static List<Treatment> searched(List<Dressed> themes) {
        var tokens = SemanticTokens.INSTANCE;
        var out = new LinkedHashSet<Treatment>();
        for (State s : State.values()) {
            if (s == State.REST) continue;
            for (Component<?> c : TAXONOMY.components())
                for (Target t : tokens.cssLeaves()) {
                    if (!t.states().contains(s)) continue;
                    for (Dressed d : themes) {
                        Resolution r = ResolveSemanticClass.INSTANCE.resolve(TAXONOMY, d, new Casting(c), s, t);
                        if (r.outcome() instanceof Resolution.Answered a && !a.body().isBlank()) { out.add(new Treatment(c, s, t)); break; }
                    }
                }
        }
        return List.copyOf(out);
    }
}
