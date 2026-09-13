package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;

import java.util.Optional;

/**
 * RFC 0058 — an app whose one positioned node addresses <b>sub-nodes by
 * anchor</b>. The catalogue tree positions {@code (app, params)}; some params
 * name not the node but a path <em>inside</em> it — a workspace kind inside its
 * group — and the address for that is the node's path plus a fragment:
 * {@code /cat/…/<node>#<anchor>}.
 *
 * <p>An app that implements this tells {@code /goto} how to answer for such
 * params: which params name the node it lives at, and what the anchor is. The
 * path index knows nothing of anchors — a fragment never reaches the server,
 * so it can never be a position — and stays exactly as RFC 0051 left it.</p>
 *
 * @param <P> the app's params type
 */
public interface AnchorAddressable<P extends AppModule._Param> {

    /**
     * The node these params live at, and the anchor inside it — or empty when
     * the params name the node itself (or name nothing this app can place).
     *
     * @param node   the params of the positioned node — what the placement wrote
     * @param anchor the fragment, without its {@code #}
     */
    record Anchored<P extends AppModule._Param>(P node, String anchor) {
        public Anchored {
            java.util.Objects.requireNonNull(node,   "Anchored.node");
            java.util.Objects.requireNonNull(anchor, "Anchored.anchor");
            if (anchor.isBlank() || anchor.startsWith("#")) {
                throw new IllegalArgumentException("Anchored.anchor is the fragment without '#', non-blank: '" + anchor + "'");
            }
        }
    }

    Optional<Anchored<P>> anchorOf(P params);
}
