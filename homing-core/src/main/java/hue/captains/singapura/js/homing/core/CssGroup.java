package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * A CSS resource declaration that also acts as an {@link EsModule}.
 * <p>The generated JS module exports frozen {@link CssClass} objects.
 * Mirrors the {@link SvgGroup} pattern: each CssGroup is a module
 * whose exports are its declared CSS classes.</p>
 *
 * @param <C> self-type
 */
public interface CssGroup<C extends CssGroup<C>> extends EsModule<C> {

    /**
     * The groups this one depends on — RFC 0064: DERIVED from its classes'
     * {@link CssClass#dependsOn()}, in first-mention order, this group itself
     * excluded. A group may still override to declare more, never less; the
     * conformance check compares the two.
     */
    @SuppressWarnings("unchecked")
    default CssImportsFor<C> cssImports() {
        List<CssGroup<?>> groups = new java.util.ArrayList<>();
        for (CssClass<C> cls : cssClasses()) {
            for (CssClass<?> dep : cls.dependsOn()) {
                CssGroup<?> g = CssClass.groupOf(dep);
                if (g.getClass() == getClass()) continue;
                boolean seen = false;
                for (CssGroup<?> have : groups) if (have.getClass() == g.getClass()) { seen = true; break; }
                if (!seen) groups.add(g);
            }
        }
        return new CssImportsFor<>((C) this, List.copyOf(groups));
    }

    /**
     * RFC 0064 — a group that predates the dependency discipline: it declares
     * no dependencies and everything implicitly leans on it (the studio's
     * base styles). The client loads priors before the graph, always. A prior
     * with dependencies is refused.
     *
     * <p>Default: {@code false}.</p>
     */
    default boolean prior() { return false; }

    List<CssClass<C>> cssClasses();

    @Override
    default ImportsFor<C> imports() {
        return ImportsFor.noImports();
    }

    @SuppressWarnings("unchecked")
    @Override
    default ExportsOf<C> exports() {
        return new ExportsOf<>((C) this, List.copyOf(cssClasses()));
    }
}
