package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.Scalar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A <b>name-path</b>: the chain of {@link NodeName}s from a root down to a node,
 * {@code '/'}-joined on the wire ({@code ""} for the root, {@code "animals"} for
 * its child, {@code "animals/turtle"} for a grandchild).
 *
 * <p>A name-path is a <b>stable</b> address (RFC 0039): inserting or reordering
 * siblings never renumbers it, so anchors, deep links and content lookup all
 * survive edits — unlike the child-index path it replaces. Because every segment
 * is a {@code NodeName}, the character set and length are settled at construction
 * and a path joins into a URL without escaping.</p>
 *
 * <p><b>Parsing is all-or-nothing</b>, and that is the point. A partial parse of a
 * positional address is still a well-formed address, so a parser that skips or
 * truncates on a bad segment does not fail — it silently resolves somewhere else.
 * {@link #parse} therefore refuses the whole path when any segment is illegal,
 * returning {@code null} rather than throwing because reading a path off a request
 * is reading UNTRUSTED input: {@code animals/../etc} is a miss to answer, not a
 * server error to raise. Nothing here touches the filesystem, but a segment that
 * could not be a {@code NodeName} dies at the type before it reaches a lookup.</p>
 *
 * @param segments the chain from the root, root-first; empty is the root itself
 * @since RFC 0053
 */
public record NamePath(List<NodeName> segments) implements Scalar {

    /** The root — an empty chain. Its wire form is the empty string. */
    public static final NamePath ROOT = new NamePath(List.of());

    public NamePath {
        Objects.requireNonNull(segments, "NamePath.segments");
        for (NodeName s : segments) {
            if (s == null) throw new IllegalArgumentException("NamePath has a null segment");
        }
        segments = List.copyOf(segments);
    }

    /** The path of these segments, root first. */
    public NamePath(NodeName... segments) {
        this(List.of(segments));
    }

    /** This path with one more segment on the end. */
    public NamePath then(NodeName segment) {
        var out = new ArrayList<>(segments);
        out.add(Objects.requireNonNull(segment, "segment"));
        return new NamePath(out);
    }

    /**
     * This path re-rooted beneath {@code prefix} — {@code turtle} under
     * {@code cat/demo/animals} gives {@code cat/demo/animals/turtle}.
     *
     * <p>How a source-internal path composes with the host position it was grafted
     * at. Note that a node's IDENTITY never needs this: an identity is minted from
     * what its subtree already owns globally and rides a graft unchanged. Only the
     * position composes.</p>
     */
    public NamePath under(NamePath prefix) {
        Objects.requireNonNull(prefix, "prefix");
        if (prefix.isEmpty()) return this;
        if (isEmpty()) return prefix;
        var out = new ArrayList<>(prefix.segments);
        out.addAll(segments);
        return new NamePath(out);
    }

    /** Whether this is the root: no segments. */
    public boolean isEmpty() { return segments.isEmpty(); }
    /** How many segments it has. */
    public int     depth()   { return segments.size(); }

    /** The {@code '/'}-joined wire form; {@code ""} for the root. */
    public String wire() {
        var sb = new StringBuilder();
        for (NodeName s : segments) {
            if (sb.length() > 0) sb.append(NodeName.SEPARATOR);
            sb.append(s.value());
        }
        return sb.toString();
    }

    @Override public String toString() { return wire(); }
}
