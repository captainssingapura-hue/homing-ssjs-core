package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.tree.NodeIdentity;

/**
 * The typed identity of a navigable: the app plus its params, and nothing else.
 *
 * <p>This is the pair RFC 0051's path axiom is stated over — <i>each navigable
 * {@code (app, args)} has at most one position in the catalogue</i> — so it is
 * what Law 1 polices and what determines a URL. Nothing finer, nothing coarser.</p>
 *
 * <p><b>It is also a {@link NodeIdentity}</b> (RFC 0053), rather than being wrapped
 * in one. A catalogue vertex and a catalogue leaf are both identified by their
 * binding, and a binding is already global — the app class and its typed params owe
 * nothing to where the vertex sits, so the identity survives being grafted under a
 * different parent. Making {@code NavKey} the identity directly is what lets a flat
 * permalink and a catalogue path terminate at the same resolver: {@code /app?app=…}
 * already carries exactly this pair, so no translation step stands between the two
 * routes and they cannot drift apart.</p>
 *
 * <p>Promoted from a private record inside {@link CatalogueRegistry} by RFC 0053.
 * The move is visibility only — the shape, and every use of it, are unchanged.</p>
 *
 * @param app    the app module's class
 * @param params the typed params identifying this particular navigable
 * @since RFC 0051 (as a private key); RFC 0053 (promoted, and a NodeIdentity)
 */
public record NavKey(Class<?> app, AppModule._Param params) implements NodeIdentity {
}
