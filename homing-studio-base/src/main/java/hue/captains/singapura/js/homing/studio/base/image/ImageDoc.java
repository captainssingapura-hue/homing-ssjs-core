package hue.captains.singapura.js.homing.studio.base.image;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocId;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * RFC 0020 — first-class Doc subtype carrying a classpath-shipped raster
 * image. Kind = {@code "image"}; viewer is {@code ImageViewer} (extends
 * {@link hue.captains.singapura.js.homing.studio.base.app.DocViewer DocViewer}).
 *
 * <p>Raw tier per RFC 0017 — no theming attempted on the raster itself;
 * chrome around it (figure, caption, border) is themed via the framework's
 * tokens.</p>
 *
 * <p>Identity is deterministic by classpath path (same shape as
 * {@link hue.captains.singapura.js.homing.studio.base.SvgDoc SvgDoc}'s
 * derivation), so the same {@code resourcePath} always produces the same
 * Doc UUID across rebuilds.</p>
 *
 * <p><b>Content shape on the wire</b> — {@link #contents()} returns a JSON
 * envelope:</p>
 * <pre>{
 *   "alt":      "...",
 *   "caption":  "...",      // empty if absent
 *   "width":    400,        // present only if supplied
 *   "height":   300,        // present only if supplied
 *   "dataUrl":  "data:image/png;base64,…"
 * }</pre>
 *
 * <p>The PoC inlines the bytes as a base64 data URL — no new server
 * endpoint required, consistent with how TableDoc and ComposedDoc serve
 * structured JSON. Intended for diagrams, screenshots, and small artwork.
 * A future {@code /asset} endpoint would let very large rasters opt out
 * of inlining; the JSON contract can carry both shapes when that lands.</p>
 *
 * @param resourcePath classpath-relative path to the image bytes
 * @param mimeType     declared MIME type (e.g. {@code "image/png"})
 * @param alt          required alt text (accessibility)
 * @param caption      optional caption shown beneath the image
 * @param width        optional intrinsic width in pixels (for layout-shift-free rendering)
 * @param height       optional intrinsic height in pixels
 *
 * @since RFC 0020
 */
public record ImageDoc(
        String resourcePath,
        String mimeType,
        String alt,
        String caption,
        Optional<Integer> width,
        Optional<Integer> height
) implements Doc {

    public ImageDoc {
        Objects.requireNonNull(resourcePath, "ImageDoc.resourcePath");
        Objects.requireNonNull(mimeType,     "ImageDoc.mimeType");
        Objects.requireNonNull(alt,          "ImageDoc.alt (accessibility — required)");
        Objects.requireNonNull(width,        "ImageDoc.width (use Optional.empty)");
        Objects.requireNonNull(height,       "ImageDoc.height (use Optional.empty)");
        if (caption == null) caption = "";
        if (resourcePath.isBlank()) {
            throw new IllegalArgumentException("ImageDoc.resourcePath must not be blank");
        }
        if (alt.isBlank()) {
            throw new IllegalArgumentException(
                    "ImageDoc.alt must not be blank — accessibility text is required");
        }
    }

    /** Convenience — no caption, no intrinsic dimensions. */
    public static ImageDoc of(String resourcePath, String mimeType, String alt) {
        return new ImageDoc(resourcePath, mimeType, alt, "", Optional.empty(), Optional.empty());
    }

    /** Convenience — with caption, no intrinsic dimensions. */
    public static ImageDoc of(String resourcePath, String mimeType, String alt, String caption) {
        return new ImageDoc(resourcePath, mimeType, alt, caption,
                Optional.empty(), Optional.empty());
    }

    @Override public UUID uuid() {
        return UUID.nameUUIDFromBytes(
                ("image:" + resourcePath).getBytes(StandardCharsets.UTF_8));
    }

    @Override public DocId  id()            { return new DocId.ByUuid(uuid()); }
    @Override public String title()         { return alt; }
    @Override public String summary()       { return caption; }
    @Override public String category()      { return "IMAGE"; }
    @Override public String kind()          { return "image"; }
    @Override public String contentType()   { return "application/json; charset=utf-8"; }
    @Override public String fileExtension() { return ""; }

    @Override public String contents() {
        // Resolve at request time — class-loader chain reads from the
        // classpath ClassLoader (works for both maven test/run and jar/war
        // deployments). Failure surfaces as an IllegalStateException;
        // DocGetAction translates exceptions to a 404 with the message.
        byte[] bytes;
        try (InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException(
                        "ImageDoc resource not found on classpath: " + resourcePath);
            }
            bytes = in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "ImageDoc resource read failed: " + resourcePath, e);
        }
        String b64 = Base64.getEncoder().encodeToString(bytes);

        var sb = new StringBuilder("{");
        sb.append("\"alt\":").append(jstr(alt)).append(',');
        sb.append("\"caption\":").append(jstr(caption)).append(',');
        width.ifPresent(w  -> sb.append("\"width\":").append(w).append(','));
        height.ifPresent(h -> sb.append("\"height\":").append(h).append(','));
        sb.append("\"dataUrl\":\"data:").append(mimeType).append(";base64,").append(b64).append("\"");
        sb.append("}");
        return sb.toString();
    }

    private static String jstr(String v) {
        var sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
