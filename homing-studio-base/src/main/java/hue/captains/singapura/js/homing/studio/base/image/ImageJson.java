package hue.captains.singapura.js.homing.studio.base.image;

import java.util.Base64;

/**
 * An image as the image viewers read it: its alt text, caption, intrinsic size when
 * given, and its bytes inlined as a base64 data URL. The legacy wire's, made from the
 * doc; the doc knows nothing of it.
 */
public final class ImageJson {

    private ImageJson() {}

    /** The envelope for {@code doc}; its bytes read now. */
    public static String write(ImageDoc doc) {
        String b64 = Base64.getEncoder().encodeToString(doc.bytes());
        var sb = new StringBuilder("{");
        sb.append("\"alt\":").append(jstr(doc.alt())).append(',');
        sb.append("\"caption\":").append(jstr(doc.caption())).append(',');
        doc.width().ifPresent(w  -> sb.append("\"width\":").append(w).append(','));
        doc.height().ifPresent(h -> sb.append("\"height\":").append(h).append(','));
        sb.append("\"dataUrl\":\"data:").append(doc.mimeType()).append(";base64,").append(b64).append("\"");
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
