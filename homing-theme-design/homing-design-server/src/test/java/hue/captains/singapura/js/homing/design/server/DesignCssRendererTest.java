package hue.captains.singapura.js.homing.design.server;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.design.Box;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Feedback;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.server.ServedModules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The seam, end to end: what the served components wear, a design, a slug, a sheet. */
class DesignCssRendererTest {

    static final DesignClass<Target.Color.Surface> DANGER = of(Feedback.Danger.class, Target.Color.Surface.class);
    static final DesignClass<Target.Color.Surface> SUCCESS = of(Feedback.Success.class, Target.Color.Surface.class);
    static final DesignClass<Target.Shape.Corner> CORNER = of(Box.Control.class, Target.Shape.Corner.class);

    record Plain() implements Design {
        static final Map<DesignClass<?>, Impl> WORDS = Map.of(
                DANGER, Impl.Bindings.none().at(State.REST, "background-color", "#B00020").at(State.HOVER, "background-color", "#C51F31"),
                SUCCESS, Impl.Bindings.none().at(State.REST, "background-color", "#0A7D3A"),
                CORNER, Impl.Bindings.of("4px"));
        @Override public String slug() { return "plain"; }
        @Override public Impl impl(DesignClass<?> pair) { return WORDS.get(pair); }
    }
    record Brutal() implements Design {
        @Override public String slug() { return "brutal"; }
        @Override public Impl impl(DesignClass<?> pair) { return pair.equals(CORNER) ? Impl.Bindings.of("0") : new Plain().impl(pair); }
    }

    /** A served component: wears danger and the corner, never success. */
    record Wearer() implements CssGroup<Wearer> {
        static final Wearer INSTANCE = new Wearer();
        record button() implements CssClass<Wearer> {
            @Override public List<? extends Wearable> wears() { return List.of(DANGER, CORNER); }
            @Override public String body() { return "display: inline-flex;"; }
        }
        @Override public List<CssClass<Wearer>> cssClasses() { return List.of(new button()); }
    }

    static final ServedModules SERVED = new ServedModules(Map.of(Wearer.class.getCanonicalName(), Wearer.INSTANCE));
    static final DesignRegistry REGISTRY = new DesignRegistry(List.of(new Plain(), new Brutal()), List.of());

    @Test
    void theRegistryListsDesignsAsThemes_andOneRenderer() {
        assertEquals(List.of("plain", "brutal"), REGISTRY.themes().stream().map(t -> t.slug()).toList());
        var renderers = REGISTRY.renderers(SERVED);
        assertEquals(1, renderers.size());
        assertTrue(renderers.get(0).owns(Target.Color.Surface.INSTANCE));
        assertTrue(renderers.get(0).varies(Target.Color.Surface.INSTANCE));
    }

    @Test
    void aTargetSheet_underASlug_carriesWhatIsWorn_andNothingElse() {
        String css = REGISTRY.renderers(SERVED).get(0).render(Target.Color.Surface.INSTANCE, "plain").orElseThrow();
        assertTrue(css.contains(":root {\n    --danger-color-surface-background-color: #B00020;\n    --danger-color-surface-background-color-hover: #C51F31;\n}"), css);
        assertTrue(css.contains(".danger-color-surface {\n    background-color: var(--danger-color-surface-background-color);"), css);
        assertFalse(css.contains("success-color-surface"), "a pair nothing wears is not in the sheet, whatever the design could say: " + css);
    }

    @Test
    void aThemeOverABase_rendersItsOwnWordAndTheBaseRest() {
        String corner = REGISTRY.renderers(SERVED).get(0).render(Target.Shape.Corner.INSTANCE, "brutal").orElseThrow();
        assertTrue(corner.contains("--control-shape-corner: 0;"), corner);
        String surface = REGISTRY.renderers(SERVED).get(0).render(Target.Color.Surface.INSTANCE, "brutal").orElseThrow();
        assertTrue(surface.contains("--danger-color-surface-background-color: #B00020;"), surface);
    }

    @Test
    void aGroupThatIsNotATarget_isNotOwned() {
        assertFalse(REGISTRY.renderers(SERVED).get(0).owns(Wearer.INSTANCE));
    }
}
