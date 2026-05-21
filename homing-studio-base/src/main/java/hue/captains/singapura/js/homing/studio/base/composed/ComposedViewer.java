package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetMPA;

/**
 * RFC 0019 → RFC 0024 — viewer AppModule for {@link ComposedDoc}.
 * URL contract:
 *
 * <pre>/app?app=composed-viewer&id=&lt;composeddoc-uuid&gt;</pre>
 *
 * <p>Originally landed (RFC 0019) as a {@code DocViewer<P, M>} subclass
 * with inline body delegating to the legacy 528-line
 * {@code ComposedViewerRenderer.js}. Reborn under RFC 0024 Phase P1c as
 * a "fake AppModule" — a {@link SingleWidgetMPA} thin shell hosting
 * {@link ComposedWidget}. The URL grammar, typed Params, {@link AppLink},
 * and {@link ComposedContentViewer} routing are unchanged; what's
 * underneath is now the new contract:</p>
 *
 * <ul>
 *   <li>Chrome via DomOpsParty branches (header + main + footer at L1
 *       under root, framework-owned).</li>
 *   <li>Body is {@code ComposedWidget.mountInto(branch, parent, params)}
 *       — orchestrator dispatches to per-segment renderer EsModules,
 *       each its own file (Modest File Size compliance — the legacy
 *       528-line renderer split into 9 focused modules).</li>
 *   <li>Recursive ComposedDoc embedding via the new {@link ComposedSegment}
 *       variant + {@link ComposedSegmentRenderer} — the orchestrator's
 *       mount function is self-callable, threading a renderingStack
 *       parameter forward for cycle detection.</li>
 *   <li>No {@code @LegacyAppMain} — this viewer IS the contract now.</li>
 * </ul>
 *
 * @since RFC 0019 (original) — RFC 0024 Phase P1c (reborn as widget shell
 *        + 9-file split + recursive composedDoc support)
 * @see ComposedWidget — the widget this shell hosts
 * @see SingleWidgetMPA — the abstract base for fake-AppModule shells
 */
public final class ComposedViewer extends SingleWidgetMPA<ComposedViewer.Params, ComposedViewer> {

    public static final ComposedViewer INSTANCE = new ComposedViewer();

    private ComposedViewer() {}  // singleton via INSTANCE

    /** @param id UUID of the ComposedDoc to render. */
    public record Params(String id) implements AppModule._Param {}

    public record appMain() implements AppModule._AppMain<Params, ComposedViewer> {}
    public record link() implements AppLink<ComposedViewer> {}

    @Override public String simpleName() { return "composed-viewer"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "doc"; }

    @Override
    protected AppModule._AppMain<Params, ComposedViewer> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return ComposedWidget.INSTANCE;
    }
}
