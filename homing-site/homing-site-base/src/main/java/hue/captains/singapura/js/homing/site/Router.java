package hue.captains.singapura.js.homing.site;

import java.util.Optional;

/**
 * RFC 0066 Episode 3 — path in, navigable out.
 *
 * <p>A router is the site's whole answer to "what is at this address": a
 * function from a {@link Path} to the {@link Navigable} there, or nothing.
 * It may match segments literally, read some of them as parameters and bind
 * them into the navigable it returns, delegate a prefix to another router, or
 * consult a tree it built at boot, as the studio's catalogue registry does.
 * None of that shows through; the contract is the one method.</p>
 *
 * <p>Empty is the ordinary miss. A router does not say why — which segment
 * failed, whether a leaf was walked past — because a site that wants to say
 * why can say it on a navigable of its own. The studio's {@code
 * PathResolution.Miss} is that elaboration, made where it is needed.</p>
 */
@FunctionalInterface
public interface Router {

    /** The navigable at {@code path}, or empty when the site has nothing there. */
    Optional<Navigable> resolve(Path path);

    /** A router with nothing anywhere. */
    Router NONE = path -> Optional.empty();
}
