package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.widget.SingleWidgetMPA;
import hue.captains.singapura.js.homing.studio.base.widget.SvgWidget;

/**
 * RFC 0016 → RFC 0024 — the SvgDoc viewer. URL contract:
 *
 * <pre>/app?app=svg-viewer&id=&lt;svgdoc-uuid&gt;</pre>
 *
 * <p>Originally landed (RFC 0016) as a {@code DocViewer<P, M>} subclass
 * with inline chrome + inline body JS. Reborn under RFC 0024 Phase P1b
 * as a "fake AppModule" — a {@link SingleWidgetMPA} thin shell hosting
 * {@link SvgWidget}. The URL grammar, typed Params, {@link AppLink},
 * and {@link SvgContentViewer} routing are unchanged; what's underneath
 * is now the new contract:</p>
 *
 * <ul>
 *   <li>Chrome via DomOpsParty branches (header + main + footer at L1
 *       under root, framework-owned).</li>
 *   <li>Body is {@code SvgWidget.mountInto(branch, parent, params)} —
 *       phase sub-branches for loading / error / content states; full
 *       DomOpsParty ownership discipline.</li>
 *   <li>No {@code @LegacyAppMain} — this viewer IS the contract now,
 *       not pre-contract.</li>
 * </ul>
 *
 * <p>The "fake AppModule" pattern dissolved the framework-routing-refactor
 * blocker: every existing per-Doc-kind viewer can migrate independently
 * by becoming a {@code SingleWidgetMPA} subclass hosting its matching
 * widget. No ContentViewer override mechanism needed; no AppLink rewrite
 * needed; no URL-grammar refactor needed. The viewer keeps its identity;
 * its insides upgrade.</p>
 *
 * @since RFC 0016 (original) — RFC 0024 Phase P1b (reborn as widget shell)
 * @see SvgWidget — the widget this shell hosts
 * @see SingleWidgetMPA — the abstract base for fake-AppModule shells
 */
public final class SvgViewer extends SingleWidgetMPA<SvgViewer.Params, SvgViewer> {

    public static final SvgViewer INSTANCE = new SvgViewer();

    private SvgViewer() {}  // singleton via INSTANCE

    /** @param id UUID of the SvgDoc to render. Same URL grammar as the
     *  original DocViewer-based implementation. */
    public record Params(String id) implements AppModule._Param {}

    public record appMain() implements AppModule._AppMain<Params, SvgViewer> {}
    public record link() implements AppLink<SvgViewer> {}

    @Override public String simpleName() { return "svg-viewer"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "svg"; }

    @Override
    protected AppModule._AppMain<Params, SvgViewer> appMain() {
        return new appMain();
    }

    @Override
    protected Widget<?, ?> widget() {
        return SvgWidget.INSTANCE;
    }
}
