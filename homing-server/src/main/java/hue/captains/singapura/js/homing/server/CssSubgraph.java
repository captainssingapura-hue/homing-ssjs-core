package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssGroupDependencies;
import hue.captains.singapura.js.homing.core.CssGroupResolver;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/** A group's dependency subgraph as a JS object: the group and everything it depends on, in dependency order. */
public record CssSubgraph() implements StatelessFunctionalObject {

    public static final CssSubgraph INSTANCE = new CssSubgraph();

    /**
     * {@code { "<fqcn>": { deps: ["<fqcn>", …] }, … }} for the group and
     * everything it transitively depends on, in dependency order. Class names
     * need no escaping; the resolver refuses cycles.
     */
    public String js(CssGroup<?> root) {
        StringBuilder sb = new StringBuilder("{ ");
        boolean first = true;
        for (CssGroup<?> g : CssGroupResolver.INSTANCE.resolve(List.of(root))) {
            if (!first) sb.append(", ");
            first = false;
            sb.append('"').append(g.getClass().getCanonicalName()).append("\": { deps: [");
            boolean firstDep = true;
            for (CssGroup<?> dep : CssGroupDependencies.INSTANCE.of(g)) {
                if (!firstDep) sb.append(", ");
                firstDep = false;
                sb.append('"').append(dep.getClass().getCanonicalName()).append('"');
            }
            sb.append("] }");
        }
        return sb.append(" }").toString();
    }
}
