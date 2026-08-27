package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;
import hue.captains.singapura.js.homing.studio.base.Reference;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * RFC 0015 Phase 3 — Doc subtype wrapping a {@link Plan} tracker. Lets
 * Plans participate in the unified {@link Doc} family while keeping their
 * existing {@code PlanAppHost} viewer and {@code /plan?id=<class-fqn>}
 * endpoint.
 *
 * <p>Identity: {@link DocId.ByClass} keyed by the wrapped Plan's class
 * (a Plan tracker is a class-identified singleton). The {@link #uuid()}
 * is derived deterministically from the class FQN via UUID v3, so legacy
 * UUID-keyed paths see a stable identifier — but the canonical id is the
 * typed {@code DocId.ByClass} variant.</p>
 *
 * <p>Body: empty. PlanDocs are not served through {@code /doc?id=…} —
 * their viewer is {@code PlanAppHost} at the URL returned by
 * {@link #url()}. The body string is part of the {@link Doc} protocol
 * for prose Docs; for PlanDoc it stays empty.</p>
 *
 * <p>Phase 3a: the record is defined but no Entry factory yet wraps
 * incoming Plans in PlanDoc. Phase 3b rewires the factories; until then
 * PlanDoc is available for opt-in use by callers who want the unified
 * Doc identity.</p>
 *
 * @since RFC 0015 Phase 3
 */
public record PlanDoc(Plan plan) implements Doc {

    public PlanDoc {
        Objects.requireNonNull(plan, "PlanDoc.plan");
    }

    @Override public UUID uuid() {
        return UUID.nameUUIDFromBytes(
                ("plan:" + plan.getClass().getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override public DocId id() {
        return new DocId.ByClass(plan.getClass());
    }

    /**
     * RFC 0051 Law 2 — every PlanDoc shares this class, so the segment comes
     * from the wrapped Plan's class instead. Unlike the other value-Docs this
     * one has a real per-instance class to derive from, and {@link #id()}
     * already keys identity that same way — so the path segment and the
     * identity agree by construction rather than by coincidence.
     */
    @Override public NodeName slug()   { return NodeName.ofType(plan.getClass(), "Plan"); }

    @Override public String title()    { return plan.name(); }
    @Override public String summary()  { return plan.summary(); }

    @Override public String category() {
        String kicker = plan.kicker();
        return (kicker == null || kicker.isBlank()) ? "PLAN" : kicker;
    }

    @Override public String kind()     { return "plan"; }


    /** PlanDocs have no markdown body; viewer is PlanAppHost. */
    @Override public String contents() { return ""; }

    @Override public String contentType()   { return "application/json; charset=utf-8"; }
    @Override public String fileExtension() { return ""; }

    @Override public List<Reference> references() { return List.of(); }
}
