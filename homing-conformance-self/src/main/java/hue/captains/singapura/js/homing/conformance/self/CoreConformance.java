package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.theme.color.ThemeColorCrate;
import hue.captains.singapura.js.homing.theme.type.ThemeTypeCrate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Core's crates on the new stack, as its gate grades them and its export reports them: its JS,
 * its server, its palettes - held to the default policy, with the allowances that are core's to
 * make and the baseline its debt is kept in. One configuration, so the gate ({@code
 * CoreSelfConformanceTest}), the export ({@link CoreConformanceExport}) and the studio downstream
 * that browses these crates grade identically.
 */
public final class CoreConformance {

    private CoreConformance() {}

    /** The crates graded, reported and browsed. */
    public static final List<Crate> TOP_LEVEL = List.of(
            CoreJsCrate.INSTANCE,
            ServerCrate.INSTANCE,
            // RFC 0066 - the palettes, so the CSS graph rules see the priors as crated.
            ThemeColorCrate.INSTANCE,
            ThemeTypeCrate.INSTANCE);

    /** What core lets pass on purpose: the managers are where the typed paths end. */
    public static final List<Allowance> ALLOWANCES = List.of(
            new Allowance(
                    "hue.captains.singapura.js.homing.server.HrefManager",
                    new RuleId("no-raw-href"),
                    "HrefManager IS the href-manager implementation - it defines the href.* API "
                            + "the rule redirects consumers to; window.location/setAttribute('href') "
                            + "here are the sanctioned primitives, not a bypass."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.CssClassManager",
                    new RuleId("no-raw-href"),
                    "CssClassManager builds its own stylesheet <link> href - framework "
                            + "infrastructure that emits the served CSS, not a consumer view."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.CssClassManager",
                    new RuleId("no-raw-css"),
                    "CssClassManager IS the css-manager implementation - its classList calls are "
                            + "the css.* API the rule redirects DOM owners to. The one module that "
                            + "may touch classList raw, because it is where the typed path ends."));

    public static Collection<Crate> closure() { return CrateClosure.of(TOP_LEVEL); }

    /** The provisions the CSS graph laws derive their priors from: the framework designs' palettes. */
    public static List<PaletteProvision<?, ?>> provisions() {
        return HomingDesigns.REGISTRY.palettes();
    }

    /** The debt core keeps, for these crates: {@code core-conformance-baseline.txt}. */
    public static Baseline baseline() {
        try (InputStream in = CoreConformance.class.getResourceAsStream("/core-conformance-baseline.txt")) {
            if (in == null) return Baseline.EMPTY;
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("core's conformance baseline could not be read", e);
        }
    }

    public static FindingGrader grader(boolean allowPreExisting) {
        return FindingGrader.STRICT.withAllowlist(ALLOWANCES).withBaseline(baseline()).allowingPreExisting(allowPreExisting);
    }
}
