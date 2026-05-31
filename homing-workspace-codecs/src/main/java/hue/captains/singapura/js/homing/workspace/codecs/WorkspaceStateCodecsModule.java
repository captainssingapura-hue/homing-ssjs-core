package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Java {@link DomModule} declaration for the umbrella of generated
 * workspace codecs.
 *
 * <p>The JS body of this module is produced at build time by
 * {@link WorkspaceCodecGen} from
 * {@link WorkspaceStateCodecsManifest}. The framework's
 * {@code EsModuleWriter} reads the body from
 * {@code target/classes/...WorkspaceStateCodecs.js} as a normal
 * classpath resource — there is no runtime codegen path.</p>
 *
 * <h2>Exports list mirrors the manifest</h2>
 *
 * <p>For each {@code ObjectDefinition} in the manifest, the generated JS
 * contains <i>two</i> top-level classes — the typed class declaration
 * ({@code PaneId}) and its codec ({@code PaneIdCodec}). Both are exported,
 * so consumers can construct typed instances and round-trip them via the
 * matching codec by importing whichever they need.</p>
 *
 * <p><b>Cycle A scope.</b> Manifest contains {@code PaneId}; this module
 * therefore exports {@code PaneId} and {@code PaneIdCodec}. As the manifest
 * grows, the {@code exports()} list will grow in lockstep — a future
 * conformance test enforces that every manifest entry has its two matching
 * {@code Exportable._Class} records here.</p>
 *
 * @since RFC 0034 P2-prep — codec generation foundation
 */
public record WorkspaceStateCodecsModule() implements DomModule<WorkspaceStateCodecsModule> {

    public static final WorkspaceStateCodecsModule INSTANCE = new WorkspaceStateCodecsModule();

    // ── Exported JS classes ──────────────────────────────────────────────
    // One typed-class entry + one codec entry per manifest record (the
    // enum exports its frozen-object form, not a class — but the
    // Exportable._Class record carries the name either way).
    //
    // Names match exactly what EcmaDefinitionCodeGen / EcmaFunctionsCodeGen
    // emit (Java simpleName for the typed name; + "Codec" for the codec).

    // Single-component scalar wrappers — typed classes.
    public record PaneId()               implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceKind()        implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceName()        implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceInstanceId()  implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetInstanceId()     implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetKind()           implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetTitle()          implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record ThemeName()            implements Exportable._Class<WorkspaceStateCodecsModule> {}
    // Enum — typed value is a frozen string-keyed map.
    public record Orientation()          implements Exportable._Class<WorkspaceStateCodecsModule> {}
    // Sealed sums + composite + envelope records.
    public record WidgetLocation()       implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record LayoutNode()           implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record ChromeState()          implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetInstance()       implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceState()       implements Exportable._Class<WorkspaceStateCodecsModule> {}

    // Codecs — one per typed entry above.
    public record PaneIdCodec()              implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceKindCodec()       implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceNameCodec()       implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceInstanceIdCodec() implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetInstanceIdCodec()    implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetKindCodec()          implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetTitleCodec()         implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record ThemeNameCodec()           implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record OrientationCodec()         implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetLocationCodec()      implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record LayoutNodeCodec()          implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record ChromeStateCodec()         implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WidgetInstanceCodec()      implements Exportable._Class<WorkspaceStateCodecsModule> {}
    public record WorkspaceStateCodec()      implements Exportable._Class<WorkspaceStateCodecsModule> {}

    @Override
    public ImportsFor<WorkspaceStateCodecsModule> imports() {
        // WorkspaceStateCodec.transformTo / transformFrom dispatch via the
        // WidgetParamsCodecRegistry — must be in this module's JS scope.
        return ImportsFor.<WorkspaceStateCodecsModule>builder()
                .add(new hue.captains.singapura.js.homing.core.ModuleImports<>(
                        java.util.List.of(new hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule.WidgetParamsCodecRegistry()),
                        hue.captains.singapura.js.homing.workspace.persistence.WidgetParamsCodecRegistryModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WorkspaceStateCodecsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new PaneId(),              new PaneIdCodec(),
                new WorkspaceKind(),       new WorkspaceKindCodec(),
                new WorkspaceName(),       new WorkspaceNameCodec(),
                new WorkspaceInstanceId(), new WorkspaceInstanceIdCodec(),
                new WidgetInstanceId(),    new WidgetInstanceIdCodec(),
                new WidgetKind(),          new WidgetKindCodec(),
                new WidgetTitle(),         new WidgetTitleCodec(),
                new ThemeName(),           new ThemeNameCodec(),
                new Orientation(),         new OrientationCodec(),
                new WidgetLocation(),      new WidgetLocationCodec(),
                new LayoutNode(),          new LayoutNodeCodec(),
                new ChromeState(),         new ChromeStateCodec(),
                new WidgetInstance(),      new WidgetInstanceCodec(),
                new WorkspaceState(),      new WorkspaceStateCodec()
        ));
    }
}
