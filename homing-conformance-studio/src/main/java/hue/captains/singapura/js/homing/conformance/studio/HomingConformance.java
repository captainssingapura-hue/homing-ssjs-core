package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.Allowance;
import hue.captains.singapura.js.homing.conformance.rules.Baseline;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.FindingGrader;
import hue.captains.singapura.js.homing.conformance.rules.RuleId;
import hue.captains.singapura.js.homing.core.Crate;

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
 * RFC 0044 — homing-ssjs-core's own conformance configuration, in one place so
 * the build-fail gate ({@code SelfConformanceTest}) and the report export
 * ({@code SelfConformanceExport}) grade identically: the same crate closure, the
 * same committed {@link Baseline}, and the same documented {@link Allowance}s.
 */
public final class HomingConformance {

    private HomingConformance() {}

    /** homing-ssjs-core's documented, intentional exceptions (not debt). */
    public static final List<Allowance> ALLOWANCES = List.of(
            // ── RFC 0050 — the Relation Grid family ──────────────────────
            // Deliberate design decisions recorded in the RFC, not debt: they
            // are ALLOWANCES (reasoned, permanent) rather than baseline entries
            // (grandfathered, meant to shrink). MTP/SplitPane carry the same
            // findings as baselined debt because they predate the rules; the
            // grid chose this posture knowingly, so it says so.
            new Allowance(
                    "hue.captains.singapura.js.homing.grid.GridLayoutModule",
                    new RuleId("use-dom-ops-party"),
                    "The layout branch is a PRIMITIVE that owns the <table> skeleton itself, on "
                            + "the MultiTabPane / SplitPane precedent: raw DOM internally keeps it "
                            + "portable and free of a DomOpsParty dependency. The party discipline "
                            + "applies to the CELLS branch, which does mint through a handed-in "
                            + "branch — that is the ownership boundary RFC 0050 is built on."),
            new Allowance(
                    "hue.captains.singapura.js.homing.grid.GridLayoutModule",
                    new RuleId("view-doctrine"),
                    "The single lookup is getElementById on the grid's own injected <style> tag — "
                            + "the idempotence check that keeps N grids from injecting N stylesheets. "
                            + "It reads infrastructure this module authored, never view content."),
            new Allowance(
                    "hue.captains.singapura.js.homing.grid.GridHeaderDragModule",
                    new RuleId("use-dom-ops-party"),
                    "Split out of GridLayout by the line ratchet; it mints the same layout-branch "
                            + "chrome (resize handle, drop band) and inherits the same posture."),
            new Allowance(
                    "hue.captains.singapura.js.homing.grid.StockCellsModule",
                    new RuleId("use-dom-ops-party"),
                    "A cell mints its OWN editor (input / select) inside its own element. The cell "
                            + "element itself came from the cells branch; the editor is a sub-life of "
                            + "that cell, created and removed within one edit."),
            new Allowance(
                    "hue.captains.singapura.js.homing.grid.StockCellsModule",
                    new RuleId("no-dom-destruction"),
                    "textContent = \"\" clears the cell's OWN text before mounting its editor, and "
                            + "restores it after — the narrowest possible scope, on an element the "
                            + "cell owns outright. Not a wholesale wipe of anything foreign."),
            new Allowance(
                    "hue.captains.singapura.js.homing.studio.base.ui.layout.ModalModule",
                    new RuleId("no-dom-destruction"),
                    "Modal.setContent is a wholesale-body-swap API; the drag-to-modal flow never "
                            + "wipes widget DOM (MultiTabPaneDragModule moves content out first)."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.HrefManager",
                    new RuleId("no-raw-href"),
                    "HrefManager IS the href-manager implementation — it defines the href.* API "
                            + "the rule redirects consumers to; window.location/setAttribute('href') "
                            + "here are the sanctioned primitives, not a bypass."),
            new Allowance(
                    "hue.captains.singapura.js.homing.server.CssClassManager",
                    new RuleId("no-raw-href"),
                    "CssClassManager builds its own stylesheet <link> href — framework "
                            + "infrastructure that emits the served CSS, not a consumer view."));

    /** The crate closure the gate + export run over — every first-party served module. */
    public static Collection<Crate> closure() {
        return CrateClosure.of(TopLevelCrates.ALL);
    }

    /** The committed baseline of grandfathered pre-existing violations. */
    public static Baseline baseline() {
        try (InputStream in = HomingConformance.class.getResourceAsStream("/conformance-baseline.txt")) {
            if (in == null) return Baseline.EMPTY;
            var lines = new ArrayList<String>();
            try (var r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                for (String line; (line = r.readLine()) != null; ) lines.add(line);
            }
            return Baseline.of(lines);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to load conformance baseline", e);
        }
    }

    /** The framework-strict grader plus this app's allowances + baseline. */
    public static FindingGrader grader(boolean allowPreExisting) {
        return FindingGrader.STRICT
                .withAllowlist(ALLOWANCES)
                .withBaseline(baseline())
                .allowingPreExisting(allowPreExisting);
    }
}
