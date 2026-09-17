package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.design.Deployment.Finding;
import hue.captains.singapura.js.homing.design.Feedback.Danger;
import hue.captains.singapura.js.homing.design.Feedback.Success;
import hue.captains.singapura.js.homing.design.Interaction.Interactive;
import hue.captains.singapura.js.homing.design.Pairing.OnDanger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A deployment resolves its requirement set against a design, flattens delegation, and refuses what must be refused. */
class DeploymentTest {

    // ── a danger button's requirement set ────────────────────────────────
    static final Set<Class<? extends DesignClass<?, ?>>> DANGER_BUTTON = Set.of(
            Danger.danger_color_surface.class, OnDanger.on_danger_color_ink.class, Interactive.interactive_motion_transform.class, Interactive.interactive_motion_ease.class,
            Box.Control.control_shape_corner.class, Box.Control.control_size_inset.class, Text.Label.label_type_face.class);

    // ── a base design that answers all of it ─────────────────────────────
    record Plain() implements Design {
        @Override public String slug() { return "plain"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(
                    ImplProvider.of(Danger.danger_color_surface.class, Impl.Bindings.none()
                            .at(State.REST, "background-color", "#B00020")
                            .at(State.HOVER, "background-color", "#C51F31")
                            .in(Mode.DARK, State.REST, "background-color", "#FF5A6E")),
                    ImplProvider.of(Success.success_color_surface.class, Impl.Bindings.none().at(State.REST, "background-color", "#0A7D3A")),
                    ImplProvider.of(OnDanger.on_danger_color_ink.class, Impl.Bindings.of("#FFFFFF")),
                    ImplProvider.of(Interactive.interactive_motion_transform.class, Impl.Bindings.none().at(State.ACTIVE, "scale(.97)")),
                    ImplProvider.of(Interactive.interactive_motion_ease.class, Impl.Bindings.of("transform 80ms, background-color 80ms")),
                    ImplProvider.of(Box.Control.control_shape_corner.class, Impl.Bindings.of("4px")),
                    ImplProvider.of(Box.Control.control_size_inset.class, Impl.Bindings.of("6px 12px")),
                    ImplProvider.of(Text.Label.label_type_face.class, Impl.Bindings.of("system-ui, sans-serif")));
        }
    }

    // ── a theme over it: three opinions, delegates the rest ──────────────
    record Brutal() implements Design {
        @Override public String slug() { return "brutal"; }
        @Override public Optional<Design> base() { return Optional.of(new Plain()); }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(
                    ImplProvider.of(Danger.danger_color_surface.class, Impl.Bindings.none().at(State.REST, "background-color", "#FFE800")),
                    ImplProvider.of(Box.Control.control_shape_corner.class, Impl.Bindings.of("0")),
                    ImplProvider.of(Interactive.interactive_motion_transform.class, Impl.Silence.css()));
        }
    }

    @Test
    void aCompleteDesign_resolvesEveryRequiredClass() {
        var r = Deployment.of(DANGER_BUTTON, new Plain()).resolve();
        assertEquals(List.of(), r.findings());
        assertEquals(DANGER_BUTTON, r.impls().keySet());
    }

    @Test
    void aThemeOverABase_isCompleteThroughTheBase_andItsOwnWordWins() {
        var r = Deployment.of(DANGER_BUTTON, new Brutal()).resolve();
        assertEquals(List.of(), r.findings());
        var danger = assertInstanceOf(Impl.Bindings.class, r.impls().get(Danger.danger_color_surface.class));
        assertEquals("#FFE800", danger.values().get(Mode.LIGHT).get(State.REST).get("background-color"));
        assertInstanceOf(Impl.Silence.class, r.impls().get(Interactive.interactive_motion_transform.class), "silence is a fulfilment");
        assertInstanceOf(Impl.Bindings.class, r.impls().get(OnDanger.on_danger_color_ink.class), "the base answers what the theme did not");
    }

    @Test
    void aMissingClass_isNamed() {
        var required = Set.<Class<? extends DesignClass<?, ?>>>of(Danger.danger_color_surface.class, Danger.danger_sound_cue.class);
        var r = Deployment.of(required, new Plain()).resolve();
        assertEquals(1, r.findings().size(), r.findings().toString());
        assertEquals(Finding.Kind.MISSING, r.findings().get(0).kind());
        assertEquals(Danger.danger_sound_cue.class, r.findings().get(0).designClass());
    }

    // ── extension: a product's own leaf, derived from the base's semantics ──
    record Up() implements Feedback {
        public record up_color_surface() implements DesignClass<Up, Target.Color.Surface> {}
    }
    record FinDash() implements DesignExtension {
        @Override public String slug() { return "fin-dash"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(ImplProvider.as(Up.up_color_surface.class, Success.success_color_surface.class));
        }
    }

    @Test
    void anExtension_fulfilsItsOwnClass_byDelegation_andTheResultIsFlat() {
        var required = Set.<Class<? extends DesignClass<?, ?>>>of(Up.up_color_surface.class, Success.success_color_surface.class);
        var r = new Deployment(required, new Brutal(), List.of(new FinDash())).resolve();
        assertEquals(List.of(), r.findings());
        var up = assertInstanceOf(Impl.Bindings.class, r.impls().get(Up.up_color_surface.class));
        assertEquals("#0A7D3A", up.values().get(Mode.LIGHT).get(State.REST).get("background-color"), "Up is Success, resolved through the base, flat");
    }

    // ── refusals ─────────────────────────────────────────────────────────
    record Twice() implements DesignExtension {
        @Override public String slug() { return "twice"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(ImplProvider.of(Danger.danger_color_surface.class, Impl.Bindings.of("#000")));
        }
    }

    @Test
    void twoProvidersForOneClass_isRefused_notLastWins() {
        var r = new Deployment(Set.of(Danger.danger_color_surface.class), new Plain(), List.of(new Twice())).resolve();
        assertEquals(Finding.Kind.DOUBLE, r.findings().get(0).kind(), r.findings().toString());
        assertTrue(r.findings().get(0).detail().contains("plain") && r.findings().get(0).detail().contains("twice"));
    }

    record WrongCarrier() implements Design {
        @Override public String slug() { return "wrong"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(
                    ImplProvider.of(Danger.danger_sound_cue.class, Impl.Bindings.of("beep")),                                  // CSS impl for an audio target
                    ImplProvider.of(Danger.danger_color_ink.class, Impl.Bindings.none().at(State.REST, "background-color", "#000")), // ink does not own background
                    ImplProvider.of(Box.Control.control_shape_corner.class, Impl.Bindings.none().at(State.HOVER, "0")), // corner offers no hover slot
                    ImplProvider.of(Danger.danger_color_surface.class, Impl.Bindings.of("#000")),                              // SOLE on a two-property target
                    ImplProvider.of(Success.success_color_surface.class, new Impl.Body("background-color: #0A7D3A;\npadding-top: 4px;\n& .st-card { color: red; }\n")));
        }
    }

    @Test
    void impls_areValidatedAgainstTheirTarget() {
        var required = Set.<Class<? extends DesignClass<?, ?>>>of(Danger.danger_sound_cue.class, Danger.danger_color_ink.class, Box.Control.control_shape_corner.class, Danger.danger_color_surface.class, Success.success_color_surface.class);
        var r = Deployment.of(required, new WrongCarrier()).resolve();
        var kinds = r.findings().stream().map(Finding::kind).toList();
        assertTrue(kinds.contains(Finding.Kind.CARRIER_MISMATCH), kinds.toString());
        assertEquals(3, kinds.stream().filter(k -> k == Finding.Kind.INVALID_BINDING).count(), r.findings().toString());
        assertEquals(2, kinds.stream().filter(k -> k == Finding.Kind.INVALID_BODY).count(), "padding-top and the nested class: " + r.findings());
    }

    record Loop() implements Design {
        @Override public String slug() { return "loop"; }
        @Override public List<ImplProvider<?>> providers() {
            return List.of(ImplProvider.as(Danger.danger_color_surface.class, Success.success_color_surface.class),
                           ImplProvider.as(Success.success_color_surface.class, Danger.danger_color_surface.class));
        }
    }

    @Test
    void aDelegationCycle_isRefused() {
        var r = Deployment.of(Set.of(Danger.danger_color_surface.class), new Loop()).resolve();
        assertTrue(r.findings().stream().anyMatch(f -> f.kind() == Finding.Kind.CYCLE), r.findings().toString());
    }

    @Test
    void projectionsOf_buildsARequirementSetByMeaning() {
        var set = Deployment.projectionsOf(Danger.class);
        assertEquals(8, set.size());
        assertTrue(set.contains(Danger.danger_sound_cue.class));
        assertEquals(Map.of(), Map.of()); // keeps the import honest
    }
}
