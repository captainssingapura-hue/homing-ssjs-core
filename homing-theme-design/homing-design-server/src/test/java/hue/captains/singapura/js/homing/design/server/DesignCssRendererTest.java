package hue.captains.singapura.js.homing.design.server;

import hue.captains.singapura.js.homing.design.Box;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignCrate;
import hue.captains.singapura.js.homing.design.Feedback;
import hue.captains.singapura.js.homing.design.Impl;
import hue.captains.singapura.js.homing.design.ImplProvider;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The seam, end to end: a target group, a slug, a sheet. */
class DesignCssRendererTest {

    record Plain() implements Design {
        @Override public String slug() { return "plain"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(
                    ImplProvider.of(Feedback.Danger.danger_color_surface.class, Impl.Bindings.none().at(State.REST, "background-color", "#B00020").at(State.HOVER, "background-color", "#C51F31")),
                    ImplProvider.of(Box.Control.control_shape_corner.class, Impl.Bindings.of("4px")));
        }
    }
    record Brutal() implements Design {
        @Override public String slug() { return "brutal"; }
        @Override public Optional<Design> base() { return Optional.of(new Plain()); }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(ImplProvider.of(Box.Control.control_shape_corner.class, Impl.Bindings.of("0")));
        }
    }

    static final DesignRegistry REGISTRY = new DesignRegistry(List.of(new Plain(), new Brutal()), List.of());

    @Test
    void theRegistryListsDesignsAsThemes_andOneRenderer() {
        var ignored = DesignCrate.INSTANCE;   // registers the vocabulary
        assertEquals(List.of("plain", "brutal"), REGISTRY.themes().stream().map(t -> t.slug()).toList());
        assertEquals(1, REGISTRY.renderers().size());
        assertTrue(REGISTRY.renderers().get(0).owns(Target.Color.Surface.INSTANCE));
        assertTrue(REGISTRY.renderers().get(0).varies(Target.Color.Surface.INSTANCE));
    }

    @Test
    void aTargetSheet_underASlug_isTheTemplateAndTheBindings() {
        var ignored = DesignCrate.INSTANCE;
        String css = REGISTRY.renderers().get(0).render(Target.Color.Surface.INSTANCE, "plain").orElseThrow();
        assertTrue(css.contains(":root {\n    --danger-color-surface-background-color: #B00020;\n    --danger-color-surface-background-color-hover: #C51F31;\n}"), css);
        assertTrue(css.contains(".danger-color-surface {\n    background-color: var(--danger-color-surface-background-color);"), css);
        assertFalse(css.contains("success-color-surface"), "a projection the design has no word for is absent, not an error: " + css);
    }

    @Test
    void aThemeOverABase_rendersItsOwnWordAndTheBaseRest() {
        var ignored = DesignCrate.INSTANCE;
        String corner = REGISTRY.renderers().get(0).render(Target.Shape.Corner.INSTANCE, "brutal").orElseThrow();
        assertTrue(corner.contains("--control-shape-corner: 0;"), corner);
        String surface = REGISTRY.renderers().get(0).render(Target.Color.Surface.INSTANCE, "brutal").orElseThrow();
        assertTrue(surface.contains("--danger-color-surface-background-color: #B00020;"), surface);
    }

    record Plain_Group() implements hue.captains.singapura.js.homing.core.CssGroup<Plain_Group> {
        @Override public List<hue.captains.singapura.js.homing.core.CssClass<Plain_Group>> cssClasses() { return List.of(); }
    }

    @Test
    void aGroupThatIsNotATarget_isNotOwned() {
        assertFalse(REGISTRY.renderers().get(0).owns(new Plain_Group()));
    }
}
