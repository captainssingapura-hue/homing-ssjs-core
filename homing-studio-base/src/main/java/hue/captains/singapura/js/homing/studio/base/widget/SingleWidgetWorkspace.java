package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Widget;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedWidget;

import java.util.List;

/**
 * RFC 0040 — the leveled-Open shell. A {@link StandardMPA} that shows exactly
 * one doc widget at a time, picked at request time by the {@code ?widget=}
 * param (which the Navigator derives from the node's {@code kind}). It is the
 * standalone page a tree leaf opens into:
 *
 * <pre>/app?app=singleWidgetWorkspace&treeId=&lt;id&gt;&l0=&lt;i&gt;&l1=&lt;i&gt;&widget=svg-widget</pre>
 *
 * <p>Why this exists: opening a Navigator leaf used to <i>redirect</i> to the
 * doc's legacy per-kind viewer addressed by uuid, so the page URL
 * ({@code ?app=svg-viewer&id=<uuid>}) and the path-derived breadcrumb
 * disagreed. This shell keeps the reader in the leveled world — the page URL
 * <i>is</i> the tree path, no uuid — and the hosted widget fetches its content
 * by that same path locator ({@code /open-content?l0=…}). The breadcrumb is
 * fetched from {@code /open-refs?l0=…}, so URL and breadcrumb share one
 * source of truth.</p>
 *
 * <p>The uuid locator still works: a widget hosted here accepts {@code ?id=}
 * exactly as before, so legacy links and the per-kind viewers are unaffected.
 * This shell simply offers the positional sibling.</p>
 *
 * <p><b>Every</b> catalogue leaf opens through this one shell. The kinds the
 * tree-based doc effort is converging on render in place — {@link SvgWidget}
 * (svg) and {@link ComposedWidget} (composed). Every other kind (plain
 * {@code doc}, {@code plan}, {@code app}, …) is routed to
 * {@link LegacyRedirectWidget}, which client-redirects to the doc's own
 * {@code url()} (its existing per-kind viewer). That uniformity is what lets
 * the legacy server-side {@code /open} redirect retire completely — there is
 * no longer any server endpoint that resolves a path to a redirect.</p>
 *
 * @since homing-studio-base — RFC 0040 leveled Open
 */
public final class SingleWidgetWorkspace
        extends StandardMPA<AppModule._None, SingleWidgetWorkspace> {

    public static final SingleWidgetWorkspace INSTANCE = new SingleWidgetWorkspace();

    private SingleWidgetWorkspace() {}

    public record appMain() implements AppModule._AppMain<AppModule._None, SingleWidgetWorkspace> {}
    public record link() implements AppLink<SingleWidgetWorkspace> {}

    @Override public String simpleName() { return "singleWidgetWorkspace"; }
    @Override public String title() { return "studio"; }

    @Override
    protected AppModule._AppMain<AppModule._None, SingleWidgetWorkspace> appMain() {
        return new appMain();
    }

    @Override
    protected List<? extends Widget<?, ?>> widgets() {
        return List.of(SvgWidget.INSTANCE, ComposedWidget.INSTANCE, LegacyRedirectWidget.INSTANCE);
    }
}
