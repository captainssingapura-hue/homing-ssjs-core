package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RFC 0044 — the architectural-layering rule for the Crate model: every JS
 * import must land in a crate the importer <b>directly</b> requires (or its own
 * crate). Reaching <i>through</i> a required crate to one it depends on is a
 * violation — transitive dependencies must be declared explicitly (Decision 3:
 * non-transitive visibility). This is JS-level dependency discipline the
 * compiler alone can't give: a module can compile an import of anything on its
 * (transitive Maven) classpath, but the curated crate graph is narrower.
 *
 * <p>Discovery, by contrast, IS transitive: the crate universe is the full
 * closure of {@code requires()} from the passed-in root, so a single app crate
 * validates every crate it ships.</p>
 */
public final class CrateDependencyRule {

    private CrateDependencyRule() {}

    /** Findings: illegal (undeclared-crate) and un-crated imports across the closure. Empty = compliant. */
    public static List<String> check(Crate root) {
        List<Crate> closure = closureOf(root);

        // Reverse index: module FQCN -> owning crate name.
        Map<String, String> owner = new HashMap<>();
        for (Crate c : closure) {
            for (CrateEntry e : c.entries()) owner.put(e.moduleClass(), c.name());
        }

        var findings = new ArrayList<String>();
        for (Crate c : closure) {
            Set<String> allowed = new LinkedHashSet<>();
            allowed.add(c.name());
            for (Crate r : c.requires()) allowed.add(r.name());

            for (CrateEntry entry : c.entries()) {
                String importerClass = entry.moduleClass();
                for (String importedClass : importedEsModuleClasses(entry)) {
                    if (importedClass.equals(importerClass)) continue; // self
                    String importedCrate = owner.get(importedClass);
                    if (importedCrate == null) {
                        findings.add("un-crated: " + importerClass + " (crate '" + c.name()
                                + "') imports " + importedClass
                                + ", which no crate in the closure declares");
                    } else if (!allowed.contains(importedCrate)) {
                        findings.add("illegal import: " + importerClass + " (crate '" + c.name()
                                + "') imports " + importedClass + " (crate '" + importedCrate
                                + "'), but '" + c.name() + "' does not require '" + importedCrate + "'");
                    }
                }
            }
        }
        return List.copyOf(findings);
    }

    /** The imported modules of one entry, as FQCNs — only EsModule sources (JS imports), not nav-only links. */
    private static List<String> importedEsModuleClasses(CrateEntry entry) {
        var out = new ArrayList<String>();
        Map<Importable, ModuleImports<?>> all = entry.module().imports().getAllImports();
        for (ModuleImports<?> mi : all.values()) {
            Importable from = mi.from();
            if (from instanceof EsModule<?>) out.add(from.getClass().getName());
        }
        return out;
    }

    /** Transitive closure of {@code requires()} from the root (dedup by crate name). */
    private static List<Crate> closureOf(Crate root) {
        var byName = new LinkedHashMap<String, Crate>();
        Deque<Crate> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Crate c = queue.removeFirst();
            if (byName.putIfAbsent(c.name(), c) != null) continue;
            queue.addAll(c.requires());
        }
        return List.copyOf(byName.values());
    }
}
