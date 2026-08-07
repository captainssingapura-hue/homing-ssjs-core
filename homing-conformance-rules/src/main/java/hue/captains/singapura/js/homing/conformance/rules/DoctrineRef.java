package hue.captains.singapura.js.homing.conformance.rules;

import java.util.Objects;

/**
 * A typed reference to the doctrine a {@link JsRule} enforces — its <i>why</i>.
 * Carried on every rule so the studio can show, and a reader can trace, the
 * principle a gate defends. A slug (e.g. {@code "managed-dom-ops"}); resolving
 * it to the doctrine doc is the studio's concern, not the model's.
 *
 * @param slug a non-blank doctrine slug
 */
public record DoctrineRef(String slug) {
    public DoctrineRef {
        Objects.requireNonNull(slug, "DoctrineRef.slug");
        if (slug.isBlank()) throw new IllegalArgumentException("DoctrineRef.slug must be non-blank");
    }
    @Override public String toString() { return slug; }
}
