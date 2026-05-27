package hue.captains.singapura.js.homing.workspace.state.codec;

import hue.captains.singapura.js.homing.codec.ObjectDefinition;
import hue.captains.singapura.js.homing.workspace.state.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Drop-in bundler that returns the complete JS source for RFC 0029
 * workspace-state persistence as a single concatenated string, ready
 * to be included in a workspace chrome's {@code bodyJs()}.
 *
 * <p>The bundle contains, in dependency order:</p>
 *
 * <ol>
 *   <li>The eighteen codec sources (Definition + Functions for each of
 *       PaneId, WidgetInstanceId, WidgetKind, WorkspaceKind, WidgetTitle,
 *       ThemeName, Orientation, WidgetLocation, LayoutNode, ChromeState,
 *       WidgetInstance, WorkspaceState).</li>
 *   <li>{@code WidgetParamsCodecRegistry} — the polymorphism boundary
 *       for Widget._Param.</li>
 *   <li>{@code LocalStorageStore} — the default local-only backend.</li>
 *   <li>{@code captureLiveWorkspace} — view → WorkspaceState helper.</li>
 *   <li>{@code WorkspaceStatePersister} — debounced save coordinator.</li>
 *   <li>{@code WorkspaceStatePersistence} — the high-level attach() facade.</li>
 * </ol>
 *
 * <h2>Usage in a workspace chrome's {@code bodyJs()}</h2>
 *
 * <pre>{@code
 * return List.of(
 *     WorkspaceStatePersistence.allJs(),       // prepend the bundle
 *     "// rest of chrome body...",
 *     "var persistenceLayer = WorkspaceStatePersistence.attach({",
 *     "    workspaceKind: 'AnimalsPlayground',",
 *     "    storage: window.localStorage,",
 *     "    paramsCodecs: { ... }",
 *     "});",
 *     "var persister = persistenceLayer.create(function () { return captureLiveState(); });",
 *     ...
 * );
 * }</pre>
 *
 * <p>Per the {@code CodeGen as Functions} doctrine, the codec sources
 * embedded here are produced by hand-written
 * {@link hue.captains.singapura.js.homing.codec.FunctionsCodeGen} /
 * {@link hue.captains.singapura.js.homing.codec.DefinitionCodeGen}
 * implementations. A future automated generator would produce the same
 * shape; the consumer never sees the difference.</p>
 *
 * @since RFC 0029 — the wire-in.
 */
public final class WorkspaceStatePersistence {

    private WorkspaceStatePersistence() {}

    /**
     * The complete persistence JS bundle as a single string. Cached on
     * first call — codec generation is pure and the result is
     * deterministic.
     */
    public static String allJs() {
        return BUNDLE;
    }

    private static final String BUNDLE = build();

    private static String build() {
        var sb = new StringBuilder();
        sb.append("// ─── RFC 0029 persistence bundle ─────────────────────────────────\n");

        // -- Typed identifier codecs (wrapper-type pattern) --
        appendCodec(sb, PaneIdJsDefinition.INSTANCE,           PaneIdJsFunctions.INSTANCE,           PaneId.class);
        appendCodec(sb, WidgetInstanceIdJsDefinition.INSTANCE, WidgetInstanceIdJsFunctions.INSTANCE, WidgetInstanceId.class);
        appendCodec(sb, WidgetKindJsDefinition.INSTANCE,       WidgetKindJsFunctions.INSTANCE,       WidgetKind.class);
        appendCodec(sb, WorkspaceKindJsDefinition.INSTANCE,    WorkspaceKindJsFunctions.INSTANCE,    WorkspaceKind.class);
        appendCodec(sb, WidgetTitleJsDefinition.INSTANCE,      WidgetTitleJsFunctions.INSTANCE,      WidgetTitle.class);
        appendCodec(sb, ThemeNameJsDefinition.INSTANCE,        ThemeNameJsFunctions.INSTANCE,        ThemeName.class);

        // -- Enum + sealed-of-records codecs --
        appendCodec(sb, OrientationJsDefinition.INSTANCE,      OrientationJsFunctions.INSTANCE,      Orientation.class);
        appendCodec(sb, WidgetLocationJsDefinition.INSTANCE,   WidgetLocationJsFunctions.INSTANCE,   WidgetLocation.class);

        // -- Recursive sealed + composite codecs --
        appendCodec(sb, LayoutNodeJsDefinition.INSTANCE,       LayoutNodeJsFunctions.INSTANCE,       LayoutNode.class);
        appendCodec(sb, ChromeStateJsDefinition.INSTANCE,      ChromeStateJsFunctions.INSTANCE,      ChromeState.class);

        // -- Polymorphism registry (must precede WidgetInstance codec) --
        appendResource(sb, "WidgetParamsCodecRegistry.js");

        // -- Composite + envelope codecs --
        appendCodec(sb, WidgetInstanceJsDefinition.INSTANCE,   WidgetInstanceJsFunctions.INSTANCE,   WidgetInstance.class);
        appendCodec(sb, WorkspaceStateJsDefinition.INSTANCE,   WorkspaceStateJsFunctions.INSTANCE,   WorkspaceState.class);

        // -- Store, capture helper, persister, facade --
        appendResource(sb, "LocalStorageStore.js");
        appendResource(sb, "captureLiveWorkspace.js");
        appendResource(sb, "WorkspaceStatePersister.js");
        appendResource(sb, "WorkspaceStatePersistenceFacade.js");

        return sb.toString();
    }

    private static <T> void appendCodec(StringBuilder sb,
            hue.captains.singapura.js.homing.codec.DefinitionCodeGen defGen,
            hue.captains.singapura.js.homing.codec.FunctionsCodeGen  fnGen,
            Class<T> type) {
        var def = ObjectDefinition.of(type);
        sb.append(defGen.generate(def));
        sb.append('\n');
        sb.append(fnGen.generate(def));
        sb.append('\n');
    }

    private static void appendResource(StringBuilder sb, String fileName) {
        var path = "/homing/js/hue/captains/singapura/js/homing/workspace/state/codec/" + fileName;
        try (InputStream in = WorkspaceStatePersistence.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("classpath resource not found: " + path);
            }
            sb.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            sb.append('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("WorkspaceStatePersistence: failed to read " + path, e);
        }
    }
}
