package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/** Names for nodes, made from what they stand for ({@link NodeName}). */
public record NodeNames() implements StatelessFunctionalObject {

    public static final NodeNames INSTANCE = new NodeNames();

    /**
     * Derive a {@code NodeName} from an arbitrary label — lowercased, every run of
     * non-{@code [a-z0-9]} folded to a single {@code '-'}, trimmed, and clipped to
     * {@value NodeName#MAX_CHARS}. A label that reduces to nothing yields {@code "n"}. Handy
     * when mirroring a source whose nodes carry human names rather than ids;
     * callers must still guarantee sibling-uniqueness (the normalizer enforces it).
     */
    public NodeName slug(String label) {
        if (label == null) return new NodeName("n");
        String s = label.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+)|(-+$)", "");
        if (s.isBlank()) s = "n";
        if (s.length() > NodeName.MAX_CHARS) s = s.substring(0, NodeName.MAX_CHARS).replaceAll("-+$", "");
        if (s.isBlank()) s = "n";
        return new NodeName(s);
    }

    /** Target length for {@link #conciseSlug}; well under {@link NodeName#MAX_CHARS}. */
    private int conciseChars() { return 32; }

    /**
     * Like {@link #slug}, but clipped at a word boundary rather than mid-word:
     * a long title yields {@code "rfc-0037-color-palettes-as"} instead of
     * {@code "rfc-0037-color-palettes-as-typed-tree-structured"}. RFC 0051 uses
     * this for path segments derived from prose titles, where the full text is
     * both unwieldy in a URL and no more distinguishing than its opening words.
     *
     * <p>Shortening trades away some distinguishing power, so it can collide
     * where the full form would not. That is deliberate rather than risky: the
     * boot-time sibling-uniqueness check is the backstop, and it names both
     * claimants so the author can override one. A silent collision is the thing
     * being prevented; a loud one is a fine cost for readable URLs.</p>
     */
    public NodeName conciseSlug(String label) {
        String s = slug(label).value();
        if (s.length() <= conciseChars()) return new NodeName(s);
        String cut = s.substring(0, conciseChars());
        int lastDash = cut.lastIndexOf('-');
        // Keep the hard clip when the first "word" alone is already too long —
        // better a truncated token than falling back to the empty string.
        if (lastDash > 0) cut = cut.substring(0, lastDash);
        cut = cut.replaceAll("-+$", "");
        return new NodeName(cut.isBlank() ? s.substring(0, conciseChars()) : cut);
    }

    /**
     * Derive a {@code NodeName} from a type, dropping a trailing role suffix:
     * {@code DoctrineCatalogue} with suffix {@code "Catalogue"} yields
     * {@code "doctrine"}. RFC 0051 uses this for URL path segments, where
     * deriving from the class rather than the display label keeps a path
     * stable when someone rewords a heading.
     *
     * <p>The suffix is dropped only when something would remain — a class
     * named exactly {@code Catalogue} keeps its name rather than reducing to
     * nothing. Anonymous classes have no simple name and fall back to the
     * binary name, which is ugly but deterministic and per-class distinct;
     * anything user-facing should override rather than rely on that.</p>
     */
    public NodeName ofType(Class<?> cls, String suffix) {
        if (cls == null) return new NodeName("n");
        String s = cls.getSimpleName();
        if (s.isBlank()) s = cls.getName();
        if (suffix != null && !suffix.isEmpty()
                && s.length() > suffix.length() && s.endsWith(suffix)) {
            s = s.substring(0, s.length() - suffix.length());
        }
        // Split camel humps before slugging, or every class name collapses to
        // one run-together token: DocTreeOntology would read "doctreeontology"
        // in the URL bar. Also splits the acronym-to-word boundary, so
        // HtmlDocView gives "html-doc-view" rather than "htmld-oc-view".
        s = s.replaceAll("(?<=[a-z0-9])(?=[A-Z])", "-")
             .replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", "-");
        return slug(s);
    }

    /** A node's segment from its index path: its last index, or {@code doc} for the root. */
    public NodeName indexSegment(List<Integer> path) {
        return path.isEmpty()
                ? new NodeName("doc")
                : new NodeName(String.valueOf(path.get(path.size() - 1)));
    }
}
