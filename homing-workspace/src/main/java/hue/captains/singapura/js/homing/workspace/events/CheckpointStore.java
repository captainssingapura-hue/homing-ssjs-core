package hue.captains.singapura.js.homing.workspace.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Drop-in bundler that returns the complete JS source for the RFC 0034
 * checkpoint store as a single concatenated string, ready for inclusion
 * in a workspace chrome's {@code bodyJs()}.
 *
 * <p>P1 — main-thread cadence-driven writes. P2 will add a Web Worker for
 * the LMAX consumer-thread shape; the store API exposed to chromes stays
 * the same.</p>
 *
 * <p>The bundle contains just one file:</p>
 * <ol>
 *   <li>{@code CheckpointStore.js} — IndexedDB wrapper (read / write / clear),
 *       keyed by composite key {@code (kind, workspaceId)}; one row per
 *       workspace.</li>
 * </ol>
 *
 * <h2>Usage in a workspace chrome's {@code bodyJs()}</h2>
 *
 * <pre>{@code
 * return List.of(
 *     CheckpointStore.allJs(),
 *     "// rest of chrome body...",
 *     "var checkpointStore = createCheckpointStore();",
 *     "checkpointStore.read('AnimalsPlayground', wsId).then(function (cp) { ... });"
 * );
 * }</pre>
 *
 * @since RFC 0034 P1 — main-thread cadence-driven checkpoint store.
 */
public final class CheckpointStore {

    private CheckpointStore() {}

    /**
     * The complete checkpoint-store JS bundle as a single string. Cached on
     * first call — the bundle is pure resource concatenation and deterministic.
     */
    public static String allJs() {
        return BUNDLE;
    }

    private static final String BUNDLE = build();

    private static String build() {
        var sb = new StringBuilder();
        sb.append("// ─── RFC 0034 checkpoint store bundle ──────────────────────────────\n");
        appendResource(sb, "CheckpointStore.js");
        return sb.toString();
    }

    private static void appendResource(StringBuilder sb, String fileName) {
        // Resources live alongside this Java class at
        // /homing/js/hue/captains/singapura/js/homing/workspace/events/
        String path = "/homing/js/hue/captains/singapura/js/homing/workspace/events/" + fileName;
        try (InputStream in = CheckpointStore.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "CheckpointStore: missing classpath resource " + path);
            }
            sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            sb.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
