package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code WriteLockGuard} — Phase 7 of the workspace-shell chrome.
 * Acquires the per-workspace write lock via the Web Locks API and
 * coordinates the read-only overlay that appears when a sibling
 * browsing context already holds the lock.
 *
 * <p>Two cooperating classes:</p>
 *
 * <ul>
 *   <li>{@code WriteLockGuard} — singleton; holds the
 *       {@code navigator.locks} dep + the {@code ReadOnlyOverlay}
 *       constructor; {@code attach} returns a per-workspace
 *       <em>lock controller</em>.</li>
 *   <li>{@code ReadOnlyOverlay} — per-workspace instance; owns the
 *       click-absorbing mask and the multi-window banner with Take
 *       Over / Reload buttons; gets mounted/unmounted by the
 *       controller as lock state flips.</li>
 * </ul>
 *
 * <p>State changes flow outward via three side channels:</p>
 *
 * <ul>
 *   <li>{@code recorder.setWriteLockHeld(b)} — Phase 6 fence; the
 *       recorder drops {@code emit} when the flag is false so this
 *       browsing context never journals events it can't durably
 *       persist.</li>
 *   <li>{@code overlay.show()/hide()} — Phase 7 visual; mask + banner
 *       lifecycle.</li>
 *   <li>{@code opts.onChange(held)} — passthrough hook the
 *       orchestrator wires (Phase 16 badge will subscribe).</li>
 * </ul>
 *
 * <p><b>Make It Impossible, Not Forbidden:</b> a single transparent
 * absorber over the host (z:40) beats per-component fences. The badge
 * (z:50) and banner (z:55) sit above; everything else under host is
 * below z:40 and unreachable while read-only.</p>
 *
 * <p>Explicit Substrate: instance + INSTANCE + no static methods.
 * Constructor injection for {@code navigatorLocks} (test-overridable)
 * and {@code OverlayCtor}. Per-call {@code attach} receives only data
 * (lockName, host, workspaceName, recorder, onChange).</p>
 *
 * @since post-RFC-0034 workspace chrome decomposition — Phase 7
 */
public record WriteLockGuardModule()
        implements DomModule<WriteLockGuardModule> {

    public static final WriteLockGuardModule INSTANCE = new WriteLockGuardModule();

    /** The {@code WriteLockGuard} JS class (factory). */
    public record WriteLockGuard()  implements Exportable._Class<WriteLockGuardModule> {}

    /** The {@code ReadOnlyOverlay} JS class (per-workspace DOM). */
    public record ReadOnlyOverlay() implements Exportable._Class<WriteLockGuardModule> {}

    @Override
    public ImportsFor<WriteLockGuardModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<WriteLockGuardModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new WriteLockGuard(), new ReadOnlyOverlay()));
    }
}
