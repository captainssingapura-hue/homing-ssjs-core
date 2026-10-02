package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * What a doc is shown as: a body, and the media type that says how to read it.
 *
 * <p>Content is made <b>from</b> a doc, by a {@code Function<Doc, Content>} - never by the
 * doc. A doc is data: its identity, its words, its structure. How it travels is the
 * business of whoever shows it - a viewer's wire format is one such function, an
 * export another, a search index a third - and each is free to differ without the doc
 * knowing. A doc whose data is already text (markdown, an SVG) hands that text over
 * through {@link Doc#contents()}; one whose data is structure (a composed doc, a rigid
 * tree, a table) has no text of its own to hand over, and says so.</p>
 *
 * @param body      the bytes, as text
 * @param mediaType what they are, e.g. {@code text/markdown; charset=utf-8}
 */
public record Content(String body, String mediaType) implements ValueObject {

    public Content {
        Objects.requireNonNull(body,      "Content.body");
        Objects.requireNonNull(mediaType, "Content.mediaType");
    }
}
