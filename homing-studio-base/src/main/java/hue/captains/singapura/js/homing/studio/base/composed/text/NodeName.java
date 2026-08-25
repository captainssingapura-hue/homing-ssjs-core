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

    /** Target length for {@link #conciseSlug}; well under {@link #MAX_CHARS}. */
    public static final int CONCISE_CHARS = 32;

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
    public static NodeName conciseSlug(String label) {
        String s = slug(label).value();
        if (s.length() <= CONCISE_CHARS) return new NodeName(s);
        String cut = s.substring(0, CONCISE_CHARS);
        int lastDash = cut.lastIndexOf('-');
        // Keep the hard clip when the first "word" alone is already too long —
        // better a truncated token than falling back to the empty string.
        if (lastDash > 0) cut = cut.substring(0, lastDash);
        cut = cut.replaceAll("-+$", "");
        return new NodeName(cut.isBlank() ? s.substring(0, CONCISE_CHARS) : cut);
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
    public static NodeName ofType(Class<?> cls, String suffix) {
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
