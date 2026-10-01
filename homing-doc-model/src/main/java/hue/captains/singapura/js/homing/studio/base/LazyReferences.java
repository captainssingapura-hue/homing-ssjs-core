package hue.captains.singapura.js.homing.studio.base;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A doc's references, declared lazily: read from their supplier each time they are asked for,
 * never when the doc is made. Docs whose constants name each other can then all be made, and each
 * names the others - an eager list reads the other's constant while it is still being made, and
 * holds null.
 *
 * <p>It is equal only to itself, so comparing or hashing a doc never reads the docs it names -
 * which may name it back.</p>
 *
 * <pre>{@code
 * List<Reference> references = LazyReferences.of(() -> List.of(
 *         new DocReference("rfc-30", Rfc0030Doc.INSTANCE)));
 * }</pre>
 */
public final class LazyReferences extends AbstractList<Reference> {

    private final Supplier<List<Reference>> references;

    private LazyReferences(Supplier<List<Reference>> references) {
        this.references = Objects.requireNonNull(references, "LazyReferences.references");
    }

    /** References read from the supplier when they are asked for. */
    public static List<Reference> of(Supplier<List<Reference>> references) { return new LazyReferences(references); }

    @Override public Reference get(int index) { return references.get().get(index); }
    @Override public int size() { return references.get().size(); }
    @Override public Iterator<Reference> iterator() { return references.get().iterator(); }

    /** Itself alone: what it names is not read to compare it. */
    @Override public boolean equals(Object o) { return this == o; }
    @Override public int hashCode() { return System.identityHashCode(this); }
}
