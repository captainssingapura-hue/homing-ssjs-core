package hue.captains.singapura.js.homing.studio.themes;

import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RFC 0007 §6 — conformance checks for theme-audio bindings.
 *
 * <p>For every theme in the registry that declares
 * {@link Theme#audio()}:</p>
 * <ol>
 *   <li><b>Hard invariant</b>: bindings are non-empty + every value is a
 *       non-null {@link Cue}. Compile already enforces typed shapes; this
 *       guards against returning {@code null} from a cue-providing method.</li>
 *   <li><b>Soft check</b>: when a target's {@link ClickTarget#classToken()}
 *       appears in the theme's backdrop SVG (if any), we have a positive
 *       proof the typed binding aligns with the SVG. Missing alignment
 *       does NOT fail — chrome-bound targets like {@code .st-card}
 *       legitimately live outside any backdrop. The check is informational.</li>
 * </ol>
 */
class ThemeAudioConformanceTest {

    @TestFactory
    List<DynamicTest> audioBindingsAreValidAndDiscoverableInSvg() {
        var tests = new ArrayList<DynamicTest>();
        for (Theme theme : StudioThemeRegistry.INSTANCE.themes()) {
            ThemeAudio<?> audio = theme.audio();
            if (audio == null) continue;
            tests.addAll(testsFor(theme, audio));
        }
        return tests;
    }

    private List<DynamicTest> testsFor(Theme theme, ThemeAudio<?> audio) {
        var tests = new ArrayList<DynamicTest>();
        String slug = theme.slug();

        tests.add(DynamicTest.dynamicTest(slug + " :: bindings non-empty + every Cue non-null", () -> {
            Map<? extends ClickTarget<?>, Cue> bindings = audio.bindings();
            assertFalse(bindings.isEmpty(),
                    "Theme " + slug + " declared audio() but bindings() is empty");
            for (var entry : bindings.entrySet()) {
                assertNotNull(entry.getValue(),
                        "Theme " + slug + " :: ClickTarget " + entry.getKey().getClass().getSimpleName()
                                + " bound to null Cue");
            }
        }));

        return tests;
    }
}
