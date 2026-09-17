package hue.captains.singapura.js.homing.design;

/**
 * What carries a physical target's fulfilment to the user. CSS carries most;
 * sound and assets are their own carriers with their own {@link Impl} kinds.
 * A provider's impl must match its target's carrier — checked at resolution.
 */
public enum Carrier { CSS, AUDIO, ASSET }
