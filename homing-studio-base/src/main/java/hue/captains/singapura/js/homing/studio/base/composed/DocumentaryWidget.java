package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.composed.text.Line;

import java.util.Objects;
import java.util.Optional;

/**
 * Segment kind for embedding a typed interactive widget inside a
 * {@link ComposedDoc}. A {@code DocumentaryWidget} wraps a typed
 * {@link AppModule} — its render output is loaded into a sub-region of
 * the composed page's body. The widget is an isolated island; cross-widget
 * communication inside a ComposedDoc remains forbidden per the
 * <i>Typed Content Vocabulary</i> doctrine.
 *
 * <p>The name captures the dual nature: <b>Documentary</b> — the Doc-shaped
 * framing (the widget appears as a typed segment of a Doc, citable in
 * context, themed by the framework chrome that wraps the parent
 * ComposedDoc); <b>Widget</b> — the wrapped EsModule is interactive,
 * stateful, JS-driven.</p>
 *
 * <p>On the wire: the segment emits a typed module URL pointing at the
 * wrapped AppModule's JS module. The composed viewer's client-side
 * renderer dynamic-imports that URL and invokes its {@code appMain}
 * function against a sub-region (not the full {@code rootElement}).
 * Per the <i>Thin HTML, Typed JS</i> doctrine, the browser's native ES
 * module loader handles transitive resolution of the widget's
 * dependencies; the framework supplies only the entry URL.</p>
 *
 * @param <P>            typed Params of the wrapped AppModule
 * @param <M>            self-type of the wrapped AppModule
 * @param widget         the typed AppModule the segment embeds
 * @param params         typed parameters passed to the widget on mount
 * @param captionOverride optional caption rendered above the widget's host
 */
public record DocumentaryWidget<P extends AppModule._Param,
                                M extends AppModule<P, M>>(
        M widget,
        P params,
        Optional<Line.Plain> captionOverride
) implements Segment {

    public DocumentaryWidget {
        Objects.requireNonNull(widget,          "DocumentaryWidget.widget");
        Objects.requireNonNull(params,          "DocumentaryWidget.params");
        Objects.requireNonNull(captionOverride, "DocumentaryWidget.captionOverride (use Optional.empty)");
    }

    /** Convenience — no caption override. */
    public DocumentaryWidget(M widget, P params) {
        this(widget, params, Optional.empty());
    }

    /** Convenience — caption from a raw string (blank becomes no override). */
    public DocumentaryWidget(M widget, P params, String caption) {
        this(widget, params, Line.optionalPlain(caption));
    }

    /** The caption to render — explicit override, or the widget's title when not provided. */
    public String resolvedCaption() {
        return captionOverride.map(Line.Plain::raw).orElse(widget.title());
    }
}
