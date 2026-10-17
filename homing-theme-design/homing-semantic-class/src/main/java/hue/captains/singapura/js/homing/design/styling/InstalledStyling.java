package hue.captains.singapura.js.homing.design.styling;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The styling an instance installed at bootstrap (RFC 0067, appendix The Style Jars): the one every served sheet reads -
 * a style's, a palette's, a design's. Installed once, explicitly, before the first sheet is served; the
 * same styling installed again changes nothing, another is refused, and asking before any is installed is refused.
 */
public final class InstalledStyling {

    private InstalledStyling() {}

    private static final AtomicReference<Styling> INSTALLED = new AtomicReference<>();

    /** Installs an instance's styling: once, at bootstrap. */
    public static void install(Styling styling) {
        Objects.requireNonNull(styling, "InstalledStyling.styling");
        if (!INSTALLED.compareAndSet(null, styling) && INSTALLED.get() != styling)
            throw new IllegalStateException("a styling is installed already: an instance installs its own, once, at bootstrap");
    }

    /** The installed styling. */
    public static Styling get() {
        Styling s = INSTALLED.get();
        if (s == null) throw new IllegalStateException("no styling is installed: an instance installs its styles at bootstrap");
        return s;
    }

    /** The installed styling, if one is. */
    public static Optional<Styling> installed() { return Optional.ofNullable(INSTALLED.get()); }
}
