package hue.captains.singapura.js.homing.components.conformance;

import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.DefaultJsRulePolicy;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.JsRulePolicy;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.preferences.UiPreferencesCrate;
import hue.captains.singapura.js.homing.site.mpa.MpaCrate;
import hue.captains.singapura.js.homing.studio.themes.StudioThemeRegistry;
import hue.captains.singapura.js.homing.ui.dialog.UiDialogCrate;
import hue.captains.singapura.js.homing.ui.elements.UiElementsCrate;
import hue.captains.singapura.js.homing.ui.docking.UiDockingCrate;
import hue.captains.singapura.js.homing.ui.splitgrid.UiSplitGridCrate;
import hue.captains.singapura.js.homing.ui.icons.UiIconsCrate;
import hue.captains.singapura.js.homing.ui.menu.UiMenuCrate;
import hue.captains.singapura.js.homing.ui.floating.UiFloatingCrate;
import hue.captains.singapura.js.homing.ui.panes.UiPanesCrate;
import hue.captains.singapura.js.homing.ui.split.UiSplitCrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The components' conformance configuration, in one place so the gate
 * ({@code ComponentsConformanceTest}), the export ({@link
 * ComponentsConformanceExport}) and the served studio ({@link
 * ComponentsConformanceStudioServer}) grade identically. A downstream of the
 * framework's {@code HomingConformance}, as the demo's is: the framework's
 * default policy, its strict grader, and a baseline that is empty on
 * purpose — these modules were cut on the substrate and carry no debt, so
 * any finding is a regression.
 *
 * <p>The top level is every fresh crate: the five components and the
 * standard MPA that composes them. The framework crates they require —
 * core-js, the base's runtime, the design targets — are gated upstream and
 * are in the closure only so that imports and CSS reach resolve.</p>
 */
public final class ComponentsConformance {

    private ComponentsConformance() {}

    /** The fresh crates: what the gate grades and the studio browses. */
    public static final List<Crate> TOP_LEVEL = List.of(
            UiElementsCrate.INSTANCE,
            UiDialogCrate.INSTANCE,
            UiPreferencesCrate.INSTANCE,
            UiSplitCrate.INSTANCE,
            UiPanesCrate.INSTANCE,
            UiFloatingCrate.INSTANCE,
            UiDockingCrate.INSTANCE,
            UiSplitGridCrate.INSTANCE,
            UiIconsCrate.INSTANCE,
            UiMenuCrate.INSTANCE,
            MpaCrate.INSTANCE);

    /** One class per Maven module, for the coverage check: nothing served from these modules may be uncrated. */
    public static final List<Class<?>> ANCHORS = List.of(
            UiElementsCrate.class, UiDialogCrate.class, UiPreferencesCrate.class,
            UiSplitCrate.class, UiPanesCrate.class, UiFloatingCrate.class, UiDockingCrate.class, UiSplitGridCrate.class, UiIconsCrate.class, UiMenuCrate.class, MpaCrate.class);

    /** The framework's policy, unextended: the components declare no types of their own. */
    public static final JsRulePolicy POLICY = DefaultJsRulePolicy.INSTANCE;

    /** Documented, intentional exceptions: none. */
    public static final List<Allowance> ALLOWANCES = List.of();

    /** Every theme the studio offers — the seven designs, each in the palettes that fit it; each must bind every pair a class wears. */
    public static List<Design> designs() {
        return StudioThemeRegistry.INSTANCE.themes().stream().map(t -> (Design) t).toList();
    }

    /** The provisions the CSS graph laws derive their priors from: the studio's palettes. */
    public static List<PaletteProvision<?, ?>> provisions() {
        return StudioThemeRegistry.INSTANCE.palettes();
    }

    /** The top level and everything it requires, transitively. */
    public static Collection<Crate> closure() {
        return CrateClosure.of(TOP_LEVEL);
    }

    /** The served identities of the top level's own modules — what the gate and the export grade. */
    public static Set<String> ownModules() {
        return TOP_LEVEL.stream().flatMap(c -> c.entries().stream()).map(e -> e.moduleClass()).collect(Collectors.toSet());
    }

    /** The committed baseline: empty, and held empty on purpose. */
    public static Baseline baseline() {
        try (InputStream in = ComponentsConformance.class.getResourceAsStream("/components-conformance-baseline.txt")) {
            if (in == null) return Baseline.EMPTY;
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load the components' conformance baseline", e);
        }
    }

    /** The framework-strict grader with the components' (empty) allowances and baseline. */
    public static FindingGrader grader(boolean allowPreExisting) {
        return FindingGrader.STRICT
                .withAllowlist(ALLOWANCES)
                .withBaseline(baseline())
                .allowingPreExisting(allowPreExisting);
    }
}
