package hue.captains.singapura.js.homing.workspace.state;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Registry-key identity for a theme — names the active theme in
 * {@link ChromeState}. The wrapper that the {@code Names Are Types}
 * doctrine demands — no raw {@code String} crosses framework code as a
 * theme key.
 *
 * <p>Examples: {@code "default"}, {@code "forest"}, {@code "bauhaus"},
 * {@code "jazz-drums"}. Grammar: lowercase letters, digits, hyphen —
 * the kebab-case shape the framework's CSS layer uses.</p>
 *
 * @param value the underlying theme registry key
 * @since RFC 0029 cycle 1
 */
public record ThemeName(String value) {

    private static final Pattern GRAMMAR = Pattern.compile("[a-z][a-z0-9-]*");

    public ThemeName {
        Objects.requireNonNull(value, "ThemeName.value");
        if (!GRAMMAR.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "ThemeName.value '" + value + "' — kebab-case required (lowercase letters, digits, hyphen)");
        }
    }

    /** The framework's boot-time default. */
    public static final ThemeName DEFAULT = new ThemeName("default");

    public static ThemeName of(String value) {
        return new ThemeName(value);
    }

    @Override public String toString() { return value; }
}
