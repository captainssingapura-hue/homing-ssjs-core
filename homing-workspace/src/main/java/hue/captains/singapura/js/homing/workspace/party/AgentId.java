package hue.captains.singapura.js.homing.workspace.party;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable, path-shaped identity for an Agent (Secretary or Actor) within
 * a Party's tree. Segments are simple identifiers; the full path is the
 * addressable handle the runtime uses for dispatch and inspection.
 *
 * <p>Examples (printed via {@link #asPath()}):</p>
 *
 * <ul>
 *   <li>{@code workspace} — root Secretary of a Party</li>
 *   <li>{@code workspace/ribbon} — sub-Secretary</li>
 *   <li>{@code workspace/ribbon/animalSelector} — Actor</li>
 *   <li>{@code workspace/widgets/tab-3} — widget Actor</li>
 * </ul>
 *
 * @param segments path components, root-first; immutable copy
 * @since RFC 0028 cycle 1
 */
public record AgentId(List<String> segments) {

    /**
     * Segment grammar: letters, digits, hyphen, underscore. Enforced so
     * paths printed via {@link #asPath()} are unambiguous (no embedded
     * slashes that would break path parsing).
     */
    private static final Pattern SEGMENT = Pattern.compile("[A-Za-z0-9_-]+");

    public AgentId {
        Objects.requireNonNull(segments, "AgentId.segments");
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("AgentId.segments must not be empty");
        }
        for (String s : segments) {
            Objects.requireNonNull(s, "AgentId.segments element");
            if (!SEGMENT.matcher(s).matches()) {
                throw new IllegalArgumentException(
                        "AgentId segment '" + s + "' is invalid — only letters, digits, hyphen, and underscore allowed");
            }
        }
        segments = List.copyOf(segments);
    }

    /** Convenience: build from variadic segments. */
    public static AgentId of(String... segments) {
        return new AgentId(List.of(segments));
    }

    /** Human-readable form: segments joined by {@code /}. */
    public String asPath() {
        return String.join("/", segments);
    }

    /** Append one segment to derive a child identity. */
    public AgentId child(String segment) {
        var next = new java.util.ArrayList<String>(segments.size() + 1);
        next.addAll(segments);
        next.add(segment);
        return new AgentId(next);
    }

    /** Parent identity, or {@code null} if this id is at the root. */
    public AgentId parentOrNull() {
        if (segments.size() <= 1) return null;
        return new AgentId(segments.subList(0, segments.size() - 1));
    }

    /**
     * True if {@code candidate} is a strict descendant of this id —
     * shares this id as prefix and is at least one segment deeper. Used
     * by Secretaries to decide whether an incoming envelope came from
     * one of their own members (FromMember) vs from their parent
     * (FromParent).
     */
    public boolean isStrictAncestorOf(AgentId candidate) {
        Objects.requireNonNull(candidate, "candidate");
        if (candidate.segments.size() <= segments.size()) return false;
        for (int i = 0; i < segments.size(); i++) {
            if (!segments.get(i).equals(candidate.segments.get(i))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return asPath();
    }
}
