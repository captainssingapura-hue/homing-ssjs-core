package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

/**
 * The boundary: one semantic leaf projected onto one physical leaf — a
 * <b>value</b>, made of the two type tokens and nothing else. A component
 * declares what its element wears as these values:
 * {@code DesignClass.of(Danger.class, Color.Surface.class)}; a design is a
 * function from them to a fulfilment. Nobody declares a projection, so none
 * can be misspelt, declared twice, or left out.
 *
 * <p>Toward the module graph it is a {@link Wearable}: a token for the
 * element ({@code danger-color-surface} — the semantic without its branch, a
 * meaning may be re-curated without its token changing; the target with,
 * leaf names repeat across media) and a group whose sheet must load — its
 * target. It has no body: it is not a class of any group's own, and what it
 * looks like is a design's answer, never its own.</p>
 *
 * <p>Equality is by value, so the same pair asked for in two crates is one
 * token, and a requirement set is a set.</p>
 *
 * @param semantic the semantic leaf — a record with exactly one branch
 * @param target   the physical leaf — a leaf of the sealed tree, and the class's group
 * @param <T>      the target leaf
 */
public record DesignClass<T extends Target & CssGroup<T>>(Class<? extends Semantic> semantic, Class<T> target)
        implements Wearable {

    public DesignClass {
        Trees.requireSemanticLeaf(semantic);
        Trees.requireTargetLeaf(target);
    }

    public static <T extends Target & CssGroup<T>> DesignClass<T> of(Class<? extends Semantic> semantic, Class<T> target) {
        return new DesignClass<>(semantic, target);
    }

    /** {@code <semantic>-<branch>-<leaf>}: the class token on the element and the stem of every variable. */
    @Override public String cssName() { return Trees.semanticToken(semantic) + "-" + Trees.targetToken(target); }

    /** The group is the target: virtual, and derived from the pair. */
    @Override public CssGroup<?> group() { return targetLeaf(); }

    /** The target leaf's singleton — the properties, states and carrier this class is bound within. */
    @SuppressWarnings("unchecked")
    public T targetLeaf() { return (T) Trees.targetInstance(target); }

    /** Whether this class is on the colour plane — the {@link Target.Color} branch, the one an orthogonal palette answers. */
    public boolean onColourPlane() { return Target.Color.class.isAssignableFrom(target); }

    /**
     * A reference to this class's value at rest, for another word to read:
     * {@code var(--body-color-ink)}. The way a physique names a colour without
     * carrying one — a shadow is "the ink, offset", a glow "the primary
     * surface, blurred" — and the way a dressed body paints a part the DOM
     * cannot dress. Resolved by the browser against the palette's root
     * binding, mode included; refused at deployment when the referenced pair
     * is not required, so a reference never dangles.
     */
    public String var() {
        var owned = targetLeaf().properties();
        if (owned.size() != 1) throw new IllegalArgumentException(cssName() + " owns " + owned.size() + " properties; name one");
        return "var(" + Trees.variable(this, owned.iterator().next()) + ")";
    }

    public String var(String property) {
        if (!targetLeaf().properties().contains(property)) throw new IllegalArgumentException(cssName() + " does not own " + property);
        return "var(" + Trees.variable(this, property) + ")";
    }

    /** The reference at a state — {@code var(--link-color-ink-hover)} — for a word that wants another's hover, selected, … value. */
    public String var(State state) {
        var owned = targetLeaf().properties();
        if (owned.size() != 1) throw new IllegalArgumentException(cssName() + " owns " + owned.size() + " properties; name one");
        return var(owned.iterator().next(), state);
    }

    public String var(String property, State state) {
        if (!targetLeaf().properties().contains(property)) throw new IllegalArgumentException(cssName() + " does not own " + property);
        if (!targetLeaf().states().contains(state)) throw new IllegalArgumentException(cssName() + " offers no slot for " + state);
        return "var(" + Trees.variable(this, property) + state.suffix() + ")";
    }

    @Override public String toString() { return cssName(); }
}
