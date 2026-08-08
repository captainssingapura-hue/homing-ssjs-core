package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RFC 0044 — the structured conformance evaluation of a crate closure, for the
 * Crate-Studio's Conformance panes. The <b>crate</b> is the aggregation point:
 * each crate carries its own orphans + rolled-up illegal imports; each
 * <b>module</b> carries its own per-module findings (its slice of the layering
 * rule today; richer once the engine lands). The {@link Result#modules()} map
 * also resolves {@code moduleClass -> crate}, which is how a crate-level pane
 * derives its crate from a module selection.
 *
 * <p>Mirrors the pass/fail of {@link OrphanCheck} + {@link CrateDependencyRule}
 * but returns typed results instead of human strings so the studio can render
 * per-crate and per-module status.</p>
 */
public final class CrateConformance {

    private CrateConformance() {}

    public record Result(boolean ok,
                         Map<String, CrateResult> crates,
                         Map<String, ModuleResult> modules) {}

    /** @param illegalImports this crate's modules' illegal/un-crated imports, aggregated. */
    public record CrateResult(String name, int modules, boolean ok,
                              List<String> orphans, List<String> illegalImports) {}

    /** @param findings this module's own illegal/un-crated imports. */
    public record ModuleResult(String moduleClass, String crate, String form,
                               boolean ok, List<String> findings) {}

    public static Result evaluate(List<Crate> closure) {
        // moduleClass -> owning crate name
        Map<String, String> owner = new HashMap<>();
        for (Crate c : closure) {
            for (CrateEntry e : c.entries()) owner.put(e.moduleClass(), c.name());
        }

        Map<String, CrateResult> crates = new LinkedHashMap<>();
        Map<String, ModuleResult> modules = new LinkedHashMap<>();
        boolean allOk = true;

        for (Crate c : closure) {
            Set<String> allowed = new LinkedHashSet<>();
            allowed.add(c.name());
            for (Crate r : c.requires()) allowed.add(r.name());

            List<String> orphans = OrphanCheck.check(c);
            List<String> crateIllegal = new ArrayList<>();

            for (CrateEntry entry : c.entries()) {
                List<String> findings = moduleFindings(entry, c.name(), allowed, owner);
                crateIllegal.addAll(findings);
                modules.put(entry.moduleClass(), new ModuleResult(
                        entry.moduleClass(), c.name(),
                        entry.form().name().toLowerCase().replace('_', '-'),
                        findings.isEmpty(), List.copyOf(findings)));
            }

            boolean crateOk = orphans.isEmpty() && crateIllegal.isEmpty();
            allOk &= crateOk;
            crates.put(c.name(), new CrateResult(
                    c.name(), c.entries().size(), crateOk,
                    List.copyOf(orphans), List.copyOf(crateIllegal)));
        }
        return new Result(allOk, crates, modules);
    }

    /** One module's illegal (undeclared-crate) + un-crated imports. */
    private static List<String> moduleFindings(
            CrateEntry entry, String crateName, Set<String> allowed, Map<String, String> owner) {
        var findings = new ArrayList<String>();
        String importer = entry.moduleClass();
        Map<Importable, ModuleImports<?>> all = entry.module().imports().getAllImports();
        for (ModuleImports<?> mi : all.values()) {
            Importable from = mi.from();
            if (!(from instanceof EsModule<?>)) continue;
            String imported = from.getClass().getName();
            if (imported.equals(importer)) continue;
            String ownerCrate = owner.get(imported);
            if (ownerCrate == null) {
                findings.add("imports " + imported + " — no crate in scope declares it");
            } else if (!allowed.contains(ownerCrate)) {
                findings.add("imports " + imported + " (crate '" + ownerCrate
                        + "'), which '" + crateName + "' does not require");
            }
        }
        return findings;
    }
}
