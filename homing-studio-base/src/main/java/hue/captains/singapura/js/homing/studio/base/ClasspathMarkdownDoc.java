package hue.captains.singapura.js.homing.studio.base;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A markdown {@link Doc} whose {@code .md} file lives next to its record class on the
 * classpath, under the conventional {@code docs/} resource prefix.
 *
 * <p>The default {@link #resourcePath()} is derived from the record's fully-qualified
 * class name. A Doc record at:</p>
 *
 * <pre>com.example.studio.docs.blocks.AtomsDoc</pre>
 *
 * <p>expects its companion file at:</p>
 *
 * <pre>resources/docs/com/example/studio/docs/blocks/AtomsDoc.md</pre>
 *
 * <p>Renaming or moving the record renames or moves the file in lock-step (most IDEs offer
 * to refactor the matching resource when the resource sits in the conventional layout).</p>
 *
 * <p>Override {@link #resourcePath()} only when the convention doesn't fit; for that case,
 * {@link ResourceMarkdownDoc} is usually the right interface to implement instead — it makes
 * the explicit-path nature visible at the type level.</p>
 *
 * <h2>Deprecated — prefer {@code ComposedDoc}</h2>
 *
 * <p>{@code ComposedDoc} (introduced alongside RFC 0024 and matured through the segment
 * ontology — {@code TextSegment}, {@code MarkdownSegment}, {@code CodeSegment},
 * {@code SvgSegment}, {@code TableSegment}, {@code ComposedSegment}) supersedes the
 * prose-only doc kinds. It addresses the structural limits this interface carries:</p>
 *
 * <ul>
 *   <li><b>Typed References resolve at compile time.</b> Citations like
 *       {@code [label](#ref:doc-foo)} flow through the {@code References} list on the
 *       containing record; the compiler refuses references to missing docs. The .md path
 *       is anchor-string-typed; broken references surface only at runtime when the user
 *       clicks.</li>
 *   <li><b>Segments compose.</b> SVG diagrams, code blocks with language tags, embedded
 *       tables, and nested doc references are first-class segment kinds — not
 *       reflow-prone HTML escapes scattered through a prose body.</li>
 *   <li><b>The doc is its own data.</b> A ComposedDoc record IS the document — no
 *       sidecar file to drift out of sync, no resource-loading path to forget. Tests
 *       can assert against doc structure as typed values.</li>
 *   <li><b>The export, navigation, and TOC paths converge.</b> The {@code ComposedWidget}
 *       chrome owns the export pill, fragment-correct hrefs, and TOC sidebar; per-Doc
 *       behaviour stays declarative.</li>
 * </ul>
 *
 * <p>The .md-based doc kinds remain supported for existing content and for cases where
 * external markdown sources are the natural form. New docs should be authored as
 * {@code ComposedDoc} records; existing .md docs may migrate at the author's discretion.
 * The framework will not remove these interfaces in the foreseeable future.</p>
 *
 * @since RFC 0004
 * @deprecated Prefer {@code ComposedDoc} for new docs.
 *             {@code ComposedDoc} carries typed references, segment composition,
 *             and a single source of truth (the record itself, not record + .md file).
 *             This interface remains supported for existing content; no removal date.
 */
@Deprecated
public interface ClasspathMarkdownDoc extends Doc {

    /**
     * Default classpath path for this Doc's bytes: {@code docs/<package-as-path>/<SimpleName>.md}.
     * Override only if the file genuinely cannot live in the conventional location.
     */
    default String resourcePath() {
        return "docs/" + getClass().getName().replace('.', '/') + fileExtension();
    }

    @Override
    default String contents() {
        String path = resourcePath();
        ClassLoader loader = getClass().getClassLoader();
        try (var in = loader.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Doc " + getClass().getName() + " missing classpath resource: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
