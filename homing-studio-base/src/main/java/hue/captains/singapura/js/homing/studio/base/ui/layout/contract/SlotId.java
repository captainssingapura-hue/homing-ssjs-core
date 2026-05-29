package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import java.util.Objects;

/**
 * Typed identifier for one of MultiTabPane's mtp-minted pane slot ids
 * — opaque strings the pane mints internally to identify each leaf in
 * its split tree. Distinct from {@code PaneId} (the structural path —
 * "_1_1", "_2_2_1") which is intrinsic to the split tree's shape.
 *
 * <p>Per the Names Are Types doctrine, no raw {@code String} crosses
 * MTP's contract boundary as a slot id.</p>
 *
 * @param value the underlying mtp-minted opaque string
 * @since RFC 0035 — MTP contract package
 */
public record SlotId(String value) {

    public SlotId {
        Objects.requireNonNull(value, "SlotId.value");
        if (value.isEmpty()) throw new IllegalArgumentException("SlotId.value: must be non-empty");
    }

    public static SlotId of(String value) { return new SlotId(value); }

    @Override public String toString() { return value; }
}
