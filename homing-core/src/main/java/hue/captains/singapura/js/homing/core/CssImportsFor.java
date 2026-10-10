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
 * <p>Only provided groups depend on each other. The derived sheets a group's
 * classes wear or read are not dependencies of the group - they vary with the
 * theme, and the theme's owner loads them - and are answered apart, by
 * {@link #derivedOf} (RFC 0064, D25).</p>
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
            var depGroups = new ArrayList<CssGroup<?>>();
            for (CssClass<?> dep : cls.dependsOn()) depGroups.add(CssClass.groupOf(dep));
            for (CssGroup<?> g : depGroups) {
                if (g.getClass() == group.getClass()) continue;
                boolean seen = false;
                for (CssGroup<?> have : groups) if (have.getClass() == g.getClass()) { seen = true; break; }
                if (!seen) groups.add(g);
            }
        }
        return new CssImportsFor<>(group, List.copyOf(groups));
    }

    /**
     * The derived sheets a group's classes wear or read, first-mention order, each once: what the theme's
     * owner loads beside the group, and swaps when the theme changes.
     */
    public static List<DerivedCss<?>> derivedOf(CssGroup<?> group) {
        var out = new ArrayList<DerivedCss<?>>();
        for (CssClass<?> cls : group.cssClasses()) {
            var worn = new ArrayList<Wearable>(cls.wears());
            worn.addAll(cls.reads());
            for (Wearable w : worn) {
                DerivedCss<?> s = w.sheet();
                boolean seen = false;
                for (DerivedCss<?> have : out) if (have.getClass() == s.getClass()) { seen = true; break; }
                if (!seen) out.add(s);
            }
        }
        return List.copyOf(out);
    }

    /** The derivation for a group whose self-type is not in hand. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<CssGroup<?>> dependenciesOf(CssGroup<?> group) {
        return of((CssGroup) group).imports();
    }
}
