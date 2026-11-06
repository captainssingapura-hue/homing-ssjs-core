package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;

/** The groups a {@link CssGroup} depends on, read from its classes' {@link CssClass#dependsOn()} ({@link CssImportsFor}). */
public record CssGroupDependencies() implements StatelessFunctionalObject {

    public static final CssGroupDependencies INSTANCE = new CssGroupDependencies();

    /** The other groups its classes name, each once, in the order they are first named. */
    public List<CssGroup<?>> of(CssGroup<?> group) {
        List<CssGroup<?>> groups = new ArrayList<>();
        for (CssClass<?> cls : group.cssClasses()) {
            for (CssClass<?> dep : cls.dependsOn()) {
                CssGroup<?> g = dep.group();
                if (g.getClass() == group.getClass()) continue;
                boolean seen = false;
                for (CssGroup<?> have : groups) if (have.getClass() == g.getClass()) { seen = true; break; }
                if (!seen) groups.add(g);
            }
        }
        return List.copyOf(groups);
    }
}
