package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * RFC 0060 — where a workspace's opening arrangement came from.
 *
 * <p>Carried by the {@code WorkspaceSeeded} stamp, so a workspace records not
 * merely <i>that</i> it was given a starting arrangement but <b>which one</b>.
 * That is the difference that matters when a spec's arrangement changes later
 * and somebody asks why an old workspace looks different from a new one.</p>
 *
 * <h2>Sealed, and deliberately small</h2>
 *
 * <p>One variant exists because one thing can produce a seed today: the workspace
 * kind's own {@code arrangement()}. The intended growth is an arrangement
 * received from another user, or taken from a marketplace — and neither is stubbed
 * here, because nothing can emit them and their fields would be a guess. The
 * sealed interface is the seam that makes adding them a compile-checked change
 * rather than a silent widening.</p>
 *
 * <h2>This is a wire format</h2>
 *
 * <p>It goes into the event log, so it outlives the code that wrote it. A stamp
 * written today must still decode when further variants exist, which is why each
 * variant is tagged by name rather than by position, and why the decoder must
 * have a stated behaviour for a tag it does not recognise — a log written by a
 * newer build should not hard-fail an older one.</p>
 *
 * <p>It is also kept <b>small</b> on purpose. Its job is "seeded, and with what",
 * not to carry a copy of the arrangement: richer provenance belongs in whatever
 * registry the name resolves through, not duplicated into every workspace's
 * log.</p>
 *
 * @since RFC 0060
 */
public sealed interface ArrangementSource extends ValueObject {

    /** The tag this variant is written under. Stable — it is on the wire. */
    String tag();

    /**
     * The workspace kind's own declared arrangement — the only source today.
     *
     * @param kind            the workspace kind that declared it
     * @param arrangementName the shape's name, as {@code PaneArrangement.name()}
     */
    record FromKind(WorkspaceKind kind, String arrangementName) implements ArrangementSource {

        public static final String TAG = "fromKind";

        public FromKind {
            Objects.requireNonNull(kind,            "ArrangementSource.FromKind.kind");
            Objects.requireNonNull(arrangementName, "ArrangementSource.FromKind.arrangementName");
            if (arrangementName.isBlank()) {
                throw new IllegalArgumentException(
                        "ArrangementSource.FromKind.arrangementName must not be blank");
            }
        }

        @Override public String tag() { return TAG; }

        @Override public String toString() { return kind.value() + ":" + arrangementName; }
    }
}
