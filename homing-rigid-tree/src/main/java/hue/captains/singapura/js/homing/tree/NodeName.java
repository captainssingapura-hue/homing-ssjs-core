package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A node's stable, URL-safe identity segment — sibling-unique and hard-capped at
 * {@value #MAX_CHARS} chars. The chain of {@code NodeName}s from the root forms a
 * node's <b>name-path</b> ({@code "animals/turtle"}, see {@link NamePath}), the
 * position-independent address by which its content is looked up (RigidDocV2 /
 * RFC 0039) — stable across reordering, unlike a child-index path.
 *
 * <p>Charset is deliberately narrow — {@code [A-Za-z0-9._-]} — so a name travels
 * unescaped in a URL, an HTML anchor, and the {@code '/'}-joined path key. The
 * separator {@code '/'} is therefore <b>not</b> a legal name char. The constraint
 * lives in the type, never in a raw {@code String}.</p>
 *
 * <p><b>Why it lives in the substrate</b> (RFC 0053): a vertex carries its segment
 * as a mandatory field rather than an optional dimension, so the segment type must
 * be visible to {@code homing-rigid-tree} itself. It previously sat beside the text
 * value objects in {@code studio-base}, which is why {@link NameValue} had to wrap a
 * raw {@code String} and discard the validation at the boundary. That weak typing
 * was the module boundary, not carelessness, and moving the type is what removes it.
 * The humanizing {@code Title} derivation stayed behind with the text family, where
 * presentation belongs.</p>
 */
public record NodeName(String value) implements Scalar {

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
        // "." and ".." are legal under the charset below — dots are allowed —
        // but they mean "here" and "up" in every path notation a reader knows,
        // and a name chain is a path. A node called ".." would make a
        // name-path ambiguous to a human and traversal-shaped to a machine,
        // for no expressive gain: no real node wants that name.
        if (value.equals(".") || value.equals("..")) {
            throw new IllegalArgumentException(
                    "NodeName.value must not be '" + value + "' — reserved by path notation");
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

    @Override public String toString() { return value; }
}
