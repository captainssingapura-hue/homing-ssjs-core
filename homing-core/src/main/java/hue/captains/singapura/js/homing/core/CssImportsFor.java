package hue.captains.singapura.js.homing.core;

import java.util.ArrayList;
import java.util.List;

/**
 * The groups a {@link CssGroup} depends on — RFC 0064: DERIVED, never
 * declared. A group's dependencies are completely determined by its classes'
 * {@link CssClass#dependsOn()}: every reason one group needs another is some
 * rule in some class referencing it, so the fact lives on that class and the
 * group-level list is a computation. There is no override point; a group
 * that could declare its own list could only restate this one or disagree
 * with it, and the disagreement that matters — declaring nothing — would
 * silence every {@code dependsOn()} added later.
 *
 * @param <C> the group these are the dependencies of
 * @param cssGroup the group
 * @param imports  its dependencies, first-mention order, the group itself excluded
 */
public record CssImportsFor<C extends CssGroup<C>>(
    C cssGroup,
    List<CssGroup<?>> imports
) {

    /** The derivation. One group per dependency class's enclosing group, deduplicated by class. */
    public static <C extends CssGroup<C>> CssImportsFor<C> of(C group) {
        List<CssGroup<?>> groups = new ArrayList<>();
        for (CssClass<C> cls : group.cssClasses()) {
            var deps = new ArrayList<CssClass<?>>(cls.dependsOn());
            deps.addAll(cls.wears());
            for (CssClass<?> dep : deps) {
                CssGroup<?> g = CssClass.groupOf(dep);
                if (g.getClass() == group.getClass()) continue;
                boolean seen = false;
                for (CssGroup<?> have : groups) if (have.getClass() == g.getClass()) { seen = true; break; }
                if (!seen) groups.add(g);
            }
        }
        return new CssImportsFor<>(group, List.copyOf(groups));
    }

    /** The derivation for a group whose self-type is not in hand. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<CssGroup<?>> dependenciesOf(CssGroup<?> group) {
        return of((CssGroup) group).imports();
    }
}
