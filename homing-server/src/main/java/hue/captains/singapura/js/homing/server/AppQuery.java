package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.tao.http.action.Param;

/**
 * Query parameters accepted by {@link AppHtmlGetAction} as of RFC 0001 Step 07.
 *
 * <p>The app is named by {@code ?app=&lt;simpleName&gt;}, resolved via
 * {@link hue.captains.singapura.js.homing.core.SimpleAppResolver}. The old
 * {@code ?class=&lt;canonicalClassName&gt;} is retired (NoReflectionAtRunTime): an app
 * is never found by its class's name. With no {@code ?app=}, dispatch fails with a 404.</p>
 *
 * @param simpleName the value of {@code ?app=}
 * @param theme      optional theme propagated to the loaded module
 * @param locale     optional locale propagated to the loaded module
 */
public record AppQuery(String simpleName, String theme, String locale,
                       java.util.Map<String, java.util.List<String>> all)
        implements Param._QueryString {

    /**
     * RFC 0051 — the whole query, so the resolved app's own
     * {@code ParamCodec} can read its params server-side.
     *
     * <p>The three named components above are the ACTION's business: which app,
     * which theme, which locale. Everything else belongs to the app, and the
     * action has no business interpreting it — it only needs to hand the raw
     * map over. That split is what lets one action serve every app without
     * knowing any app's parameters.</p>
     */
    public AppQuery {
        all = (all == null) ? java.util.Map.of() : all;
    }

    /** The pre-RFC-0051 shape, for callers that do not carry a query map. */
    public AppQuery(String simpleName, String theme, String locale) {
        this(simpleName, theme, locale, java.util.Map.of());
    }

    public boolean hasSimpleName() {
        return simpleName != null && !simpleName.isBlank();
    }
}
