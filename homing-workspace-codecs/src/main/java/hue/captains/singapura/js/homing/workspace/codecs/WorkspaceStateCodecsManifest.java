package hue.captains.singapura.js.homing.workspace.codecs;

import hue.captains.singapura.js.homing.codec.ecma.EcmaDefinitionCodeGen;
import hue.captains.singapura.js.homing.codec.ecma.EcmaFunctionsCodeGen;
import hue.captains.singapura.js.homing.workspace.state.ChromeState;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.Orientation;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.js.homing.workspace.state.ThemeName;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstance;
import hue.captains.singapura.js.homing.workspace.state.WidgetInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WidgetKind;
import hue.captains.singapura.js.homing.workspace.state.WidgetLocation;
import hue.captains.singapura.js.homing.workspace.state.WidgetTitle;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceInstanceId;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceKind;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceName;
import hue.captains.singapura.js.homing.workspace.state.WorkspaceState;
import hue.captains.singapura.js.homing.workspace.state.codec.ChromeStateJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.ChromeStateJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.LayoutNodeJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.LayoutNodeJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.OrientationJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.OrientationJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.PaneIdJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.PaneIdJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.ThemeNameJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.ThemeNameJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetInstanceIdJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetInstanceIdJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetInstanceJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetInstanceJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetKindJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetKindJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetLocationJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetLocationJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetTitleJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WidgetTitleJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WorkspaceKindJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WorkspaceKindJsFunctions;
import hue.captains.singapura.js.homing.workspace.state.codec.WorkspaceStateJsDefinition;
import hue.captains.singapura.js.homing.workspace.state.codec.WorkspaceStateJsFunctions;

import java.util.List;

/**
 * Typed manifest of every entry the workspace codec umbrella emits.
 *
 * <p>Each entry pairs a Java type with two stateless codegens — one for
 * the class definition (mirroring the Java record's invariants) and one
 * for the codec functions ({@code transformTo}/{@code transformFrom}).
 * The umbrella's generator main ({@link WorkspaceCodecGen}) iterates this
 * list to produce the final {@code WorkspaceStateCodecs.js} body.</p>
 *
 * <p>Provenance is invisible at the manifest level — hand-written
 * codegens (the 12 existing {@code XxxJsDefinition} / {@code XxxJsFunctions}
 * pairs in {@code homing-workspace/state/codec/}) and reflective codegens
 * ({@link EcmaDefinitionCodeGen}, {@link EcmaFunctionsCodeGen}) both
 * conform to the same {@code DefinitionCodeGen} / {@code FunctionsCodeGen}
 * contracts. The consumer reads a uniform stream of generated JS.</p>
 *
 * <p>The CodeGen as Functions doctrine names exactly this property —
 * generators are pure functions over typed Object Definitions; the
 * implementation strategy (hand-written, reflective, AI-assisted) is
 * generator-internal.</p>
 *
 * <h2>Ordering</h2>
 *
 * <p>Entries are listed in dependency order — a codec for a record that
 * contains nested typed identifiers (e.g., {@code WidgetInstance} carries
 * a {@code WidgetInstanceId}) is declared after its component types. The
 * JS file reads top-to-bottom, so a forward reference to a class declared
 * later in the same file would throw {@code ReferenceError} at module
 * evaluation time. The current order matches what the existing
 * {@code WorkspaceStatePersistence.allJs()} bundle uses.</p>
 *
 * @since RFC 0034 P2-prep Cycle A — codec umbrella manifest
 */
public final class WorkspaceStateCodecsManifest {

    private WorkspaceStateCodecsManifest() {}

    public static final List<CodecEntry<?>> ENTRIES = List.of(
            // ── Typed identifier wrappers (single string component) ──────
            //    All have hand-written codegens that mirror the Java
            //    record's compact-constructor grammar validation.
            CodecEntry.manual(PaneIdJsDefinition.INSTANCE,
                              PaneIdJsFunctions.INSTANCE,
                              PaneId.class),
            CodecEntry.manual(WidgetInstanceIdJsDefinition.INSTANCE,
                              WidgetInstanceIdJsFunctions.INSTANCE,
                              WidgetInstanceId.class),
            CodecEntry.manual(WidgetKindJsDefinition.INSTANCE,
                              WidgetKindJsFunctions.INSTANCE,
                              WidgetKind.class),
            CodecEntry.manual(WorkspaceKindJsDefinition.INSTANCE,
                              WorkspaceKindJsFunctions.INSTANCE,
                              WorkspaceKind.class),
            CodecEntry.manual(WidgetTitleJsDefinition.INSTANCE,
                              WidgetTitleJsFunctions.INSTANCE,
                              WidgetTitle.class),
            CodecEntry.manual(ThemeNameJsDefinition.INSTANCE,
                              ThemeNameJsFunctions.INSTANCE,
                              ThemeName.class),

            // ── Reflective fallbacks (no hand-written pair yet) ──────────
            //    Newer typed identifiers introduced by RFC 0031 multi-
            //    workspace work. The simple-record path of the Ecma
            //    codegens covers them losslessly. When the slim-Jackson
            //    project lands, these become indistinguishable from the
            //    hand-written ones above.
            CodecEntry.reflective(WorkspaceName.class,
                                  EcmaDefinitionCodeGen.INSTANCE,
                                  EcmaFunctionsCodeGen.INSTANCE),
            CodecEntry.reflective(WorkspaceInstanceId.class,
                                  EcmaDefinitionCodeGen.INSTANCE,
                                  EcmaFunctionsCodeGen.INSTANCE),

            // ── Enum + sealed-of-records codecs ──────────────────────────
            //    Hand-written; the structural shape (frozen-namespace with
            //    typed variants) is what each consumer expects to import.
            CodecEntry.manual(OrientationJsDefinition.INSTANCE,
                              OrientationJsFunctions.INSTANCE,
                              Orientation.class),
            CodecEntry.manual(WidgetLocationJsDefinition.INSTANCE,
                              WidgetLocationJsFunctions.INSTANCE,
                              WidgetLocation.class),

            // ── Recursive sealed + composite records ─────────────────────
            //    LayoutNode is a self-recursive sealed sum (Split contains
            //    nested LayoutNodes). ChromeState bundles ThemeName.
            CodecEntry.manual(LayoutNodeJsDefinition.INSTANCE,
                              LayoutNodeJsFunctions.INSTANCE,
                              LayoutNode.class),
            CodecEntry.manual(ChromeStateJsDefinition.INSTANCE,
                              ChromeStateJsFunctions.INSTANCE,
                              ChromeState.class),

            // ── Composite + envelope codecs ──────────────────────────────
            //    WidgetInstance composes the identifiers above; WorkspaceState
            //    is the top-level envelope. Both reach across the entire
            //    catalogue of preceding entries.
            CodecEntry.manual(WidgetInstanceJsDefinition.INSTANCE,
                              WidgetInstanceJsFunctions.INSTANCE,
                              WidgetInstance.class),
            CodecEntry.manual(WorkspaceStateJsDefinition.INSTANCE,
                              WorkspaceStateJsFunctions.INSTANCE,
                              WorkspaceState.class)
    );
}
