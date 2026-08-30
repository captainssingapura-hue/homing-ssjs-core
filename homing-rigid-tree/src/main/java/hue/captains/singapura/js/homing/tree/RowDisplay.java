package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What one tree ROW shows, and how its host dispatches on it (RFC 0053) — the
 * contract between the generic renderer and the widget that embeds it.
 *
 * <p>This is what replaces the {@code DimensionKey} / {@code DimensionValue}
 * vocabulary, and the difference is what is being named. Six sealed keys were a
 * guess at <i>what trees are</i> — "axes a tree can be labelled, grouped, or
 * pivoted on". The pivot never arrived, no consumer ever grouped by anything,
 * and the open value side produced four trivial string wrappers with a single
 * reader. These four fields are a fact about <i>what a row does</i>: it draws a
 * label, optionally a badge and a note, and it tells its host what kind of thing
 * was selected.</p>
 *
 * <p><b>It is not a family's display type.</b> {@code Illustration} belongs to a
 * catalogue listing and carries an icon this has no use for; a crate's answer
 * carries a module form and an FQCN. Each family keeps its own, richly typed,
 * and PROJECTS into this to be drawn. The substrate never learns any of
 * them.</p>
 *
 * <p><b>Why {@code kind} is here</b> and not left to the family: it is a
 * discriminator hosts already dispatch on before they know anything else about
 * a node — the workspace doc pane refuses to render a {@code 'catalogue'} and
 * looks the rest up in a renderable table. That is a renderer-level concern, so
 * it belongs in the renderer-level contract. It is a free-form tag, never a
 * domain type.</p>
 *
 * @param label the row's text — the one field a row cannot do without
 * @param badge short uppercase tag before the label; empty when there is none
 * @param note  one line beside the label; empty when there is none
 * @param kind  the host's dispatch tag; empty when the family has nothing to say
 * @since RFC 0053
 */
public record RowDisplay(String label, String badge, String note, String kind)
        implements ValueObject {

    public RowDisplay {
        Objects.requireNonNull(label, "RowDisplay.label");
        badge = badge == null ? "" : badge;
        note  = note  == null ? "" : note;
        kind  = kind  == null ? "" : kind;
    }

    /** A row that only has something to say about itself. */
    public static RowDisplay of(String label) {
        return new RowDisplay(label, "", "", "");
    }
}
