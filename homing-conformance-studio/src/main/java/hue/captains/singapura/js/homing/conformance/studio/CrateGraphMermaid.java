package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.core.Crate;

import java.util.List;
import java.util.Set;

/**
 * RFC 0044 — renders a crate dependency graph as Mermaid {@code flowchart} text
 * (generated on the backend; the browser draws it via the existing Mermaid
 * proxy). Nodes are crates (owned crates styled solid, external ones dashed),
 * edges are {@code requires} relations (arrow points from dependent to
 * dependency). Top-down, so dependents sit above the crates they stand on.
 *
 * <p>Deliberately simple for now — a static diagram. A fully interactive graph
 * (node selection into the party bus, pan/zoom) is a later expansion.</p>
 */
public final class CrateGraphMermaid {

    private CrateGraphMermaid() {}

    public static String render(List<Crate> crates, Set<String> ownedNames) {
        var sb = new StringBuilder();
        sb.append("flowchart TD\n");

        for (Crate c : crates) {
            int n = c.entries().size();
            sb.append("  ").append(id(c.name()))
              .append("[\"").append(c.name())
              .append("<br/>").append(n).append(n == 1 ? " module" : " modules").append("\"]\n");
        }
        for (Crate c : crates) {
            for (Crate r : c.requires()) {
                sb.append("  ").append(id(c.name())).append(" --> ").append(id(r.name())).append('\n');
            }
        }

        sb.append("  classDef owned fill:#eaf2ff,stroke:#3b6db3,color:#1a2a44;\n");
        sb.append("  classDef external fill:#f4f4f4,stroke:#aaa,stroke-dasharray:4 3,color:#555;\n");

        String owned = joinIds(crates, ownedNames, true);
        String external = joinIds(crates, ownedNames, false);
        if (!owned.isEmpty())    sb.append("  class ").append(owned).append(" owned;\n");
        if (!external.isEmpty()) sb.append("  class ").append(external).append(" external;\n");

        return sb.toString();
    }

    private static String joinIds(List<Crate> crates, Set<String> ownedNames, boolean owned) {
        return String.join(",", crates.stream()
                .filter(c -> ownedNames.contains(c.name()) == owned)
                .map(c -> id(c.name()))
                .toList());
    }

    /** Mermaid node ids must be identifier-safe — crate names carry dashes. */
    private static String id(String crateName) {
        return crateName.replaceAll("[^A-Za-z0-9]", "_");
    }
}
