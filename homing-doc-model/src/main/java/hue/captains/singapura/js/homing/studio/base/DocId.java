package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0015 Phase 2 — typed identity for {@link Doc}. Sealed sum of the
 * three identifier shapes the framework supports:
 *
 * <ul>
 *   <li>{@link ByUuid} — wraps a {@link UUID}. The identity used by all
 *       prose Docs ({@link ClasspathMarkdownDoc} and friends); also the
 *       identity used by {@code ProxyDoc} (its own UUID, distinct from
 *       the target's).</li>
 *   <li>{@link ByClass} — wraps a {@link Class}. Reserved for future
 *       {@code PlanDoc} (RFC 0015 Phase 3) — Plan trackers are
 *       class-identified singletons.</li>
 *   <li>{@link ByClassAndParams} — wraps a class plus an opaque params
 *       value. Reserved for future {@code AppDoc} (RFC 0015 Phase 3) —
 *       AppModule navigables carry typed Params that participate in
 *       identity.</li>
 * </ul>
 *
 * <p>Realises Doc ontology axioms A1 (identifier kind), A2 (universality
 * of the identifier), and A4 (local uniqueness within a deployment).</p>
 *
 * <p><b>Phase 2 status:</b> the type family is defined; {@code ByUuid} is
 * the only variant in use (every existing Doc carries it via {@link
 * Doc#id()}'s default). {@code ByClass} and {@code ByClassAndParams} land
 * with their realising Doc subtypes in Phase 3.</p>
 *
 * @since RFC 0015 Phase 2
 */
public sealed interface DocId extends ValueObject {

    /** Identity-by-UUID. The variant used by all prose Docs. */
    record ByUuid(UUID id) implements DocId {
        public ByUuid {
            Objects.requireNonNull(id, "DocId.ByUuid.id");
        }
    }

    /**
     * Identity-by-class. Reserved for Phase 3 PlanDoc — a Plan tracker is
     * a class-identified singleton; two tracker instances of the same
     * class are the same Plan.
     */
    record ByClass(Class<?> cls) implements DocId {
        public ByClass {
            Objects.requireNonNull(cls, "DocId.ByClass.cls");
        }
    }

    /**
     * Identity-by-class-and-params. Reserved for Phase 3 AppDoc — an
     * AppModule navigable's identity is the pair of its AppModule class
     * and the value of its typed Params record.
     */
    record ByClassAndParams(Class<?> cls, Object params) implements DocId {
        public ByClassAndParams {
            Objects.requireNonNull(cls, "DocId.ByClassAndParams.cls");
            // params may legitimately be the framework's "no params" sentinel value
            // (currently AppModule._None.INSTANCE); we accept any non-null value.
            Objects.requireNonNull(params, "DocId.ByClassAndParams.params");
        }
    }
}
