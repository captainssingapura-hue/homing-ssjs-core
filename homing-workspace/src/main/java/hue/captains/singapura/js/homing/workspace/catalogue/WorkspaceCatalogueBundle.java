package hue.captains.singapura.js.homing.workspace.catalogue;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Drop-in bundler that returns the complete JS source for the RFC 0031
 * V1 workspace catalogue store as a single concatenated string, ready
 * for inclusion in a workspace chrome's {@code bodyJs()}.
 *
 * <p>The bundle contains just one file:</p>
 * <ol>
 *   <li>{@code WorkspaceCatalogueStore.js} — IndexedDB wrapper
 *       conforming structurally to {@link
 *       hue.captains.singapura.js.homing.workspace.catalogue.contract.WorkspaceCatalogue}.</li>
 * </ol>
 *
 * @since RFC 0031 V1 — multi-instance workspace support.
 */
public final class WorkspaceCatalogueBundle {

    private WorkspaceCatalogueBundle() {}

    public static String allJs() { return BUNDLE; }

    private static final String BUNDLE = build();

    private static String build() {
        var sb = new StringBuilder();
        sb.append("// ─── RFC 0031 V1 workspace catalogue bundle ──────────────────────\n");
        appendResource(sb, "WorkspaceCatalogueStore.js");
        return sb.toString();
    }

    private static void appendResource(StringBuilder sb, String fileName) {
        String path = "/homing/js/hue/captains/singapura/js/homing/workspace/catalogue/" + fileName;
        try (InputStream in = WorkspaceCatalogueBundle.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "WorkspaceCatalogueBundle: missing classpath resource " + path);
            }
            sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            sb.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
