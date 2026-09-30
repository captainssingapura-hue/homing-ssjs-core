package hue.captains.singapura.js.homing.studio.base;

/**
 * A {@link Doc} whose contents are provided directly — typically a Java text block —
 * rather than loaded from a classpath resource. No companion {@code .md} file.
 *
 * <p>Useful for changelog snippets, status notices, fixtures, and generated content. The
 * implementer is required to override {@link #contents()}; everything else inherits from
 * {@link Doc}'s defaults.</p>
 *
 * <p>See the {@link ClasspathMarkdownDoc} Javadoc for the rationale behind preferring
 * {@code ComposedDoc} for new docs. For inline content specifically, a {@code ComposedDoc}
 * with a single {@code MarkdownSegment} (or {@code TextSegment}) covers the same use case
 * with typed references and segment composition available when needed.</p>
 *
 * @since RFC 0004
 * @deprecated Prefer {@code ComposedDoc} (with a single {@code MarkdownSegment} for the
 *             same one-shot inline use case) for new docs. This interface remains supported
 *             for existing content; no removal date.
 */
@Deprecated
public interface InlineDoc extends Doc {

    @Override
    String contents();
}
