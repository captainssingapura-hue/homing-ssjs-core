package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RFC 0044 — renders a TUI-style text report of the crate set: the crate →
 * module tree (with each module's {@link hue.captains.singapura.js.homing.core.ModuleForm}),
 * the {@code requires} dependency graph, and the live conformance result
 * (OrphanCheck + CrateDependencyRule). A self-checking, presentable artifact.
 */
public final class CrateReport {

    private CrateReport() {}

    private static final int W = 74;

    public static String render(List<Crate> crates) {
        int totalModules = crates.stream().mapToInt(c -> c.entries().size()).sum();

        // ── run the checks ──
        Map<String, List<String>> orphansByCrate = new LinkedHashMap<>();
        for (Crate c : crates) orphansByCrate.put(c.name(), OrphanCheck.check(c));
        List<String> layering = CrateDependencyRule.check(allRoot(crates));
        int orphanTotal = orphansByCrate.values().stream().mapToInt(List::size).sum();
        boolean green = orphanTotal == 0 && layering.isEmpty();

        var sb = new StringBuilder();
        line(sb, '╔', '╗');
        boxed(sb, "  HOMING · JS CRATE CONFORMANCE REPORT");
        line(sb, '╚', '╝');
        sb.append('\n');
        sb.append(String.format("  %d crates · %d served modules · conformance: %s%n",
                crates.size(), totalModules, green ? "✓ ALL GREEN" : "✗ " + (orphanTotal + layering.size()) + " ISSUE(S)"));
        sb.append('\n');

        // ── crate → module tree ──
        section(sb, "CRATES");
        for (Crate c : crates) {
            String reqs = c.requires().isEmpty() ? "—"
                    : String.join(", ", c.requires().stream().map(Crate::name).toList());
            sb.append(String.format("%n  %s  [%d]  requires: %s%n", c.name(), c.entries().size(), reqs));
            List<CrateEntry> es = c.entries();
            for (int i = 0; i < es.size(); i++) {
                CrateEntry e = es.get(i);
                String branch = (i == es.size() - 1) ? "  └─ " : "  ├─ ";
                sb.append(String.format("%s%-34s %s%n", branch, e.module().getClass().getSimpleName(), e.form()));
            }
        }
        sb.append('\n');

        // ── dependency graph ──
        section(sb, "DEPENDENCY GRAPH  (non-transitive; each crate lists its DIRECT requires)");
        for (Crate c : crates) {
            String reqs = c.requires().isEmpty() ? "· (leaf)"
                    : "→ " + String.join(", ", c.requires().stream().map(Crate::name).toList());
            sb.append(String.format("  %-32s %s%n", c.name(), reqs));
        }
        sb.append('\n');

        // ── conformance ──
        section(sb, "CONFORMANCE");
        for (Crate c : crates) {
            int orphans = orphansByCrate.get(c.name()).size();
            sb.append(String.format("  %-32s orphans: %d   %s%n",
                    c.name(), orphans, orphans == 0 ? "✓" : "✗"));
        }
        sb.append(String.format("  %-32s illegal cross-crate imports: %d   %s%n",
                "«layering (all crates)»", layering.size(), layering.isEmpty() ? "✓" : "✗"));
        if (!layering.isEmpty()) {
            for (String f : layering) sb.append("      ✗ ").append(f).append('\n');
        }
        for (var en : orphansByCrate.entrySet()) {
            for (String f : en.getValue()) sb.append("      ✗ ").append(f).append('\n');
        }
        sb.append('\n');
        line(sb, '╔', '╗');
        boxed(sb, green ? "  RESULT: ✓ ALL CRATES CONFORMANT" : "  RESULT: ✗ NON-CONFORMANT");
        line(sb, '╚', '╝');
        return sb.toString();
    }

    /** True when the crate set is fully conformant. */
    public static boolean isGreen(List<Crate> crates) {
        if (!CrateDependencyRule.check(allRoot(crates)).isEmpty()) return false;
        return crates.stream().allMatch(c -> OrphanCheck.check(c).isEmpty());
    }

    /** A synthetic root requiring every crate, so the layering closure covers them all. */
    private static Crate allRoot(List<Crate> crates) {
        return new Crate() {
            @Override public String name() { return "«all»"; }
            @Override public List<CrateEntry> entries() { return List.of(); }
            @Override public List<Crate> requires() { return crates; }
        };
    }

    private static void section(StringBuilder sb, String title) {
        sb.append("── ").append(title).append(' ')
          .append("─".repeat(Math.max(0, W - title.length() - 4))).append('\n');
    }

    private static void line(StringBuilder sb, char l, char r) {
        sb.append(l).append("═".repeat(W - 2)).append(r).append('\n');
    }

    private static void boxed(StringBuilder sb, String text) {
        String t = text.length() > W - 4 ? text.substring(0, W - 4) : text;
        sb.append('║').append(String.format("%-" + (W - 2) + "s", t)).append('║').append('\n');
    }
}
