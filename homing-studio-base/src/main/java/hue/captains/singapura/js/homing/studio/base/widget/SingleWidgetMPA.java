package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;

import java.util.List;

/**
 * RFC 0024 — abstract base for "fake AppModule" shells hosting exactly
 * one {@link Widget}. The pattern that retired DocViewer1 without
 * requiring a framework-side routing refactor: each legacy per-Doc-kind
 * viewer (SvgViewer, ComposedViewer, …) keeps its identity — same URL,
 * same {@code simpleName}, same {@code AppLink}, same {@link
 * hue.captains.singapura.js.homing.studio.base.app.ContentViewer}
 * mapping — but its insides become a thin shell hosting the matching
 * widget.
 *
 * <h2>What's preserved vs what changes</h2>
 *
 * <p>Preserved (from the viewer's point of view):</p>
 * <ul>
 *   <li>URL grammar — {@code /app?app=svg-viewer&id=...}. The shell's
 *       {@link StandardMPA} parent defaults the {@code ?widget=} param
 *       to the sole widget's simpleName when only one is hosted, so
 *       the legacy URL still resolves.</li>
 *   <li>Typed Params — the {@code P} type parameter flows through. A
 *       viewer with {@code Params(String id)} preserves its typed URL
 *       generation surface.</li>
 *   <li>{@code AppLink<MyViewer>} references — the AppModule class
 *       continues to exist.</li>
 *   <li>{@link hue.captains.singapura.js.homing.studio.base.app.ContentViewer}
 *       registration — points at the viewer's {@code INSTANCE} as
 *       before.</li>
 * </ul>
 *
 * <p>Changes:</p>
 * <ul>
 *   <li>Chrome is now DomOpsParty branches (header + main + footer at
 *       L1) rendered through {@link StandardMPA}'s machinery, not
 *       DocViewer1's inline chrome construction.</li>
 *   <li>The body is the widget's {@code mountInto}, not inline JS.</li>
 *   <li>The {@code @LegacyAppMain} marker comes off — the viewer is no
 *       longer pre-Component-contract; it IS using the contract,
 *       through the widget.</li>
 * </ul>
 *
 * <h2>Subclass template</h2>
 *
 * <pre>{@code
 * public final class SvgViewer extends SingleWidgetMPA<SvgViewer.Params, SvgViewer> {
 *     public static final SvgViewer INSTANCE = new SvgViewer();
 *     private SvgViewer() {}
 *
 *     public record Params(String id) implements AppModule._Param {}
 *     public record appMain() implements AppModule._AppMain<Params, SvgViewer> {}
 *     public record link() implements AppLink<SvgViewer> {}
 *
 *     @Override public String simpleName() { return "svg-viewer"; }
 *     @Override public Class<Params> paramsType() { return Params.class; }
 *     @Override public String title() { return "svg"; }
 *
 *     @Override protected AppModule._AppMain<Params, SvgViewer> appMain() { return new appMain(); }
 *     @Override protected Widget<?, ?> widget() { return SvgWidget.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> typed URL Params record this viewer accepts
 * @param <M> self-type (CRTP)
 * @since RFC 0024 Phase P1b — the "fake AppModule" pattern that
 *        dissolved the framework-routing-refactor blocker
 */
public abstract class SingleWidgetMPA<P extends AppModule._Param, M extends SingleWidgetMPA<P, M>>
        extends StandardMPA<P, M> {

    /** The single widget this shell hosts. */
    protected abstract Widget<?, ?> widget();

    @Override
    protected final List<? extends Widget<?, ?>> widgets() {
        return List.of(widget());
    }
}
