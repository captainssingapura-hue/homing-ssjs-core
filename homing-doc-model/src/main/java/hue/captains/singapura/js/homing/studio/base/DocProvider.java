package hue.captains.singapura.js.homing.studio.base;

import java.util.List;

/**
 * Marker interface implemented by {@link hue.captains.singapura.js.homing.core.AppModule}s
 * that contribute typed {@link Doc}s to the studio's {@code DocRegistry}.
 *
 * <p>The boot-time {@code DocRegistry.from(appResolver)} walks the app closure for
 * {@code instanceof DocProvider} and unions every contributor's {@link #docs()} into a
 * single UUID-indexed registry.</p>
 *
 * <p>Catalogues, browsers, and trackers that already aggregate docs (because they display
 * them) are the natural implementors. There is no separate "DocGroup" module — the
 * displayer is the contributor.</p>
 *
 * @since RFC 0004
 */
public interface DocProvider {

    /** Docs this provider contributes to the studio's {@code DocRegistry}. */
    List<Doc> docs();
}
