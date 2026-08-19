package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * RFC 0044 — the whole‑app <b>coverage</b> guarantee: every served JS module the
 * app ships is declared in <em>some</em> crate of the closure. Where {@link
 * OrphanCheck} is per‑crate (does <i>this</i> crate list every served module in
 * its own Maven module?), {@code CrateCoverage} is per‑app: does <i>every</i>
 * served module across the app's Maven modules belong to a crate at all?
 *
 * <p>This closes the "serve without conformance" leak at build time — a served
 * module in a crate‑less Maven module (or simply forgotten) would otherwise be
 * served by reflection yet never conformance‑checked. It is the build‑time
 * complement to the runtime crate‑gate ({@code EsModuleGetAction}'s allow‑list):
 * the gate refuses to serve an uncrated module at request time; this fails the
 * build so you never ship one.</p>
 *
 * <p>Scope is explicit and cheap: you pass one <b>anchor class per app‑owned
 * Maven module</b> (any class in it); each module's build output is scanned for
 * concrete {@link hue.captains.singapura.js.homing.core.EsModule}s via the same
 * code‑source scan {@link OrphanCheck} uses — no whole‑classpath crawl, so
 * framework jars are never mis‑flagged.</p>
 */
public final class CrateCoverage {

    private CrateCoverage() {}

    /**
     * @param closure    the crate closure whose union of entries counts as "crated"
     *                   (typically {@code CrateClosure.of(topLevelCrates)})
     * @param appAnchors one class per app‑owned Maven module; each module's build
     *                   output is scanned for served modules
     * @return one finding per served module that is in no crate of the closure;
     *         empty means full coverage (nothing can serve uncrated)
     */
    public static List<String> check(Collection<? extends Crate> closure, Collection<Class<?>> appAnchors) {
        Set<String> crated = new HashSet<>();
        for (Crate c : closure) {
            for (CrateEntry e : c.entries()) crated.add(e.moduleClass());
        }
        var findings = new ArrayList<String>();
        var seen = new TreeSet<String>();
        for (Class<?> anchor : appAnchors) {
            for (String fqcn : OrphanCheck.servedModuleClassesInModuleOf(anchor)) {
                if (seen.add(fqcn) && !crated.contains(fqcn)) {
                    findings.add("uncrated served module: " + fqcn + " is served (found in the build output of "
                            + anchor.getName() + "'s Maven module) but is declared in no crate of the closure"
                            + " — it would serve but escape conformance");
                }
            }
        }
        return List.copyOf(findings);
    }
}
