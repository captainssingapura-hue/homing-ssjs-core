package hue.captains.singapura.js.homing.studio.base.export;

import hue.captains.singapura.js.homing.tree.NamePath;

import java.util.Objects;

/**
 * The fragment a doc node or one of its segments answers to — the address that
 * makes a table of contents work in an <b>exported page with no JavaScript</b>.
 *
 * <p>Live, nothing uses these: the reader holds its sections in a map it built
 * while minting them (Owned References), and the TOC scrolls through the
 * {@code TocSyncSecretary} rather than through the browser. The id and the href
 * earn their keep only once the JavaScript is gone, which is why they live under
 * {@code export}.</p>
 *
 * <h2>Why this type exists</h2>
 *
 * <p>There were two implementations of one scheme, in two languages, that had to
 * agree by hand: Java built segment anchors as {@code "seg-" + key + "-" + i},
 * and the client built node ids with its own concatenation. Nothing checked that
 * they stayed consistent, and they share the key. This is the Java half named
 * once.</p>
 *
 * <h2>What it deliberately is not</h2>
 *
 * <p>It would be tidier for the server to send each node its finished anchor.
 * That is not possible without harm: node metadata travels as
 * {@code DimensionKey}, which is <b>sealed</b> to {@code DisplayLabel} and
 * {@code NodeKey} precisely so the substrate's vocabulary cannot grow by
 * accident. An anchor is not a property of a tree — it is how one particular
 * renderer addresses a node — so widening that seal to carry it would put a
 * presentation concern inside the rigid-tree substrate. The client therefore
 * still composes node ids from the name-path it is walking anyway; what it no
 * longer does is <i>invent</i> the shape.</p>
 *
 * @param value the raw fragment text, without the leading {@code #}
 */
public record DocAnchor(String value) {

    /** The root's anchor: a name-path is empty there, and an empty id is invalid HTML. */
    public static final String ROOT = "doc";

    public DocAnchor {
        Objects.requireNonNull(value, "DocAnchor.value");
        if (value.isBlank()) throw new IllegalArgumentException("DocAnchor.value must not be blank");
    }

    /** A node's own anchor — its name-path, or {@link #ROOT} at the top. */
    public static DocAnchor ofNode(NamePath path) {
        Objects.requireNonNull(path, "DocAnchor.ofNode: path");
        return new DocAnchor(path.isEmpty() ? ROOT : path.wire());
    }

    /**
     * One segment inside a node. Prefixed, because a node and its first segment
     * would otherwise both want the node's key: the prefix is what keeps the two
     * families apart, not decoration.
     */
    public static DocAnchor ofSegment(String nodeKey, int index) {
        String base = (nodeKey == null || nodeKey.isEmpty()) ? ROOT : nodeKey;
        return new DocAnchor("seg-" + base + "-" + index);
    }

    /** The href form. */
    public String fragment() { return "#" + value; }

    @Override public String toString() { return value; }
}
