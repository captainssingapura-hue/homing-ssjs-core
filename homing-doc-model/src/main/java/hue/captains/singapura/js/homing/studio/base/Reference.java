package hue.captains.singapura.js.homing.studio.base;

/**
 * A typed cross-reference declared by a {@link Doc} and rendered in the DocReader's
 * "References" section beneath the markdown body.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0004Ext1Doc.md">RFC 0004-ext1</a>,
 * every out-of-document link in markdown content is a managed Reference. The markdown body
 * cites each reference using a normal anchor link of the form {@code [label](#ref:<name>)},
 * where {@code <name>} matches a declared reference's {@link #name()}. The renderer emits
 * the References section with a stable {@code id="ref:<name>"} per entry; the browser
 * handles fragment navigation natively — no DOM walking, no href substitution.</p>
 *
 * <p>Sealed to three subtypes:</p>
 * <ul>
 *   <li>{@link DocReference} — cross-reference to another typed Doc.</li>
 *   <li>{@link ExternalReference} — external web URL with a label and description.</li>
 *   <li>{@link ImageReference} — classpath-shipped image (rendering deferred per RFC 0004-ext1 §4.6).</li>
 * </ul>
 *
 * @since RFC 0004-ext1
 */
public sealed interface Reference
        permits DocReference, ExternalReference, ImageReference {

    /**
     * Short kebab-case anchor key. Cited in markdown as {@code #ref:<name>} and emitted
     * as {@code id="ref:<name>"} on the rendered References-section element. Must be
     * unique within a single Doc's {@link Doc#references()} list.
     */
    String name();
}
