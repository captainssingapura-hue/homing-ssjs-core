package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RFC 0051 — a position in the catalogue tree, as a sequence of typed
 * segments below the literal root.
 *
 * <p>A path is the tree spelled out: the root studio's segment, then one per
 * rung, ending either at a catalogue or at a leaf. Because Law 2 makes
 * sibling segments distinct and Law 1 gives a being at most one position, a
 * path names at most one thing and a thing has exactly one path — which is
 * what lets the URL and the breadcrumb be the same statement.</p>
 *
 * <p>Segments are {@link NodeName}s rather than strings, so the character set
 * and length are settled at construction and a path can be joined into a URL
 * without escaping — {@code /} is reserved as the separator by the type.</p>
 */
public record CataloguePath(List<NodeName> segments) implements ValueObject {

    /**
     * The literal first segment of every catalogue URL.
     *
     * <p>It exists so that a catalogue path is recognisable as one from its
     * first character, rather than competing with every other top-level route
     * the server might ever want. The studio's own root segment follows it,
     * which is why an umbrella deployment stays unambiguous.</p>
     */
    public static final String ROOT = "cat";

    public CataloguePath {
        Objects.requireNonNull(segments, "CataloguePath.segments");
        for (NodeName s : segments) {
            if (s == null) throw new IllegalArgumentException("CataloguePath has a null segment");
        }
        segments = List.copyOf(segments);
    }

    public static CataloguePath of(List<NodeName> segments) {
        return new CataloguePath(segments);
    }

    /** This path with one more segment on the end. */
    public CataloguePath then(NodeName segment) {
        var out = new ArrayList<>(segments);
        out.add(Objects.requireNonNull(segment, "segment"));
        return new CataloguePath(out);
    }

    public boolean isEmpty()  { return segments.isEmpty(); }
    public int     depth()    { return segments.size(); }

    /** The URL for this position: {@code /cat/studio/meta/ontology}. */
    public String toUrl() {
        var sb = new StringBuilder("/").append(ROOT);
        for (NodeName s : segments) sb.append(NodeName.SEPARATOR).append(s.value());
        return sb.toString();
    }

    /**
     * Read a path back out of a URL. The {@code cat} root is required, so
     * {@code /cat} alone gives an empty path — which resolves to the root
     * catalogue itself.
     *
     * <p>Returns null when the input is not a catalogue path at all — a
     * different route, or a segment that is not a legal {@link NodeName}. Null
     * rather than an exception because parsing a URL is reading UNTRUSTED
     * input: a request for {@code /cat/../etc} is a miss to answer, not a
     * server error to raise. Nothing here ever touches the filesystem, but a
     * segment that could not be a {@link NodeName} is refused before it
     * reaches a lookup, so traversal-shaped input dies at the type.</p>
     */
    public static CataloguePath parse(String url) {
        if (url == null) return null;
        String s = url.trim();
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q);
        while (s.startsWith("/")) s = s.substring(1);
        while (s.endsWith("/"))   s = s.substring(0, s.length() - 1);
        if (s.equals(ROOT)) return new CataloguePath(List.of());
        if (!s.startsWith(ROOT + NodeName.SEPARATOR)) return null;
        s = s.substring(ROOT.length() + 1);
        if (s.isEmpty()) return new CataloguePath(List.of());

        var out = new ArrayList<NodeName>();
        for (String part : s.split(String.valueOf(NodeName.SEPARATOR))) {
            if (part.isEmpty()) return null;          // "//" — not a path we mint
            try {
                out.add(new NodeName(part));
            } catch (IllegalArgumentException notASegment) {
                return null;                          // ".." and friends land here
            }
        }
        return new CataloguePath(out);
    }

    @Override public String toString() { return toUrl(); }
}
