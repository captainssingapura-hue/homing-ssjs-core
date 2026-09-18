package hue.captains.singapura.js.homing.design;

/**
 * Pairing — ink that sits <i>on</i> a strongly coloured surface. The
 * component knows which surface it put the text on, so the pairing is the
 * component's to state; the design guarantees the contrast. Only the
 * surfaces that are strongly coloured need a pairing; ink on a layer is
 * {@link Text} or {@link Emphasis}.
 */
public interface Pairing extends Semantic {

    record OnPrimary() implements Pairing {}

    record OnSecondary() implements Pairing {}

    record OnDanger() implements Pairing {}

    record OnWarning() implements Pairing {}

    record OnSuccess() implements Pairing {}

    record OnInfo() implements Pairing {}

    record OnInverted() implements Pairing {}

    /** Muted ink on the inverted layer — the second weight of text on a dark band. */
    record OnInvertedMuted() implements Pairing {}

    record OnOverlay() implements Pairing {}
}
