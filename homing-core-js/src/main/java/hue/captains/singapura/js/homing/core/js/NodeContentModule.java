package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0039 — the client-side <b>twin</b> of a doc-tree node's content object.
 *
 * <p>The wire value at a node's path is either the legacy flat array
 * ({@code [seg, …]}) or, when the node carries a caption, the object form
 * ({@code {caption, segments}}). {@code NodeContent} is the small JS class that
 * normalizes both into a typed {@code { caption, segments }} — the front-end
 * mirror of the server's {@code ComposedLeaf}, so the renderer reads a stable
 * shape instead of poking raw properties.</p>
 *
 * <p>Substrate-level (homing-core-js): no studio styling, just the shape.</p>
 *
 * @since homing-core-js — RFC 0039 (node content object twin)
 */
public record NodeContentModule() implements DomModule<NodeContentModule> {

    public static final NodeContentModule INSTANCE = new NodeContentModule();

    /** The {@code NodeContent(bundle)} JS class. */
    public record NodeContent() implements Exportable._Constant<NodeContentModule> {}

    @Override
    public ImportsFor<NodeContentModule> imports() {
        return ImportsFor.<NodeContentModule>builder().build();
    }

    @Override
    public ExportsOf<NodeContentModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new NodeContent()));
    }
}
