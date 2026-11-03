package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.tao.ontology.Scalar;

import java.util.Objects;
import java.util.Optional;

/**
 * Typed reference to a single SVG asset declared inside an {@link SvgGroup}.
 * Pairs the group (the import unit / namespace) with a specific
 * {@link SvgBeing} (the asset within the group) so the compiler enforces that
 * the being belongs to the group it's declared in.
 *
 * <p>Use sites — anywhere the studio wants to embed a typed SVG glyph at a
 * fixed presentational role: brand logo, custom badge, marker icon. Each role
 * takes an {@code SvgRef<?>} and renders the resolved SVG markup; the same
 * being can be reused across roles without losing type safety.</p>
 *
 * <p>Resource layout — the SVG file lives at
 * {@code homing/svg/<group package>/<group simple>/<being simple>.svg} on the
 * classpath, the same layout the framework's {@link SvgGroupContentProvider}
 * already serves. {@link #resolve()} reads the file's contents on demand.</p>
 *
 * @param <G> the SvgGroup the being belongs to
 */
public record SvgRef<G extends SvgGroup<G>>(G group, SvgBeing<G> being) implements Scalar {

    public SvgRef {
        Objects.requireNonNull(group, "SvgRef.group");
        Objects.requireNonNull(being, "SvgRef.being");
    }

    /** Classpath path: {@code homing/svg/<group-pkg>/<group-simple>/<being-simple>.svg}. */
    public String resourcePath() {
        String pkg         = group.getClass().getPackageName().replace('.', '/');
        String groupSimple = group.getClass().getSimpleName();
        String beingSimple = being.getClass().getSimpleName();
        return "homing/svg/" + pkg + "/" + groupSimple + "/" + beingSimple + ".svg";
    }

    /**
     * Read the SVG markup from the classpath. Returns {@link Optional#empty()}
     * when the resource is absent — caller decides the fallback (e.g. render
     * a default glyph instead).
     */
    public Optional<String> resolve() {
        try {
            return Optional.of(String.join("\n",
                    ResourceReader.INSTANCE.getStringsFromResource(resourcePath())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
