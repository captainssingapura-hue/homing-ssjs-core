package hue.captains.singapura.js.homing.site;

import hue.captains.singapura.tao.ontology.Scalar;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0066 Episode 3 — the path a request arrived on, as the segments between
 * its slashes and nothing else.
 *
 * <p>This is what a {@link Router} takes in. It knows no mount, no root word
 * and no node vocabulary: {@code /cat/rfcs/rfc0051} is three plain segments
 * here, and whether the first is a prefix the router owns or a page of its
 * own is the router's reading, not the path's. The studio's {@code
 * CataloguePath} is a path that has been read that way already.</p>
 *
 * <p>Segments are stored decoded and rendered encoded, so a segment holding a
 * space or a slash round-trips: {@code parse(p.toString()).equals(p)}.</p>
 *
 * @param segments the decoded segments, in order; empty for the root
 */
public record Path(List<String> segments) implements Scalar {

    /** The site's front door: no segments at all. */
    public static final Path ROOT = new Path(List.of());

    public Path {
        Objects.requireNonNull(segments, "Path.segments");
        for (String s : segments) {
            if (s == null || s.isEmpty()) {
                throw new IllegalArgumentException("Path segment must not be empty: " + segments);
            }
        }
        segments = List.copyOf(segments);
    }

    /** {@code new Path("a", "b")} is {@code /a/b}. */
    public Path(String... segments) {
        this(List.of(segments));
    }

    /** Whether this is the root: no segments. */
    public boolean isRoot() { return segments.isEmpty(); }
    /** How many segments it has. */
    public int     depth()  { return segments.size(); }

    /** The first segment, when there is one. */
    public Optional<String> head() {
        return segments.isEmpty() ? Optional.empty() : Optional.of(segments.get(0));
    }

    /** Everything after the head; the root's tail is the root. */
    public Path tail() {
        return segments.isEmpty() ? ROOT : new Path(segments.subList(1, segments.size()));
    }

    /** This path with one more segment on the end. */
    public Path child(String segment) {
        var out = new ArrayList<>(segments);
        out.add(segment);
        return new Path(out);
    }

    /** This path with {@code other}'s segments on the end. */
    public Path plus(Path other) {
        if (other.segments.isEmpty()) return this;
        var out = new ArrayList<>(segments);
        out.addAll(other.segments);
        return new Path(out);
    }

    /**
     * The rest of this path below {@code prefix}, or empty when this path does
     * not start with it. {@code /cat/a/b} under {@code /cat} is {@code /a/b};
     * anything under {@link #ROOT} is itself.
     */
    public Optional<Path> under(Path prefix) {
        int n = prefix.segments.size();
        if (n > segments.size() || !segments.subList(0, n).equals(prefix.segments)) return Optional.empty();
        return Optional.of(n == 0 ? this : new Path(segments.subList(n, segments.size())));
    }

    /** The address form: a leading slash, each segment percent-encoded. */
    @Override
    public String toString() {
        if (segments.isEmpty()) return "/";
        var sb = new StringBuilder();
        for (String s : segments) sb.append('/').append(encode(s));
        return sb.toString();
    }

    private String encode(String s) {
        // URLEncoder is a form encoder: a space becomes a plus, which a path
        // reader takes literally. Put the space back as %20, which is what a
        // path means by it.
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

}
