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
            @Override public String slug() { return "odd"; }
            @Override public Impl impl(DesignClass<?> pair) {
                if (pair.equals(DeploymentTest.PRESS)) return Impl.Silence.css();
                if (pair.equals(DeploymentTest.DANGER_SURFACE)) return new Impl.Body("background-color: #B00020;\nbackground-image: url(hatch.svg);\n&:hover { background-color: #C51F31; }\n");
                return null;
            }
        }
        var r = Deployment.of(Set.of(DeploymentTest.PRESS, DeploymentTest.DANGER_SURFACE), new Odd()).resolve();
        assertEquals(List.of(), r.findings());
        var sheets = Sheets.targetSheets(r);
        assertFalse(sheets.containsKey("motion-transform"), "silence emits nothing");
        assertTrue(sheets.get("color-surface").contains(".danger-color-surface {\n    background-color: #B00020;\n    background-image: url(hatch.svg);\n    &:hover { background-color: #C51F31; }\n}"), sheets.get("color-surface"));
        assertEquals("", Sheets.rootSheet(r), "a body binds no variable");
    }
}
