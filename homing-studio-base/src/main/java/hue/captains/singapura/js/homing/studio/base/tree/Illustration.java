package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a listing's vertex looks like — the display projection, and the whole of it
 * (RFC 0053).
 *
 * <p>These five fields were carried as {@code dimensions} on a catalogue's
 * {@code NormalizedNode}s: presentation living inside structure, in a substrate
 * whose claim is that it knows nothing about display. They belong on the answer a
 * resolver gives, not on the node a walk steps through — which is why this type
 * is in {@code studio-base} and not in {@code homing-rigid-tree}.</p>
 *
 * <p>Five is what a <i>listing</i> shows, not what every tree node has — the four
 * doc normalizers only ever carried a label. This is the catalogue family's
 * projection, which is what {@link ListingDetails} is named for.</p>
 *
 * <p><b>The label is supplied, never derived.</b> A segment is a lossy slug of a
 * label — em-dashes, word order and qualifiers all fall out — and measurement over
 * the live studio settles it: of 223 vertices, 213 labels cannot be reconstructed
 * from their segment. {@code doc-ontology} humanises to "Doc Ontology" where the
 * label is "Ontology — Doc". So the label travels here rather than riding on
 * {@link hue.captains.singapura.js.homing.tree.NodeName}, which is the key
 * children are looked up by and must stay exactly the address.</p>
 *
 * <p>Strict, though: one label per position, because a vertex has one identity and
 * an identity resolves to one answer. There is no per-placement override, which is
 * what stops a page authoring a claim its path does not support.</p>
 *
 * @param label   the human wording — what a crumb or a card title shows
 * @param summary one line beneath it; empty when there is none
 * @param badge   the category chip; empty when there is none
 * @param icon    an optional glyph; empty when there is none
 * @param kind    what sort of thing this is, for per-kind styling and viewers
 * @since RFC 0053
 */
public record Illustration(String label, String summary, String badge,
                           String icon, String kind) implements ValueObject {

    public Illustration {
        Objects.requireNonNull(label, "Illustration.label");
        if (summary == null) summary = "";
        if (badge   == null) badge   = "";
        if (icon    == null) icon    = "";
        if (kind    == null) kind    = "";
    }

    /** The common case: a label and a summary, with no chrome. */
    public static Illustration of(String label, String summary) {
        return new Illustration(label, summary, "", "", "");
    }
}
