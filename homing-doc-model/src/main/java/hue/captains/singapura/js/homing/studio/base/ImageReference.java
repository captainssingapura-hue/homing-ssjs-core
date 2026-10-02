package hue.captains.singapura.js.homing.studio.base;

/**
 * Reference to a classpath-shipped image. Renders in the References section as a placeholder
 * block in v1 (per RFC 0004-ext1 §4.6 — image rendering itself is deferred to a sibling RFC
 * that adds the {@code /asset} endpoint required to serve the bytes).
 *
 * @param name         short anchor key cited in markdown as {@code #ref:<name>}
 * @param resourcePath classpath-relative path to the image bytes (e.g. {@code "docs/diagrams/architecture.png"})
 * @param alt          alternative text for accessibility
 * @param caption      figcaption shown beneath the image
 * @since RFC 0004-ext1
 */
public record ImageReference(String name, String resourcePath, String alt, String caption)
        implements Reference {}
