package hue.captains.singapura.js.homing.design;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The sheet is the target's template filled with the design's variables; the root sheet is the design's bindings. */
class SheetsTest {

    static final Deployment.Resolution R = Deployment.of(DeploymentTest.DANGER_BUTTON, new DeploymentTest.Plain()).resolve();

    @Test
    void oneSheetPerTargetReached_inTreeOrder() {
        Map<String, String> sheets = Sheets.targetSheets(R);
        assertEquals(List.of("color-surface", "color-ink", "shape-corner", "size-inset", "type-face", "motion-ease", "motion-transform"),
                List.copyOf(sheets.keySet()));
    }

    @Test
    void theTemplate_readsVariables_andNestsStates_withRestFallback() {
        String surface = Sheets.targetSheets(R).get("color-surface");
        assertTrue(surface.contains(".danger-color-surface {"), surface);
        assertTrue(surface.contains("    background-color: var(--danger-color-surface-background-color);"), surface);
        assertTrue(surface.contains("    &:hover {\n        background-color: var(--danger-color-surface-background-color-hover, var(--danger-color-surface-background-color));"), surface);
        assertFalse(surface.contains("#B00020"), "no value in a target sheet");
    }

    @Test
    void aSingleProperty_target_dropsThePropertyFromTheVariable() {
        String face = Sheets.targetSheets(R).get("type-face");
        assertTrue(face.contains("font-family: var(--label-type-face);"), face);
        String transform = Sheets.targetSheets(R).get("motion-transform");
        assertTrue(transform.contains("&:active {\n        transform: var(--interactive-motion-transform-active);"), transform);
        assertFalse(transform.contains(", var(--interactive-motion-transform)"), "no rest fallback when rest is unbound: " + transform);
    }

    @Test
    void theRootSheet_carriesTheValues_perMode() {
        String root = Sheets.rootSheet(R);
        assertTrue(root.startsWith(":root {\n"), root);
        assertTrue(root.contains("    --danger-color-surface-background-color: #B00020;"), root);
        assertTrue(root.contains("    --danger-color-surface-background-color-hover: #C51F31;"), root);
        assertTrue(root.contains("    --label-type-face: system-ui, sans-serif;"), root);
        assertTrue(root.contains("    --control-shape-corner: 4px;"), root);
        assertTrue(root.contains("    --on-danger-color-ink: #FFFFFF;"), root);
        assertTrue(root.contains("@media (prefers-color-scheme: dark) {\n    :root {\n        --danger-color-surface-background-color: #FF5A6E;"), root);
    }

    @Test
    void silence_andABody_render_asTheyShould() {
        record Odd() implements Design {
            @Override public DesignId id() { return new DesignId("odd"); }
            @Override public Impl impl(DesignClass<?> pair) {
                if (pair.equals(DeploymentTest.PRESS)) return Impl.Silence.css();
                // a body says its colour by reference — to the success surface, bound below
                if (pair.equals(DeploymentTest.DANGER_SURFACE)) return new Impl.Body("background-color: " + DeploymentTest.SUCCESS_SURFACE.var("background-color") + ";\nbackground-image: url(hatch.svg);\n&:hover { background-color: " + DeploymentTest.SUCCESS_SURFACE.var("background-color", State.HOVER) + "; }\n");
                if (pair.equals(DeploymentTest.SUCCESS_SURFACE)) return Impl.Bindings.none().at(State.REST, "background-color", "#0A7D3A").at(State.HOVER, "background-color", "#0C9A47");
                return null;
            }
        }
        Set<DesignClass<?>> required = Set.of(DeploymentTest.PRESS, DeploymentTest.DANGER_SURFACE, DeploymentTest.SUCCESS_SURFACE);
        var r = Deployment.of(required, new Odd()).resolve();
        assertEquals(List.of(), r.findings(), r.findings().toString());
        var sheets = Sheets.targetSheets(r);
        assertFalse(sheets.containsKey("motion-transform"), "silence emits nothing");
        assertTrue(sheets.get("color-surface").contains(".danger-color-surface {\n    background-color: var(--success-color-surface-background-color);\n    background-image: url(hatch.svg);\n    &:hover { background-color: var(--success-color-surface-background-color-hover); }\n}"), sheets.get("color-surface"));
        assertFalse(Sheets.rootSheet(r).contains("--danger-color-surface"), "a body binds no variable of its own");
    }

    // ── the order within a sheet is fixed: what a thing is, then what it says, then what it does ──
    @Test
    void rulesInASheet_followThePrecedence_whateverTheRequirementOrder() {
        var raised   = DesignClass.of(Layer.Raised.class,           Target.Color.Surface.class);
        var selected = DesignClass.of(Interaction.Selected.class,   Target.Color.Surface.class);
        var primary  = DesignClass.of(Emphasis.Primary.class,       Target.Color.Surface.class);
        var danger   = DesignClass.of(Feedback.Danger.class,        Target.Color.Surface.class);
        var code     = DesignClass.of(Text.Code.class,              Target.Color.Surface.class);
        record Flat() implements Design {
            @Override public DesignId id() { return new DesignId("flat"); }
            @Override public Impl impl(DesignClass<?> pair) { return pair.onColourPlane() ? Impl.Bindings.none().at(State.REST, "background-color", "#123456") : null; }
        }
        java.util.function.Function<List<DesignClass<?>>, List<String>> order = required -> {
            var sheet = Sheets.targetSheets(Deployment.of(new java.util.LinkedHashSet<>(required), new Flat()).resolve()).get("color-surface");
            var out = new java.util.ArrayList<String>();
            for (String line : sheet.split("\n")) if (line.startsWith(".")) out.add(line.substring(1, line.indexOf(' ')));
            return out;
        };
        var expected = List.of("raised-color-surface", "code-color-surface", "primary-color-surface", "danger-color-surface", "selected-color-surface");
        assertEquals(expected, order.apply(List.of(selected, danger, primary, code, raised)), "worn in reverse");
        assertEquals(expected, order.apply(List.of(code, raised, selected, primary, danger)), "worn shuffled");
        // so a selected look applied beside a raised base wins by a rule, not by a coin
        assertTrue(expected.indexOf("selected-color-surface") > expected.indexOf("raised-color-surface"));
    }

    @Test
    void aBody_mayNestOnlyAStateOrAPseudoElement_onSelf() {
        var surface = DeploymentTest.DANGER_SURFACE;
        record Inventive() implements Design {
            @Override public DesignId id() { return new DesignId("inventive"); }
            @Override public Impl impl(DesignClass<?> pair) {
                return new Impl.Body("background-color: transparent;\n&[aria-selected=\"true\"] { background-color: transparent; }\n&::after { background-color: transparent; }\n&[data-lifted] { background-color: transparent; }\n&:hover, &.on { background-color: transparent; }\ntd:hover { background-color: transparent; }\n");
            }
        }
        var r = Deployment.of(Set.of(surface), new Inventive()).resolve();
        var details = r.findings().stream().map(Deployment.Finding::detail).toList();
        assertEquals(2, r.findings().size(), details.toString());
        assertTrue(details.stream().anyMatch(d -> d.contains("&[data-lifted]")), "an invented state on self is refused: " + details);
        assertTrue(details.stream().anyMatch(d -> d.contains("class or id")), "a class on self is refused: " + details);
        // aria-selected, ::after, :hover on self and td:hover below all pass
    }

    /**
     * A word anchored at zero and at −1 scales: the rule interpolates by the
     * element's extent — the pole by its sign, then from neutral by its
     * magnitude, in oklch — the root sheet carries all three anchors, and the
     * sheet registers the extent as a non-inherited number that is 1 by default.
     * A word without anchors renders as it always did, and a pair worn with an
     * extent whose word lacks an anchor is a finding; anchors off the colour
     * plane are refused.
     */
    @Test
    void anAnchoredWord_scalesByTheExtent_andAnUnanchoredOne_isAFinding() {
        var success = DesignClass.of(Feedback.Success.class, Target.Color.Ink.class);
        var corner  = DesignClass.of(Box.Control.class, Target.Shape.Corner.class);
        record Graded() implements Design {
            @Override public Impl impl(DesignClass<?> pair) {
                if (pair.semantic() == Feedback.Success.class && pair.target() == Target.Color.Ink.class)
                    return Impl.Bindings.of("#1B5E20").at(State.HOVER, "#2E7D32")
                            .at(Extent.ZERO, State.REST, "#64748B").at(Extent.NEG, State.REST, "#7F1D1D")
                            .in(Mode.DARK, State.REST, "#86EFAC").in(Mode.DARK, Extent.NEG, State.REST, "#FCA5A5").in(Mode.DARK, Extent.ZERO, State.REST, "#94A3B8");
                if (pair.semantic() == Box.Control.class && pair.target() == Target.Shape.Corner.class)
                    return Impl.Bindings.of("4px").at(Extent.ZERO, State.REST, "0").at(Extent.NEG, State.REST, "0");
                return null;
            }
            @Override public DesignId id() { return new DesignId("graded"); }
            @Override public String label() { return "Graded"; }
            @Override public String group() { return "t"; }
            @Override public String inspiration() { return ""; }
        }
        var r = Deployment.of(Set.of(success), Set.of(success), new Graded()).resolve();
        assertEquals(List.of(), r.findings(), r.findings().toString());
        String ink = Sheets.targetSheets(r).get("color-ink");
        assertTrue(ink.startsWith("/* color-ink — generated; the template is the target's, the values the design's */\n@property --extent { syntax: \"<number>\"; inherits: false; initial-value: 1; }\n"), ink);
        assertTrue(ink.contains("    color: color-mix(in oklab, var(--success-color-ink-zero) calc((1 - abs(var(--extent))) * 100%), "
                + "color-mix(in oklab, var(--success-color-ink-neg) calc((1 - sign(var(--extent))) / 2 * 100%), var(--success-color-ink)));"), ink);
        // a state falls back to the rest anchors, extent by extent
        assertTrue(ink.contains("        color: color-mix(in oklab, var(--success-color-ink-hover-zero, var(--success-color-ink-zero)) calc((1 - abs(var(--extent))) * 100%), "
                + "color-mix(in oklab, var(--success-color-ink-hover-neg, var(--success-color-ink-neg)) calc((1 - sign(var(--extent))) / 2 * 100%), "
                + "var(--success-color-ink-hover, var(--success-color-ink))));"), ink);
        String root = Sheets.rootSheet(r);
        assertTrue(root.contains("    --success-color-ink: #1B5E20;\n") && root.contains("    --success-color-ink-neg: #7F1D1D;\n") && root.contains("    --success-color-ink-zero: #64748B;\n"), root);
        assertTrue(root.contains("        --success-color-ink-neg: #FCA5A5;"), root);

        // unanchored, worn plainly: as it always was
        var plain = Deployment.of(DeploymentTest.DANGER_BUTTON, new DeploymentTest.Plain()).resolve();
        assertFalse(Sheets.targetSheets(plain).get("color-ink").contains("@property"), "no scaled word, no registration");
        assertTrue(Sheets.targetSheets(plain).get("color-ink").contains("color: var(--on-danger-color-ink);"));

        // worn with an extent, but the word has no anchors: a finding per missing anchor
        var onDanger = DesignClass.of(Pairing.OnDanger.class, Target.Color.Ink.class);
        var missing = Deployment.of(DeploymentTest.DANGER_BUTTON, Set.of(onDanger), new DeploymentTest.Plain()).resolve();
        assertEquals(2, missing.findings().stream().filter(f -> f.kind() == Deployment.Finding.Kind.MISSING_ANCHOR).count(), missing.findings().toString());

        // anchors off the colour plane: refused
        var off = Deployment.of(Set.of(corner), new Graded()).resolve();
        assertTrue(off.findings().stream().anyMatch(f -> f.kind() == Deployment.Finding.Kind.INVALID_BINDING && f.detail().contains("extent")), off.findings().toString());
    }
}
