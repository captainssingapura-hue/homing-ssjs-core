package hue.captains.singapura.js.homing.workspace.events;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Drop-in bundler that returns the complete JS source for the RFC 0030
 * workspace event log as a single concatenated string, ready for inclusion
 * in a workspace chrome's {@code bodyJs()}.
 *
 * <p>Phase 1 — record-only. The chrome calls {@code WorkspaceEventLog.attach(...)}
 * once per workspace instance with a {@code (workspaceKind, workspaceId)} pair,
 * then {@code emit(name, payload)} on every state-affecting action. Events
 * append to IndexedDB under a per-instance composite key; recovery still
 * uses RFC 0029 snapshots through Phase 2. Replay-from-events lands in
 * Phase 2 once we've verified the event vocabulary covers what the chrome
 * actually does.</p>
 *
 * <p>The bundle contains, in dependency order:</p>
 * <ol>
 *   <li>{@code EventLogStore.js} — IndexedDB wrapper (append + range query
 *       + clear), keyed by {@code (kind, workspaceId, seq)}.</li>
 *   <li>{@code WorkspaceEventLog.js} — high-level facade that binds the
 *       {@code (kind, workspaceId)} pair once and exposes {@code emit / query /
 *       clear}.</li>
 * </ol>
 *
 * <h2>Usage in a workspace chrome's {@code bodyJs()}</h2>
 *
 * <pre>{@code
 * return List.of(
 *     WorkspaceEventLog.allJs(),       // prepend the bundle
 *     "// rest of chrome body...",
 *     "var eventLog = WorkspaceEventLog.attach({",
 *     "    workspaceKind: 'AnimalsPlayground',",
 *     "    workspaceId:   computePlaceholderInstanceUuid('AnimalsPlayground')",
 *     "});",
 *     "eventLog.emit('TabAdded', { paneId: 'tl', tabId: '...', widgetKind: '...' });",
 *     ...
 * );
 * }</pre>
 *
 * <p>Per-workspace scoping aligns with RFC 0031 from day one — events are
 * already segregated by {@code (kind, workspaceId)} so the multi-workspace
 * promotion is a no-op at the storage layer. Single-workspace use today
 * passes a deterministic placeholder UUID derived from the kind name.</p>
 *
 * @since RFC 0030 Phase 1 — record-only event log.
 */
public final class WorkspaceEventLog {

    private WorkspaceEventLog() {}

    /**
     * The complete event-log JS bundle as a single string. Cached on first
     * call — the bundle is pure resource concatenation and deterministic.
     */
    public static String allJs() {
        return BUNDLE;
    }

    private static final String BUNDLE = build();

    private static String build() {
        var sb = new StringBuilder();
        sb.append("// ─── RFC 0030 event log bundle ────────────────────────────────────\n");
        appendResource(sb, "EventLogStore.js");
        appendResource(sb, "WorkspaceEventLog.js");
        return sb.toString();
    }

    private static void appendResource(StringBuilder sb, String fileName) {
        // Resources live alongside this Java class at
        // /homing/js/hue/captains/singapura/js/homing/workspace/events/
        String path = "/homing/js/hue/captains/singapura/js/homing/workspace/events/" + fileName;
        try (InputStream in = WorkspaceEventLog.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(
                        "WorkspaceEventLog: missing classpath resource " + path);
            }
            sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            sb.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + path, e);
        }
    }
}
