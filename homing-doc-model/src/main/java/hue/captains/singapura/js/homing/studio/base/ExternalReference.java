package hue.captains.singapura.js.homing.studio.base;

/**
 * Reference to an external web URL. Renders in the References section as a card with the
 * label (linked to the URL with {@code target="_blank" rel="noopener"}) and description.
 *
 * @param name        short anchor key cited in markdown as {@code #ref:<name>}
 * @param url         the external URL
 * @param label       display label (used as the card heading and link text)
 * @param description short paragraph explaining the citation; may be empty
 * @since RFC 0004-ext1
 */
public record ExternalReference(String name, String url, String label, String description)
        implements Reference {}
