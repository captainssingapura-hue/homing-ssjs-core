package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A node's stable, URL-safe identity segment — sibling-unique and hard-capped at
 * {@value #MAX_CHARS} chars. The chain of {@code NodeName}s from the root forms a
 * node's <b>name-path</b> ({@code "animals/turtle"}), the position-independent
 * address by which its content is looked up (RigidDocV2 / RFC 0039) — stable
 * across reordering, unlike a child-index path.
 *
 * <p>Charset is deliberately narrow — {@code [A-Za-z0-9._-]} — so a name travels
 * unescaped in a URL, an HTML anchor, and the {@code '/'}-joined path key. The
 * separator {@code '/'} is therefore <b>not</b> a legal name char. Like the other
 * {@code text} value objects, the constraint lives in the type, never in a raw
 * {@code String}.</p>
 */
public record NodeName(String value) implements ValueObject {

    /** Hard cap on a name segment's length, in chars — a short machine id, not prose. */
    public static final int MAX_CHARS = 48;

    /** The name-path separator; never a legal char inside a {@code NodeName}. */
    public static final char SEPARATOR = '/';

    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9._-]+");

    public NodeName {
        Objects.requireNonNull(value, "NodeName.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("NodeName.value must not be blank");
        }
        if (value.length() > MAX_CHARS) {
            throw new IllegalArgumentException(
                    "NodeName.value exceeds " + MAX_CHARS + " chars (was " + value.length() + "): " + value);
        }
        if (!SAFE.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "NodeName.value must be URL-safe [A-Za-z0-9._-] with no '/': " + value);
        }
    }

    /**
     * Derive a {@code NodeName} from an arbitrary label — lowercased, every run of
     * non-{@code [a-z0-9]} folded to a single {@code '-'}, trimmed, and clipped to
     * {@value #MAX_CHARS}. A label that reduces to nothing yields {@code "n"}. Handy
     * when mirroring a source whose nodes carry human names rather than ids;
     * callers must still guarantee sibling-uniqueness (the normalizer enforces it).
     */
    public static NodeName slug(String label) {
        if (label == null) return new NodeName("n");
        String s = label.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        if (s.isBlank()) s = "n";
        if (s.length() > MAX_CHARS) s = s.substring(0, MAX_CHARS).replaceAll("-+$", "");
        if (s.isBlank()) s = "n";
        return new NodeName(s);
    }

    /**
     * A human {@link Title} derived from this name — words split on {@code -}, {@code _},
     * or {@code .}, each capitalized, joined by spaces ({@code "dancing-animals"} →
     * {@code "Dancing Animals"}). The default heading when a caller has a slug-like
     * name but no separate title; always within the {@link Title} cap, since a name is
     * shorter still.
     */
    public Title defaultTitle() {
        var sb = new StringBuilder(value.length());
        for (String word : value.split("[-_.]+")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) sb.append(word, 1, word.length());
        }
        return new Title(sb.length() == 0 ? value : sb.toString());
    }

    @Override public String toString() { return value; }
}
