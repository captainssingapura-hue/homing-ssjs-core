package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The two-way TOC↔body sync law, as a Secretary — a pure
 * {@code (state, msg) → { newState, actions }} coordinator plus an initial
 * state, exported as one {@code TocSyncSecretary} JS object.
 *
 * <p>Lifted out of {@link DocTreeRendererModule} unchanged in behaviour (RFC
 * 0043 wrote it there, inline). It moved because a <i>second</i> reader needs
 * the same law: the rigid-tree reader draws its TOC as a
 * {@link TreeRendererModule TreeRenderer} over structure nodes and the markdown
 * reader draws a flat list of heading anchors, but the coordination between a
 * TOC and the body it points into is one problem, and it has one bug risk.</p>
 *
 * <h2>The law</h2>
 *
 * <p>A TOC row is chosen, so the body scrolls; the scroll fires, so a scroll-spy
 * re-selects; the re-selection scrolls again. The loop is dissolved
 * structurally rather than by timing: <b>one authority</b>
 * ({@code currentKey}), <b>two writers</b> (a TOC Actor via
 * {@code NavRequested}, a spy Actor via {@code ScrolledTo}), and an
 * <b>asymmetry</b> — navigation SCROLLS, the spy only HIGHLIGHTS. The single
 * residual, flicker through intermediate sections during a programmatic scroll,
 * is absorbed by the {@code programmaticScroll} field that {@code ScrollSettled}
 * lifts.</p>
 *
 * <h2>State shape</h2>
 * <pre>{@code
 * {
 *     currentKey        : string | null,   // the one authority
 *     programmaticScroll: boolean          // a scroll WE caused is in flight
 * }
 * }</pre>
 *
 * <h2>Message kinds</h2>
 * <ul>
 *   <li>{@code NavRequested(key, path)} — a row was chosen, by click or key.
 *       Takes the authority and arms the guard in the same step, so the scroll
 *       it causes cannot come back as a selection move.</li>
 *   <li>{@code ScrolledTo(key, path)} — the spy's report. Ignored while the
 *       guard is up or when it names the current key; otherwise takes the
 *       authority and emits {@code scroll: false}.</li>
 *   <li>{@code ScrollSettled} — the guard lifts.</li>
 * </ul>
 *
 * <p>The only action is {@code SyncTo(key, path, scroll)}, and the host applies
 * it: highlight, move the TOC selection <i>silently</i>, and scroll only when
 * asked. {@code key} is deliberately opaque — a node's index-key in one reader,
 * a heading slug in the other.</p>
 *
 * <p>A plain message rather than an envelope: this Secretary is hosted locally
 * by one reader and joins no Party, so there is no {@code from} to record.</p>
 *
 * @since homing-core-js — extracted from RFC 0043's inline coordinator
 */
public record TocSyncSecretaryModule() implements DomModule<TocSyncSecretaryModule> {

    /** The single export — a JS object with {@code initial} and {@code behavior}. */
    public record TocSyncSecretary() implements Exportable._Constant<TocSyncSecretaryModule> {}

    public static final TocSyncSecretaryModule INSTANCE = new TocSyncSecretaryModule();

    @Override
    public ImportsFor<TocSyncSecretaryModule> imports() {
        return ImportsFor.<TocSyncSecretaryModule>builder().build();
    }

    @Override
    public ExportsOf<TocSyncSecretaryModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TocSyncSecretary()));
    }
}
