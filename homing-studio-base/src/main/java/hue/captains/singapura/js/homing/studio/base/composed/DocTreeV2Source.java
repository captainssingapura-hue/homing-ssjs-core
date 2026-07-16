package hue.captains.singapura.js.homing.studio.base.composed;

/**
 * RFC 0039 (name-path variant) — a {@link hue.captains.singapura.js.homing.studio.base.Doc Doc}
 * that supplies its own {@link DocTreeV2}, whose content is addressed by
 * <b>name-path</b> rather than child-index. The doc-tree endpoints
 * ({@code DocTreeGetAction} / {@code DocTreeContentGetAction}) recognise this
 * interface and serve the V2 payload, taking precedence over the index-path
 * {@link DocTreeSource} dispatch.
 *
 * <p>A Doc implements <i>either</i> {@code DocTreeSource} (index-path, V1) or this
 * (name-path, V2), never both — the endpoints check for the V2 form first.</p>
 */
public interface DocTreeV2Source {

    /** This doc's name-path rigid-tree structure + content seam (RFC 0039). */
    DocTreeV2 toDocTreeV2();
}
