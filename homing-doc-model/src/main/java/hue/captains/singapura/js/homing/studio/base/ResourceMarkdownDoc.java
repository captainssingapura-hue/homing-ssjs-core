package hue.captains.singapura.js.homing.studio.base;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * A markdown {@link Doc} loaded from the classpath at an explicit path, rather than at the
 * conventional location implied by the record's class name (see {@link ClasspathMarkdownDoc}).
 *
 * <p>Use this when the markdown file's location is fixed by an external constraint —
 * e.g. third-party content shipped under a specific path, or generated content placed by
 * a build step.</p>
 *
 * <p>See the {@link ClasspathMarkdownDoc} Javadoc for the rationale behind preferring
 * {@code ComposedDoc} for new docs.</p>
 *
 * @since RFC 0004
 * @deprecated Prefer {@code ComposedDoc} for new docs. This interface remains supported
 *             for cases where the markdown bytes genuinely live at an external-fixed
 *             path; no removal date.
 */
@Deprecated
public interface ResourceMarkdownDoc extends Doc {

    /** Explicit classpath path for this Doc's bytes. */
    String resourcePath();

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
