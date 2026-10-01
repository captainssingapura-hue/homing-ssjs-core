package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CssConformance;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.PaletteProvision;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.themes.StudioWorkspaceThemes;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The old studio stack, gated on its way out: studio-base, the old workspace, its codecs,
 * persistence and shell, the studio workspace - graded as the old conformance studio graded them,
 * with their allowances, their lines of its ledger and the studio palettes as the CSS priors. On
 * the test classpath only: nothing downstream sees them. Retiring a crate retires its lines here.
 */
class OldStackConformanceTest extends SelfGate {

    static final List<Crate> OLD_STACK = List.of(
            StudioBaseCrate.INSTANCE,
            WorkspaceCrate.INSTANCE,
            WorkspaceCodecsCrate.INSTANCE,
            WorkspacePersistenceCrate.INSTANCE,
            WorkspaceShellCrate.INSTANCE,
            StudioWorkspaceCrate.INSTANCE);

    static final List<Allowance> ALLOWANCES = List.of(
            new Allowance(
                    "hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule",
                    new RuleId("no-dom-destruction"),
                    "Modal.setContent is a wholesale-body-swap API; the drag-to-modal flow never "
                            + "wipes widget DOM (MultiTabPaneDragModule moves content out first)."),
            new Allowance(
                    "hue.captains.singapura.js.homing.studio.base.css.StudioStyles",
                    CssConformance.NESTED_DECLARED,
                    "st_page's print rule names #__theme_picker_slot__: an element the page template "
                            + "mints (AppHtmlGetAction), not a class of any group. It becomes a class when "
                            + "the picker slot leaves the server (RFC 0065 D2)."),
            new Allowance(
                    "hue.captains.singapura.js.homing.workspace.shell.CssGraphStyles",
                    CssConformance.TOKEN_DECLARED,
                    "cg_note_err reads --color-danger, the feedback role no palette declares yet - RFC 0066 "
                            + "Episode 2's base tree names it feedback/error; until then it falls to unset, "
                            + "which is the state RFC 0050-ext3 recorded when it asked for the token."));

    private static final String BASELINE = "old-stack-conformance-baseline.txt";

    private static Baseline baseline() {
        try (InputStream in = OldStackConformanceTest.class.getResourceAsStream("/" + BASELINE)) {
            if (in == null) throw new IllegalStateException(BASELINE + " is missing");
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("the old stack's baseline could not be read", e);
        }
    }

    @Override Collection<Crate> closure() { return CrateClosure.of(OLD_STACK); }

    @Override FindingGrader grader() {
        return FindingGrader.STRICT.withAllowlist(ALLOWANCES).withBaseline(baseline()).allowingPreExisting(ALLOW_PRE_EXISTING);
    }

    @Override List<PaletteProvision<?, ?>> provisions() { return StudioWorkspaceThemes.INSTANCE.palettes(); }

    @Override Predicate<String> owns() {
        Set<String> own = OLD_STACK.stream().flatMap(c -> c.entries().stream()).map(e -> e.moduleClass()).collect(Collectors.toSet());
        return own::contains;
    }

    @Override String baselineFile() { return BASELINE; }
}
