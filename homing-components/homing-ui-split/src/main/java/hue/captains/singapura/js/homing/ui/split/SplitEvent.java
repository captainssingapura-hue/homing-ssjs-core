package hue.captains.singapura.js.homing.ui.split;

import java.util.List;
import java.util.Objects;

/**
 * The closed vocabulary of a splitter's mutations — the sealed sum the JS
 * {@code SplitEvents} factories mirror, record for record, component for
 * component; {@code SplitEventsTest} holds the two together. One kind for
 * a fixed layout: the shares of one split changed. A path names the split
 * by child indexes from the root, "" for the root itself.
 */
public sealed interface SplitEvent {

    /** The kind: the record's simple name, the JS object's {@code kind}. */
    default String kind() { return getClass().getSimpleName(); }

    /** A split's children were re-shared, by a divider drag or by {@code setRatios}; the ratios sum to one. */
    record RatioChanged(String path, List<Double> ratios) implements SplitEvent {
        public RatioChanged {
            Objects.requireNonNull(path, "RatioChanged.path");
            if (!path.matches("(\\d+(/\\d+)*)?")) throw new IllegalArgumentException("RatioChanged.path: child indexes joined by '/', or empty");
            Objects.requireNonNull(ratios, "RatioChanged.ratios");
            if (ratios.size() < 2) throw new IllegalArgumentException("RatioChanged.ratios: two or more");
            double sum = 0;
            for (Double r : ratios) {
                if (r == null || !(r > 0)) throw new IllegalArgumentException("RatioChanged.ratios: positive numbers");
                sum += r;
            }
            if (Math.abs(sum - 1) > 1e-6) throw new IllegalArgumentException("RatioChanged.ratios: must sum to one");
            ratios = List.copyOf(ratios);
        }
    }
}
