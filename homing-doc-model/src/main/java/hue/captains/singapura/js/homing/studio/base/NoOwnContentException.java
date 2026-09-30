package hue.captains.singapura.js.homing.studio.base;

/**
 * Thrown by {@link Doc#contents()} (and {@link Doc#contentType()}) on a doc whose data is
 * structure, not text - a composed doc, a rigid tree, a table, an image. Such a doc has
 * no bytes of its own to hand over; how it is shown is made from it by a
 * {@code Function<Doc, Content>}, which is the shower's to choose. In the studio, the
 * legacy viewers' JSON is made by {@code LegacyDocWire}.
 */
public final class NoOwnContentException extends UnsupportedOperationException {

    public NoOwnContentException(Doc doc) {
        super(doc.getClass().getSimpleName() + " '" + doc.title() + "' has no content of its own: its data is"
                + " its structure. Make content from it with a Function<Doc, Content>"
                + " (the studio's legacy viewers use LegacyDocWire).");
    }
}
